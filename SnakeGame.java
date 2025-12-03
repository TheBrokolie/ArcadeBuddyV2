import framework.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.Timer;

/**
 * Snake-Spiel für das Arcade-Framework.
 * Steuerung: Joystick 1 (Up/Down/Left/Right)
 * Neustart: Button MENU
 */
public class SnakeGame implements ArcadeGame {

    private static final int TILE_SIZE = 20;
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 25;
    private static final int GAME_SPEED_MS = 120;

    // Spielfeld
    private LinkedList<Point> snake = new LinkedList<>();
    private Point food;

    // Richtung
    private enum Dir {UP, DOWN, LEFT, RIGHT}
    private Dir direction = Dir.RIGHT;

    private boolean isRunning = false;
    private boolean allowTurn = true;

    // Framework Komponenten
    private final Gamepad input = new UsbGamepad();
    private InputWatcher watcher;
    private Timer timer;
    private JFrame frame;
    private JPanel panel;

    // Highscores
    private final String HIGHSCORE_FILE = "snake_highscores.txt";
    private ArrayList<HighScore> highscores = new ArrayList<>();

    public SnakeGame() {
        loadHighScores();
        setupInputWatcher();
        initGame();
    }

    private void initGame() {
        snake.clear();
        snake.add(new Point(5, 5));
        snake.add(new Point(4, 5));
        snake.add(new Point(3, 5));

        direction = Dir.RIGHT;
        spawnFood();
        isRunning = true;
    }

    private void spawnFood() {
        Random rand = new Random();
        while (true) {
            int x = rand.nextInt(GRID_WIDTH);
            int y = rand.nextInt(GRID_HEIGHT);

            Point p = new Point(x, y);
            if (!snake.contains(p)) {
                food = p;
                return;
            }
        }
    }

    // ---------- Input ----------
    private void setupInputWatcher() {
        watcher = new InputWatcher(input, new InputListener() {

            @Override public void onJoystick1Up() {
                if (allowTurn && direction != Dir.DOWN) direction = Dir.UP;
                allowTurn = false;

                if (!isRunning) {
                    initGame();
                    startTimer();
                    panel.repaint();
                }
            }

            @Override public void onJoystick1Down() {
                if (allowTurn && direction != Dir.UP) direction = Dir.DOWN;
                allowTurn = false;
            }

            @Override public void onJoystick1Left() {
                if (allowTurn && direction != Dir.RIGHT) direction = Dir.LEFT;
                allowTurn = false;
            }

            @Override public void onJoystick1Right() {
                if (allowTurn && direction != Dir.LEFT) direction = Dir.RIGHT;
                allowTurn = false;
            }

            @Override public void onButtonMenuPressed() {

            }

            // Rest ignorieren
            @Override public void onJoystick2Up() {}
            @Override public void onJoystick2Down() {}
            @Override public void onJoystick2Left() {}
            @Override public void onJoystick2Right() {}
            @Override public void onButtonA1Pressed() {}
            @Override public void onButtonB1Pressed() {}
            @Override public void onButtonX1Pressed() {}
            @Override public void onButtonY1Pressed() {}
            @Override public void onButtonA2Pressed() {}
            @Override public void onButtonB2Pressed() {}
            @Override public void onButtonX2Pressed() {}
            @Override public void onButtonY2Pressed() {}
            @Override public void onButtonMenuReleased() {}
            @Override public void onButtonA1Released() {}
            @Override public void onButtonB1Released() {}
            @Override public void onButtonX1Released() {}
            @Override public void onButtonY1Released() {}
            @Override public void onButtonA2Released() {}
            @Override public void onButtonB2Released() {}
            @Override public void onButtonX2Released() {}
            @Override public void onButtonY2Released() {}
        });
    }

    // ---------- Game Loop ----------
    private void updateGame() {
        if (!isRunning) return;

        Point head = snake.getFirst();
        Point newHead = new Point(head);

        switch (direction) {
            case UP -> newHead.y--;
            case DOWN -> newHead.y++;
            case LEFT -> newHead.x--;
            case RIGHT -> newHead.x++;
        }

        allowTurn = true;

        // Kollision Wand → Game Over
        if (newHead.x < 0 || newHead.x >= GRID_WIDTH ||
                newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
            gameOver();
            return;
        }

        // Kollision mit sich selbst
        if (snake.contains(newHead)) {
            gameOver();
            return;
        }

        snake.addFirst(newHead);

        // Essen?
        if (newHead.equals(food)) {
            spawnFood();
        } else {
            snake.removeLast();
        }

        panel.repaint();
    }

    // ---------- Highscores ----------
    private void loadHighScores() {
        File f = new File(HIGHSCORE_FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            highscores.clear();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                highscores.add(new HighScore(parts[0], Integer.parseInt(parts[1])));
            }
        } catch (Exception ignored) {}
    }

    private void saveHighScores() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(HIGHSCORE_FILE))) {
            for (HighScore hs : highscores) {
                pw.println(hs.getName() + "," + hs.getScore());
            }
        } catch (Exception ignored) {}
    }

    private void addHighScore(int score) {
        String name = JOptionPane.showInputDialog(frame,
                "Name eingeben für Highscore (" + score + "):",
                "Highscore",
                JOptionPane.PLAIN_MESSAGE);

        if (name == null || name.isBlank()) name = "Player";

        highscores.add(new HighScore(name, score));

        // Sortieren (höchster Score zuerst)
        highscores.sort((a, b) -> b.getScore() - a.getScore());

        saveHighScores();
    }

    // ---------- Game Over ----------
    private void gameOver() {
        isRunning = false;
        timer.cancel();

        int score = snake.size() - 3;
        addHighScore(score);

        JOptionPane.showMessageDialog(frame,
                "GAME OVER\nScore: " + score + "\nDrücke MENU zum Neustart",
                "Snake",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- Framework ----------
    @Override
    public JFrame start() {
        frame = new JFrame("Snake (Joystick 1)");
        frame.setSize(GRID_WIDTH * TILE_SIZE + 15, GRID_HEIGHT * TILE_SIZE + 40);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        panel = new GamePanel();
        frame.add(panel);

        frame.setVisible(true);

        watcher.startWatching();
        startTimer();

        return frame;
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                updateGame();
            }
        }, 0, GAME_SPEED_MS);
    }

    @Override
    public void stop() {
        if (timer != null) timer.cancel();
        if (watcher != null) watcher.stopWatching();
    }

    @Override
    public ArrayList<HighScore> getHighScores() {
        return highscores;
    }

    // ---------- Rendering ----------
    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Essen
            g.setColor(Color.RED);
            g.fillOval(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

            // Snake
            g.setColor(Color.GREEN);
            for (Point p : snake) {
                g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }

            // Score oben links
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + (snake.size() - 3), 10, 20);
        }
    }

    // Optional Main
    public static void main(String[] args) {
        new SnakeGame().start();
    }
}

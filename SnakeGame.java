import framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class SnakeGame implements ArcadeGame {

    // --- Spielkonstanten ---
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int TILE_SIZE = 25;
    private static final int GAME_SPEED_MS = 200; // Bewegungsfrequenz der Schlange
    private static final String HIGHSCORE_FILE = "highscores.txt";

    // --- Spielzustand ---
    private List<Point> snake;
    private Point food;
    private int score;
    private Direction direction;
    private boolean isRunning;
    private Random random;

    // --- Framework Komponenten ---
    private final Gamepad input = new EmulatorGamepad();
    private InputWatcher watcher;
    private Timer gameTimer;
    private JFrame frame;
    private GamePanel panel;

    // --- Richtungen Enum ---
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // --- Konstruktor und Initialisierung ---

    public SnakeGame() {
        random = new Random();
        initGame();
        setupInputWatcher();
    }

    private void initGame() {
        snake = new ArrayList<>();
        // Startposition der Schlange
        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
        snake.add(new Point(WIDTH / 2 - TILE_SIZE, HEIGHT / 2));
        snake.add(new Point(WIDTH / 2 - 2 * TILE_SIZE, HEIGHT / 2));

        direction = null;
        score = 0;
        isRunning = false;
        placeFood();
    }

    // Platziere das Essen an einer zufälligen, freien Position
    private void placeFood() {
        int max_x = (WIDTH / TILE_SIZE) - 1;
        int max_y = (HEIGHT / TILE_SIZE) - 1;

        int x, y;
        do {
            x = random.nextInt(max_x) * TILE_SIZE;
            y = random.nextInt(max_y) * TILE_SIZE;
            food = new Point(x, y);
        } while (snake.contains(food)); // Stelle sicher, dass das Essen nicht auf der Schlange ist
    }

    // --- Steuerung (Anpassung an dein Beispiel) ---

    private void setupInputWatcher() {
        watcher = new InputWatcher(input, new InputListener() {
            @Override
            public void onJoystick1Up() {
                if (direction != Direction.DOWN) {
                    direction = Direction.UP;
                }
            }

            @Override
            public void onJoystick1Down() {
                System.out.println("hallo");

                if (direction != Direction.UP) {
                    direction = Direction.DOWN;
                }
            }

            @Override
            public void onJoystick1Left() {
                if (direction != Direction.RIGHT) {
                    direction = Direction.LEFT;
                }
            }

            @Override
            public void onJoystick1Right() {
                if (direction != Direction.LEFT) {
                    direction = Direction.RIGHT;
                }
            }
        });
    }

    // --- Spiel-Logik ---

    private void updateGame() {

        System.out.println(direction);

        if(!(direction == null)){
            isRunning = true;
        }

        if (!isRunning) {
            return;
        }

        // 1. Neuen Kopf berechnen
        Point head = snake.get(0);
        Point newHead = (Point) head.clone();

        switch (direction) {
            case UP:
                newHead.y -= TILE_SIZE;
                break;
            case DOWN:
                newHead.y += TILE_SIZE;
                break;
            case LEFT:
                newHead.x -= TILE_SIZE;
                break;
            case RIGHT:
                newHead.x += TILE_SIZE;
                break;
        }

        // 2. Kollisionsprüfung
        if (checkCollision(newHead)) {
            gameOver();
            return;
        }

        // 3. Kopf hinzufügen
        snake.add(0, newHead);

        // 4. Essen prüfen
        if (newHead.equals(food)) {
            score++;
            placeFood(); // Neues Essen platzieren
        } else {
            // Wenn kein Essen gegessen wurde, entferne das Ende
            snake.remove(snake.size() - 1);
        }

        panel.repaint(); // Neuzeichnen anstoßen
    }

    private boolean checkCollision(Point head) {
        // Kollision mit Wänden
        if (head.x < 0 || head.x >= WIDTH || head.y < 0 || head.y >= HEIGHT) {
            return true;
        }

        // Kollision mit sich selbst (beginne ab Index 1, da Index 0 der Kopf ist)
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void gameOver() {
        isRunning = false;
        gameTimer.cancel();
        watcher.stopWatching();

        String playerName = JOptionPane.showInputDialog(frame, "Game Over! Dein Score: " + score + "\nGib deinen Namen ein:", "Neuer Highscore", JOptionPane.PLAIN_MESSAGE);

        if (playerName != null && !playerName.trim().isEmpty()) {
            saveHighScore(new HighScore(playerName.trim(), score));
        }

        // Spiel neustarten
        initGame();
        setupInputWatcher();
        startTimer();
        watcher.startWatching();
    }

    // --- High Score Management ---

    private void saveHighScore(HighScore newScore) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(HIGHSCORE_FILE, true))) {
            pw.println(newScore.getName() + "," + newScore.getScore());
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern des Highscores: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<HighScore> getHighScores() {
        ArrayList<HighScore> highScores = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        String name = parts[0].trim();
                        int scoreValue = Integer.parseInt(parts[1].trim());
                        highScores.add(new HighScore(name, scoreValue));
                    } catch (NumberFormatException e) {
                        System.err.println("Ungültiges Score-Format in Highscore-Datei: " + line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // Datei existiert noch nicht, kein Problem
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Highscores: " + e.getMessage());
        }

        // Sortiere die Highscores absteigend
        highScores.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));

        return highScores;
    }

    // --- Framework Implementierung ---

    @Override
    public JFrame start() {
        frame = new JFrame("Snake Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT + 22); // +22 für die Titelleiste
        frame.setResizable(false);

        panel = new GamePanel();
        frame.add(panel);
        frame.setVisible(true);

        watcher.startWatching();
        startTimer();

        return frame;
    }

    private void startTimer() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateGame();
            }
        }, 0, GAME_SPEED_MS);
    }

    @Override
    public void stop() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        if (watcher != null) {
            watcher.stopWatching();
        }
    }

    // --- Panel für die Spielanzeige ---

    private class GamePanel extends JPanel {

        public GamePanel() {
            // Stellt sicher, dass das Panel den Fokus für KeyEvents erhalten kann (falls Tastatur-Steuerung zusätzlich verwendet wird)
            setFocusable(true);
            setBackground(Color.LIGHT_GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Score anzeigen
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Monospaced", Font.BOLD, 14));
            g.drawString("Score: " + score, 10, 20);

            // Essen zeichnen (Rot)
            if (food != null) {
                g.setColor(Color.RED);
                g.fillOval(food.x, food.y, TILE_SIZE, TILE_SIZE);
            }

            // Schlange zeichnen (Grün)
            g.setColor(Color.GREEN.darker());
            for (Point p : snake) {
                g.fillRect(p.x, p.y, TILE_SIZE, TILE_SIZE);
            }

            // Kopf der Schlange hervorheben (Dunkler)
            if (!snake.isEmpty()) {
                Point head = snake.get(0);
                g.setColor(Color.GREEN.darker().darker());
                g.fillRect(head.x, head.y, TILE_SIZE, TILE_SIZE);
            }

            // Spielrand (optional)
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        }
    }

    // --- Main Methode zum Starten ---
    public static void main(String[] args) {
        new SnakeGame().start();
    }
}
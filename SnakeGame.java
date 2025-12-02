import framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Snake-Spiel für das vorhandene Framework.
 * Steuerung:
 *  - Gamepad 1 Joystick: oben/unten/links/rechts
 *  - Menü-Taste (Gamepad 1): Neustart, wenn Spiel vorbei
 *
 * Implementiert ArcadeGame wie im Beispiel.
 */
public class SnakeGame implements ArcadeGame {

    // --- Spielkonstanten ---
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TILE_SIZE = 20;
    private static final int COLUMNS = WINDOW_WIDTH / TILE_SIZE;
    private static final int ROWS = WINDOW_HEIGHT / TILE_SIZE;
    private static final int GAME_SPEED_MS = 120; // Bewegungstakt (ms), kleiner = schneller

    // --- Spielzustand ---
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction direction = Direction.RIGHT; // Start-Richtung

    private final ArrayList<Point> snake = new ArrayList<>();
    private Point apple;
    private boolean isRunning = false;
    private int score = 0;

    // --- Framework Komponenten ---
    private final Gamepad input = new EmulatorGamepad();
    private InputWatcher watcher;
    private Timer gameTimer;
    private JFrame frame;
    private GamePanel panel;

    private final Random rand = new Random();

    // --- Konstruktor ---
    public SnakeGame() {
        initGame();
        setupInputWatcher();
    }

    private void initGame() {
        snake.clear();

        // Start-Schlange (3 Felder) in der Mitte
        int startX = COLUMNS / 2;
        int startY = ROWS / 2;
        snake.add(new Point(startX - 0, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        direction = Direction.RIGHT;
        score = 0;
        placeApple();
        isRunning = true;
    }

    private void placeApple() {
        while (true) {
            int ax = rand.nextInt(COLUMNS);
            int ay = rand.nextInt(ROWS);
            Point candidate = new Point(ax, ay);
            boolean onSnake = false;
            for (Point p : snake) {
                if (p.equals(candidate)) { onSnake = true; break; }
            }
            if (!onSnake) {
                apple = candidate;
                return;
            }
        }
    }

    // --- Eingabe / InputWatcher ---
    private void setupInputWatcher() {
        watcher = new InputWatcher(input, new InputListener() {

            @Override public void onButtonMenuPressed() {
                if (!isRunning) {
                    initGame();
                    startTimer();
                    panel.requestFocusInWindow();
                }
            }

            // Joystick-Events setzen die Richtung (vermeide 180°-Umdrehung)
            @Override public void onJoystick1Up() {
                if (direction != Direction.DOWN) direction = Direction.UP;
            }
            @Override public void onJoystick1Down() {
                if (direction != Direction.UP) direction = Direction.DOWN;
            }
            @Override public void onJoystick1Left() {
                if (direction != Direction.RIGHT) direction = Direction.LEFT;
            }
            @Override public void onJoystick1Right() {
                if (direction != Direction.LEFT) direction = Direction.RIGHT;
            }

            // Die restlichen Events ignorieren oder leer implementieren
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

    // --- Spiel-Logik ---
    private void updateGame() {
        if (!isRunning) return;

        // Kopfposition berechnen
        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);
        switch (direction) {
            case UP:    newHead.y -= 1; break;
            case DOWN:  newHead.y += 1; break;
            case LEFT:  newHead.x -= 1; break;
            case RIGHT: newHead.x += 1; break;
        }

        // 1) Wand-Kollision -> Game Over
        if (newHead.x < 0 || newHead.x >= COLUMNS || newHead.y < 0 || newHead.y >= ROWS) {
            gameOver();
            return;
        }

        // 2) Selbst-Kollision -> Game Over
        for (Point p : snake) {
            if (p.equals(newHead)) {
                gameOver();
                return;
            }
        }

        // 3) Apfel gefressen?
        boolean grow = false;
        if (newHead.equals(apple)) {
            grow = true;
            score += 1;
            placeApple();
        }

        // 4) neue Position einfügen
        snake.add(0, newHead);
        if (!grow) {
            // Ende abschneiden
            snake.remove(snake.size() - 1);
        }

        panel.repaint();
    }

    private void gameOver() {
        isRunning = false;
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame,
                    "Game Over!\nPunkte: " + score + "\nDrücken Sie [Menu] zum Neustart.",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // --- Framework-Methoden ---
    @Override
    public JFrame start() {
        frame = new JFrame("Snake Game (Joystick: Gamepad 1, Menu zum Neustart)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT + 40);
        frame.setResizable(false);

        panel = new GamePanel();
        frame.add(panel);
        frame.setVisible(true);

        watcher.startWatching();
        startTimer();

        return frame;
    }

    private void startTimer() {
        if (gameTimer != null) gameTimer.cancel();
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { updateGame(); }
        }, 0, GAME_SPEED_MS);
    }

    @Override
    public void stop() {
        if (gameTimer != null) gameTimer.cancel();
        if (watcher != null) watcher.stopWatching();
    }

    @Override
    public ArrayList<HighScore> getHighScores() {
        return new ArrayList<>(); // kein Highscore-System implementiert
    }

    // --- Panel für Anzeige ---
    private class GamePanel extends JPanel {
        public GamePanel() {
            setFocusable(true);
            setBackground(Color.BLACK);
            // Optional: Tastatur-Fallback für Tests mit der Tastatur
            addKeyListener(new java.awt.event.KeyAdapter() {
                @Override public void keyPressed(java.awt.event.KeyEvent e) {
                    int code = e.getKeyCode();
                    if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                        if (direction != Direction.DOWN) direction = Direction.UP;
                    } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                        if (direction != Direction.UP) direction = Direction.DOWN;
                    } else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                        if (direction != Direction.RIGHT) direction = Direction.LEFT;
                    } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                        if (direction != Direction.LEFT) direction = Direction.RIGHT;
                    } else if (code == KeyEvent.VK_ENTER) {
                        if (!isRunning) {
                            initGame();
                            startTimer();
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Raster / Hintergrund
            g.setColor(Color.DARK_GRAY);
            for (int x = 0; x <= WINDOW_WIDTH; x += TILE_SIZE) {
                g.drawLine(x, 0, x, WINDOW_HEIGHT);
            }
            for (int y = 0; y <= WINDOW_HEIGHT; y += TILE_SIZE) {
                g.drawLine(0, y, WINDOW_WIDTH, y);
            }

            // Apfel
            g.setColor(Color.RED);
            g.fillOval(apple.x * TILE_SIZE, apple.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

            // Schlange
            g.setColor(Color.GREEN);
            for (int i = 0; i < snake.size(); i++) {
                Point p = snake.get(i);
                if (i == 0) {
                    // Kopf dunkler/größer zeichnen
                    g.setColor(new Color(0,150,0));
                    g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.GREEN);
                } else {
                    g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }

            // Score oben links
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            g.drawString("Punkte: " + score, 10, 20);

            // Game Over Hinweis
            if (!isRunning) {
                g.setColor(Color.RED);
                g.setFont(new Font("Monospaced", Font.BOLD, 36));
                String msg = "GAME OVER";
                FontMetrics fm = g.getFontMetrics();
                int tx = (WINDOW_WIDTH - fm.stringWidth(msg)) / 2;
                int ty = WINDOW_HEIGHT / 2;
                g.drawString(msg, tx, ty);
            }
        }
    }

    // --- Main zum schnellen Starten ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SnakeGame().start();
        });
    }
}

import framework.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.Timer;

/**
 * Breakout-Spiel für das Arcade-Framework.
 * Steuerung: Joystick 1 (Left/Right)
 * Neustart/Pause: Button MENU
 */
public class BreakoutGame implements ArcadeGame {

    // Konstanten
    private static final int WIDTH = 600;
    private static final int HEIGHT = 450;
    private static final int PADDLE_WIDTH = 80;
    private static final int PADDLE_HEIGHT = 10;
    private static final int BALL_SIZE = 8;
    private static final int BRICK_ROWS = 5;
    private static final int BRICK_COLS = 10;
    private static final int BRICK_WIDTH = WIDTH / BRICK_COLS;
    private static final int BRICK_HEIGHT = 20;
    private static final int GAME_SPEED_MS = 16; // Ca. 60 FPS

    // Spielobjekte
    private Rectangle paddle;
    private Rectangle ball;
    private Brick[][] bricks; // Das Gitter der Ziegel
    private int score = 0;
    private int lives = 3;

    // Ballbewegung
    private int ballXDir = 2;
    private int ballYDir = -2;
    private int paddleSpeed = 6;

    // Spielstatus
    private boolean isRunning = true;
    private boolean isPaused = false;

    // Framework Komponenten
    private final Gamepad input = new UsbGamepad();
    private InputWatcher watcher;
    private Timer timer;
    private JFrame frame;
    private JPanel panel;

    // Highscores
    private final String HIGHSCORE_FILE = "breakout_highscores.txt";
    private ArrayList<HighScore> highscores = new ArrayList<>();

    public BreakoutGame() {
        loadHighScores();
        setupInputWatcher();
        initGame();
    }

    private void initGame() {
        // Initialisiere die Positionen von Schläger und Ball
        paddle = new Rectangle(WIDTH / 2 - PADDLE_WIDTH / 2, HEIGHT - 30, PADDLE_WIDTH, PADDLE_HEIGHT);
        ball = new Rectangle(WIDTH / 2 - BALL_SIZE / 2, HEIGHT - 40, BALL_SIZE, BALL_SIZE);

        score = 0;
        lives = 3;
        ballXDir = 2;
        ballYDir = -2;

        createBricks();
        isRunning = true;
        isPaused = false;
    }

    private void createBricks() {
        bricks = new Brick[BRICK_ROWS][BRICK_COLS];
        for (int i = 0; i < BRICK_ROWS; i++) {
            for (int j = 0; j < BRICK_COLS; j++) {
                int x = j * BRICK_WIDTH;
                int y = i * BRICK_HEIGHT + 50; // Versetzt von oben
                Color color;
                switch (i) {
                    case 0 -> color = Color.RED;
                    case 1 -> color = Color.ORANGE;
                    case 2 -> color = Color.YELLOW;
                    case 3 -> color = Color.GREEN;
                    default -> color = Color.CYAN;
                }
                bricks[i][j] = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, color);
            }
        }
    }

    private void resetBallPosition() {
        ball.setLocation(WIDTH / 2 - BALL_SIZE / 2, HEIGHT - 40);
        ballXDir = 2;
        ballYDir = -2;
    }

    // ---------- Input ----------
    private void setupInputWatcher() {
        watcher = new InputWatcher(input, new InputListener() {

            // Schläger bewegen (Joystick 1 Left/Right)
            @Override public void onJoystick1Left() {
                movePaddle(-paddleSpeed);
            }
            @Override public void onJoystick1Right() {
                movePaddle(paddleSpeed);
            }

            // Neustart / Pause
            @Override public void onButtonMenuPressed() {}

            // Rest ignorieren
            @Override public void onJoystick1Up() {
                if (!isRunning) {
                    initGame();
                    startTimer();
                    panel.repaint();
                }
            }
            @Override public void onJoystick1Down() {}
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

    private void movePaddle(int dx) {
        if (!isRunning || isPaused) return;

        int newX = paddle.x + dx;
        // Begrenzung innerhalb des Spielfelds
        if (newX >= 0 && newX <= WIDTH - PADDLE_WIDTH) {
            paddle.x = newX;
        }
        panel.repaint();
    }

    // ---------- Game Loop ----------
    private void updateGame() {
        if (!isRunning || isPaused) return;

        // 1. Ball bewegen
        ball.x += ballXDir;
        ball.y += ballYDir;

        // 2. Kollision mit Wänden
        if (ball.x <= 0 || ball.x >= WIDTH - BALL_SIZE) {
            ballXDir *= -1; // Seitenwände
        }
        if (ball.y <= 0) {
            ballYDir *= -1; // Obere Wand
        }
        if (ball.y >= HEIGHT - BALL_SIZE) {
            // Ball verloren
            lives--;
            if (lives <= 0) {
                gameOver("Du hast alle Leben verloren.");
                return;
            }
            resetBallPosition();
        }

        // 3. Kollision mit Schläger
        if (ball.intersects(paddle)) {
            // Ball prallt vom Schläger ab
            ballYDir = -Math.abs(ballYDir);

            // Optional: Geschwindigkeit und Richtung basierend auf Trefferpunkt anpassen
            double centerPaddle = paddle.x + PADDLE_WIDTH / 2.0;
            double centerBall = ball.x + BALL_SIZE / 2.0;
            double diff = centerBall - centerPaddle;
            ballXDir = (int) (diff / 10.0); // Beschleunigung/Richtung nach Trefferpunkt
        }

        // 4. Kollision mit Ziegeln
        checkBrickCollision();

        // 5. Gewonnen?
        if (allBricksDestroyed()) {
            gameOver("Alle Ziegel zerstört!");
        }

        panel.repaint();
    }

    private void checkBrickCollision() {
        for (int i = 0; i < BRICK_ROWS; i++) {
            for (int j = 0; j < BRICK_COLS; j++) {
                Brick brick = bricks[i][j];
                if (brick != null && brick.isVisible()) {
                    Rectangle brickRect = brick.getBounds();
                    if (ball.intersects(brickRect)) {
                        brick.setInvisible();
                        score += 10;

                        // Einfache Kollisionserkennung: Richtungsumkehr
                        if (ball.x + BALL_SIZE <= brickRect.x || ball.x >= brickRect.x + brickRect.width) {
                            ballXDir *= -1;
                        } else {
                            ballYDir *= -1;
                        }
                        return; // Nur ein Treffer pro Update
                    }
                }
            }
        }
    }

    private boolean allBricksDestroyed() {
        for (int i = 0; i < BRICK_ROWS; i++) {
            for (int j = 0; j < BRICK_COLS; j++) {
                if (bricks[i][j] != null && bricks[i][j].isVisible()) {
                    return false;
                }
            }
        }
        return true;
    }

    // ---------- Spielende / Highscores ----------
    private void gameOver(String message) {
        isRunning = false;
        timer.cancel();

        addHighScore(score);

        JOptionPane.showMessageDialog(frame,
                "GAME OVER\n" + message + "\nFinaler Score: " + score + "\nDrücke MENU zum Neustart.",
                "Breakout",
                JOptionPane.INFORMATION_MESSAGE);
    }

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

        highscores.sort((a, b) -> b.getScore() - a.getScore());

        saveHighScores();
    }

    // ---------- Framework ----------
    @Override
    public JFrame start() {
        frame = new JFrame("Breakout (Joystick 1)");
        frame.setSize(WIDTH + 15, HEIGHT + 40);
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

            // Ziegel (Bricks)
            for (int i = 0; i < BRICK_ROWS; i++) {
                for (int j = 0; j < BRICK_COLS; j++) {
                    Brick brick = bricks[i][j];
                    if (brick != null && brick.isVisible()) {
                        g.setColor(brick.getColor());
                        g.fillRect(brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
                        g.setColor(Color.DARK_GRAY);
                        g.drawRect(brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
                    }
                }
            }

            // Schläger
            g.setColor(Color.WHITE);
            g.fillRect(paddle.x, paddle.y, paddle.width, paddle.height);

            // Ball
            g.setColor(Color.YELLOW);
            g.fillOval(ball.x, ball.y, ball.width, ball.height);

            // Score und Leben
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 10, 20);
            g.drawString("Leben: " + lives, WIDTH - 100, 20);

            // Pause-Meldung
            if (isPaused) {
                g.setFont(new Font("Arial", Font.BOLD, 50));
                g.drawString("PAUSE", WIDTH / 2 - 80, HEIGHT / 2);
            }
        }
    }

    // ---------- Hilfsklasse für Ziegel ----------
    private static class Brick {
        private final Rectangle bounds;
        private final Color color;
        private boolean visible;

        public Brick(int x, int y, int width, int height, Color color) {
            this.bounds = new Rectangle(x, y, width, height);
            this.color = color;
            this.visible = true;
        }

        public Rectangle getBounds() { return bounds; }
        public Color getColor() { return color; }
        public int getX() { return bounds.x; }
        public int getY() { return bounds.y; }
        public int getWidth() { return bounds.width; }
        public int getHeight() { return bounds.height; }
        public boolean isVisible() { return visible; }
        public void setInvisible() { this.visible = false; }
    }

    // Optional Main
    public static void main(String[] args) {
        new BreakoutGame().start();
    }
}
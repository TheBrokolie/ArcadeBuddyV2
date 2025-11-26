import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import framework.*;

public class TetrisGame implements ArcadeGame {

    // --- Spielfeld Konstanten ---
    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int TILE = 25;

    private static final int WIDTH = COLS * TILE;
    private static final int HEIGHT = ROWS * TILE;

    private static final int GAME_SPEED_MS = 500;
    private static final String HIGHSCORE_FILE = "tetris_scores.txt";

    // --- Spielzustand ---
    private int[][] grid = new int[ROWS][COLS];
    private Tetromino current;
    private Tetromino next;

    private int score = 0;
    private boolean running = true;

    // --- Framework ---
    private final Gamepad input = new UsbGamepad();
    private InputWatcher watcher;
    private Timer timer;
    private JFrame frame;
    private GamePanel panel;

    // --- Konstruktor ---
    public TetrisGame() {
        initGame();
        setupInputWatcher();
    }

    private void initGame() {
        grid = new int[ROWS][COLS];
        current = Tetromino.random();
        next = Tetromino.random();
        score = 0;
        running = true;
    }

    // --- Input Steuerung ---
    private void setupInputWatcher() {
        watcher = new InputWatcher(input, new InputListener() {

            @Override
            public void onJoystick1Left() {
                if (running) moveCurrent(-1);
            }

            @Override
            public void onJoystick1Right() {
                if (running) moveCurrent(1);
            }

            @Override
            public void onJoystick1Up() {
                if (running) rotateCurrent();
            }

            @Override
            public void onJoystick1Down() {
                if (running) dropOneRow();
            }
        });
    }

    // --- Spiellogik ---
    private void updateGame() {
        if (!running) return;

        if (!moveDown()) {
            mergeCurrent();
            clearLines();
            spawnNext();
        }

        panel.repaint();
    }

    private boolean moveDown() {
        if (isValidMove(current.x, current.y + 1, current.shape)) {
            current.y++;
            return true;
        }
        return false;
    }

    private void moveCurrent(int dx) {
        if (isValidMove(current.x + dx, current.y, current.shape)) {
            current.x += dx;
        }
        panel.repaint();
    }

    private void rotateCurrent() {
        int[][] rotated = current.rotate();
        if (isValidMove(current.x, current.y, rotated)) {
            current.shape = rotated;
        }
        panel.repaint();
    }

    private void dropOneRow() {
        moveDown();
    }

    private boolean isValidMove(int newX, int newY, int[][] shape) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] == 0) continue;

                int x = newX + c;
                int y = newY + r;

                if (x < 0 || x >= COLS || y >= ROWS) return false;
                if (y >= 0 && grid[y][x] != 0) return false;
            }
        }
        return true;
    }

    private void mergeCurrent() {
        for (int r = 0; r < current.shape.length; r++) {
            for (int c = 0; c < current.shape[0].length; c++) {
                if (current.shape[r][c] != 0) {
                    int gx = current.x + c;
                    int gy = current.y + r;

                    if (gy < 0) {
                        gameOver();
                        return;
                    }
                    grid[gy][gx] = current.color;
                }
            }
        }
    }

    private void clearLines() {
        int lines = 0;

        for (int r = 0; r < ROWS; r++) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                lines++;
                for (int y = r; y > 0; y--) {
                    grid[y] = Arrays.copyOf(grid[y - 1], COLS);
                }
                grid[0] = new int[COLS];
            }
        }

        score += lines * 100;
    }

    private void spawnNext() {
        current = next;
        next = Tetromino.random();
        current.x = COLS / 2 - 2;
        current.y = -2;

        if (!isValidMove(current.x, current.y, current.shape)) {
            gameOver();
        }
    }

    private void gameOver() {
        running = false;
        timer.cancel();
        watcher.stopWatching();

        String name = JOptionPane.showInputDialog(frame,
                "GAME OVER\nScore: " + score + "\nName:");
        if (name != null && !name.trim().isEmpty()) {
            saveHighScore(new HighScore(name.trim(), score));
        }

        initGame();
        setupInputWatcher();
        startTimer();
        watcher.startWatching();
    }

    // --- Highscore speichern ---
    private void saveHighScore(HighScore hs) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(HIGHSCORE_FILE, true))) {
            pw.println(hs.getName() + "," + hs.getScore());
        } catch (Exception ignored) {}
    }

    @Override
    public ArrayList<HighScore> getHighScores() {
        ArrayList<HighScore> scores = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                scores.add(new HighScore(p[0], Integer.parseInt(p[1])));
            }
        } catch (Exception ignored) {}
        scores.sort((a, b) -> b.getScore() - a.getScore());
        return scores;
    }

    // --- Framework Start ---
    @Override
    public JFrame start() {
        frame = new JFrame("Tetris");
        frame.setSize(WIDTH, HEIGHT + 22);
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
            public void run() { updateGame(); }
        }, 0, GAME_SPEED_MS);
    }

    @Override
    public void stop() {
        if (timer != null) timer.cancel();
        if (watcher != null) watcher.stopWatching();
    }

    // -----------------------------------
    // Rendering Panel
    // -----------------------------------
    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.BLACK);
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw grid
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (grid[r][c] != 0) {
                        g.setColor(new Color(grid[r][c]));
                        g.fillRect(c * TILE, r * TILE, TILE, TILE);
                    }
                }
            }

            // Draw current piece
            if (current != null) {
                g.setColor(new Color(current.color));
                for (int r = 0; r < current.shape.length; r++) {
                    for (int c = 0; c < current.shape[0].length; c++) {
                        if (current.shape[r][c] != 0) {
                            int x = (current.x + c) * TILE;
                            int y = (current.y + r) * TILE;
                            if (y >= 0)
                                g.fillRect(x, y, TILE, TILE);
                        }
                    }
                }
            }

            // Score
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 20);
        }
    }

    // -----------------------------------
    // Tetromino Klasse
    // -----------------------------------
    private static class Tetromino {
        int[][] shape;
        int color;
        int x = 3;
        int y = -2;

        private Tetromino(int[][] s, int c) {
            shape = s;
            color = c;
        }

        static Tetromino random() {
            Random r = new Random();
            int[][][] shapes = {
                    {{1,1,1,1}},                       // I
                    {{1,1},{1,1}},                    // O
                    {{0,1,0},{1,1,1}},                // T
                    {{1,0,0},{1,1,1}},                // J
                    {{0,0,1},{1,1,1}},                // L
                    {{1,1,0},{0,1,1}},                // S
                    {{0,1,1},{1,1,0}}                 // Z
            };
            int idx = r.nextInt(shapes.length);
            return new Tetromino(shapes[idx], Color.HSBtoRGB(r.nextFloat(), 1f, 1f));
        }

        int[][] rotate() {
            int rows = shape.length;
            int cols = shape[0].length;
            int[][] rotated = new int[cols][rows];

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    rotated[c][rows - 1 - r] = shape[r][c];
                }
            }
            return rotated;
        }
    }

    // --- Main für Tests ---
    public static void main(String[] args) {
        new TetrisGame().start();
    }
}

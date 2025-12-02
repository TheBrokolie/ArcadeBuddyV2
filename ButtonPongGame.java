import framework.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Multiplayer-Pong-Spiel zur Überprüfung der Button-Funktionalität von zwei HID-Gamepads.
 * Schläger werden über die Buttons X und B gesteuert.
 */
public class ButtonPongGame implements ArcadeGame {

    // --- Spielkonstanten ---
    private static final int game_width = 600;
    private static final int game_height = 400;
    private static final int PADDLE_HEIGHT = 60;
    private static final int PADDLE_WIDTH = 10;
    private static final int BALL_SIZE = 10;
    private static final int GAME_SPEED_MS = 20; // 50 FPS

    // --- Spielzustand ---
    private int paddle1Y = game_height / 2 - PADDLE_HEIGHT / 2;
    private int paddle2Y = game_height / 2 - PADDLE_HEIGHT / 2;
    private final int PADDLE_SPEED = 8;

    private double ballX, ballY;
    private double ballSpeedX;
    private double ballSpeedY;
    private final double INITIAL_BALL_SPEED = 5.0;

    private int score1 = 0;
    private int score2 = 0;
    private boolean isRunning = false;

    // --- Framework Komponenten ---
    private final Gamepad input = new EmulatorGamepad();
    private InputWatcher watcher;
    private Timer gameTimer;
    private JFrame frame;
    private JPanel panel;

    // --- Konstruktor und Initialisierung ---

    public ButtonPongGame() {
        initGame();
        setupInputWatcher();
    }

    private void initGame() {
        // Starte den Ball in der Mitte und gib ihm eine zufällige Richtung
        ballX = game_width / 2.0;
        ballY = game_height / 2.0;

        Random rand = new Random();
        ballSpeedX = (rand.nextBoolean() ? 1.0 : -1.0) * INITIAL_BALL_SPEED;
        ballSpeedY = (rand.nextDouble() * 2 - 1) * 3.0;

        paddle1Y = game_height / 2 - PADDLE_HEIGHT / 2;
        paddle2Y = game_height / 2 - PADDLE_HEIGHT / 2;
        score1 = 0;
        score2 = 0;
        isRunning = true;
    }

    // --- Steuerung über Buttons ---
    private void setupInputWatcher() {
        watcher = new InputWatcher(input, new InputListener() {

            // Menü-Taste zum Neustart (Gamepad 1)
            @Override public void onButtonMenuPressed() {
                if (!isRunning) {
                    initGame();
                    startTimer();
                }
            }

            // Ignoriere alle Joystick- und die meisten Button-Events
            @Override public void onJoystick1Up() {}
            @Override public void onJoystick1Down() {}
            @Override public void onJoystick1Left() {}
            @Override public void onJoystick1Right() {}
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
        if (!isRunning) {
            return;
        }

        // 1. Schlägerbewegung aktualisieren (Nutzung von isDown() für kontinuierliche Bewegung)

        // Spieler 1 (Links)
        if (input.buttonX1().isPressed()) { // Button X1 (Gamepad 1) = Hoch
            paddle1Y -= PADDLE_SPEED;
        }

        if (input.buttonB1().isPressed()) { // Button B1 (Gamepad 1) = Runter
            paddle1Y += PADDLE_SPEED;
        }

        // Spieler 2 (Rechts)
        if (input.buttonX2().isPressed()) { // Button X2 (Gamepad 2) = Hoch
            paddle2Y -= PADDLE_SPEED;
        }
        if (input.buttonB2().isPressed()) { // Button B2 (Gamepad 2) = Runter
            paddle2Y += PADDLE_SPEED;
        }

        // Schläger innerhalb der Grenzen halten
        paddle1Y = Math.max(0, Math.min(paddle1Y, game_height - PADDLE_HEIGHT));
        paddle2Y = Math.max(0, Math.min(paddle2Y, game_height - PADDLE_HEIGHT));

        // 2. Ballbewegung aktualisieren
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // 3. Kollisionen mit Wänden (oben/unten)
        if (ballY <= 0 || ballY >= game_height - BALL_SIZE) {
            ballSpeedY *= -1; // Richtung umkehren
        }

        // 4. Kollisionen mit Schlägern (Logik bleibt unverändert)

        // Schläger 1 (Links)
        if (ballX <= PADDLE_WIDTH &&
                ballY >= paddle1Y &&
                ballY <= paddle1Y + PADDLE_HEIGHT) {

            ballSpeedX *= -1.05; // Richtung umkehren und Geschwindigkeit leicht erhöhen

            // Vertikalen Einfluss basierend auf Treffpunkt
            double relativeIntersectY = (paddle1Y + (PADDLE_HEIGHT / 2.0)) - (ballY + (BALL_SIZE / 2.0));
            double normalizedRelativeIntersectY = relativeIntersectY / (PADDLE_HEIGHT / 2.0);
            ballSpeedY = -normalizedRelativeIntersectY * 5.0;
        }

        // Schläger 2 (Rechts)
        if (ballX >= game_width - 2 * PADDLE_WIDTH - BALL_SIZE &&
                ballY >= paddle2Y &&
                ballY <= paddle2Y + PADDLE_HEIGHT) {

            ballSpeedX *= -1.05; // Richtung umkehren und Geschwindigkeit leicht erhöhen

            // Vertikalen Einfluss basierend auf Treffpunkt
            double relativeIntersectY = (paddle2Y + (PADDLE_HEIGHT / 2.0)) - (ballY + (BALL_SIZE / 2.0));
            double normalizedRelativeIntersectY = relativeIntersectY / (PADDLE_HEIGHT / 2.0);
            ballSpeedY = -normalizedRelativeIntersectY * 5.0;
        }

        // 5. Torprüfung (Score)
        // Wenn Ball den linken Rand passiert, erhält Spieler 2 einen Punkt.
        if (ballX < 0) {
            score2++;
            resetBall(1); // Ball startet in Richtung des Gegners (Rechts, Spieler 2)
        }
        // Wenn Ball den rechten Rand passiert, erhält Spieler 1 einen Punkt.
        else if (ballX > game_width) {
            score1++;
            resetBall(-1); // Ball startet in Richtung des Gegners (Links, Spieler 1)
        }

        // Spielende
        if (score1 >= 10 || score2 >= 10) {
            gameOver();
            return;
        }

        panel.repaint(); // Neuzeichnen anstoßen
    }

    // ... (restliche Methoden bleiben unverändert)

    private void resetBall(int direction) {
        ballX = game_width / 2.0;
        ballY = game_height / 2.0;
        ballSpeedX = direction * INITIAL_BALL_SPEED; // Starte in Richtung des Verlierers
        ballSpeedY = (new Random().nextDouble() * 2 - 1) * 3.0;
    }

    private void gameOver() {
        isRunning = false;
        gameTimer.cancel();
        String winner = (score1 > score2) ? "Spieler 1" : "Spieler 2";
        JOptionPane.showMessageDialog(frame, "Spielende! " + winner + " gewinnt!\n" +
                "Drücken Sie [Menu] zum Neustart.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }


    // --- Framework Implementierung ---

    @Override
    public JFrame start() {
        frame = new JFrame("Button Pong Game (Spieler 1: X1/B1 | Spieler 2: X2/B2)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(game_width, game_height+40);
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

    @Override
    public ArrayList<HighScore> getHighScores() {
        return new ArrayList<>(); // Kein Highscore-System in Pong
    }

    // --- Panel für die Spielanzeige ---

    private class GamePanel extends JPanel {

        public GamePanel() {
            setFocusable(true);
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Mittellinie
            g.setColor(Color.WHITE);
            g.drawLine(game_width / 2, 0, game_width / 2, game_height);

            // Ball zeichnen
            g.fillOval((int) ballX, (int) ballY, BALL_SIZE, BALL_SIZE);

            // Schläger 1 (Links)
            g.fillRect(0, paddle1Y, PADDLE_WIDTH, PADDLE_HEIGHT);

            // Schläger 2 (Rechts)
            g.fillRect(game_width - 3*PADDLE_WIDTH, paddle2Y, PADDLE_WIDTH, PADDLE_HEIGHT);

            // Score anzeigen
            g.setFont(new Font("Monospaced", Font.BOLD, 30));
            g.drawString(String.valueOf(score1), game_height / 2 - 50, 30);
            g.drawString(String.valueOf(score2), game_width / 2 + 30, 30);

            if (!isRunning) {
                g.setColor(Color.RED);
                g.setFont(new Font("Monospaced", Font.BOLD, 40));
                g.drawString("GAME OVER", game_width / 2 - 120, game_height / 2);
            }
        }
    }

    // --- Main Methode zum Starten ---
    public static void main(String[] args) {
        new ButtonPongGame().start();
    }
}
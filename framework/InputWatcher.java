package framework;

public class InputWatcher {

    private final Gamepad input;
    private final InputListener listener;

    private boolean lastA1 = false;
    private boolean lastB1 = false;
    private boolean lastX1 = false;
    private boolean lastY1 = false;
    private boolean lastMENU = false;
    private int lastX = 0;
    private int lastY = 0;

    private volatile boolean running = true; // Thread läuft
    private volatile boolean active = true;  // Eingaben verarbeiten

    private Thread thread;

    public InputWatcher(Gamepad input, InputListener listener) {
        this.input = input;
        this.listener = listener;
        startWatching();
    }

    private void watchLoop() {
        while (running) {
            try {
                if (active) checkChanges();
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void checkChanges() {
        boolean a1 = input.buttonA1().isPressed();
        boolean b1 = input.buttonB1().isPressed();
        boolean x1 = input.buttonX1().isPressed();
        boolean y1 = input.buttonY1().isPressed();
        boolean menu = input.buttonMenu().isPressed();
        int x = getAxisX();
        int y = getAxisY();

        if (a1 && !lastA1) listener.onButtonA1Pressed();
        if (!a1 && lastA1) listener.onButtonA1Released();
        lastA1 = a1;

        if (b1 && !lastB1) listener.onButtonB1Pressed();
        if (!b1 && lastB1) listener.onButtonB1Released();
        lastB1 = b1;

        if (x1 && !lastX1) listener.onButtonX1Pressed();
        if (!x1 && lastX1) listener.onButtonX1Released();
        lastX1 = x1;

        if (y1 && !lastY1) listener.onButtonY1Pressed();
        if (!y1 && lastY1) listener.onButtonY1Released();
        lastY1 = y1;

        if(menu && !lastMENU) listener.onButtonMenuPressed();
        if (!menu && lastMENU) listener.onButtonMenuReleased();
        lastMENU = menu;

        if (y == 1 && lastY != 1) listener.onJoystick1Up();
        if (y == -1 && lastY != -1) listener.onJoystick1Down();
        if (x == 1 && lastX != 1) listener.onJoystick1Left();
        if (x == -1 && lastX != -1) listener.onJoystick1Right();

        lastX = x;
        lastY = y;
    }

    private int getAxisX() {
        if (input.joystick1().left()) return 1;
        if (input.joystick1().right()) return -1;
        return 0;
    }

    private int getAxisY() {
        if (input.joystick1().up()) return 1;
        if (input.joystick1().down()) return -1;
        return 0;
    }

    // --- öffentlicher Methoden ---

    // Eingaben verarbeiten aktivieren
    public void startWatching() {
        active = true;
        running = true;
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this::watchLoop, "InputWatcher");
            thread.setDaemon(true);
            thread.start();
        }
    }

    // Eingaben verarbeiten deaktivieren (Pause)
    public void stopWatching() {
        active = false;
    }

    // Thread vollständig stoppen
    public void shutdown() {
        running = false;
    }
}

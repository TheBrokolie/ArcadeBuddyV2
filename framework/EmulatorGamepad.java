package framework;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class EmulatorGamepad implements Gamepad {

    private final Set<Integer> pressedKeys = new HashSet<>();

    private final EmulatorJoystick joystick1 = new EmulatorJoystick(
            KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
    private final EmulatorJoystick joystick2 = new EmulatorJoystick(
            KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);

    // --- Tastenzuordnung für Buttons ---
    private final int A1 = KeyEvent.VK_H;
    private final int B1 = KeyEvent.VK_J;
    private final int X1 = KeyEvent.VK_K;
    private final int Y1 = KeyEvent.VK_U;

    private final int A2 = KeyEvent.VK_NUMPAD4;
    private final int B2 = KeyEvent.VK_NUMPAD5;
    private final int X2 = KeyEvent.VK_NUMPAD6;
    private final int Y2 = KeyEvent.VK_NUMPAD8;

    private final int MENU = KeyEvent.VK_ESCAPE;

    public EmulatorGamepad() {
        // Globale Tastenerkennung
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            synchronized (pressedKeys) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    pressedKeys.add(e.getKeyCode());
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    pressedKeys.remove(e.getKeyCode());
                }
            }
            return false;
        });
    }

    // --- Joysticks ---
    @Override
    public Joystick joystick1() {
        return joystick1;
    }

    @Override
    public Joystick joystick2() {
        return joystick2;
    }

    // --- Buttons Spieler 1 ---
    @Override
    public Button buttonA1() { return new Button(pressedKeys.contains(A1)); }
    @Override
    public Button buttonB1() { return new Button(pressedKeys.contains(B1)); }
    @Override
    public Button buttonX1() { return new Button(pressedKeys.contains(X1)); }
    @Override
    public Button buttonY1() { return new Button(pressedKeys.contains(Y1)); }

    // --- Buttons Spieler 2 ---
    @Override
    public Button buttonA2() { return new Button(pressedKeys.contains(A2)); }
    @Override
    public Button buttonB2() { return new Button(pressedKeys.contains(B2)); }
    @Override
    public Button buttonX2() { return new Button(pressedKeys.contains(X2)); }
    @Override
    public Button buttonY2() { return new Button(pressedKeys.contains(Y2)); }
    @Override
    public Button buttonMenu(){ return new Button(pressedKeys.contains(MENU)); }

    // --- Innere Klasse für Emulator-Joystick ---
    private class EmulatorJoystick implements Joystick {
        private final int up, down, left, right;

        EmulatorJoystick(int up, int down, int left, int right) {
            this.up = up;
            this.down = down;
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean up() { return pressedKeys.contains(up); }

        @Override
        public boolean down() { return pressedKeys.contains(down); }

        @Override
        public boolean left() { return pressedKeys.contains(left); }

        @Override
        public boolean right() { return pressedKeys.contains(right); }
    }
}

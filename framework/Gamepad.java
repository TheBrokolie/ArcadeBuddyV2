package framework;

public interface Gamepad {

    // --- Joysticks ---
    Joystick joystick1();
    Joystick joystick2();

    // --- Buttons für Spieler 1 ---
    Button buttonA1();
    Button buttonB1();
    Button buttonX1();
    Button buttonY1();

    // --- Buttons für Spieler 2 ---
    Button buttonA2();
    Button buttonB2();
    Button buttonX2();
    Button buttonY2();

    Button buttonMenu();
}

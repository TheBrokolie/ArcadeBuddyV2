package framework;

/**
 * Ein Listener für Ereignisse von den {@link Gamepad}-Buttons und Joysticks.
 * Die Standardimplementierung ({@code default}) für viele Methoden
 * ermöglicht eine einfache Implementierung, bei der nur die benötigten Events überschrieben werden müssen.
 */
public interface InputListener {
    // === Buttons Spieler 1 ===

    default void onButtonA1Pressed() {}
    default void onButtonA1Released() {}

    default void onButtonB1Pressed() {}
    default void onButtonB1Released() {}

    default void onButtonX1Pressed() {}
    default void onButtonX1Released() {}

    default void onButtonY1Pressed() {}
    default void onButtonY1Released() {}

    // === Buttons Spieler 2 ===
    // (Diese haben aktuell keine 'default'-Implementierung im Code, daher muss die Spiel-Logik sie implementieren.)

    /**
     * Wird aufgerufen, wenn Button A2 gedrückt wird.
     */
    void onButtonA2Pressed();

    /**
     * Wird aufgerufen, wenn Button A2 losgelassen wird.
     */
    void onButtonA2Released();

    /**
     * Wird aufgerufen, wenn Button B2 gedrückt wird.
     */
    void onButtonB2Pressed();

    /**
     * Wird aufgerufen, wenn Button B2 losgelassen wird.
     */
    void onButtonB2Released();

    /**
     * Wird aufgerufen, wenn Button X2 gedrückt wird.
     */
    void onButtonX2Pressed();

    /**
     * Wird aufgerufen, wenn Button X2 losgelassen wird.
     */
    void onButtonX2Released();

    /**
     * Wird aufgerufen, wenn Button Y2 gedrückt wird.
     */
    void onButtonY2Pressed();

    /**
     * Wird aufgerufen, wenn Button Y2 losgelassen wird.
     */
    void onButtonY2Released();


    // === Menü Button ===

    default void onButtonMenuPressed() {}
    default void onButtonMenuReleased() {}

    // === Joystick 1 Bewegungen ===

    default void onJoystick1Up() {}
    default void onJoystick1Down() {}
    default void onJoystick1Left() {}
    default void onJoystick1Right() {}

    // === Joystick 2 Bewegungen ===
    // (Diese haben aktuell keine 'default'-Implementierung im Code, daher muss die Spiel-Logik sie implementieren.)

    /**
     * Wird aufgerufen, wenn Joystick 2 nach oben bewegt wird.
     */
    void onJoystick2Up();

    /**
     * Wird aufgerufen, wenn Joystick 2 nach unten bewegt wird.
     */
    void onJoystick2Down();

    /**
     * Wird aufgerufen, wenn Joystick 2 nach links bewegt wird.
     */
    void onJoystick2Left();

    /**
     * Wird aufgerufen, wenn Joystick 2 nach rechts bewegt wird.
     */
    void onJoystick2Right();
}
package framework;

/**
 * Definiert die generische Schnittstelle für ein Gamepad (Controller),
 * über die der Zustand von Joysticks und Buttons abgefragt werden kann.
 */
public interface Gamepad {

    // --- Joysticks ---

    /**
     * Gibt den ersten Joystick (typischerweise für Spieler 1) zurück.
     *
     * @return Die {@link Joystick}-Instanz für Achse 1.
     */
    Joystick joystick1();

    /**
     * Gibt den zweiten Joystick (typischerweise für Spieler 2 oder andere Steuerungen) zurück.
     *
     * @return Die {@link Joystick}-Instanz für Achse 2.
     */
    Joystick joystick2();

    // --- Buttons für Spieler 1 ---

    /**
     * Gibt den A-Button für Spieler 1 zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonA1();
    /**
     * Gibt den B-Button für Spieler 1 zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonB1();
    /**
     * Gibt den X-Button für Spieler 1 zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonX1();
    /**
     * Gibt den Y-Button für Spieler 1 zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonY1();

    // --- Buttons für Spieler 2 ---

    /**
     * Gibt den A-Button für Spieler 2 zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonA2();
    /**
     * Gibt den B-Button für Spieler 2 zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonB2();
    /**
     * Gibt den X-Button für Spieler 2 zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonX2();
    /**
     * Gibt den Y-Button für Spieler 2 zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonY2();

    /**
     * Gibt den Menü-Button (Pause, Optionen) zurück.
     * @return Die {@link Button}-Instanz.
     */
    Button buttonMenu();
}
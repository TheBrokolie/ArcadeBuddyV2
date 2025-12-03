package framework;

/**
 * Definiert die Abfragemethoden für einen einzelnen analogen oder digitalen Joystick.
 * Liefert den aktuellen Zustand der vier Hauptrichtungen.
 */
public interface Joystick {

    /**
     * Überprüft, ob der Joystick nach oben bewegt wird.
     * @return {@code true}, wenn oben gedrückt.
     */
    boolean up();

    /**
     * Überprüft, ob der Joystick nach unten bewegt wird.
     * @return {@code true}, wenn unten gedrückt.
     */
    boolean down();

    /**
     * Überprüft, ob der Joystick nach links bewegt wird.
     * @return {@code true}, wenn links gedrückt.
     */
    boolean left();

    /**
     * Überprüft, ob der Joystick nach rechts bewegt wird.
     * @return {@code true}, wenn rechts gedrückt.
     */
    boolean right();
}
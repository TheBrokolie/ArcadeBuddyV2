package framework;

/**
 * Überwacht in einem eigenen Thread den Zustand des {@link Gamepad} und
 * sendet Zustandswechsel-Ereignisse an den {@link InputListener}.
 * Sollte vom Spiel- oder Framework-Kern einmalig instanziiert werden.
 */
public class InputWatcher {

    // ... interner Zustand ...

    /**
     * Erstellt eine neue {@code InputWatcher}-Instanz und beginnt sofort mit der Überwachung
     * des Zustands.
     *
     * @param input Die {@link Gamepad}-Implementierung, die überwacht werden soll (z.B. {@code EmulatorGamepad}).
     * @param listener Die {@link InputListener}-Implementierung, die die Events empfängt.
     */
    public InputWatcher(Gamepad input, InputListener listener) {
        // ... Implementierungsdetails ...
    }

    /**
     * Aktiviert die Verarbeitung von Eingaben (standardmäßig aktiv nach der Instanziierung).
     * Setzt den internen Thread fort, falls dieser mit {@link #stopWatching()} pausiert wurde.
     */
    public void startWatching() {
        // ... Implementierungsdetails ...
    }

    /**
     * Deaktiviert die Verarbeitung von Eingaben (pausiert die Event-Weitergabe),
     * ohne den Überwachungs-Thread zu beenden.
     * Nützlich für Pausen-Menüs.
     */
    public void stopWatching() {
        // ... Implementierungsdetails ...
    }

    /**
     * Stoppt den internen Überwachungs-Thread vollständig und gibt die Ressourcen frei.
     * Sollte beim Beenden des Spiels aufgerufen werden.
     */
    public void shutdown() {
        // ... Implementierungsdetails ...
    }
}
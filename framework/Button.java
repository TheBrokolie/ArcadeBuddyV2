package framework;

import java.util.function.BooleanSupplier;

/**
 * Stellt einen einzelnen Gamepad-Button dar, dessen Zustand (gedrückt/nicht gedrückt)
 * dynamisch über einen {@link BooleanSupplier} abgefragt wird.
 */
public class Button {

    // ... interner Zustand (nicht für den Spieleentwickler relevant) ...

    /**
     * @deprecated Dieser Konstruktor ist für die manuelle Zustandssetzung (statisch)
     * vorgesehen und sollte nicht für Gamepad-Buttons im Framework verwendet werden.
     * Er dient nur der Kompatibilität.
     */
    public Button(boolean pressed) {
        // ... Implementierungsdetails ...
        // ACHTUNG: Der Zustand ist statisch (liefert immer den bei der Erstellung übergebenen Wert)
        this.stateSupplier = () -> pressed;
    }

    /**
     * NEUER Konstruktor, der eine Funktion (Lambda) annimmt, die den Button-Zustand zur Laufzeit abfragt.
     * Dies ist der Standardkonstruktor, der von {@link Gamepad}-Implementierungen (z.B. {@code UsbGamepad})
     * verwendet werden sollte.
     *
     * @param stateSupplier Die Funktion, die den aktuellen Zustand des Buttons liefert.
     */
    public Button(BooleanSupplier stateSupplier) {
        this.stateSupplier = stateSupplier;
    }

    /**
     * Fragt den aktuellen Zustand des Buttons ab.
     *
     * @return {@code true}, wenn der Button gedrückt ist, andernfalls {@code false}.
     */
    public boolean isPressed() {
        return stateSupplier.getAsBoolean();
    }
}
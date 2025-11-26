package framework;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.PureJavaHidApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gamepad-Implementierung für den echten USB-Joystick (DragonRise Generic USB Joystick).
 * Verwaltet mehrere angeschlossene Gamepads.
 */
public class UsbGamepad implements Gamepad {

    private static final int VENDOR_ID = 0x0079;   // DragonRise
    private static final int PRODUCT_ID = 0x0006;  // USB Joystick
    private static final int DEADZONE = 20;

    // Liste, die den Zustand und die Verbindung zu jedem physischen Gamepad speichert.
    private final List<HidGamepadState> connectedDevices = new ArrayList<>();

    // Framework-spezifische Joystick-Instanzen
    private final Joystick joystick1;
    private final Joystick joystick2;

    public UsbGamepad() {
        System.out.println("UsbGamepad: Suche nach HID-Devices...");

        // 1. Alle passenden Geräte finden
        List<HidDeviceInfo> joystickInfos = new ArrayList<>();
        for (HidDeviceInfo info : PureJavaHidApi.enumerateDevices()) {
            if (info.getVendorId() == VENDOR_ID && info.getProductId() == PRODUCT_ID) {
                joystickInfos.add(info);
            }
        }

        if (joystickInfos.isEmpty()) {
            System.out.println("UsbGamepad: ❌ Keine Joysticks gefunden – alle Eingaben bleiben false.");
            joystick1 = new DummyJoystick();
            joystick2 = new DummyJoystick();
            return;
        }

        System.out.printf("UsbGamepad: ✔ %d Joystick(s) gefunden.\n", joystickInfos.size());

        // 2. Jeden Joystick öffnen und den Zustand initialisieren
        for (int i = 0; i < joystickInfos.size(); i++) {
            HidDeviceInfo deviceInfo = joystickInfos.get(i);
            int joystickId = i + 1;

            if (joystickId > 2) { // Wir benötigen nur die ersten zwei
                System.out.printf("UsbGamepad: Ignoriere Joystick %d, da nur zwei unterstützt werden.\n", joystickId);
                continue;
            }

            try {
                HidDevice device = PureJavaHidApi.openDevice(deviceInfo);
                if (device != null) {
                    System.out.printf("UsbGamepad: ✔ Verbunden mit Joystick %d: %s\n", joystickId, deviceInfo.getProductString());
                    HidGamepadState state = new HidGamepadState(joystickId);
                    connectedDevices.add(state);
                    setupDeviceListener(device, state);
                } else {
                    System.out.printf("UsbGamepad: ❌ Gerät %d konnte nicht geöffnet werden.\n", joystickId);
                }
            } catch (IOException e) {
                System.out.printf("UsbGamepad: ❌ IOException beim Öffnen von Joystick %d.\n", joystickId);
                e.printStackTrace();
            }
        }

        // 3. Framework-Instanzen zuordnen
        if (connectedDevices.size() >= 1) {
            joystick1 = new HardwareJoystick(connectedDevices.get(0)); // Gamepad 1 verwendet den ersten HID-Zustand
        } else {
            joystick1 = new DummyJoystick();
        }

        if (connectedDevices.size() >= 2) {
            joystick2 = new HardwareJoystick(connectedDevices.get(1)); // Gamepad 2 verwendet den zweiten HID-Zustand
        } else {
            joystick2 = new DummyJoystick();
        }
    }

    // ===================== HID-Listener Setup =====================

    /**
     * Registriert den InputReportListener für ein spezifisches HID-Gerät.
     * @param device Das zu überwachende HidDevice.
     * @param state Der HidGamepadState, in dem die Daten gespeichert werden.
     */
    private void setupDeviceListener(HidDevice device, HidGamepadState state) {
        device.setInputReportListener((src, reportId, data, len) -> {
            state.update(data, len);
        });
    }

    // ===================== Gamepad-Interface Implementierung =====================

    /**
     * Gibt den Joystick für Spieler 1 zurück, der mit dem ersten gefundenen HID-Gerät verbunden ist.
     */
    @Override
    public Joystick joystick1() {
        return joystick1;
    }

    /**
     * Gibt den Joystick für Spieler 2 zurück, der mit dem zweiten gefundenen HID-Gerät verbunden ist.
     */
    @Override
    public Joystick joystick2() {
        return joystick2;
    }

    // --- Button Mapping ---
    // Mapping der Framework-Buttons auf die Bits des 1. Gamepads (index 0)
    private Button fromMask(int mask, int deviceIndex) {
        if (deviceIndex >= connectedDevices.size()) {
            return new Button(false); // Kein Gerät angeschlossen
        }
        int buttons = connectedDevices.get(deviceIndex).getButtons();
        return new Button((buttons & mask) != 0);
    }

    // Buttons Spieler 1 (vom 1. gefundenen HID-Gerät)
    @Override
    public Button buttonA1() { return fromMask(0x80, 0); } // Button 4 (im alten Code B1)
    @Override
    public Button buttonB1() { return fromMask(0x40, 0); } // Button 3 (im alten Code B2)
    @Override
    public Button buttonX1() { return fromMask(0x20, 0); } // Button 1 (im alten Code B3)
    @Override
    public Button buttonY1() { return fromMask(0x10, 0); } // Button 5 (im alten Code B4)

    // Buttons Spieler 2 (vom 2. gefundenen HID-Gerät)
    @Override
    public Button buttonA2() { return fromMask(0x80, 1); }
    @Override
    public Button buttonB2() { return fromMask(0x40, 1); }
    @Override
    public Button buttonX2() { return fromMask(0x20, 1); }
    @Override
    public Button buttonY2() { return fromMask(0x10, 1); }

    // Menü-Taste – hier Button 2 (0x08)
    @Override
    public Button buttonMenu() { return fromMask(0x08, 0); } // Nehmen wir Button 2 (0x08) von Gamepad 1

    // ===================== Innere Klasse: Zustand des physischen Gamepads =====================

    /**
     * Speichert den aktuellen Zustand (Achsen, Buttons) eines einzelnen physischen HID-Gamepads
     * und verarbeitet die eingehenden Reports.
     */
    private static class HidGamepadState {
        private final int joystickId;
        private volatile int x = 127;
        private volatile int y = 127;
        private volatile int buttons = 0;

        private int centerX = 127;
        private int centerY = 127;
        private boolean centerSet = false;

        public HidGamepadState(int id) {
            this.joystickId = id;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getCenterX() { return centerX; }
        public int getCenterY() { return centerY; }
        public int getButtons() { return buttons; }
        public boolean isCenterSet() { return centerSet; }

        /**
         * Aktualisiert den Zustand basierend auf einem eingehenden HID-Report.
         */
        public void update(byte[] data, int len) {
            if (len < 7) return;

            int lx = data[0] & 0xFF; // X-Achse
            int ly = data[1] & 0xFF; // Y-Achse
            int btn = data[6] & 0xFF; // Buttons (Bitmaske)

            if (!centerSet) {
                centerX = lx;
                centerY = ly;
                centerSet = true;
                System.out.printf("UsbGamepad [J%d]: Center gesetzt: X=%d, Y=%d%n", joystickId, centerX, centerY);
            }

            x = lx;
            y = ly;
            buttons = btn;
        }
    }

    // ===================== Innere Klasse: Joystick-Implementierungen =====================

    /**
     * Joystick, der direkt auf den Zustand eines spezifischen HidGamepadState basiert.
     */
    private static class HardwareJoystick implements Joystick {
        private final HidGamepadState state;

        public HardwareJoystick(HidGamepadState state) {
            this.state = state;
        }

        @Override
        public boolean up() {
            if (!state.isCenterSet()) return false;
            return (state.getY() < state.getCenterY() - DEADZONE);
        }

        @Override
        public boolean down() {
            if (!state.isCenterSet()) return false;
            return (state.getY() > state.getCenterY() + DEADZONE);
        }

        @Override
        public boolean left() {
            if (!state.isCenterSet()) return false;
            return (state.getX() < state.getCenterX() - DEADZONE);
        }

        @Override
        public boolean right() {
            if (!state.isCenterSet()) return false;
            return (state.getX() > state.getCenterX() + DEADZONE);
        }
    }

    /**
     * Dummy-Joystick, wenn kein physisches Gerät angeschlossen ist.
     */
    private static class DummyJoystick implements Joystick {
        @Override public boolean up() { return false; }
        @Override public boolean down() { return false; }
        @Override public boolean left() { return false; }
        @Override public boolean right() { return false; }
    }
}
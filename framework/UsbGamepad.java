package framework;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.PureJavaHidApi;

import java.io.IOException;

/**
 * Gamepad-Implementierung für den echten USB-Joystick (DragonRise Generic USB Joystick).
 */
public class UsbGamepad implements Gamepad {

    private static final int VENDOR_ID = 0x0079;   // DragonRise
    private static final int PRODUCT_ID = 0x0006;  // USB Joystick

    // Achsen-Zustände (werden vom HID-Listener aktualisiert)
    private volatile int x = 127;
    private volatile int y = 127;
    private volatile int buttons = 0;

    // Center-Werte (werden beim ersten Report gesetzt)
    private int centerX = 127;
    private int centerY = 127;
    private boolean centerSet = false;

    private static final int DEADZONE = 20; // anpassen falls nötig

    // Joystick-Instanzen für das Framework
    private final Joystick joystick1 = new HardwareJoystick();
    private final Joystick joystick2 = new DummyJoystick(); // aktuell kein zweiter Stick

    public UsbGamepad() {
        try {
            System.out.println("UsbGamepad: Suche nach HID-Device...");

            HidDeviceInfo deviceInfo = null;

            for (HidDeviceInfo info : PureJavaHidApi.enumerateDevices()) {
                if (info.getVendorId() == VENDOR_ID && info.getProductId() == PRODUCT_ID) {
                    deviceInfo = info;
                    break;
                }
            }

            if (deviceInfo == null) {
                System.out.println("UsbGamepad: ❌ Joystick nicht gefunden – alle Eingaben bleiben false.");
                return;
            }

            System.out.println("UsbGamepad: ✔ Joystick gefunden: " + deviceInfo.getProductString());

            HidDevice device = PureJavaHidApi.openDevice(deviceInfo);
            if (device == null) {
                System.out.println("UsbGamepad: ❌ Gerät konnte nicht geöffnet werden – alle Eingaben bleiben false.");
                return;
            }

            System.out.println("UsbGamepad: ✔ Verbunden, HID-Listener wird registriert.");

            device.setInputReportListener((src, reportId, data, len) -> {
                if (len < 7) return; // Sicherheit

                int lx = data[0] & 0xFF; // X-Achse
                int ly = data[1] & 0xFF; // Y-Achse
                int btn = data[6] & 0xFF; // Buttons (Bitmaske)

                if (!centerSet) {
                    centerX = lx;
                    centerY = ly;
                    centerSet = true;
                    System.out.printf("UsbGamepad: Center gesetzt: X=%d, Y=%d%n", centerX, centerY);
                }

                x = lx;
                y = ly;
                buttons = btn;
            });

        } catch (IOException e) {
            System.out.println("UsbGamepad: ❌ IOException beim Öffnen des HID-Geräts.");
            e.printStackTrace();
        }
    }

    // ===================== Gamepad-Interface =====================

    @Override
    public Joystick joystick1() {
        return joystick1;
    }

    @Override
    public Joystick joystick2() {
        return joystick2; // aktuell unbenutzt
    }

    // --- Buttons Spieler 1 ---
    // Mapping laut deinen Messungen:
    // Byte 6:
    // 0x20 -> Button 1
    // 0x08 -> Button 2
    // 0x40 -> Button 3
    // 0x80 -> Button 4
    // 0x10 -> Button 5

    private Button fromMask(int mask) {
        return new Button((buttons & mask) != 0);
    }

    @Override
    public Button buttonA1() { return fromMask(0x20); } // Button 1
    @Override
    public Button buttonB1() { return fromMask(0x08); } // Button 2
    @Override
    public Button buttonX1() { return fromMask(0x40); } // Button 3
    @Override
    public Button buttonY1() { return fromMask(0x80); } // Button 4

    // --- Buttons Spieler 2 (derzeit nicht belegt) ---
    @Override
    public Button buttonA2() { return new Button(false); }
    @Override
    public Button buttonB2() { return new Button(false); }
    @Override
    public Button buttonX2() { return new Button(false); }
    @Override
    public Button buttonY2() { return new Button(false); }

    // Menü-Taste – hier Button 5 (0x10)
    @Override
    public Button buttonMenu() { return fromMask(0x10); }

    // ===================== Joystick-Implementierungen =====================

    /**
     * Joystick, der direkt auf den USB-HID-Daten basiert.
     */
    private class HardwareJoystick implements Joystick {

        @Override
        public boolean up() {
            if (!centerSet) return false;
            return (y < centerY - DEADZONE);
        }

        @Override
        public boolean down() {
            if (!centerSet) return false;
            return (y > centerY + DEADZONE);
        }

        @Override
        public boolean left() {
            if (!centerSet) return false;
            return (x < centerX - DEADZONE);
        }

        @Override
        public boolean right() {
            if (!centerSet) return false;
            return (x > centerX + DEADZONE);
        }
    }

    /**
     * Dummy-Joystick für Spieler 2 (derzeit nicht verdrahtet).
     */
    private static class DummyJoystick implements Joystick {
        @Override public boolean up() { return false; }
        @Override public boolean down() { return false; }
        @Override public boolean left() { return false; }
        @Override public boolean right() { return false; }
    }
}

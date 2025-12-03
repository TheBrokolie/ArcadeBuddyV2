package framework;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.PureJavaHidApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoystickEventTest {

    // --- Konstanten für die Geräteidentifikation ---
    private static final int VENDOR_ID = 0x0079;   // DragonRise
    private static final int PRODUCT_ID = 0x0006;  // USB Joystick

    // --- Enum für die Richtung (kann in die Handler-Klasse verschoben werden) ---
    private enum Direction {
        CENTER, UP, DOWN, LEFT, RIGHT
    }

    /**
     * Hauptmethode zum Suchen, Öffnen und Initialisieren aller Joysticks.
     */
    public static void main(String[] args) {
        try {
            System.out.println("Suche nach HID-Devices...");

            // 1. Alle passenden Joysticks finden
            List<HidDeviceInfo> joystickInfos = new ArrayList<>();

            for (HidDeviceInfo info : PureJavaHidApi.enumerateDevices()) {
                System.out.printf("Gefunden: VID=%04X PID=%04X (%s)\n",
                        info.getVendorId(), info.getProductId(), info.getProductString());
                if (info.getVendorId() == VENDOR_ID && info.getProductId() == PRODUCT_ID) {
                    joystickInfos.add(info);
                }
            }

            if (joystickInfos.isEmpty()) {
                System.out.println("❌ Keine Joysticks gefunden!");
                return;
            }

            // 2. Jeden Joystick öffnen und einen Listener zuweisen
            List<HidDevice> openedDevices = new ArrayList<>();
            System.out.printf("✔ %d Joystick(s) gefunden.\n", joystickInfos.size());

            for (int i = 0; i < joystickInfos.size(); i++) {
                HidDeviceInfo deviceInfo = joystickInfos.get(i);
                int joystickId = i + 1; // ID zur Unterscheidung in der Ausgabe

                System.out.printf("   Öffne Joystick %d: %s\n", joystickId, deviceInfo.getProductString());

                HidDevice device = PureJavaHidApi.openDevice(deviceInfo);
                if (device != null) {
                    openedDevices.add(device);
                    // Den spezifischen Listener mit eigenem Zustands-Handler einrichten
                    setupDeviceListener(device, joystickId);
                } else {
                    System.out.printf("   ❌ Joystick %d konnte nicht geöffnet werden!\n", joystickId);
                }
            }

            if (openedDevices.isEmpty()) {
                System.out.println("❌ Es konnten keine Geräte geöffnet werden!");
                return;
            }

            System.out.println("\n✔ Alle verbundenen Joysticks initialisiert! Warte auf Events...");

            // 3. Haupt-Loop, um das Programm am Laufen zu halten
            while (true) {
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Richtet den InputReportListener für ein spezifisches HID-Gerät ein.
     * @param device Das zu überwachende HidDevice.
     * @param id Die eindeutige ID des Joysticks für die Ausgabe.
     */
    private static void setupDeviceListener(HidDevice device, int id) {
        // Erstellt einen Handler, der den individuellen Zustand des Joysticks speichert
        JoystickHandler handler = new JoystickHandler(id);

        device.setInputReportListener((src, reportId, data, len) -> {
            // Leitet die Rohdaten zur Verarbeitung an den spezifischen Handler weiter
            handler.processInput(data, len);
        });
    }


    /**
     * Klasse zur Kapselung des Zustands und der Logik für EINEN einzelnen Joystick.
     */
    static class JoystickHandler {
        private final int joystickId;
        private int centerX = 127;
        private int centerY = 127;
        private boolean centerSet = false;

        private static final int DEADZONE = 20;

        private Direction lastDirection = Direction.CENTER;

        // Zustandsspeicher für die Buttons
        private boolean lastB1 = false;
        private boolean lastB2 = false;
        private boolean lastB3 = false;
        private boolean lastB4 = false;
        private boolean lastB5 = false;

        public JoystickHandler(int id) {
            this.joystickId = id;
        }

        /**
         * Verarbeitet die rohen HID-Input-Daten und gibt Statusänderungen aus.
         */
        public void processInput(byte[] data, int len) {
            // Prüfen, ob der Report die erwartete Länge hat
            if (len < 7) return;

            // Rohwerte parsen
            int x = data[0] & 0xFF;
            int y = data[1] & 0xFF;
            int buttons = data[6] & 0xFF;

            // --- 1. Center-Position kalibrieren ---
            if (!centerSet) {
                centerX = x;
                centerY = y;
                centerSet = true;
                System.out.printf("[J%d] Center gesetzt: X=%d, Y=%d%n", joystickId, centerX, centerY);
            }

            // --- 2. Richtung bestimmen und ausgeben ---
            boolean left  = x < centerX - DEADZONE;
            boolean right = x > centerX + DEADZONE;
            boolean up    = y < centerY - DEADZONE;
            boolean down  = y > centerY + DEADZONE;

            Direction currentDir = Direction.CENTER;

            if (up && !down) currentDir = Direction.UP;
            else if (down && !up) currentDir = Direction.DOWN;
            else if (left && !right) currentDir = Direction.LEFT;
            else if (right && !left) currentDir = Direction.RIGHT;

            if (currentDir != lastDirection) {
                switch (currentDir) {
                    case UP -> System.out.printf("[J%d] Joystick nach OBEN erkannt!%n", joystickId);
                    case DOWN -> System.out.printf("[J%d] Joystick nach UNTEN erkannt!%n", joystickId);
                    case LEFT -> System.out.printf("[J%d] Joystick nach LINKS erkannt!%n", joystickId);
                    case RIGHT -> System.out.printf("[J%d] Joystick nach RECHTS erkannt!%n", joystickId);
                    case CENTER -> System.out.printf("[J%d] Joystick in Mittelstellung.%n", joystickId);
                }
                lastDirection = currentDir;
            }

            // --- 3. Buttons prüfen und ausgeben ---
            boolean b1 = (buttons & 0x20) != 0;
            boolean b2 = (buttons & 0x08) != 0;
            boolean b3 = (buttons & 0x40) != 0;
            boolean b4 = (buttons & 0x80) != 0;
            boolean b5 = (buttons & 0x10) != 0;

            if (b1 != lastB1) {
                System.out.printf("[J%d] Button 1 %s%n", joystickId, b1 ? "gedrückt!" : "losgelassen!");
                lastB1 = b1;
            }
            if (b2 != lastB2) {
                System.out.printf("[J%d] Button 2 %s%n", joystickId, b2 ? "gedrückt!" : "losgelassen!");
                lastB2 = b2;
            }
            if (b3 != lastB3) {
                System.out.printf("[J%d] Button 3 %s%n", joystickId, b3 ? "gedrückt!" : "losgelassen!");
                lastB3 = b3;
            }
            if (b4 != lastB4) {
                System.out.printf("[J%d] Button 4 %s%n", joystickId, b4 ? "gedrückt!" : "losgelassen!");
                lastB4 = b4;
            }
            if (b5 != lastB5) {
                System.out.printf("[J%d] Button 5 %s%n", joystickId, b5 ? "gedrückt!" : "losgelassen!");
                lastB5 = b5;
            }
        }
    }
}
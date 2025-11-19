package framework;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.PureJavaHidApi;

import java.io.IOException;
import java.util.Arrays;

public class TestHID {

    // ---- FELDER (oben in der Klasse, NICHT in main!) ----
    private static byte[] lastReport = null;
    private static byte[] baselineReport = null;

    private static final int VENDOR_ID = 0x0079;   // DragonRise
    private static final int PRODUCT_ID = 0x0006;  // USB Joystick

    public static void main(String[] args) {

        try {
            System.out.println("Suche nach HID-Device...");

            HidDeviceInfo deviceInfo = null;

            // Alle HID-Geräte anzeigen
            for (HidDeviceInfo info : PureJavaHidApi.enumerateDevices()) {
                System.out.printf("Gefunden: VID=%04X PID=%04X (%s)\n",
                        info.getVendorId(), info.getProductId(), info.getProductString());

                if (info.getVendorId() == VENDOR_ID && info.getProductId() == PRODUCT_ID) {
                    deviceInfo = info;
                }
            }

            if (deviceInfo == null) {
                System.out.println("❌ Joystick nicht gefunden!");
                return;
            }

            System.out.println("✔ Joystick gefunden: " + deviceInfo.getProductString());

            HidDevice device = PureJavaHidApi.openDevice(deviceInfo);

            if (device == null) {
                System.out.println("❌ Gerät konnte nicht geöffnet werden!");
                return;
            }

            System.out.println("✔ Verbunden! Warte auf Input...\n");

            // ---- LISTENER (hier DARF kein 'private static' mehr stehen) ----
            device.setInputReportListener((src, reportId, data, len) -> {

                // Ersten Report als Basis merken
                if (lastReport == null) {
                    lastReport = Arrays.copyOf(data, len);
                    baselineReport = Arrays.copyOf(data, len);
                    System.out.print("Initialer Report: ");
                    printHex(data, len);
                    return;
                }

                boolean realChange = false;

                for (int i = 0; i < len; i++) {
                    int oldVal = lastReport[i] & 0xFF;
                    int newVal = data[i] & 0xFF;
                    int baseVal = baselineReport[i] & 0xFF;

                    // --- ACHSEN (z.B. Byte 0–3) ---
                    if (i <= 3) {
                        // Deadzone um Mittelstellung: nur reagieren, wenn wir >10 Schritte vom Ruhewert weg sind
                        int distFromCenter = Math.abs(newVal - baseVal);
                        int oldDistFromCenter = Math.abs(oldVal - baseVal);

                        // Wenn sowohl alt als auch neu innerhalb der Deadzone sind → ignorieren
                        if (distFromCenter < 10 && oldDistFromCenter < 10) {
                            continue;
                        }

                        // Wenn die Distanz sich kaum ändert → ignorieren (kleine Flacker)
                        if (Math.abs(distFromCenter - oldDistFromCenter) < 3) {
                            continue;
                        }
                    }

                    // --- BUTTONS (ab Byte 4) ---
                    if (i >= 4) {
                        if (oldVal == newVal) {
                            continue; // kein Bit geändert
                        }
                    }

                    realChange = true;
                }

                if (!realChange) {
                    return; // nichts interessantes passiert
                }

                System.out.println("\n=== ECHTE Änderung erkannt ===");
                System.out.print("Neuer Report:   ");
                printHex(data, len);
                System.out.print("Vorheriger:     ");
                printHex(lastReport, len);

                for (int i = 0; i < len; i++) {
                    int oldVal = lastReport[i] & 0xFF;
                    int newVal = data[i] & 0xFF;
                    if (oldVal != newVal) {
                        System.out.printf(
                                "Byte %d: %3d -> %3d   altBits=%s neuBits=%s%n",
                                i, oldVal, newVal,
                                toBitString((byte) oldVal),
                                toBitString((byte) newVal)
                        );
                    }
                }

                lastReport = Arrays.copyOf(data, len);
            });

            while (true) {
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printHex(byte[] data, int len) {
        for (int i = 0; i < len; i++) {
            System.out.printf("%02X ", data[i]);
        }
        System.out.println();
    }

    private static String toBitString(byte b) {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 7; i >= 0; i--) {
            sb.append(((b >> i) & 1) == 1 ? '1' : '0');
        }
        return sb.toString();
    }
}

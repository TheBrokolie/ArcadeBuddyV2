package framework;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.PureJavaHidApi;

import java.io.IOException;

public class JoystickEventTest {

    private static final int VENDOR_ID = 0x0079;   // DragonRise
    private static final int PRODUCT_ID = 0x0006;  // USB Joystick

    private static int centerX = 127;
    private static int centerY = 127;
    private static boolean centerSet = false;

    private static final int DEADZONE = 20;

    private enum Direction {
        CENTER, UP, DOWN, LEFT, RIGHT
    }

    private static Direction lastDirection = Direction.CENTER;

    private static boolean lastB1 = false;
    private static boolean lastB2 = false;
    private static boolean lastB3 = false;
    private static boolean lastB4 = false;
    private static boolean lastB5 = false;

    public static void main(String[] args) {
        try {
            System.out.println("Suche nach HID-Device...");

            HidDeviceInfo deviceInfo = null;

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

            System.out.println("✔ Verbunden! Warte auf Joystick-Events...\n");

            device.setInputReportListener((src, reportId, data, len) -> {
                if (len < 7) return;

                int x = data[0] & 0xFF;
                int y = data[1] & 0xFF;
                int buttons = data[6] & 0xFF;

                if (!centerSet) {
                    centerX = x;
                    centerY = y;
                    centerSet = true;
                    System.out.printf("Center gesetzt: X=%d, Y=%d%n", centerX, centerY);
                }

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
                        case UP -> System.out.println("Joystick nach OBEN erkannt!");
                        case DOWN -> System.out.println("Joystick nach UNTEN erkannt!");
                        case LEFT -> System.out.println("Joystick nach LINKS erkannt!");
                        case RIGHT -> System.out.println("Joystick nach RECHTS erkannt!");
                        case CENTER -> System.out.println("Joystick in Mittelstellung.");
                    }
                    lastDirection = currentDir;
                }

                boolean b1 = (buttons & 0x20) != 0;
                boolean b2 = (buttons & 0x08) != 0;
                boolean b3 = (buttons & 0x40) != 0;
                boolean b4 = (buttons & 0x80) != 0;
                boolean b5 = (buttons & 0x10) != 0;

                if (b1 != lastB1) {
                    System.out.println(b1 ? "Button 1 gedrückt!" : "Button 1 losgelassen!");
                    lastB1 = b1;
                }
                if (b2 != lastB2) {
                    System.out.println(b2 ? "Button 2 gedrückt!" : "Button 2 losgelassen!");
                    lastB2 = b2;
                }
                if (b3 != lastB3) {
                    System.out.println(b3 ? "Button 3 gedrückt!" : "Button 3 losgelassen!");
                    lastB3 = b3;
                }
                if (b4 != lastB4) {
                    System.out.println(b4 ? "Button 4 gedrückt!" : "Button 4 losgelassen!");
                    lastB4 = b4;
                }
                if (b5 != lastB5) {
                    System.out.println(b5 ? "Button 5 gedrückt!" : "Button 5 losgelassen!");
                    lastB5 = b5;
                }
            });

            while (true) {
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

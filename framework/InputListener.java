package framework;

public interface InputListener {
    default void onButtonA1Pressed() {}
    default void onButtonA1Released() {}
    default void onButtonB1Pressed() {}
    default void onButtonB1Released() {}
    default void onButtonX1Pressed() {}
    default void onButtonX1Released() {}
    default void onButtonY1Pressed() {}
    default void onButtonY1Released() {}

    default void onJoystick1Up() {}
    default void onJoystick1Down() {}
    default void onJoystick1Left() {}
    default void onJoystick1Right() {}

    default void onButtonMenuPressed() {}
    default void onButtonMenuReleased() {}


}

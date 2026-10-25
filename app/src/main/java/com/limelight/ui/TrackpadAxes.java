package com.limelight.ui;

import java.util.Locale;

/**
 * Decides whether a pointer is the external keyboard-cover trackpad, and how
 * to flip its axes. Bluetooth mice stay untouched.
 *
 * <p>Android reports a touchpad's <em>device</em> sources as
 * {@code SOURCE_MOUSE | SOURCE_TOUCHPAD}, but {@code MotionEvent.getSource()}
 * is usually only {@code SOURCE_MOUSE}, or {@code SOURCE_MOUSE_RELATIVE}
 * while pointer capture is active. A Bluetooth mouse uses that same event
 * source and does not advertise {@code SOURCE_TOUCHPAD}. Callers pass those
 * facts in as booleans computed from the platform constants.
 */
public final class TrackpadAxes {
    /** Samsung Electronics, used only when the touchpad source bit is missing. */
    public static final int SAMSUNG_VENDOR_ID = 0x04E8;

    private TrackpadAxes() {}

    public static boolean isExternalTrackpad(boolean eventHasTouchpadSource,
                                             boolean deviceHasTouchpadSource,
                                             boolean deviceHasTouchscreen,
                                             boolean deviceHasKeyboard,
                                             boolean deviceHasMouse,
                                             String deviceName,
                                             int vendorId) {
        // sec_touchpad is the built-in digitizer on some Samsung phones, not
        // the Book Cover Keyboard. It must not pick up these toggles.
        if (isBuiltInSamsungDigitizer(deviceName) || deviceHasTouchscreen) {
            return false;
        }
        if (eventHasTouchpadSource || deviceHasTouchpadSource) {
            return true;
        }
        if (nameLooksLikeCoverTrackpad(deviceName)) {
            return true;
        }
        // Some pogo keyboards only advertise keyboard + mouse. A Bluetooth
        // mouse does not also advertise a keyboard.
        if (vendorId == SAMSUNG_VENDOR_ID && deviceHasKeyboard && deviceHasMouse
                && !nameLooksLikePlainMouse(deviceName)) {
            return true;
        }
        return false;
    }

    public static float invert(float delta, boolean invertAxis) {
        return invertAxis ? -delta : delta;
    }

    /** Reflect an absolute touchpad coordinate across that axis. */
    public static int reflect(int value, int max, boolean invertAxis) {
        if (!invertAxis) {
            return value;
        }
        int reflected = max - value;
        if (reflected < 0) {
            return 0;
        }
        if (reflected > max) {
            return max;
        }
        return reflected;
    }

    private static boolean isBuiltInSamsungDigitizer(String deviceName) {
        if (deviceName == null) {
            return false;
        }
        String name = deviceName.toLowerCase(Locale.ROOT);
        return name.contains("sec_touchpad") || name.contains("sec_touchscreen");
    }

    private static boolean nameLooksLikeCoverTrackpad(String deviceName) {
        if (deviceName == null || nameLooksLikePlainMouse(deviceName)) {
            return false;
        }
        String name = deviceName.toLowerCase(Locale.ROOT);
        return name.contains("touchpad")
                || name.contains("trackpad")
                || name.contains("book cover")
                || name.contains("keyboard cover")
                || name.contains("pogo");
    }

    private static boolean nameLooksLikePlainMouse(String deviceName) {
        if (deviceName == null) {
            return false;
        }
        String name = deviceName.toLowerCase(Locale.ROOT);
        if (!name.contains("mouse")) {
            return false;
        }
        return !name.contains("touchpad")
                && !name.contains("trackpad")
                && !name.contains("book cover")
                && !name.contains("keyboard cover");
    }
}

package com.limelight.ui;

import java.util.Locale;

/**
 * Decides whether a pointer is the external keyboard-cover trackpad, and how
 * to remap its axes. Bluetooth mice stay untouched.
 *
 * <p>Android reports a touchpad's <em>device</em> sources as
 * {@code SOURCE_MOUSE | SOURCE_TOUCHPAD}, but {@code MotionEvent.getSource()}
 * is usually only {@code SOURCE_MOUSE}, or {@code SOURCE_MOUSE_RELATIVE}
 * while pointer capture is active. A Bluetooth mouse uses that same event
 * source and does not advertise {@code SOURCE_TOUCHPAD}. Callers pass those
 * facts in as booleans computed from the platform constants.
 *
 * <p>When a remap is requested, the order is always:
 * <ol>
 * <li>Swap X and Y (and, for absolute positions, their reference maxima).</li>
 * <li>Invert the resulting X axis.</li>
 * <li>Invert the resulting Y axis.</li>
 * </ol>
 * +X is right and +Y is down. Applied to a reported relative delta {@code (x, y)}:
 * <ul>
 * <li>no swap, no invert — {@code (x, y)}, unchanged</li>
 * <li>invert X — {@code (-x, y)}, left/right mirror</li>
 * <li>invert Y — {@code (x, -y)}, up/down mirror</li>
 * <li>invert X and invert Y — {@code (-x, -y)}, 180°</li>
 * <li>swap — {@code (y, x)}, reflection across the diagonal</li>
 * <li>swap and invert X — {@code (-y, x)}, 90° clockwise</li>
 * <li>swap and invert Y — {@code (y, -x)}, 90° counter-clockwise (270° clockwise)</li>
 * <li>swap and both inverts — {@code (-y, -x)}, reflection across the anti-diagonal</li>
 * </ul>
 * Those three rotations, plus the identity, are every multiple of 90°. A cover
 * whose cursor is 90° clockwise of the finger (finger up moves the cursor
 * right) is undone by swap and invert Y.
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
        // Built-in digitizers advertise SOURCE_TOUCHSCREEN (or are named
        // sec_touchscreen). Skip those so the S Pen / panel does not pick up
        // these toggles.
        //
        // Do not exclude by the name "sec_touchpad" alone: on Galaxy Tabs the
        // Book Cover trackpad uses that name, and under pointer capture it
        // drops TOUCHSCREEN and becomes KEYBOARD|TOUCHPAD. Excluding by name
        // made Swap/Invert no-ops while the cursor stayed 90° clockwise of
        // the finger (the common Samsung cover bug).
        if (deviceHasTouchscreen || isPanelDigitizerName(deviceName)) {
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

    /** Relative delta after swap, then invert X, then invert Y. */
    public static final class Delta {
        public final float x;
        public final float y;

        public Delta(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Remap a pointer-capture delta. Swap is applied to the reported axes
     * before either invert, so swap plus one invert is a 90° rotation rather
     * than a mirror of an already-rotated vector.
     */
    public static Delta remap(float x, float y, boolean swap, boolean invertX, boolean invertY) {
        float outX = swap ? y : x;
        float outY = swap ? x : y;
        return new Delta(invert(outX, invertX), invert(outY, invertY));
    }

    /**
     * Scale from remapped relative finger deltas to Moonlight high-res wheel
     * units. Matches the factor used by moonlight-android#1569.
     */
    public static final float TWO_FINGER_SCROLL_SCALE = 10f;

    /**
     * Classic mouse-wheel amounts from a two-finger trackpad move.
     * {@code vertical} is for {@code sendMouseHighResScroll},
     * {@code horizontal} for {@code sendMouseHighResHScroll}.
     *
     * <p>Samsung Book Cover keyboards report two-finger scroll as
     * {@code ACTION_MOVE} with {@code pointerCount == 2} on the relative
     * axes, not {@code ACTION_SCROLL}. After the same remap used for the
     * cursor, convert those deltas to wheel ticks: finger down (positive Y)
     * scrolls down like rolling a wheel toward you; finger right scrolls
     * right.
     */
    public static final class Scroll {
        public final short vertical;
        public final short horizontal;

        public Scroll(short vertical, short horizontal) {
            this.vertical = vertical;
            this.horizontal = horizontal;
        }
    }

    public static Scroll classicTwoFingerScroll(float remappedDeltaX, float remappedDeltaY) {
        // Classic (naturalScroll=false) from moonlight-android#1569:
        // v = -y * scale, h = x * scale
        return new Scroll(
                (short) (-remappedDeltaY * TWO_FINGER_SCROLL_SCALE),
                (short) (remappedDeltaX * TWO_FINGER_SCROLL_SCALE));
    }

    /**
     * Two fingers without a held primary click is scroll. Primary held (or a
     * finger held past tap time) plus a second finger is click-drag: the
     * cursor should move with the button still down, not wheel-scroll.
     */
    public static boolean isTwoFingerScroll(int pointerCount,
                                            boolean primaryButtonHeld,
                                            boolean fingerHeldPastTap) {
        return pointerCount == 2 && !primaryButtonHeld && !fingerHeldPastTap;
    }

    /** Absolute position, with the reference maxima swapped alongside the axes. */
    public static final class Absolute {
        public final int x;
        public final int y;
        public final int xMax;
        public final int yMax;

        public Absolute(int x, int y, int xMax, int yMax) {
            this.x = x;
            this.y = y;
            this.xMax = xMax;
            this.yMax = yMax;
        }
    }

    /**
     * Remap a pre-capture absolute touchpad position. The axis ranges are
     * exchanged in the same step as the coordinates, then each axis is
     * reflected inside its new range. Sending the original maxima after a
     * swap would scale the cursor against the wrong axis.
     */
    public static Absolute remapAbsolute(int x, int y, int xMax, int yMax,
                                         boolean swap, boolean invertX, boolean invertY) {
        int outX = swap ? y : x;
        int outY = swap ? x : y;
        int outXMax = swap ? yMax : xMax;
        int outYMax = swap ? xMax : yMax;
        return new Absolute(
                reflect(outX, outXMax, invertX),
                reflect(outY, outYMax, invertY),
                outXMax,
                outYMax);
    }

    private static boolean isPanelDigitizerName(String deviceName) {
        if (deviceName == null) {
            return false;
        }
        return deviceName.toLowerCase(Locale.ROOT).contains("sec_touchscreen");
    }

    private static boolean nameLooksLikeCoverTrackpad(String deviceName) {
        if (deviceName == null || nameLooksLikePlainMouse(deviceName) || isPanelDigitizerName(deviceName)) {
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

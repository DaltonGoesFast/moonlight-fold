package com.limelight.ui;

/**
 * Tap-to-click and click-drag helpers for external keyboard-cover trackpads
 * under pointer capture.
 *
 * <p>Outside Moonlight, Android turns taps into {@code BUTTON_PRIMARY}. Under
 * capture that often never reaches the app. Samsung also tends to clear the
 * primary bit as soon as a second finger lands, which made click-and-drag
 * fall through to two-finger scroll — so primary is sticky until every finger
 * lifts.
 *
 * <p>Plant-then-second-finger (one finger still on the pad, then another moves)
 * arms click-drag after a short plant time, without waiting a full tap timeout.
 */
public final class TrackpadTapClick {
    /** Max contact duration still counted as a tap on lift (ms). */
    public static final int TAP_TIME_MS = 300;

    /**
     * How long the first finger must rest before a second finger means
     * click-drag instead of two-finger scroll (ms).
     */
    public static final int PLANT_TIME_MS = 70;

    /** Max Manhattan relative movement still counted as a tap / plant. */
    public static final float TAP_MOVE_LIMIT = 30f;

    private boolean contactActive;
    private long downTimeMs;
    private float moved;
    /** Remembers a physical click-pad press across two-finger events. */
    private boolean primarySticky;
    /** Synthesized left-button hold for plant + second-finger drag. */
    private boolean holdDragActive;

    public void onDown(long eventTimeMs) {
        contactActive = true;
        downTimeMs = eventTimeMs;
        moved = 0f;
    }

    public void onMove(float deltaX, float deltaY) {
        if (!contactActive) {
            return;
        }
        moved += Math.abs(deltaX) + Math.abs(deltaY);
    }

    /** Latch physical {@code BUTTON_PRIMARY} until {@link #endGesture()}. */
    public void notePrimaryPressed(boolean primaryDown) {
        if (primaryDown) {
            primarySticky = true;
        }
    }

    public void armHoldDrag() {
        holdDragActive = true;
        primarySticky = true;
    }

    public boolean isHoldDragActive() {
        return holdDragActive;
    }

    public boolean isPrimarySticky() {
        return primarySticky;
    }

    /**
     * First finger has rested briefly without sliding — second finger should
     * drag, not scroll.
     */
    public boolean isPlantedForDrag(long eventTimeMs) {
        return contactActive
                && (eventTimeMs - downTimeMs) >= PLANT_TIME_MS
                && moved <= TAP_MOVE_LIMIT;
    }

    /** @deprecated use {@link #isPlantedForDrag(long)} */
    public boolean isHeldClick(long eventTimeMs) {
        return contactActive && (eventTimeMs - downTimeMs) > TAP_TIME_MS;
    }

    public boolean isContactActive() {
        return contactActive;
    }

    /** Clear tap tracking only (scroll chord); keep sticky primary if set. */
    public void cancelTap() {
        contactActive = false;
        moved = 0f;
    }

    /** Physical cancel or full lift — drop tap, sticky click, and hold-drag. */
    public void endGesture() {
        contactActive = false;
        moved = 0f;
        primarySticky = false;
        holdDragActive = false;
    }

    /** @deprecated use {@link #cancelTap()} or {@link #endGesture()} */
    public void cancel() {
        endGesture();
    }

    /**
     * Ends the contact for tap detection. Does not clear sticky primary /
     * hold-drag (call {@link #endGesture()} on the final finger-up).
     */
    public boolean onUp(long eventTimeMs) {
        if (!contactActive) {
            return false;
        }
        boolean isTap = moved <= TAP_MOVE_LIMIT
                && (eventTimeMs - downTimeMs) <= TAP_TIME_MS
                && !holdDragActive;
        contactActive = false;
        moved = 0f;
        return isTap;
    }
}

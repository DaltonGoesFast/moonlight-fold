package com.limelight.ui;

/**
 * Synthesizes a left click from a short, low-movement finger contact on an
 * external keyboard-cover trackpad.
 *
 * <p>Outside Moonlight, Android turns tap-to-click into {@code BUTTON_PRIMARY}.
 * Under pointer capture that synthesis often never reaches the app — only a
 * physical click-pad press sets the button bit (same class of bug as the DeX
 * {@code ACTION_DOWN}/{@code ACTION_UP} workaround in {@code Game}). A tap is
 * a contact that lifts quickly without sliding; sliding to move the cursor
 * cancels it so motion does not become click-and-drag.
 */
public final class TrackpadTapClick {
    /** Max contact duration still counted as a tap (ms). */
    public static final int TAP_TIME_MS = 300;

    /** Max Manhattan relative movement still counted as a tap. */
    public static final float TAP_MOVE_LIMIT = 30f;

    private boolean contactActive;
    private long downTimeMs;
    private float moved;

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

    /** Physical click-pad press owns the gesture; do not also fire a tap. */
    public void cancel() {
        contactActive = false;
        moved = 0f;
    }

    public boolean isContactActive() {
        return contactActive;
    }

    /**
     * Ends the contact. Returns true when this lift should emit a left click
     * (short duration, little movement, and the contact was still active).
     */
    public boolean onUp(long eventTimeMs) {
        if (!contactActive) {
            return false;
        }
        boolean isTap = moved <= TAP_MOVE_LIMIT
                && (eventTimeMs - downTimeMs) <= TAP_TIME_MS;
        contactActive = false;
        moved = 0f;
        return isTap;
    }
}

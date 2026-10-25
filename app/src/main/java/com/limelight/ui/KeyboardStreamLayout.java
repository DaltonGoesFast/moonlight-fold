package com.limelight.ui;

/**
 * Where to put the stream surface while a soft keyboard may be open.
 *
 * <p>Moonlight Fold uses this so an unfolded Galaxy Z Fold (and any other
 * device showing an IME over the fullscreen stream) keeps the video above
 * the keyboard. The signals are measured by {@code Game}; this class only
 * combines them so the cases stay testable without a device.
 */
public final class KeyboardStreamLayout {
    public final boolean pinToTop;
    public final int bottomInsetPx;

    public KeyboardStreamLayout(boolean pinToTop, int bottomInsetPx) {
        this.pinToTop = pinToTop;
        this.bottomInsetPx = Math.max(0, bottomInsetPx);
    }

    /**
     * @param settingEnabled        "Pin stream to top when keyboard is open"
     * @param inPictureInPicture    PiP keeps the ordinary centered layout
     * @param imeOverlapPx          {@code WindowInsets.Type.ime()} bottom, in this window
     * @param frameOverlapPx        window bottom covered by the visible-display frame
     * @param reflectedImeHeightPx  hidden {@code getInputMethodWindowVisibleHeight}, or 0
     * @param windowShrinkPx        full display height minus this window's height
     * @param minKeyboardPx         ignore nav bars and tiny strips below this height
     */
    public static KeyboardStreamLayout compute(boolean settingEnabled,
                                               boolean inPictureInPicture,
                                               int imeOverlapPx,
                                               int frameOverlapPx,
                                               int reflectedImeHeightPx,
                                               int windowShrinkPx,
                                               int minKeyboardPx) {
        if (!settingEnabled || inPictureInPicture || minKeyboardPx <= 0) {
            return closed();
        }

        // Prefer the IME inset. It is the overlap inside this window, so it
        // does not include a navigation bar the way the visible frame can.
        int overlap;
        if (imeOverlapPx >= minKeyboardPx) {
            overlap = imeOverlapPx;
        }
        else {
            overlap = Math.max(0, Math.max(imeOverlapPx, frameOverlapPx));
        }

        // Some fullscreen immersive windows never populate IME insets.
        // The reflective keyboard height is the IME window's own height.
        // Trust it as an overlap only when this window still reaches the
        // bottom of the screen; otherwise the window was already resized
        // and top-pinning that shorter region is enough.
        boolean reflectedKeyboard = reflectedImeHeightPx >= minKeyboardPx;
        if (overlap < minKeyboardPx && reflectedKeyboard && windowShrinkPx < minKeyboardPx) {
            overlap = reflectedImeHeightPx;
        }

        boolean keyboardOpen = overlap >= minKeyboardPx || reflectedKeyboard;

        // A window that is already shorter than the screen by about the
        // same amount as the keyboard has been resized out of the way.
        // Inset only the still-overlapping remainder (usually zero).
        // A much larger shrink is multi-window, not the keyboard, so the
        // window-relative overlap stays as-is.
        if (keyboardOpen && overlap >= minKeyboardPx && windowShrinkPx >= minKeyboardPx) {
            int delta = Math.abs(overlap - windowShrinkPx);
            if (delta <= Math.max(240, overlap / 3)) {
                overlap = Math.max(0, overlap - windowShrinkPx);
            }
        }

        if (!keyboardOpen) {
            return closed();
        }

        if (overlap < minKeyboardPx) {
            return new KeyboardStreamLayout(true, 0);
        }
        return new KeyboardStreamLayout(true, overlap);
    }

    private static KeyboardStreamLayout closed() {
        return new KeyboardStreamLayout(false, 0);
    }
}

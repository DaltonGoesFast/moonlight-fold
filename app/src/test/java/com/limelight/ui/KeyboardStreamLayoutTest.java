package com.limelight.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyboardStreamLayoutTest {
    private static final int MIN = 160;

    @Test
    public void closedKeyboardKeepsTheNormalLayout() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, false, 0, 48, 0, 0, MIN);
        assertFalse(layout.pinToTop);
        assertEquals(0, layout.bottomInsetPx);
    }

    @Test
    public void settingOffDoesNotMoveTheStream() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(false, false, 700, 700, 700, 0, MIN);
        assertFalse(layout.pinToTop);
        assertEquals(0, layout.bottomInsetPx);
    }

    @Test
    public void pictureInPictureKeepsTheNormalLayout() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, true, 700, 0, 0, 0, MIN);
        assertFalse(layout.pinToTop);
    }

    @Test
    public void imeInsetPinsAndShrinksToTheAreaAboveTheKeyboard() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, false, 700, 760, 700, 0, MIN);
        assertTrue(layout.pinToTop);
        // Prefer the IME inset over the slightly larger visible-frame value.
        assertEquals(700, layout.bottomInsetPx);
    }

    @Test
    public void visibleFrameIsUsedWhenImeInsetsAreMissing() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, false, 0, 640, 0, 0, MIN);
        assertTrue(layout.pinToTop);
        assertEquals(640, layout.bottomInsetPx);
    }

    @Test
    public void reflectedHeightCoversFullScreenWhenInsetsAreZero() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, false, 0, 0, 680, 0, MIN);
        assertTrue(layout.pinToTop);
        assertEquals(680, layout.bottomInsetPx);
    }

    @Test
    public void alreadyResizedWindowIsPinnedWithoutASecondInset() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, false, 0, 0, 700, 700, MIN);
        assertTrue(layout.pinToTop);
        assertEquals(0, layout.bottomInsetPx);
    }

    @Test
    public void imeInsetIsNotAppliedTwiceWhenTheWindowAlreadyShrank() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, false, 700, 700, 700, 680, MIN);
        assertTrue(layout.pinToTop);
        assertEquals(0, layout.bottomInsetPx);
    }

    @Test
    public void multiWindowOverlapIsNotReducedByTheWindowShrink() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, false, 400, 0, 700, 900, MIN);
        assertTrue(layout.pinToTop);
        assertEquals(400, layout.bottomInsetPx);
    }

    @Test
    public void multiWindowWithoutAKeyboardStaysCentered() {
        KeyboardStreamLayout layout = KeyboardStreamLayout.compute(true, false, 0, 0, 0, 900, MIN);
        assertFalse(layout.pinToTop);
        assertEquals(0, layout.bottomInsetPx);
    }
}

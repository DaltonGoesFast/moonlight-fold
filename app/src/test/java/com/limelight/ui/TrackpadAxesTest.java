package com.limelight.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrackpadAxesTest {
    @Test
    public void bluetoothMouseRelativeEventsAreNotATrackpad() {
        assertFalse(TrackpadAxes.isExternalTrackpad(
                false, false, false, false, true,
                "Logitech MX Anywhere 3", 0x046D));
    }

    @Test
    public void bookCoverReportsMouseEventsButAdvertisesTouchpad() {
        // MotionEvent source is MOUSE_RELATIVE under pointer capture.
        // The device still has SOURCE_TOUCHPAD.
        assertTrue(TrackpadAxes.isExternalTrackpad(
                false, true, false, true, true,
                "Samsung Book Cover Keyboard", TrackpadAxes.SAMSUNG_VENDOR_ID));
    }

    @Test
    public void explicitTouchpadEventIsATrackpad() {
        assertTrue(TrackpadAxes.isExternalTrackpad(
                true, true, false, true, true,
                "Samsung Book Cover Keyboard", TrackpadAxes.SAMSUNG_VENDOR_ID));
    }

    @Test
    public void touchscreenIsNotATrackpad() {
        assertFalse(TrackpadAxes.isExternalTrackpad(
                false, true, true, true, true,
                "sec_touchpad", TrackpadAxes.SAMSUNG_VENDOR_ID));
    }

    @Test
    public void builtInDigitizerNameIsExcludedEvenWithoutTouchscreen() {
        assertFalse(TrackpadAxes.isExternalTrackpad(
                true, true, false, true, true,
                "sec_touchpad", TrackpadAxes.SAMSUNG_VENDOR_ID));
    }

    @Test
    public void nameFallbackCatchesACoverThatOmitsTheTouchpadBit() {
        assertTrue(TrackpadAxes.isExternalTrackpad(
                false, false, false, true, true,
                "Samsung Book Cover Keyboard", 0));
    }

    @Test
    public void plainMouseNameIsNeverAFallback() {
        assertFalse(TrackpadAxes.isExternalTrackpad(
                false, false, false, false, true,
                "Samsung Bluetooth Mouse", TrackpadAxes.SAMSUNG_VENDOR_ID));
    }

    @Test
    public void samsungKeyboardPlusMouseWithoutTouchpadBitIsACover() {
        assertTrue(TrackpadAxes.isExternalTrackpad(
                false, false, false, true, true,
                "STM32", TrackpadAxes.SAMSUNG_VENDOR_ID));
    }

    @Test
    public void samsungMouseWithoutKeyboardIsNotACover() {
        assertFalse(TrackpadAxes.isExternalTrackpad(
                false, false, false, false, true,
                "STM32", TrackpadAxes.SAMSUNG_VENDOR_ID));
    }

    @Test
    public void invertFlipsOnlyTheRequestedAxis() {
        assertEquals(-4f, TrackpadAxes.invert(4f, true), 0f);
        assertEquals(4f, TrackpadAxes.invert(4f, false), 0f);
        assertEquals(0f, TrackpadAxes.invert(0f, true), 0f);
    }

    @Test
    public void reflectFlipsAbsoluteTouchpadCoordinates() {
        assertEquals(800, TrackpadAxes.reflect(200, 1000, true));
        assertEquals(200, TrackpadAxes.reflect(200, 1000, false));
        assertEquals(0, TrackpadAxes.reflect(1000, 1000, true));
    }

    @Test
    public void remapWithNothingOnIsIdentity() {
        assertDelta(3f, -2f, TrackpadAxes.remap(3f, -2f, false, false, false));
    }

    @Test
    public void invertXOnlyMirrorsLeftAndRight() {
        assertDelta(-3f, -2f, TrackpadAxes.remap(3f, -2f, false, true, false));
    }

    @Test
    public void invertYOnlyMirrorsUpAndDown() {
        assertDelta(3f, 2f, TrackpadAxes.remap(3f, -2f, false, false, true));
    }

    @Test
    public void bothInvertsAre180Degrees() {
        assertDelta(-3f, 2f, TrackpadAxes.remap(3f, -2f, false, true, true));
    }

    @Test
    public void swapOnlyReflectsAcrossTheDiagonal() {
        assertDelta(-2f, 3f, TrackpadAxes.remap(3f, -2f, true, false, false));
    }

    @Test
    public void swapThenInvertXIs90DegreesClockwise() {
        // (x, y) -> (-y, x). Right (3, 0) becomes down (0, 3).
        assertDelta(2f, 3f, TrackpadAxes.remap(3f, -2f, true, true, false));
        assertDelta(0f, 3f, TrackpadAxes.remap(3f, 0f, true, true, false));
    }

    @Test
    public void swapThenInvertYIs90DegreesCounterClockwise() {
        // (x, y) -> (y, -x). Right (3, 0) becomes up (0, -3).
        // This undoes a cursor that is 90° clockwise of the finger.
        assertDelta(-2f, -3f, TrackpadAxes.remap(3f, -2f, true, false, true));
        assertDelta(0f, -3f, TrackpadAxes.remap(3f, 0f, true, false, true));
    }

    @Test
    public void swapThenBothInvertsReflectsAcrossTheAntiDiagonal() {
        assertDelta(2f, -3f, TrackpadAxes.remap(3f, -2f, true, true, true));
    }

    @Test
    public void swapIsAppliedBeforeEitherInvert() {
        // Invert-then-swap of (2, 5) with invert X would be (5, -2).
        // Swap-then-invert X is (-5, 2).
        assertDelta(-5f, 2f, TrackpadAxes.remap(2f, 5f, true, true, false));
    }

    @Test
    public void absoluteRemapSwapsRangesBeforeReflecting() {
        TrackpadAxes.Absolute identity = TrackpadAxes.remapAbsolute(
                100, 400, 1000, 2000, false, false, false);
        assertAbsolute(100, 400, 1000, 2000, identity);

        // Swap, then reflect X inside the old Y range: 2000 - 400 = 1600.
        TrackpadAxes.Absolute rotated = TrackpadAxes.remapAbsolute(
                100, 400, 1000, 2000, true, true, false);
        assertAbsolute(1600, 100, 2000, 1000, rotated);

        // Swap, then reflect Y inside the old X range: 1000 - 100 = 900.
        TrackpadAxes.Absolute undone = TrackpadAxes.remapAbsolute(
                100, 400, 1000, 2000, true, false, true);
        assertAbsolute(400, 900, 2000, 1000, undone);
    }

    private static void assertDelta(float x, float y, TrackpadAxes.Delta delta) {
        assertEquals(x, delta.x, 0f);
        assertEquals(y, delta.y, 0f);
    }

    private static void assertAbsolute(int x, int y, int xMax, int yMax, TrackpadAxes.Absolute absolute) {
        assertEquals(x, absolute.x);
        assertEquals(y, absolute.y);
        assertEquals(xMax, absolute.xMax);
        assertEquals(yMax, absolute.yMax);
    }
}

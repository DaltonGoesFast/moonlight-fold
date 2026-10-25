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
}

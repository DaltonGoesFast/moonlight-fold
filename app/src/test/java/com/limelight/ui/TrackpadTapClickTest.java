package com.limelight.ui;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrackpadTapClickTest {
    @Test
    public void shortStillContactIsATap() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        assertTrue(tap.onUp(1000 + TrackpadTapClick.TAP_TIME_MS));
    }

    @Test
    public void longContactIsNotATap() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        assertFalse(tap.onUp(1000 + TrackpadTapClick.TAP_TIME_MS + 1));
    }

    @Test
    public void slidingFingerCancelsTheTap() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        tap.onMove(TrackpadTapClick.TAP_MOVE_LIMIT + 1f, 0f);
        assertFalse(tap.onUp(1100));
    }

    @Test
    public void smallJitterStillCountsAsATap() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        tap.onMove(5f, 5f);
        assertTrue(tap.onUp(1150));
    }

    @Test
    public void physicalClickCancelSuppressesTap() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        tap.cancel();
        assertFalse(tap.onUp(1100));
    }

    @Test
    public void upWithoutDownIsNotATap() {
        assertFalse(new TrackpadTapClick().onUp(1000));
    }

    @Test
    public void heldPastTapTimeIsHeldClick() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        assertFalse(tap.isHeldClick(1000 + TrackpadTapClick.TAP_TIME_MS));
        assertTrue(tap.isHeldClick(1000 + TrackpadTapClick.TAP_TIME_MS + 1));
    }

    @Test
    public void noContactIsNotHeldClick() {
        assertFalse(new TrackpadTapClick().isHeldClick(5000));
    }
}

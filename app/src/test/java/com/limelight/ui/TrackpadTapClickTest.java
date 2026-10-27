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
    public void endGestureSuppressesTap() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        tap.endGesture();
        assertFalse(tap.onUp(1100));
    }

    @Test
    public void upWithoutDownIsNotATap() {
        assertFalse(new TrackpadTapClick().onUp(1000));
    }

    @Test
    public void plantTimeIsShorterThanTapTime() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        assertFalse(tap.isPlantedForDrag(1000 + TrackpadTapClick.PLANT_TIME_MS - 1));
        assertTrue(tap.isPlantedForDrag(1000 + TrackpadTapClick.PLANT_TIME_MS));
        assertFalse(tap.isHeldClick(1000 + TrackpadTapClick.PLANT_TIME_MS));
    }

    @Test
    public void slidingBreaksPlantForDrag() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        tap.onMove(TrackpadTapClick.TAP_MOVE_LIMIT + 1f, 0f);
        assertFalse(tap.isPlantedForDrag(1000 + TrackpadTapClick.PLANT_TIME_MS + 50));
    }

    @Test
    public void primaryStickySurvivesUntilEndGesture() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.notePrimaryPressed(true);
        assertTrue(tap.isPrimarySticky());
        tap.notePrimaryPressed(false);
        assertTrue(tap.isPrimarySticky());
        tap.endGesture();
        assertFalse(tap.isPrimarySticky());
    }

    @Test
    public void armHoldDragLatchesPrimary() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.armHoldDrag();
        assertTrue(tap.isHoldDragActive());
        assertTrue(tap.isPrimarySticky());
        tap.endGesture();
        assertFalse(tap.isHoldDragActive());
    }

    @Test
    public void holdDragUpIsNotATap() {
        TrackpadTapClick tap = new TrackpadTapClick();
        tap.onDown(1000);
        tap.armHoldDrag();
        assertFalse(tap.onUp(1100));
    }
}

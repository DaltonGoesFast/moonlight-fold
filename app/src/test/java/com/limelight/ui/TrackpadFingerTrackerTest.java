package com.limelight.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrackpadFingerTrackerTest {
    @Test
    public void firstPollOnlySetsBaseline() {
        TrackpadFingerTracker tracker = new TrackpadFingerTracker();
        TrackpadAxes.Delta d = tracker.pollMostMovedFinger(
                2,
                new int[] {1, 2},
                new float[] {10f, 20f},
                new float[] {30f, 40f});
        assertEquals(0f, d.x, 0f);
        assertEquals(0f, d.y, 0f);
        assertTrue(tracker.hasBaseline());
    }

    @Test
    public void reportsTheFingerThatMovedMore() {
        TrackpadFingerTracker tracker = new TrackpadFingerTracker();
        tracker.captureBaseline(
                2,
                new int[] {1, 2},
                new float[] {10f, 20f},
                new float[] {30f, 40f});

        // Finger 1 still, finger 2 moved +5,+7
        TrackpadAxes.Delta d = tracker.pollMostMovedFinger(
                2,
                new int[] {1, 2},
                new float[] {10f, 25f},
                new float[] {30f, 47f});
        assertEquals(5f, d.x, 0f);
        assertEquals(7f, d.y, 0f);
    }

    @Test
    public void stationaryClickFingerIsIgnoredWhenOtherMoves() {
        TrackpadFingerTracker tracker = new TrackpadFingerTracker();
        tracker.captureBaseline(
                2,
                new int[] {10, 11},
                new float[] {100f, 200f},
                new float[] {100f, 200f});

        TrackpadAxes.Delta d = tracker.pollMostMovedFinger(
                2,
                new int[] {10, 11},
                new float[] {100f, 250f},
                new float[] {101f, 200f});
        // Finger 10 moved 0+1; finger 11 moved 50+0 — pick 11
        assertEquals(50f, d.x, 0f);
        assertEquals(0f, d.y, 0f);
    }

    @Test
    public void resetClearsBaseline() {
        TrackpadFingerTracker tracker = new TrackpadFingerTracker();
        tracker.captureBaseline(1, new int[] {1}, new float[] {1f}, new float[] {2f});
        assertTrue(tracker.hasBaseline());
        tracker.reset();
        assertFalse(tracker.hasBaseline());
    }
}

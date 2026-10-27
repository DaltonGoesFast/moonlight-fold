package com.limelight.ui;

/**
 * Tracks per-finger absolute positions on an external touchpad so click-drag
 * can follow the finger that is actually moving.
 *
 * <p>Under pointer capture, {@code AXIS_RELATIVE_X/Y} often only reflect the
 * clicking (stationary) finger — so holding click with one finger and sliding
 * the other produces no cursor motion. The moving finger's absolute deltas
 * still change; use {@link #pollMostMovedFinger} while primary is held with
 * two contacts.
 */
public final class TrackpadFingerTracker {
    private static final int MAX_POINTERS = 5;

    private final int[] ids = new int[MAX_POINTERS];
    private final float[] lastX = new float[MAX_POINTERS];
    private final float[] lastY = new float[MAX_POINTERS];
    private int tracked;
    private boolean hasBaseline;

    public void reset() {
        tracked = 0;
        hasBaseline = false;
    }

    /**
     * Snapshot current absolute positions without emitting a delta. Call when
     * the second finger lands so the first move does not jump.
     */
    public void captureBaseline(int pointerCount, int[] pointerIds, float[] xs, float[] ys) {
        tracked = Math.min(pointerCount, MAX_POINTERS);
        for (int i = 0; i < tracked; i++) {
            ids[i] = pointerIds[i];
            lastX[i] = xs[i];
            lastY[i] = ys[i];
        }
        hasBaseline = tracked > 0;
    }

    /**
     * Returns the largest per-finger absolute movement since the last poll /
     * baseline, then updates the baseline to the current positions.
     */
    public TrackpadAxes.Delta pollMostMovedFinger(int pointerCount,
                                                  int[] pointerIds,
                                                  float[] xs,
                                                  float[] ys) {
        if (!hasBaseline) {
            captureBaseline(pointerCount, pointerIds, xs, ys);
            return new TrackpadAxes.Delta(0f, 0f);
        }

        float bestDx = 0f;
        float bestDy = 0f;
        float bestMag = 0f;
        int count = Math.min(pointerCount, MAX_POINTERS);
        for (int i = 0; i < count; i++) {
            int id = pointerIds[i];
            for (int j = 0; j < tracked; j++) {
                if (ids[j] != id) {
                    continue;
                }
                float dx = xs[i] - lastX[j];
                float dy = ys[i] - lastY[j];
                float mag = Math.abs(dx) + Math.abs(dy);
                if (mag > bestMag) {
                    bestMag = mag;
                    bestDx = dx;
                    bestDy = dy;
                }
                break;
            }
        }

        captureBaseline(pointerCount, pointerIds, xs, ys);
        return new TrackpadAxes.Delta(bestDx, bestDy);
    }

    public boolean hasBaseline() {
        return hasBaseline;
    }
}

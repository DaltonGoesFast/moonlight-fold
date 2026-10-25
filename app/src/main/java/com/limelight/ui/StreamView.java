package com.limelight.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class StreamView extends SurfaceView {
    private double desiredAspectRatio;
    private InputCallbacks inputCallbacks;

    // While the soft keyboard is open, fit the video into the region above
    // it and pin that region to the top of the window. Zero inset with
    // pinToTop means the window itself was already resized above the keyboard.
    private boolean pinToTop;
    private int keyboardBottomInset;

    public void setDesiredAspectRatio(double aspectRatio) {
        this.desiredAspectRatio = aspectRatio;
    }

    /**
     * @return true when the layout inputs changed and a relayout was requested
     */
    public boolean setKeyboardAvoidance(boolean pinToTop, int bottomInsetPx) {
        if (bottomInsetPx < 0) {
            bottomInsetPx = 0;
        }
        if (this.pinToTop == pinToTop && this.keyboardBottomInset == bottomInsetPx) {
            return false;
        }

        this.pinToTop = pinToTop;
        this.keyboardBottomInset = bottomInsetPx;

        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp instanceof FrameLayout.LayoutParams) {
            int gravity = pinToTop ? (Gravity.TOP | Gravity.CENTER_HORIZONTAL) : Gravity.CENTER;
            ((FrameLayout.LayoutParams) lp).gravity = gravity;
        }
        requestLayout();
        return true;
    }

    public void setInputCallbacks(InputCallbacks callbacks) {
        this.inputCallbacks = callbacks;
    }

    public StreamView(Context context) {
        super(context);
    }

    public StreamView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StreamView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StreamView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (pinToTop && keyboardBottomInset > 0) {
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            // A bogus inset (for example a full-screen IME report) should not
            // collapse the stream to a sliver. Real keyboards stay well under this.
            int cappedInset = Math.min(keyboardBottomInset, (int) (heightSize * 0.75f));
            int adjusted = Math.max(1, heightSize - cappedInset);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(adjusted, mode);
        }

        // If no fixed aspect ratio has been provided, simply use the default onMeasure() behavior
        if (desiredAspectRatio == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // Based on code from: https://www.buzzingandroid.com/2012/11/easy-measuring-of-custom-views-with-specific-aspect-ratio/
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int measuredHeight, measuredWidth;
        if (widthSize > heightSize * desiredAspectRatio) {
            measuredHeight = heightSize;
            measuredWidth = (int)(measuredHeight * desiredAspectRatio);
        } else {
            measuredWidth = widthSize;
            measuredHeight = (int)(measuredWidth / desiredAspectRatio);
        }

        setMeasuredDimension(Math.max(measuredWidth, 1), Math.max(measuredHeight, 1));
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // This callbacks allows us to override dumb IME behavior like when
        // Samsung's default keyboard consumes Shift+Space.
        if (inputCallbacks != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (inputCallbacks.handleKeyDown(event)) {
                    return true;
                }
            }
            else if (event.getAction() == KeyEvent.ACTION_UP) {
                if (inputCallbacks.handleKeyUp(event)) {
                    return true;
                }
            }
        }

        return super.onKeyPreIme(keyCode, event);
    }

    public interface InputCallbacks {
        boolean handleKeyUp(KeyEvent event);
        boolean handleKeyDown(KeyEvent event);
    }
}

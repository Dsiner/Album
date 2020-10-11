package com.d.lib.album.widget.photoview;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Detects scaling transformation gestures using the supplied {@link MotionEvent}s.
 * The {@link OnScaleDragGestureListener} callback will notify users when a particular
 * gesture event has occurred.
 * <p>
 * This class should only be used with {@link MotionEvent}s reported via touch.
 * <p>
 * To use this class:
 * <ul>
 *  <li>Create an instance of the {@code ScaleGestureDetector} for your
 *      {@link View}
 *  <li>In the {@link View#onTouchEvent(MotionEvent)} method ensure you call
 *          {@link #onTouchEvent(MotionEvent)}. The methods defined in your
 *          callback will be executed when the events occur.
 * </ul>
 */
public class ScaleDragGestureDetector {
    private static final int INVALID_POINTER_ID = -1;

    private final float mTouchSlop;
    private final float mMinimumVelocity;
    private final ScaleGestureDetector mScaleGestureDetector;
    private final OnScaleDragGestureListener mOnScaleDragGestureListener;
    float mFocusX, mFocusY;
    private float mTouchX, mTouchY;
    private float mLastTouchX, mLastTouchY;
    private int mActionPointerId = INVALID_POINTER_ID;
    private boolean mIsDragging;
    private VelocityTracker mVelocityTracker;

    public ScaleDragGestureDetector(Context context, OnScaleDragGestureListener listener) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();

        mOnScaleDragGestureListener = listener;
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }
                if (scaleFactor >= 0) {
                    mFocusX = detector.getFocusX();
                    mFocusY = detector.getFocusY();
                    mOnScaleDragGestureListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                // NO-OP
            }
        });
    }

    public boolean isScaling() {
        return mScaleGestureDetector.isInProgress();
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        // Scale
        mScaleGestureDetector.onTouchEvent(ev);

        final int action = ev.getActionMasked();
        final int actionIndex = ev.getActionIndex();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActionPointerId = ev.getPointerId(0);
                mTouchX = mLastTouchX = ev.getX();
                mTouchY = mLastTouchY = ev.getY();
                mVelocityTracker = VelocityTracker.obtain();
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                }
                mIsDragging = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mActionPointerId = ev.getPointerId(actionIndex);
                mTouchX = mLastTouchX = ev.getX(actionIndex);
                mTouchY = mLastTouchY = ev.getY(actionIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                final int index = ev.findPointerIndex(mActionPointerId);
                if (index < 0) {
                    Log.e("Scale", "Error processing scroll; pointer index for id "
                            + mActionPointerId + " not found. Did any MotionEvents get skipped?");
                    return true;
                }
                final float x = ev.getX(index);
                final float y = ev.getY(index);
                final float dx = mLastTouchX - x;
                final float dy = mLastTouchY - y;
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                }
                if (!mIsDragging) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    final float a = mTouchX - x;
                    final float b = mTouchY - y;
                    mIsDragging = Math.sqrt((a * a) + (b * b)) >= mTouchSlop;
                    mLastTouchX = x;
                    mLastTouchY = y;
                    return true;
                }
                mOnScaleDragGestureListener.onDrag(ev, -dx, -dy);
                mLastTouchX = x;
                mLastTouchY = y;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    if (mVelocityTracker != null) {
                        // Compute velocity within the last 1000ms
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity();
                        final float vY = mVelocityTracker.getYVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mOnScaleDragGestureListener.onFling(ev, -vX, -vY);
                        }
                    }
                }
                // Recycle Velocity Tracker
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = e.getActionIndex();
        if (e.getPointerId(actionIndex) == mActionPointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mActionPointerId = e.getPointerId(newIndex);
            mTouchX = mLastTouchX = e.getX(newIndex);
            mTouchY = mLastTouchY = e.getY(newIndex);
        }
    }

    public interface OnScaleDragGestureListener {
        void onDrag(MotionEvent ev, float dx, float dy);

        void onFling(MotionEvent ev, float velocityX, float velocityY);

        void onScale(float scaleFactor, float focusX, float focusY);
    }
}

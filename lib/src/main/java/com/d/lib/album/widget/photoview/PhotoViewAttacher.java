package com.d.lib.album.widget.photoview;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.OverScroller;

/**
 * The component of {@link PhotoView} which does the work allowing for zooming, scaling, panning, etc.
 * It is made public in case you need to subclass something other than AppCompatImageView and still
 * gain the functionality that {@link PhotoView} offers
 */
public class PhotoViewAttacher {
    private static final float DEFAULT_MAX_SCALE = 4.0f;
    private static final float DEFAULT_MID_SCALE = 1.75f;
    private static final float DEFAULT_MIN_SCALE = 1.0f;
    private static final int DEFAULT_ZOOM_DURATION = 200;

    public final ImageView mImageView;
    public final MatrixCompat mMatrixCompat;
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mMinDragPointerCount = 1;
    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMaxScale = DEFAULT_MAX_SCALE;
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;
    private boolean mAllowParentInterceptOnEdge = true;
    private boolean mBlockParentIntercept = false;
    private boolean mZoomEnabled = true;
    private FlingRunnable mCurrentFlingRunnable;
    // Gesture Detectors
    private ScaleDragGestureDetector mScaleDragDetector;
    private GestureDetector mGestureDetector;
    // Listeners
    private View.OnClickListener mOnClickListener;
    private OnLongClickListener mLongClickListener;
    private PhotoView.OnGestureListener mOnGestureListener;

    private final ScaleDragGestureDetector.OnScaleDragGestureListener mOnScaleDragGestureListener
            = new ScaleDragGestureDetector.OnScaleDragGestureListener() {
        @Override
        public void onDrag(MotionEvent ev, float dx, float dy) {
            final int pointerCount = ev.getPointerCount();
            if (mMinDragPointerCount <= 1 && mScaleDragDetector.isScaling()) {
                return; // Do not drag if we are already scaling
            }
            if (pointerCount < mMinDragPointerCount) {
                return;
            }
            if (mOnGestureListener != null) {
                mOnGestureListener.onDrag(dx, dy);
            }
            mMatrixCompat.postTranslate(dx, dy);

            /*
             * Here we decide whether to let the ImageView's parent to start taking
             * over the touch event.
             *
             * First we check whether this function is enabled. We never want the
             * parent to take over if we're scaling. We then check the edge we're
             * on, and the direction of the scroll (i.e. if we're pulling against
             * the edge, aka 'overscrolling', let the parent take over).
             */
            ViewParent parent = mImageView.getParent();
            if (mAllowParentInterceptOnEdge
                    && !mScaleDragDetector.isScaling()
                    && !mBlockParentIntercept
                    && mMatrixCompat.getScale() >= 1f
                    && mMatrixCompat.isEdge(dx, dy)) {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
            } else {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
        }

        @Override
        public void onFling(MotionEvent ev, float velocityX, float velocityY) {
            if (ev.getPointerCount() < mMinDragPointerCount) {
                return;
            }
            mCurrentFlingRunnable = new FlingRunnable(PhotoViewAttacher.this);
            mCurrentFlingRunnable.fling(Compat.getViewWidth(mImageView),
                    Compat.getViewHeight(mImageView), (int) velocityX, (int) velocityY);
            mImageView.post(mCurrentFlingRunnable);
        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY) {
            if (mOnGestureListener != null) {
                mOnGestureListener.onScaleChange(scaleFactor, focusX, focusY);
            }
            mMatrixCompat.postScale(scaleFactor, focusX, focusY);
        }
    };

    public PhotoViewAttacher(final ImageView imageView) {
        mImageView = imageView;
        mMatrixCompat = new MatrixCompat(imageView);
        if (imageView.isInEditMode()) {
            return;
        }
        // Create Gesture Detectors...
        mScaleDragDetector = new ScaleDragGestureDetector(imageView.getContext(), mOnScaleDragGestureListener);
        mGestureDetector = new GestureDetector(imageView.getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
                // Forward long click listener
                if (mLongClickListener != null) {
                    mLongClickListener.onLongClick(mImageView);
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                if (mOnGestureListener != null) {
                    if (mMatrixCompat.getScale() > DEFAULT_MIN_SCALE) {
                        return false;
                    }
                    if (e2.getPointerCount() < mMinDragPointerCount) {
                        return false;
                    }
                    return mOnGestureListener.onFling(e1, e2, velocityX, velocityY);
                }
                return false;
            }
        });
        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(mImageView);
                }
                final RectF displayRect = mMatrixCompat.getDisplayRect();
                final float x = e.getX(), y = e.getY();
                if (mOnGestureListener != null) {
                    mOnGestureListener.onViewTap(mImageView, x, y);
                }
                if (displayRect != null) {
                    // Check to see if the user tapped on the photo
                    if (displayRect.contains(x, y)) {
                        float xResult = (x - displayRect.left)
                                / displayRect.width();
                        float yResult = (y - displayRect.top)
                                / displayRect.height();
                        if (mOnGestureListener != null) {
                            mOnGestureListener.onPhotoTap(mImageView, xResult, yResult);
                        }
                        return true;
                    } else {
                        if (mOnGestureListener != null) {
                            mOnGestureListener.onOutsidePhotoTap(mImageView);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent ev) {
                try {
                    float scale = mMatrixCompat.getScale();
                    float x = ev.getX();
                    float y = ev.getY();
                    if (scale < mMidScale) {
                        setScale(mMidScale, x, y, true);
                    } else if (scale >= mMidScale && scale < mMaxScale) {
                        setScale(mMaxScale, x, y, true);
                    } else {
                        setScale(mMinScale, x, y, true);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Can sometimes happen when getX() and getY() is called
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Wait for the confirmed onDoubleTap() instead
                return false;
            }
        });
    }


    public boolean onTouchEvent(MotionEvent ev) {
        if (!mZoomEnabled || mImageView.getDrawable() == null) {
            return false;
        }

        boolean handled = false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                ViewParent parent = mImageView.getParent();
                // First, disable the Parent from intercepting the touch
                // event
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                // If we're flinging, and the user presses down, cancel
                // fling
                cancelFling();
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // If the user has zoomed less than min scale, zoom back
                // to min scale
                float scale = mMatrixCompat.getScale();
                if (scale < mMinScale) {
                    RectF rect = mMatrixCompat.getDisplayRect();
                    if (rect != null) {
                        setScale(mMinScale, rect.centerX(), rect.centerY(), true);
                        handled = true;
                    }
                } else if (scale > mMaxScale) {
                    RectF rect = mMatrixCompat.getDisplayRect();
                    if (rect != null) {
                        setScale(mMaxScale,
                                mScaleDragDetector.mFocusX,
                                mScaleDragDetector.mFocusY,
                                true);
                        handled = true;
                    }
                }
                break;
        }

        // Try the Scale/Drag detector
        if (mScaleDragDetector != null) {
            boolean isScaling = mScaleDragDetector.isScaling();
            boolean isDragging = mScaleDragDetector.isDragging();
            handled = mScaleDragDetector.onTouchEvent(ev);
            boolean didntScale = !isScaling && !mScaleDragDetector.isScaling();
            boolean didntDrag = !isDragging && !mScaleDragDetector.isDragging();
            mBlockParentIntercept = didntScale && didntDrag;
        }
        // Check to see if the user double tapped
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev)) {
            handled = true;
        }

        return handled;
    }

    private void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    public void update() {
        // Update the base matrix using the current drawable
        mMatrixCompat.resetMatrix(mImageView.getDrawable());
    }

    public ScaleType getScaleType() {
        return mMatrixCompat.mScaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            return;
        }
        switch (scaleType) {
            case MATRIX:
                throw new IllegalStateException("Matrix scale type is not supported");
        }
        if (scaleType != mMatrixCompat.mScaleType) {
            mMatrixCompat.mScaleType = scaleType;
            update();
        }
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        if (minimumScale >= mediumScale) {
            throw new IllegalArgumentException(
                    "Minimum zoom has to be less than Medium zoom.");
        } else if (mediumScale >= maximumScale) {
            throw new IllegalArgumentException(
                    "Medium zoom has to be less than Maximum zoom.");
        }
        mMinScale = minimumScale;
        mMidScale = mediumScale;
        mMaxScale = maximumScale;
    }

    public void setScale(float scale) {
        setScale(scale, false);
    }

    public void setScale(float scale, boolean animate) {
        setScale(scale,
                (mImageView.getRight()) / 2,
                (mImageView.getBottom()) / 2,
                animate);
    }

    public void setScale(float scale, float focalX, float focalY,
                         boolean animate) {
        // Check to see if the scale is within bounds
        if (scale < mMinScale || scale > mMaxScale) {
            throw new IllegalArgumentException("Scale must be within the range of minScale and maxScale");
        }
        if (animate) {
            mImageView.post(new AnimatedZoomRunnable(this, mMatrixCompat.getScale(), scale,
                    focalX, focalY));
        } else {
            mMatrixCompat.setScale(scale, focalX, focalY);
        }
    }

    public void setMinDragPointerCount(int count) {
        mMinDragPointerCount = count;
    }

    public boolean isZoomable() {
        return mZoomEnabled;
    }

    public void setZoomable(boolean zoomable) {
        mZoomEnabled = zoomable;
        update();
    }

    public void setZoomTransitionDuration(int milliseconds) {
        this.mZoomDuration = milliseconds;
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener listener) {
        mGestureDetector.setOnDoubleTapListener(listener);
    }

    public void setOnGestureListener(PhotoView.OnGestureListener listener) {
        mOnGestureListener = listener;
    }

    public void setOnMatrixChangeListener(MatrixCompat.OnMatrixChangeListener listener) {
        mMatrixCompat.setOnMatrixChangeListener(listener);
    }

    private static class AnimatedZoomRunnable implements Runnable {
        private final PhotoViewAttacher mAttacher;
        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final PhotoViewAttacher attacher,
                                    final float currentZoom, final float targetZoom,
                                    final float focalX, final float focalY) {
            mAttacher = attacher;
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / mAttacher.mMatrixCompat.getScale();
            mAttacher.mOnScaleDragGestureListener.onScale(deltaScale, mFocalX, mFocalY);
            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                Compat.postOnAnimation(mAttacher.mImageView, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mAttacher.mZoomDuration;
            t = Math.min(1f, t);
            t = mAttacher.mInterpolator.getInterpolation(t);
            return t;
        }
    }

    private static class FlingRunnable implements Runnable {
        private final PhotoViewAttacher mAttacher;
        private final OverScroller mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(final PhotoViewAttacher attacher) {
            mAttacher = attacher;
            mScroller = new OverScroller(attacher.mImageView.getContext());
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX,
                          int velocityY) {
            final RectF rect = mAttacher.mMatrixCompat.getDisplayRect();
            if (rect == null) {
                return;
            }
            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;
            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }
            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            mCurrentX = startX;
            mCurrentY = startY;
            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX,
                        maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return; // remaining post that should not be handled
            }
            if (mScroller.computeScrollOffset()) {
                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();
                mAttacher.mMatrixCompat.postTranslate(mCurrentX - newX, mCurrentY - newY);
                mCurrentX = newX;
                mCurrentY = newY;
                // Post On animation
                Compat.postOnAnimation(mAttacher.mImageView, this);
            }
        }
    }
}

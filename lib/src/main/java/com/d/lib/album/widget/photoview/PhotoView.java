package com.d.lib.album.widget.photoview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * A zoomable ImageView. See {@link PhotoViewAttacher} for most of the details on how the zooming
 * is accomplished
 */
@SuppressLint("AppCompatCustomView")
public class PhotoView extends ImageView {
    protected PhotoViewAttacher mPhotoViewAttacher;
    protected ScaleType mPendingScaleType;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    protected void init() {
        mPhotoViewAttacher = new PhotoViewAttacher(this);
        // We always pose as a Matrix scale type, though we can change to another scale type
        // via the attacher
        super.setScaleType(ScaleType.MATRIX);
        // Apply the previously applied scale type
        if (mPendingScaleType != null) {
            setScaleType(mPendingScaleType);
            mPendingScaleType = null;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // Update our base matrix, as the bounds have changed
        if (changed) {
            mPhotoViewAttacher.update();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mPhotoViewAttacher.onTouchEvent(event);
    }

    /**
     * Get the current {@link PhotoViewAttacher} for this view. Be wary of holding on to references
     * to this attacher, as it has a reference to this view, which, if a reference is held in the
     * wrong place, can cause memory leaks.
     *
     * @return the attacher.
     */
    public PhotoViewAttacher getAttacher() {
        return mPhotoViewAttacher;
    }

    @Override
    public ScaleType getScaleType() {
        return mPhotoViewAttacher.getScaleType();
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (mPhotoViewAttacher == null) {
            mPendingScaleType = scaleType;
        } else {
            mPhotoViewAttacher.setScaleType(scaleType);
        }
    }

    @Override
    public Matrix getImageMatrix() {
        return mPhotoViewAttacher.mMatrixCompat.getMatrix();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        // setImageBitmap calls through to this method
        if (mPhotoViewAttacher != null) {
            mPhotoViewAttacher.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (mPhotoViewAttacher != null) {
            mPhotoViewAttacher.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (mPhotoViewAttacher != null) {
            mPhotoViewAttacher.update();
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            mPhotoViewAttacher.update();
        }
        return changed;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mPhotoViewAttacher.setOnClickListener(l);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mPhotoViewAttacher.setOnLongClickListener(l);
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener listener) {
        mPhotoViewAttacher.setOnDoubleTapListener(listener);
    }

    public void setOnGestureListener(OnGestureListener listener) {
        mPhotoViewAttacher.setOnGestureListener(listener);
    }

    public void setOnMatrixChangeListener(MatrixCompat.OnMatrixChangeListener listener) {
        mPhotoViewAttacher.mMatrixCompat.setOnMatrixChangeListener(listener);
    }

    /**
     * Interface definition for a callback to be invoked when the photo is experiencing a drag event
     */
    public static abstract class OnGestureListener {

        /**
         * The outside of the photo has been tapped
         */
        public void onOutsidePhotoTap(ImageView imageView) {
        }

        /**
         * A callback to receive where the user taps on a photo. You will only receive a callback if
         * the user taps on the actual photo, tapping on 'whitespace' will be ignored.
         *
         * @param view ImageView the user tapped.
         * @param x    where the user tapped from the of the Drawable, as percentage of the
         *             Drawable width.
         * @param y    where the user tapped from the top of the Drawable, as percentage of the
         *             Drawable height.
         */
        public void onPhotoTap(ImageView view, float x, float y) {
        }

        /**
         * Callback for when the scale changes
         *
         * @param scaleFactor the scale factor (less than 1 for zoom out, greater than 1 for zoom in)
         * @param focusX      focal point X position
         * @param focusY      focal point Y position
         */
        public void onScaleChange(float scaleFactor, float focusX, float focusY) {
        }

        /**
         * A callback to receive where the user flings on a ImageView. You will receive a callback if
         * the user flings anywhere on the view.
         *
         * @param e1        MotionEvent the user first touch.
         * @param e2        MotionEvent the user last touch.
         * @param velocityX distance of user's horizontal fling.
         * @param velocityY distance of user's vertical fling.
         */
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        /**
         * Callback for when the photo is experiencing a drag event. This cannot be invoked when the
         * user is scaling.
         *
         * @param dx The change of the coordinates in the x-direction
         * @param dy The change of the coordinates in the y-direction
         */
        public void onDrag(float dx, float dy) {
        }

        /**
         * A callback to receive where the user taps on a ImageView. You will receive a callback if
         * the user taps anywhere on the view, tapping on 'whitespace' will not be ignored.
         *
         * @param view - View the user tapped.
         * @param x    - where the user tapped from the left of the View.
         * @param y    - where the user tapped from the top of the View.
         */
        public void onViewTap(View view, float x, float y) {
        }
    }
}

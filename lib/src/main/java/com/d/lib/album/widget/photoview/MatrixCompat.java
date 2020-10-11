package com.d.lib.album.widget.photoview;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

public class MatrixCompat {
    private static final int HORIZONTAL_EDGE_NONE = -1;
    private static final int HORIZONTAL_EDGE_LEFT = 0;
    private static final int HORIZONTAL_EDGE_RIGHT = 1;
    private static final int HORIZONTAL_EDGE_BOTH = 2;
    private static final int VERTICAL_EDGE_NONE = -1;
    private static final int VERTICAL_EDGE_TOP = 0;
    private static final int VERTICAL_EDGE_BOTTOM = 1;
    private static final int VERTICAL_EDGE_BOTH = 2;

    final View mView;
    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();
    ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;
    private float mBaseRotation = 0.0f;
    private int mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
    private int mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
    private OnMatrixChangeListener mOnMatrixChangeListener;

    public MatrixCompat(View view) {
        mView = view;
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     */
    public void resetMatrix() {
        resetMatrix(null);
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     */
    public void resetMatrix(@Nullable Drawable drawable) {
        if (drawable != null) {
            resetBaseMatrix(drawable);
        }
        mSuppMatrix.reset();
        setRotationBy(mBaseRotation);
    }

    /**
     * Calculate Matrix for FIT_CENTER
     *
     * @param drawable - Drawable being displayed
     */
    private void resetBaseMatrix(Drawable drawable) {
        mBaseMatrix.reset();
        if (drawable == null) {
            return;
        }
        final float viewWidth = Compat.getViewWidth(mView);
        final float viewHeight = Compat.getViewHeight(mView);
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();
        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;
        if (mScaleType == ImageView.ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
                    (viewHeight - drawableHeight) / 2F);

        } else if (mScaleType == ImageView.ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else if (mScaleType == ImageView.ScaleType.CENTER_INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);
            if ((int) mBaseRotation % 180 != 0) {
                mTempSrc = new RectF(0, 0, drawableHeight, drawableWidth);
            }

            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
                    break;

                case FIT_START:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.START);
                    break;

                case FIT_END:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.END);
                    break;

                case FIT_XY:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.FILL);
                    break;

                default:
                    break;
            }
        }
    }

    private boolean checkMatrixBounds() {
        final RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }
        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;
        final int viewHeight = Compat.getViewHeight(mView);
        if (height <= viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;

                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;

                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
            mVerticalScrollEdge = VERTICAL_EDGE_BOTH;

        } else if (rect.top > 0) {
            mVerticalScrollEdge = VERTICAL_EDGE_TOP;
            deltaY = -rect.top;

        } else if (rect.bottom < viewHeight) {
            mVerticalScrollEdge = VERTICAL_EDGE_BOTTOM;
            deltaY = viewHeight - rect.bottom;

        } else {
            mVerticalScrollEdge = VERTICAL_EDGE_NONE;
        }

        final int viewWidth = Compat.getViewWidth(mView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;

                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;

                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;

        } else if (rect.left > 0) {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_LEFT;
            deltaX = -rect.left;

        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mHorizontalScrollEdge = HORIZONTAL_EDGE_RIGHT;

        } else {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_NONE;
        }
        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            onMatrixChanged(getDrawMatrix());
        }
    }

    private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    private void onMatrixChanged(Matrix matrix) {
        if (mView instanceof ImageView) {
            ImageView imageView = (ImageView) mView;
            imageView.setImageMatrix(matrix);
        }
        // Call MatrixChangedListener if needed
        if (mOnMatrixChangeListener != null) {
            if (matrix != null) {
                mOnMatrixChangeListener.onMatrixChanged(matrix);
            }
        }
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     *
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    private RectF getDisplayRect(Matrix matrix) {
        Compat.mapRect(mView, matrix, mDisplayRect);
        return mDisplayRect;
    }

    public RectF getDisplayRect() {
        return getDisplayRect(getDrawMatrix());
    }

    public float getScale() {
        return Compat.getScale(mSuppMatrix);
    }

    public Matrix getBaseMatrix() {
        return mBaseMatrix;
    }

    public Matrix getMatrix() {
        return mDrawMatrix;
    }

    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        if (mView instanceof ImageView && ((ImageView) mView).getDrawable() == null) {
            return false;
        }
        mSuppMatrix.set(finalMatrix);
        checkAndDisplayMatrix();
        return true;
    }

    public void setBaseRotation(final float degrees) {
        mBaseRotation = degrees % 360;
        resetMatrix();
        setRotationBy(mBaseRotation);
    }

    public void setRotationTo(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public void setRotationBy(float degrees) {
        mSuppMatrix.postRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public void postTranslate(float dx, float dy) {
        mSuppMatrix.postTranslate(dx, dy);
        checkAndDisplayMatrix();
    }

    public void setScale(float scaleFactor, float focusX, float focusY) {
        mSuppMatrix.setScale(scaleFactor, scaleFactor, focusX, focusY);
        checkAndDisplayMatrix();
    }

    public void postScale(float scaleFactor, float focusX, float focusY) {
        mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
        checkAndDisplayMatrix();
    }

    public boolean isEdge(float dx, float dy) {
        return mHorizontalScrollEdge == HORIZONTAL_EDGE_BOTH
                || (mHorizontalScrollEdge == HORIZONTAL_EDGE_LEFT && dx >= 1f)
                || (mHorizontalScrollEdge == HORIZONTAL_EDGE_RIGHT && dx <= -1f)
                || (mVerticalScrollEdge == VERTICAL_EDGE_TOP && dy >= 1f)
                || (mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM && dy <= -1f);
    }

    public void setOnMatrixChangeListener(OnMatrixChangeListener listener) {
        mOnMatrixChangeListener = listener;
    }

    public interface OnMatrixChangeListener {

        /**
         * Callback for when the Matrix displaying the Drawable has changed. This could be because
         * the View's bounds have changed, or the user has zoomed.
         *
         * @param matrix - Matrix displaying the Drawable's new bounds.
         */
        void onMatrixChanged(Matrix matrix);
    }
}

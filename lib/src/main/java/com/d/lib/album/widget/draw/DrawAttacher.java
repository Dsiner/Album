package com.d.lib.album.widget.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.d.lib.album.R;
import com.d.lib.album.util.Utils;
import com.d.lib.album.widget.PhotoEditView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DrawAttacher
 * Created by D on 2020/10/11.
 **/
public class DrawAttacher {
    private final Context mContext;
    private final View mView;
    private final float mTouchSlop;
    private final Paint mPaint = new Paint();
    private final Map<DrawPath, PaintOptions> mPaths = new LinkedHashMap<>();
    private final float mStrokeMinWidth;
    private DrawPath mPath = new DrawPath();
    private PaintOptions mPaintOptions = new PaintOptions();
    private int mColor;
    private float mStrokeWidth;
    private float mTouchX, mTouchY;
    private float mLastTouchX, mLastTouchY;
    private boolean mActionInvalid = false;
    private boolean mIsDragging = false;
    private OnPathChangeListener mOnPathChangeListener;

    public DrawAttacher(View view) {
        mContext = view.getContext();
        mView = view;
        mTouchSlop = Utils.dp2px(view.getContext(), 1f);
        mStrokeMinWidth = Utils.dp2px(view.getContext(), 1f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        setStrokeWidth(Utils.dp2px(view.getContext(), 3.5f));
        setColor(R.color.lib_album_color_paint_red);
    }

    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(getDisplayRect(), Region.Op.INTERSECT);
        canvas.clipRect(getDisplayRect());
        canvas.concat(getMatrix());

        for (Map.Entry<DrawPath, PaintOptions> entry : mPaths.entrySet()) {
            changePaint(mPaint, entry.getValue());
            canvas.drawPath(entry.getKey(), mPaint);
        }
        changePaint(mPaint, mPaintOptions);
        canvas.drawPath(mPath, mPaint);

        canvas.restore();
    }

    private Matrix getMatrix() {
        return ((PhotoEditView) mView).getAttacher().mMatrixCompat.getMatrix();
    }

    private RectF getDisplayRect() {
        return ((PhotoEditView) mView).getAttacher().mMatrixCompat.getDisplayRect();
    }

    private float getScale() {
        return ((PhotoEditView) mView).getAttacher().mMatrixCompat.getScale();
    }

    private void changePaint(Paint paint, PaintOptions options) {
        paint.setColor(options.color);
        paint.setStrokeWidth(options.strokeWidth);
    }

    public Bitmap save() {
        final Bitmap bitmap = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(),
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        onDraw(canvas);
        return bitmap;
    }

    public void undo() {
        if (mPaths.size() <= 0) {
            return;
        }
        DrawPath lastKey = null;
        for (DrawPath key : mPaths.keySet()) {
            lastKey = key;
        }
        mPaths.remove(lastKey);
        onPathUpdated();
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
        mPaintOptions.strokeWidth = strokeWidth;
    }

    public void setColor(int color) {
        mColor = ContextCompat.getColor(mContext, color);
        mPaintOptions.color = mColor;
    }

    public Map<DrawPath, PaintOptions> getPaths() {
        return mPaths;
    }

    public void addPath(DrawPath path, PaintOptions options) {
        mPaths.put(path, options);
        onPathUpdated();
    }

    public void clearCanvas() {
        mPath.reset();
        mPaths.clear();
        onPathUpdated();
    }

    private void onPathUpdated() {
        mView.invalidate();
        if (mOnPathChangeListener != null) {
            mOnPathChangeListener.onPathChanged(mPaths.size());
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        final Matrix matrix = getMatrix();
        final Matrix invertMatrix = new Matrix(matrix);
        if (!matrix.invert(invertMatrix)) {
            return false;
        }
        final float[] points = new float[]{event.getX(), event.getY()};
        invertMatrix.mapPoints(points);
        final float x = points[0];
        final float y = points[1];

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPaintOptions.strokeWidth = mStrokeWidth / getScale();
                mPaintOptions.strokeWidth = Math.max(mStrokeMinWidth,
                        mPaintOptions.strokeWidth);
                mPath.reset();
                mPath.moveTo(x, y);
                mTouchX = event.getX();
                mTouchY = event.getY();
                mLastTouchX = x;
                mLastTouchY = y;
                mActionInvalid = false;
                mIsDragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActionInvalid) {
                    return false;
                }
                if (!mIsDragging) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    final float dx = mTouchX - event.getX();
                    final float dy = mTouchY - event.getY();
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }
                mPath.quadTo(mLastTouchX, mLastTouchY, (x + mLastTouchX) / 2, (y + mLastTouchY) / 2);
                mView.invalidate();
                mLastTouchX = x;
                mLastTouchY = y;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mActionInvalid = true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    mPath.lineTo(mLastTouchX, mLastTouchY);
                    mPaths.put(mPath, mPaintOptions);
                }
                mPath = new DrawPath();
                mPaintOptions = new PaintOptions(mStrokeWidth, mColor);
                onPathUpdated();
                if (mActionInvalid) {
                    mIsDragging = false;
                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * Draw a dot on click
     */
    @Deprecated
    private void dot(DrawPath path, float x, float y) {
        path.lineTo(x, y + 2);
        path.lineTo(x + 1, y + 2);
        path.lineTo(x + 1, y);
    }

    public void setListener(OnPathChangeListener listener) {
        this.mOnPathChangeListener = listener;
    }

    public interface OnPathChangeListener {
        void onPathChanged(int count);
    }
}

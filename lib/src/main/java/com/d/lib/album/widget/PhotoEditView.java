package com.d.lib.album.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.d.lib.album.widget.draw.DrawAttacher;
import com.d.lib.album.widget.photoview.MatrixCompat;
import com.d.lib.album.widget.photoview.PhotoView;

/**
 * PhotoEditView
 * Created by D on 2020/10/11.
 **/
@SuppressLint("AppCompatCustomView")
public class PhotoEditView extends PhotoView {
    private DrawAttacher mDrawAttacher;

    public PhotoEditView(Context context) {
        this(context, null);
    }

    public PhotoEditView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoEditView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        setWillNotDraw(false);
        mDrawAttacher = new DrawAttacher(this);
        getAttacher().setMinDragPointerCount(2);
        getAttacher().setOnMatrixChangeListener(new MatrixCompat.OnMatrixChangeListener() {
            @Override
            public void onMatrixChanged(Matrix matrix) {

            }
        });
        getAttacher().setOnGestureListener(new OnGestureListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                invalidate();
            }

            @Override
            public void onDrag(float dx, float dy) {
                invalidate();
            }
        });
    }

    public DrawAttacher getDrawAttacher() {
        return mDrawAttacher;
    }

    public Bitmap save() {
        final ScaleType scaleTypeOld = getScaleType();
        setScaleType(ScaleType.CENTER);

        final Matrix matrix = getAttacher().mMatrixCompat.getMatrix();
        final Matrix invertMatrix = new Matrix(matrix);
        if (!matrix.invert(invertMatrix)) {
            return null;
        }

        final Drawable d = getDrawable();
        final int width = d != null ? d.getIntrinsicWidth() : 0;
        final int height = d != null ? d.getIntrinsicHeight() : 0;
        Bitmap bitmap = Bitmap.createBitmap(width > 0 ? width : getWidth(),
                height > 0 ? height : getHeight(),
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.setMatrix(invertMatrix);
        draw(canvas);

        setScaleType(scaleTypeOld);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawAttacher.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = super.onTouchEvent(event);
        mDrawAttacher.onTouchEvent(event);
        return handled;
    }
}

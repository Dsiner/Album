package com.d.lib.album.widget.photoview;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

public class Compat {
    private static final int SIXTY_FPS_INTERVAL = 1000 / 60;

    public static float getScale(Matrix matrix) {
        return (float) Math.sqrt((float) Math.pow(getValue(matrix, Matrix.MSCALE_X), 2)
                + (float) Math.pow(getValue(matrix, Matrix.MSKEW_Y), 2));
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     */
    private static float getValue(Matrix matrix, int whichValue) {
        final float[] values = new float[9];
        matrix.getValues(values);
        return values[whichValue];
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     *
     * @param view   - View
     * @param matrix - Matrix to map Drawable against
     * @param rect   - Displayed Rectangle
     * @return the result of calling rectStaysRect()
     */
    public static boolean mapRect(View view, Matrix matrix, RectF rect) {
        if (view instanceof ImageView) {
            Drawable d = ((ImageView) view).getDrawable();
            if (d != null) {
                rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            } else {
                rect.set(0, 0, view.getWidth(), view.getHeight());
            }
        } else {
            rect.set(0, 0, view.getWidth(), view.getHeight());
        }
        return matrix.mapRect(rect);
    }

    public static int getViewWidth(View view) {
        return view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
    }

    public static int getViewHeight(View view) {
        return view.getHeight() - view.getPaddingTop() - view.getPaddingBottom();
    }

    public static void postOnAnimation(View view, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.postOnAnimation(runnable);
        } else {
            view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
        }
    }

}

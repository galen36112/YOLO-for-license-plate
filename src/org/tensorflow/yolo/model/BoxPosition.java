package org.tensorflow.yolo.model;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Model to store the position of the bounding boxes
 *
 * Created by Zoltan Szabo on 1/14/18.
 * URL: https://github.com/szaza/android-yolo-v2
 */

public class BoxPosition {
    private static float left;
    private static float top;
    private static float right;
    private static float bottom;
    private static float width;
    private static float height;

    public BoxPosition(float left, float top, float width, float height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        init();
    }

    public BoxPosition(BoxPosition boxPosition) {
        this.left = boxPosition.left;
        this.top = boxPosition.top;
        this.width = boxPosition.width;
        this.height = boxPosition.height;

        init();
    }

    public void init() {
        float tmpLeft = this.left;
        float tmpTop = this.top+15;
        float tmpRight = this.left + this.width;
        float tmpBottom = this.top + this.height+15;

        this.left = Math.min(tmpLeft, tmpRight); // left should have lower value as right
        this.top = Math.min(tmpTop, tmpBottom);  // top should have lower value as bottom
        this.right = Math.max(tmpLeft, tmpRight);
        this.bottom = Math.max(tmpTop, tmpBottom);


    }

    public static float getLeft() {
        return left;
    }

    public static float getTop() {
        return top;
    }

    public static float getWidth() {
        return width;
    }

    public static float getHeight() {
        return height;
    }

    public static float getRight() {
        return right;
    }

    public static float getBottom() {
        return bottom;
    }

    @Override
    public String toString() {
        return "BoxPosition{" +
                "left=" + left +
                ", top=" + top +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}

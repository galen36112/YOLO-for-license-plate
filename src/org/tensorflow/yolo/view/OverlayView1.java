package org.tensorflow.yolo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import org.tensorflow.yolo.Config;
import org.tensorflow.yolo.model.BoxPosition;
import org.tensorflow.yolo.model.Recognition;
import org.tensorflow.yolo.util.ClassAttrProvider;

import java.util.LinkedList;
import java.util.List;

public class OverlayView1 {

    private final List<OverlayView.DrawCallback> callbacks = new LinkedList();
    private List<Recognition> results;





    public void setResults(final List<Recognition> results) {
        this.results = results;
    }

    /**
     * Interface defining the callback for client classes.
     */

}

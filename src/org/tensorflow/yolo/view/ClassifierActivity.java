package org.tensorflow.yolo.view;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.TensorFlowImageRecognizer;
import org.tensorflow.yolo.TensorFlowStringRecognizer;
import org.tensorflow.yolo.model.BoxPosition;
import org.tensorflow.yolo.model.Recognition;
import org.tensorflow.yolo.util.ImageUtils;
import org.tensorflow.yolo.view.components.BorderedText;

import java.util.List;
import java.util.Vector;

import static org.tensorflow.yolo.Config.INPUT_SIZE;
import static org.tensorflow.yolo.Config.LOGGING_TAG;

/**
 * Classifier activity class
 * Modified by Zoltan Szabo
 */
public class ClassifierActivity extends TextToSpeechActivity implements OnImageAvailableListener {
    private boolean MAINTAIN_ASPECT = true;
    private float TEXT_SIZE_DIP = 10;

    private TensorFlowImageRecognizer recognizer;
    private TensorFlowStringRecognizer stringrec;

    private Integer sensorOrientation;
    private int previewWidth = 0;
    private int previewHeight = 0;
    private Bitmap croppedBitmap = null;
    private Bitmap bm = null;
    private boolean computing = false;
    private Matrix frameToCropTransform;

    private OverlayView overlayView;
    private OverlayView overlayView1;

    //自己宣告兩個三數TextView、ImageView
    private ImageView iV1;
    private ImageView iV2;
    private TextView tV1;
    private TextView tV2;
    private BorderedText borderedText;
    private long lastProcessingTimeMs;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        //在這裡建立模型

        recognizer = TensorFlowImageRecognizer.create(getAssets());
        stringrec = TensorFlowStringRecognizer.create(getAssets());

        overlayView = (OverlayView) findViewById(R.id.overlay);
        overlayView1 = (OverlayView) findViewById(R.id.overlay1);

        iV1 = (ImageView) findViewById(R.id.iV1);
        iV2 = (ImageView) findViewById(R.id.iV2);
        tV1 = (TextView) findViewById(R.id.textView);
        tV2 = (TextView) findViewById(R.id.tV2);
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        final int screenOrientation = getWindowManager().getDefaultDisplay().getRotation();

        Log.i(LOGGING_TAG, String.format("Sensor orientation: %d, Screen orientation: %d",
                rotation, screenOrientation));

        sensorOrientation = rotation + screenOrientation;

        Log.i(LOGGING_TAG, String.format("Initializing at size %dx%d", previewWidth, previewHeight));

        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight,
                INPUT_SIZE, INPUT_SIZE, sensorOrientation, MAINTAIN_ASPECT);
        frameToCropTransform.invert(new Matrix());

        addCallback((final Canvas canvas) -> renderAdditionalInformation(canvas));
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;

        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (computing) {
                image.close();
                return;
            }

            computing = true;
            fillCroppedBitmap(image);
            image.close();
        } catch (final Exception ex) {
            if (image != null) {
                image.close();
            }
            Log.e(LOGGING_TAG, ex.getMessage());
        }

        runInBackground(() -> {
            final long startTime = SystemClock.uptimeMillis();
            final List<Recognition> results = recognizer.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
            overlayView.setResults(results);
            speak(results);
            requestRender();
            computing = false;
        });
    }

    private void fillCroppedBitmap(final Image image) {
            Bitmap rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
            rgbFrameBitmap.setPixels(ImageUtils.convertYUVToARGB(image, previewWidth, previewHeight),
                    0, previewWidth, 0, 0, previewWidth, previewHeight);
            new Canvas(croppedBitmap).drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

            //程式碼從這裡開始修改

            //旋轉Bitmap角度
            Matrix vM = new Matrix();
            vM.setRotate(90);

            //檢測座標點是否有值
            tV1.setText(Float.toString(BoxPosition.getLeft())+"\n"+Float.toString(BoxPosition.getTop()));

            //Bitmap裁切

            bm = Bitmap.createBitmap(rgbFrameBitmap,(int)(BoxPosition.getTop())+67,
                (int) (rgbFrameBitmap.getHeight()-BoxPosition.getLeft()-BoxPosition.getHeight())-65
                ,(int)(BoxPosition.getHeight()*1.2) ,(int) (BoxPosition.getWidth()*1.2
                    ),vM,true);

            iV1.setImageBitmap(bm);

            //嘗試將裁切好的Bitmap丟入model中
            List<Recognition> Output = stringrec.recognizeImage(bm);
            overlayView1.setResults(Output);

            //for(int i = 0;i<Output.size();i++)








    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.close();
        }
    }

    private void renderAdditionalInformation(final Canvas canvas) {
        final Vector<String> lines = new Vector();
        if (recognizer != null) {
            for (String line : recognizer.getStatString().split("\n")) {
                lines.add(line);
            }
        }

        //lines.add("Frame: " + previewWidth + "x" + previewHeight);
        //lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
        //lines.add("Rotation: " + sensorOrientation);
        //lines.add("Inference time: " + lastProcessingTimeMs + "ms");

        borderedText.drawLines(canvas, 10, 10, lines);
    }
}

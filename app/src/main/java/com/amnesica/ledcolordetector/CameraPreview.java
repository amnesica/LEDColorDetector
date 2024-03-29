package com.amnesica.ledcolordetector;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * A basic Camera preview class which calculates the average color
 * value within the drawn circle of DrawCircle
 */
@SuppressWarnings("ALL")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraPreview";
    // screen orientation in degrees for a vertical camera orientation
    private static final int SCREEN_ORIENTATION_VERTICAL = 90;
    // the size of the pointer (in pixels)
    private static final int POINTER_RADIUS = 100;
    private SurfaceHolder surfaceHolder;
    private MainActivity activity;
    private Camera camera;
    // size of the camera preview
    private Camera.Size previewSize;
    // array of 3 integers representing the color being selected in rgb
    private int[] selectedColor;

    public CameraPreview(Context context, Camera camera, MainActivity activity) {
        super(context);
        this.activity = activity;
        this.camera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // initialize array
        selectedColor = new int[3];
    }

    /**
     * Clip input color value to valid rgb values
     *
     * @param color input value
     * @return color value within [0-255]
     */
    private static int clipRgb(int color) {
        if (color < 0) {
            return 0;
        } else if (color > 255) {
            return 255;
        } else {
            return color;
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException | RuntimeException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity (see releaseCamera())
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (this.surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // get current screen orientation and set camera orientation to it
        try {
            camera.setDisplayOrientation(SCREEN_ORIENTATION_VERTICAL);
        } catch (RuntimeException e) {
            Log.d(TAG, "Error setting Orientation: " + e.getMessage());
        }


        // start preview with new settings
        try {
            camera.setPreviewDisplay(this.surfaceHolder);
            //start previewCallbock to use onPreviewFrame method
            camera.setPreviewCallback(this);
            camera.startPreview();
            previewSize = camera.getParameters().getPreviewSize();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        /* code in this method is taken from CameraColorPicker by tvbarthel
         * https://github.com/tvbarthel/CameraColorPicker
         * */

        final int screenCenterX = previewSize.width / 2;
        final int screenCenterY = previewSize.height / 2;

        resetSelectedColors();

        // Compute the average selected color.
        for (int i = 0; i <= POINTER_RADIUS; i++) {
            for (int j = 0; j <= POINTER_RADIUS; j++) {
                addColorFromYUV420(data, selectedColor, (i * POINTER_RADIUS + j + 1),
                        (screenCenterX - POINTER_RADIUS) + i, (screenCenterY - POINTER_RADIUS) + j,
                        previewSize.width, previewSize.height);
            }
        }

        //show color as text in textView
        activity.showColorInTextView(selectedColor[0], selectedColor[1], selectedColor[2]);
    }

    /**
     * Resets the selected color in rgb
     */
    private void resetSelectedColors() {
        // 0 -> red, 1 -> green, 2 -> blue
        selectedColor[0] = 0;
        selectedColor[1] = 0;
        selectedColor[2] = 0;
    }

    /**
     * converts colors from yuv -> rgb and calculates average values
     * method is taken from CameraColorPicker by tvbarthel
     * source: https://github.com/tvbarthel/CameraColorPicker
     */
    protected void addColorFromYUV420(byte[] data, int[] averageColor, int count, int x, int y, int width, int height) {

        final int size = width * height;
        final int Y = data[y * width + x] & 0xff;
        final int xby2 = x / 2;
        final int yby2 = y / 2;

        final float V = (data[size + 2 * xby2 + yby2 * width] & 0xff) - 128.0f;
        final float U = (data[size + 2 * xby2 + 1 + yby2 * width] & 0xff) - 128.0f;

        // do the YUV -> RGB conversion
        float yf = 1.164f * (Y) - 16.0f;
        int red = (int) (yf + 1.596f * V);
        int green = (int) (yf - 0.813f * V - 0.391f * U);
        int blue = (int) (yf + 2.018f * U);

        // clip rgb values to [0-255]
        red = clipRgb(red);
        green = clipRgb(green);
        blue = clipRgb(blue);

        // calculate average values
        averageColor[0] += (red - averageColor[0]) / count;
        averageColor[1] += (green - averageColor[1]) / count;
        averageColor[2] += (blue - averageColor[2]) / count;
    }
}
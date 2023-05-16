package com.amnesica.ledcolordetector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    //permissions stuff
    protected static final String[] PERMS = {"android.permission.CAMERA"};
    protected static final int PERMS_REQUEST_CODE = 200;

    private Camera camera;
    private CameraPreview preview;
    private TextView textView;
    private com.github.clans.fab.FloatingActionButton fab1Exit;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeApp();
    }

    private void initializeApp() {
        setContentView(R.layout.activity_main);
        context = this;

        // Setup textView and floatingActionButton
        textView = findViewById(R.id.textViewDetectedColor);
        fab1Exit = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab1);
        setupFloatingActionButtons();

        // Create an instance of Camera
        checkPermissionsAndGetCameraInstance();
    }

    /**
     * Setup the button for exit the app
     */
    private void setupFloatingActionButtons() {
        //exit app button
        fab1Exit.setOnClickListener(view -> finish());
    }

    /**
     * Show color in textView as text and background color
     */
    public void showColorInTextView(int r, int g, int b) {
        int color = Color.rgb(r, g, b);

        // set background
        textView.setBackgroundColor(color);

        // set text
        String colorAsText = getColorAsTextOld(r, g, b);
        textView.setText(colorAsText);

        // set color of text with different brightness value
        int textColor = getContrastColor(r, g, b);
        textView.setTextColor(textColor);
    }

    /**
     * Get text color with contrast value for good brightness
     */
    public int getContrastColor(int r, int g, int b) {
        double y = (299 * r + 587 * g + 114 * b) / 1000.;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }

    /**
     * Get color as text string which shows the detected color
     */
    private String getColorAsTextOld(int r, int g, int b) {
        if ((r == 255 && g == 255) || (g > 170 && r > g) || (g > r && r > 240)) {
            return context.getResources().getString(R.string.color_yellow);
        } else if (g > r && g > b) {
            return context.getResources().getString(R.string.color_green);
        } else if (b > r && b > g) {
            return context.getResources().getString(R.string.color_blue);
        } else if (r > g && r > b) {
            return context.getResources().getString(R.string.color_red);
        } else {
            return context.getResources().getString(R.string.color_not_specific);
        }
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkifDeviceHasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            // attempt to get a Camera instance of first rear facing camera
            c = Camera.open();

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * check persmissions
     */
    private void checkPermissionsAndGetCameraInstance() {
        if (checkifDeviceHasCamera(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMS, PERMS_REQUEST_CODE);
            }
        } else {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.no_camera), Toast.LENGTH_LONG).show();
            // close the app
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (requestCode == PERMS_REQUEST_CODE) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, getResources().getString(R.string.no_camera_permission), Toast.LENGTH_LONG).show();
                finish();
            } else {
                //only initialize camera and preview if they are not set yet
                if (camera == null && preview == null) {
                    initializeCameraAndPreview();
                }
            }
        }
    }

    /**
     * initializes the camera, starts the camera preview and draws the circle
     */
    private void initializeCameraAndPreview() {
        //get camera instance
        camera = getCameraInstance();

        if (camera != null) {
            // Create our Preview view and set it as the content of our activity.
            preview = new CameraPreview(this, camera, this);
            FrameLayout previewView = (FrameLayout) findViewById(R.id.camera_preview);
            previewView.addView(this.preview);

            // Draw circle on top of camera preview in center of screen
            int screenCenterX = this.getResources().getDisplayMetrics().widthPixels / 2;
            int screenCenterY = this.getResources().getDisplayMetrics().heightPixels / 2;
            DrawCircle drawCircle = new DrawCircle(this, screenCenterX, screenCenterY);
            addContentView(drawCircle, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkifDeviceHasCamera(this)) {
            //get camera and preview back / start them
            initializeCameraAndPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // release the camera immediately on pause event
        releaseCamera();
    }

    /**
     * releases the camera
     */
    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    /**
     * overridden onBackPressed to show closing dialog on exit
     */
    @Override
    public void onBackPressed() {
        //show closing dialog
        AlertDialog.Builder alertDialogBuilder;

        //create alertDialog
        alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.title_closing_application_dialog))
                .setMessage(getResources().getString(R.string.message_closing_application_dialog))
                .setPositiveButton(R.string.positive_button_closing_application_dialog, (dialog, i) -> {
                    dialog.dismiss();
                    //finish task (do not remove from recent apps list)
                    finish();
                })
                .setNegativeButton(R.string.negative_button_closing_application_dialog, null);

        final AlertDialog.Builder finalAlertDialogBuilder = alertDialogBuilder;

        AlertDialog alertDialog = finalAlertDialogBuilder.create();
        alertDialog.show();

        //change button text colors when button is shown
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }
}
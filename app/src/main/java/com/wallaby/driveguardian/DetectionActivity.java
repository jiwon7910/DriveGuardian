package com.wallaby.driveguardian;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.wallaby.driveguardian.databinding.ActivityDetectionBinding;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetectionActivity extends AppCompatActivity{

    private ActivityDetectionBinding viewBinding;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;

    private ExecutorService cameraExecutor; // 백그라운드 스레드에서 카메라 작업을 실행하는데 사용됨

    private GraphicOverlay mGraphicOverlay;
    private Bitmap mSelectedImage;

    private static final String TAG = "GuardianDrive"; //  Log에 사용되는 태그
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"; // 비디오 파일 이름 형식을 지정하는 상수
    private static final int REQUEST_CODE_PERMISSIONS = 10; //권한을 요청할 때 사용되는 요청 코드

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityDetectionBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }
    private void startCamera() {
        // camera 호출
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());

                // ImageAnalysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    // Access the ByteBuffer of the image
                    ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);

                    // Get the image dimensions
                    int width = imageProxy.getWidth();
                    int height = imageProxy.getHeight();

                    // Create a YuvImage from the image data
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);

                    // Convert YuvImage to JPEG
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);

                    // Convert JPEG to Bitmap
                    byte[] jpegBytes = out.toByteArray();
                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);

                    // Update mSelectedImage with the current image
                    mSelectedImage = imageBitmap;

                    // Close the imageProxy to release resources
                    imageProxy.close();

                    runFaceContourDetection();
                });

                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception exc) {
                Log.e(TAG, "Use case binding failed", exc);
        }
        }, ContextCompat.getMainExecutor(this));
    }
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void runFaceContourDetection() {
        // Replace with code from the codelab to run face contour detection.
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();

        FaceDetector detector = FaceDetection.getClient(options);
        // Task failed with an exception
        detector.process(image)
                .addOnSuccessListener(
                        this::processFaceContourDetectionResult)
                .addOnFailureListener(
                        Throwable::printStackTrace);
    }

    private void processFaceContourDetectionResult(List<Face> faces) {
        // Replace with code from the codelab to process the face contour detection result.
        // Task completed successfully
        if (faces.size() == 0) {
            showToast("No face found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(mGraphicOverlay);
            mGraphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}

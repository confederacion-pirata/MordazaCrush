package org.confederacionpirata.mordazacrush;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements Callback {

	private Camera camera;
	private SurfaceHolder holder;
	private boolean started;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context) {

		super(context);
		camera = null;

		holder = getHolder();
		holder.addCallback(this);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	public Camera getCamera() {
		return camera;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		if (holder.getSurface() != null) {

			if (started) {
				stopPreview();
				startPreview();
			}
		}

		selectCameraPreviewSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private void initCamera() {

		try {

			// first back-facing camera
			camera = Camera.open();

			// camera features
			Parameters params = camera.getParameters();

			// auto focus
			if (params.getFocusMode() != null) {
				if (params.getSupportedFocusModes().contains(
						Camera.Parameters.FOCUS_MODE_AUTO)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				}
			}

			// auto white balance
			if (params.getWhiteBalance() != null) {
				if (params.getSupportedWhiteBalance().contains(
						Camera.Parameters.WHITE_BALANCE_AUTO)) {
					params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
				}
			}

			// disable flash
			if (params.getFlashMode() != null) {
				if (params.getSupportedFlashModes().contains(
						Camera.Parameters.FLASH_MODE_OFF)) {
					params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				}
			}

			camera.setParameters(params);

			started = false;

		} catch (Exception e) {

			Log.e(MCApp.LOGTAG, "Error opening the camera: " + e.getMessage());
			e.printStackTrace();
			camera = null;
		}
	}

	public void startPreview() {

		if (camera == null) {
			initCamera();
		}

		if (camera != null && !started) {

			try {

				camera.setPreviewDisplay(holder);
				camera.startPreview();
				started = true;

			} catch (IOException e) {
	
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void stopPreview() {

		if (camera != null && started) {

			camera.stopPreview();
			camera.release();
			camera = null;
			started = false;
		}
	}

	private void selectCameraPreviewSize(int width, int height) {

		Parameters params = camera.getParameters();
		List<Size> camPreviewSizes = params.getSupportedPreviewSizes();

		// target
		Size selected = camPreviewSizes.get(0);
		double targetRatio = adaptedRatio(width, height);
		double minDelta = Double.MAX_VALUE;

		// explore possibilities
		for (Size size : camPreviewSizes) {
			
			// compare ratios
			double ratio = adaptedRatio(size.width, size.height);
			double delta = Math.abs(targetRatio - ratio);
			if (delta < minDelta) {

				// select the smallest difference
				selected = size;
				minDelta = delta;
			}
		}

		// set preview size
		params.setPreviewSize(selected.width, selected.height);
		camera.setParameters(params);
	}

	private double adaptedRatio(double width, double height) {

		return (width > height ? width / height : height / width);
	}

}

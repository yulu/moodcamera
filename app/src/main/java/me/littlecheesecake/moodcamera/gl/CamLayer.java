package me.littlecheesecake.moodcamera.gl;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.Matrix;


public class CamLayer {
	//camera instance
	private Camera mCamera;
	//camera Id
	private int mCameraId;
	private Camera.Parameters params;
	//Camerainfo
	@SuppressLint("NewApi")
	private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
	//SharedData
	private CamData mSharedData;
	//Surface texture
	private SurfaceTexture mSurfaceTexture;
	//which camera
	private final int FRONT = 1;
	private final int BACK = 0;
	private int which = BACK;
	private int rot_angle = 0;	
	//zoom]
	int currentZoomLevel = 1, maxZoomLevel = 0;
	
	/**
	 * Must called from Activity.onPause()
	 */
	public void onPause(){
		mSurfaceTexture = null;
		if(mCamera != null){
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	
	/**
	 * Should be called from Activity.onResuem()
	 */
	public void onResume(){
		if(which == FRONT)
			openCamera(FRONT);
		else
			openCamera(BACK);
	}
	
	/**
	 * Handles camera opening
	 */
	private void openCamera(int which){
		if(mCamera != null){
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		
		if(mCameraId >= 0){
			Camera.getCameraInfo(mCameraId, mCameraInfo);
			if(which == FRONT)
				mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
			else 
				mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
			
			params = mCamera.getParameters();
			params.setRotation(0);
			
			/**
			 * set focus mode
			 */
			List<String> FocusModes = params.getSupportedFocusModes();
            if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            {
            	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } 
            
			mCamera.setParameters(params);
			try{
				if(mSurfaceTexture != null){
					mCamera.setPreviewTexture(mSurfaceTexture);
					mCamera.startPreview();
				}
			}catch(Exception ex){
			}
		}
		
		if(mCamera == null || mSharedData == null){
			return;
		}
		
		int orientation = mCameraInfo.orientation  +  rot_angle;
		
		Matrix.setRotateM(mSharedData.mOrientationM, 0, orientation, 0f, 0f, 1f);
		
		Camera.Size size = mCamera.getParameters().getPreviewSize();
		if(orientation % 90 == 0){
			int w = size.width;
			size.width = size.height;
			size.height = w;
		}
		
		mSharedData.mAspectRatioPreview[0] = (float)Math.min(size.width, size.height) / size.width;
		mSharedData.mAspectRatioPreview[1] = (float)Math.min(size.width, size.height) / size.height;
		
	}
	
	/**
	 * Selects either front-facing or back-facing camera
	 */
	public void flipit() {
		synchronized(this) {
	    //myCamera is the Camera object
	    if (Camera.getNumberOfCameras()>=2) {
	        //"which" is just an integer flag
	        
	        if(which == FRONT){
	        	openCamera(BACK);
	        	which = BACK;
	        }else{
	        	openCamera(FRONT);
	        	which = FRONT;
	        }

	    }
	}
	}
	
	public void upsidedownit(){
		synchronized(this){
			if(rot_angle == 0){
				rot_angle = 180;
				openCamera(which);
			}else{
				rot_angle = 0;
				openCamera(which);
			}
		}
	}
	
	public void zoomin(float m){
		 if(params.isZoomSupported()){    
			    maxZoomLevel = params.getMaxZoom();
			    
			    float zoom = (float)currentZoomLevel;

			    zoom += (m/100f);   
	    			    
			    currentZoomLevel = (int)zoom;
			    		    
			    if(currentZoomLevel > maxZoomLevel)
			    	currentZoomLevel = maxZoomLevel;
			    if(currentZoomLevel < 1)
			    	currentZoomLevel = 1;
			    
			    params.setZoom(currentZoomLevel);
			    mCamera.setParameters(params);
			    
		 }
	}
	
	/**
	 * Forwards call to Camera.setPreviewTexture
	 */
	public void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException{
		mSurfaceTexture = surfaceTexture;
		mCamera.setPreviewTexture(surfaceTexture);
	}
	
	/**
	 * setter for storing shared data
	 */
	public void setSharedData(CamData sharedData){
		mSharedData = sharedData;
	}
	
	/**
	 * Forwards call to camera.startpreview
	 */
	public void startPreview(){
		mCamera.startPreview();
	}
	
	/**
	 * Forwards call to camera.stoppreview
	 */
	public void stopPreview(){
		mCamera.stopPreview();
	}
	
	/**
	 * Handles picture taking callbacks 
	 */
	public void takePicture(Observer observer){
		mCamera.autoFocus(new CameraObserver(observer));
	}
	
	/**
	 * Class for implementing camera related callbacks
	 */
	private final class CameraObserver implements Camera.ShutterCallback, Camera.AutoFocusCallback, Camera.PictureCallback{
		private Observer mObserver;
		
		private CameraObserver(Observer observer){
			mObserver = observer;
		}
		
		@Override
		public void onAutoFocus(boolean success, Camera camera){
			mObserver.onAutoFocus(success);
			mCamera.takePicture(this, null, this);
		}
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera){
			mObserver.onPictureTaken(data);
		}
		
		@Override
		public void onShutter(){
			mObserver.onShutter();
		}
	}
	
	/**
	 * Interface for observing picture
	 */
	public interface Observer{
		/**
		 * Called once auto focus is done
		 */
		public void onAutoFocus(boolean success);
		
		/**
		 * Called once picture has been taken
		 */
		public void onPictureTaken(byte[] jpeg);
		
		/**
		 * Called to notify about shutter event
		 */
		public void onShutter();
	}
}

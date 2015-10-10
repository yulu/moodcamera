package me.littlecheesecake.moodcamera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;

import me.littlecheesecake.moodcamera.gl.CamData;
import me.littlecheesecake.moodcamera.gl.CamLayer;
import me.littlecheesecake.moodcamera.gl.GLRenderer;
import me.littlecheesecake.moodcamera.stickers.StickerLayer;
import me.littlecheesecake.moodcamera.util.GuideDialog;
import me.littlecheesecake.moodcamera.util.OnSwipeTouchListener;
import me.littlecheesecake.moodcamera.R;

public class MainActivity extends Activity {
	private final CamLayer mCamera = new CamLayer();
	private GLRenderer mGLRenderer;
	private StickerLayer StickerView;
	
	private View flView;
	private ImageView shutterView;
	ObjectAnimator rotAnim;
	private boolean gesture_down = false;
	
	private final CamData mSharedData = new CamData();
	private final RendererObserver mObserverRenderer = new RendererObserver(); 
	private int stickerId = 0;
	private ImageButton stickerButton;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//set camera data
		mCamera.setSharedData(mSharedData);
		//set content view
		setContentView(R.layout.cam);
				
		//set renderer view
		mGLRenderer = (GLRenderer)findViewById(R.id.glrenderer);
		mGLRenderer.setSharedData(mSharedData);
		mGLRenderer.setObserver(mObserverRenderer);
		StickerView = new StickerLayer(this);

		 /**
         * add sticker view layer
         */
		addContentView(StickerView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		/**
		 * add flash view layer
		 */
		LayoutInflater inflater = 
                (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		flView = inflater.inflate(R.layout.flash, null);
        addContentView(flView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        shutterView = (ImageView)findViewById(R.id.shutter_image);
		rotAnim = ObjectAnimator.ofFloat(shutterView,
				"alpha", 0.0f, 1.0f);
		rotAnim.setDuration(200);
		 /**
         * add UI graphic layer
         */
		 inflater = 
                (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View uiView = inflater.inflate(R.layout.main, null);
        addContentView(uiView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
             
        /**
         * add the gallery of image buttons (filter selection)
         */
        Gallery g = (Gallery) findViewById(R.id.gallery);
        // Set the adapter to our custom adapter (below)
        g.setAdapter(new ImageAdapter(this));
        // Set a item click listener, and just Toast the clicked position

        /**
         * add gesture control to the view
         */
        mGLRenderer.setOnTouchListener(new OnSwipeTouchListener(){
			 	public void onSwipeTop() {
			 		if(gesture_down){
			 		Thread t = new Thread() {
	                    public void run() {
	                    	mCamera.upsidedownit();
	                    }
	                };
	                t.start();

			 		}
			 	}
			    public void onSwipeRight() {
			    	if(gesture_down){
			    	Thread t = new Thread() {
	                    public void run() {
	                    	mCamera.flipit();
	                    }
	                };
	                t.start();
			    }
			    }
			    public void onSwipeLeft() {
			    	if(gesture_down){
			    	Thread t = new Thread() {
	                    public void run() {
	                    	mCamera.flipit();
	                    }
	                };
	                t.start();
			    }
			    }
			    public void onSwipeBottom() {
					 if(gesture_down){
						 Thread t = new Thread() {
			                    public void run() {
			                    	mCamera.upsidedownit();
			                    }
			                };
			                t.start();

					    }
					 }
			    
			    public void onSingleActionDown(){
			    	gesture_down = true;
				}
				
				public void onSinglePointerDown(){
					gesture_down = false;
				}
				
				public void onSingleActionUp(){
					gesture_down = true;
				}
				
				public void onSinglePointerUp(){
					gesture_down =false;
				}
				
				public void onMultiTouch(float m){
					mCamera.zoomin(m);
				}
		});
        
        /**
         * add the buttons
         */
        buttons(this,g);
	}
	
	private void shutterAni(){

		rotAnim.start();
	}
	
	private void buttons(final Context context, Gallery g){
		
		/**
    	 * set the gallery
    	 */
        /*g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	mSharedData.mFilter = position;
            }
        });*/
        
        g.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            	mSharedData.mFilter = position;
            }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mSharedData.mFilter = 0;
			}
        });


    	/**
    	 * switch between front and back camera
    	 */
    	ImageButton button = (ImageButton)findViewById(R.id.camera); 	
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//Show the instruction dialog
            	GuideDialog guideDialog = new GuideDialog(context);
            	guideDialog.show();           	

            }
        });
        
        /**
         * select sticker
         */
        stickerButton = (ImageButton)findViewById(R.id.sticker);
        stickerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(stickerId == 0){
            		startSticker();
            	}
            	else{
            		stickerId = 0;
            		StickerView.setStickerId(0);
            		v.setSelected(false);
            	}
            }
        });
        
        /**
         * shutter button
         */
        button = (ImageButton)findViewById(R.id.shutter);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	/**
            	 * save the image in a temp location
            	 */

            	//save bitmap
    			GLRenderer.shutter = true;
            	shutterAni();
    			Handler handler = new Handler(); 
    		    handler.postDelayed(new SaveRunnable(), 1000);
    			
            	//Thread thread = new Thread(new SaveRunnable());
				//thread.setPriority(Thread.MAX_PRIORITY);
				//thread.start();
            }
        });
    }
	
	private final class SaveRunnable implements Runnable{
		
		@Override
		public void run(){
			try {
			
				Bitmap gl_bitmap;
		    	Bitmap sv_bitmap;
		    	Bitmap combined;
		    	
		    	/**
		    	 * get the sv bitmap
		    	 */
            	
				gl_bitmap = mGLRenderer.getBitmap();

				combined = Bitmap.createBitmap(gl_bitmap.getWidth(), gl_bitmap.getHeight(), 
	        			Bitmap.Config.ARGB_8888); 
	        
				if(gl_bitmap != null){
					

		        	//combine
		        	Canvas comboImage = new Canvas(combined); 
		            comboImage.drawBitmap(gl_bitmap, 0f, 0f, null); 
		            if(stickerId!=0){
		            	sv_bitmap = StickerView.getStickerBitmap();
		            	comboImage.drawBitmap(sv_bitmap, StickerView.getStickerPoX(), 
			            		gl_bitmap.getHeight()-StickerView.getStickerPoY(), null);
		            }
	            
		            //save
		            try {
		            	String path = Environment.getExternalStorageDirectory()
		            		.toString() + "/Pictures/moodcam/"; 
				
		            	File fileDir = new File(path);
		            	if (!fileDir.exists())
		            		fileDir.mkdirs();

		            	OutputStream fOut = null;
		            	File file = new File(path, ".temp_shot.jpg");
		            	fOut = new FileOutputStream(file);

		            	combined.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
		            	fOut.flush();
		            	fOut.close();


	    		} catch (Exception e) {

	    		}
	        }
	        
	        /**
	         * release gl bitmap
	         */
	   		combined.recycle();
	    	gl_bitmap.recycle();

	    	startShare();
			
		}catch (Exception ex) {
			
			}
		}
		
	}
	

	
    private void startShare(){
    	Intent shareIntend = new Intent(this, ShareActivity.class);
    	startActivity(shareIntend);
    }
    
    private void startSticker(){
    	Intent i = new Intent(this, StickerActivity.class);
    	startActivityForResult(i, 1);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	  if (requestCode == 1) {

    	     if(resultCode == RESULT_OK){      
    	         stickerId = data.getIntExtra("result", 0) ;
    	         
    	     }
    	     if (resultCode == RESULT_CANCELED) {    
    	         //Write your code if there's no result
    	     }
    	  }
    }//
	
	/**
     * image adapter contains all the filter buttons
     */
    public class ImageAdapter extends BaseAdapter{
        private static final int ITEM_WIDTH = 60;
        private static final int ITEM_HEIGHT = 60;
    	private final Context mContext;
    	//list of image buttons
    	private final Integer[] mImageIds = {
    			R.drawable.b_normal,
    			R.drawable.b_blossom,
    			R.drawable.b_time,
    			R.drawable.b_lonley,
    			R.drawable.b_lips,
    			R.drawable.b_choco,
    			R.drawable.b_dream,
    			R.drawable.b_picasso,
    			R.drawable.b_hot,
    			R.drawable.b_artist,
    			R.drawable.b_poster,
    			R.drawable.b_reflection
    	};
    	
    	 private final float mDensity;
    	public ImageAdapter(Context c) {
            mContext = c;
            mDensity = c.getResources().getDisplayMetrics().density;
    	}
    	
		@Override 
		public int getCount() {
			return mImageIds.length;
		}
		@Override
		public Object getItem(int position) {
			return position;
		}
		@Override
		public long getItemId(int position) {
			return position;
		} 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                convertView = new ImageView(mContext);
                imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setLayoutParams(new Gallery.LayoutParams(
                        (int) (ITEM_WIDTH * mDensity + 0.5f),
                        (int) (ITEM_HEIGHT * mDensity + 0.5f)));
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mImageIds[position]);

            return imageView;
		}
    }
    
    @Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		mCamera.onPause();
		mGLRenderer.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mCamera.onResume();
		mGLRenderer.onResume();
		StickerView.setStickerId(stickerId);
		//check sticker button status
		if(StickerView.getStickerId() != 0){
			stickerButton.setSelected(true);
		}
        shutterView.setAlpha(0.0f);
		//flView.stopFlash();
		//flView.setBackgroundColor(Color.TRANSPARENT);
		
	}
	
	private class RendererObserver implements GLRenderer.Observer{
		@Override
		public void onSurfaceTextureCreated(SurfaceTexture surfaceTexture){
			try{
				mCamera.stopPreview();
				mCamera.setPreviewTexture(surfaceTexture);
				
				mCamera.startPreview();
			}catch(final Exception ex){
				
			}
		}
	}

}

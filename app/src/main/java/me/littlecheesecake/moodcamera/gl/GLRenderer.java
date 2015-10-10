package me.littlecheesecake.moodcamera.gl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.Toast;

import me.littlecheesecake.moodcamera.R;

public class GLRenderer extends GLSurfaceView implements 
				GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{
	
	public static boolean shutter = false;
	
	/**
	 * Two FBOs 
	 */
	//External OES texture holder, camera preview that is 
	private final CamFBO mFBOExternal = new CamFBO();
	//Offscreen texture holder for storing camera preview
	private final CamFBO mFBOOffscreen = new CamFBO();
	
	/**
	 * Camera Preview and Data
	 */
	//Renderer observer
	private Observer mObserver;
	//Camera data
	private CamData	 mSharedData;

	/**
	 * Filter Shaders
	 */
	//Shader for copying preview texture into offscreen one
	private final CamShader mShaderCopyOes = new CamShader();
	private final CamShader mShaderFilterDefault = new CamShader();
	private final CamShader mShaderFilterBlossom = new CamShader();
	private final CamShader mShaderFilterTime = new CamShader();
	private final CamShader mShaderFilterDream = new CamShader();
	private final CamShader mShaderFilterLonely = new CamShader();
	private final CamShader mShaderFilterChoco = new CamShader();
	private final CamShader mShaderFilterLips = new CamShader();
	private final CamShader mShaderFilterTwirl = new CamShader();
	private final CamShader mShaderFilterToaster = new CamShader();
	private final CamShader mShaderFilterArtist = new CamShader();
	private final CamShader mShaderFilterPoster = new CamShader();
	private final CamShader mShaderFilterReflection = new CamShader();
	/**
	 * SurfaceTexture data and OpenGL params
	 */
	//One and only surfaceTexture instance
	private SurfaceTexture mSurfaceTexture;
	//Flag for indicating surfacetexture has been updated
	private boolean mSurfaceTextureUpdate;
	//View width and height
	private int mWidth, mHeight;
	//View aspect ration
	private final float mAspectRatio[] = new float[2];
	//Full view quad vertices
	private ByteBuffer mFullQuadVertices;
	//SurfaceTexture Transform Matrix
	private final float[] mTransformM = new float[16];
	private Bitmap bitmap;
	
	/**
	 * textures filters
	 */
	/** This will be used to pass in the texture. */
	private final Context mActivityContext;
	private int mTextureUniformHandle;
	
	private int mTextureDataHandle1;
	
	
	/**
	 * -----------------------------------------------------------------------------------
	 */
	public GLRenderer(Context context) {
		super(context);
		mActivityContext = context;
		init();
	}
	
	public GLRenderer(Context context, AttributeSet attrs){
		super(context, attrs);
		mActivityContext = context;
		init();
	}
	
	/**
	 * Initializes local variables for rendering
	 */
	private void init(){
		//Create full scene quad buffer
		final byte FULL_QUAD_COORDS[] = {-1, 1, -1, -1, 1, 1, 1, -1};
		mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);
		mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);
		
		setPreserveEGLContextOnPause(true);
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	/**
	 * load string from raw resouces with given id
	 */
	private String loadRawString(int rawId) throws Exception{
		InputStream is = getContext().getResources().openRawResource(rawId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while((len = is.read(buf))!= -1){
			baos.write(buf, 0, len);
		}
		return baos.toString();
	}

	@Override
	public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
		//mark a flag for indicating new frame is available
		mSurfaceTextureUpdate = true;
		requestRender();
		
	}

	@Override
	public synchronized void onDrawFrame(GL10 gl) {
		//clear view
		GLES20.glClearColor(.5f, .5f, .5f, 1.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		//if we have new preview texture
		if(mSurfaceTextureUpdate){
			//update surface texture
			mSurfaceTexture.updateTexImage();
			//update texture transform matrix
			mSurfaceTexture.getTransformMatrix(mTransformM);
			mSurfaceTextureUpdate = false;
			
			//Bind offscreen texture into use
			mFBOOffscreen.bind();
			mFBOOffscreen.bindTexture(0);
			
			//Take copy shader into use
			mShaderCopyOes.useProgram();
			
			//Uniform variables
			int uOrientationM = mShaderCopyOes.getHandle("uOrientationM");
			int uTransformM = mShaderCopyOes.getHandle("uTransformM");
			
			//Transform external texture 
			GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mSharedData.mOrientationM, 0);
			GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
			
			//Using external OES texture as source
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOExternal.getTexture(0));
			
			//Triger actual rendering
			renderQuad(mShaderCopyOes.getHandle("aPosition"));

		}
		
		//bind screen buffer into use
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glViewport(0, 0, mWidth, mHeight);
		
		CamShader shader = mShaderFilterDefault; 
		switch(mSharedData.mFilter){
		case 0:
			shader = mShaderFilterDefault;
			break;
		case 1:
			shader = mShaderFilterBlossom;

			// Load the texture
			mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext,
					R.drawable.blossom); 
			break;
		case 2:
			shader = mShaderFilterTime;

			// Load the texture
			mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext,
					R.drawable.time); 
			break;
		case 3:
			shader = mShaderFilterLonely;

			// Load the texture
			mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext,
					R.drawable.stripes); 
			break;
		case 4:
			shader = mShaderFilterLips;
			break;
		case 5:
			shader = mShaderFilterChoco;
			break;
		case 6:
			shader = mShaderFilterDream;

			// Load the texture
			mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext,
					R.drawable.dream); 
			break;
		case 7:
			shader = mShaderFilterTwirl;
			break;
		case 8:
			shader = mShaderFilterToaster;
			// Load the texture
			mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext,
					R.drawable.toaster); 
			break;
		case 9:
			shader = mShaderFilterArtist;
			break;
		case 10:
			shader = mShaderFilterPoster;
			break;
		case 11:
			shader = mShaderFilterReflection;
			break;
		}	
		
		//Take filter shader into use
		shader.useProgram();
		
		//Uniform variables
		int uAspectRatio = shader.getHandle("uAspectRatio");
		int uAspectRatioPreview = shader.getHandle("uAspectRatioPreview");
		
		GLES20.glUniform2fv(uAspectRatio, 1, mAspectRatio, 0);
		GLES20.glUniform2fv(uAspectRatioPreview, 1, mSharedData.mAspectRatioPreview, 0);
		
		//User offscreen texture as source
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOOffscreen.getTexture(0));	
		
		//pixelsize
		int uPixelSize = shader.getHandle("uPixelSize");
		GLES20.glUniform2f(uPixelSize, 1.0f/mWidth, 1.0f/mHeight);

	
		//Texture filter
		mTextureUniformHandle = GLES20.glGetUniformLocation(shader.programHandle(),
				"u_Texture");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle1);
		GLES20.glUniform1i(mTextureUniformHandle, 1);
			
		//Trigger actual rendering
		renderQuad(mShaderCopyOes.getHandle("aPosition"));
		
        //take picture
        if(shutter){
        	savePixels();
        	shutter = false;
        }
	}

	@Override
	public synchronized void onSurfaceChanged(GL10 gl, int width, int height) {
		//Store width and height
		mWidth = width;
		mHeight = height;
		
		//Calculate view aspect ratio
		mAspectRatio[0] = (float)Math.min(mWidth, mHeight)/mWidth;
		mAspectRatio[1] = (float)Math.min(mWidth, mHeight)/mHeight;
		
		//Initialize textures
		if(mFBOExternal.getWidth() != mWidth || mFBOExternal.getHeight() != mHeight){
			mFBOExternal.init(mWidth, mHeight, 1, true);
		}
		if(mFBOOffscreen.getWidth() != mWidth || mFBOOffscreen.getHeight() != mHeight){
			mFBOOffscreen.init(mWidth, mHeight, 1, false);
		}
		
		//Allocate new SurfaceTexture
		SurfaceTexture oldSurfaceTexture = mSurfaceTexture;
		mSurfaceTexture = new SurfaceTexture(mFBOExternal.getTexture(0));
		mSurfaceTexture.setOnFrameAvailableListener(this);
		if(mObserver != null){
			mObserver.onSurfaceTextureCreated(mSurfaceTexture);
		}
		if(oldSurfaceTexture != null){
			oldSurfaceTexture.release();
		}
		
		requestRender();
		
	}

	@Override
	public synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) {
		/**
		 * load shaders
		 */
		try{
			String vertexSource = loadRawString(R.raw.copy_oes_vs);
			String fragmentSource = loadRawString(R.raw.copy_oes_fs);
			mShaderCopyOes.setProgram(vertexSource, fragmentSource);
		}catch(Exception ex){
			showError(ex.getMessage());
		}
		
		final int[] FILTER_IDS={R.raw.filter_default_fs, R.raw.filter_blossom_fs, R.raw.filter_time_fs, 
								R.raw.filter_lonely_fs, R.raw.filter_lips_fs, R.raw.filter_choco_fs,
								R.raw.filter_dream_fs, R.raw.filter_twirl_fs,
								R.raw.filter_toaster_fs, R.raw.filter_artist_fs, 
								R.raw.filter_poster_fs, R.raw.filter_reflection_fs};
		final CamShader[] SHADERS={mShaderFilterDefault, mShaderFilterBlossom, mShaderFilterTime, mShaderFilterLonely,
								   mShaderFilterLips,  mShaderFilterChoco, mShaderFilterDream,
								   mShaderFilterTwirl, mShaderFilterToaster, mShaderFilterArtist, 
								   mShaderFilterPoster, mShaderFilterReflection};
		
		for(int i = 0; i < FILTER_IDS.length; ++i){
			try{
				String vertexSource = loadRawString(R.raw.filter_vs);
				String fragmentSource = loadRawString(R.raw.filter_fs);
				
				fragmentSource = fragmentSource.replace("____FUNCTION_FILTER____", loadRawString(FILTER_IDS[i]));
				SHADERS[i].setProgram(vertexSource, fragmentSource);
			}catch (Exception ex){
				showError(ex.getMessage());
			}
		}
		
		mFBOExternal.reset();
		mFBOOffscreen.reset();
		
	}
	
	/**
	 * Renders fill screen quad using given GLES id/name
	 */
	private void renderQuad(int aPosition){
		GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0, mFullQuadVertices);
		GLES20.glEnableVertexAttribArray(aPosition);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	/**
	 * Setter for observer
	 */
	public void setObserver(Observer observer){
		mObserver = observer;
	}
	/**
	 * Setter for shared data
	 */
	public void setSharedData(CamData sharedData){
		mSharedData = sharedData;
		requestRender();
	}
	
	/**
	 * Shows toast on screen with given message
	 */
	private void showError(final String errorMsg){
		Handler handler = new Handler(getContext().getMainLooper());
		handler.post(new Runnable(){
			@Override
			public void run(){
				Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	/**
	 * Observer class for renderer
	 */
	public interface Observer{
		public void onSurfaceTextureCreated(SurfaceTexture surfaceTexture);
	}
	
	/**
	 * Take the picture
	 */
	/* --- FOR TAKING SCREENSHOTS --- */
	private void savePixels() {

		
		int size = mWidth * mHeight;
		ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
		buf.order(ByteOrder.nativeOrder());
		GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA,
				GLES20.GL_UNSIGNED_BYTE, buf);
		int data[] = new int[size];
		buf.asIntBuffer().get(data);
		buf = null;
		bitmap = Bitmap.createBitmap(mWidth, mHeight,
				Bitmap.Config.RGB_565);
		bitmap.setPixels(data, 0, mWidth, 0, 0, mWidth, mHeight);
		data = null;
		short sdata[] = new short[size];
		short tempdata[] = new short[size];
		ShortBuffer sbuf = ShortBuffer.wrap(sdata);
		bitmap.copyPixelsToBuffer(sbuf);
		for (int i = 0; i < mHeight; ++i) {
			for (int j = 0; j < mWidth; ++j) {
				// BGR-565 to RGB-565
				short v = sdata[i * mWidth + j];
				sdata[i * mWidth + j] = (short) (((v & 0x1f) << 11)
						| (v & 0x7e0) | ((v & 0xf800) >> 11));
				tempdata[i * mWidth + j] = sdata[i * mWidth + j];
			}
		}

		for (int i = 0; i < mHeight; i++) {
			for (int j = 0; j < mWidth; j++) {
				sdata[i * mWidth + j] = tempdata[(mHeight - i - 1) * mWidth + j];
			}
		}

		sbuf.rewind();
		bitmap.copyPixelsFromBuffer(sbuf);

	}
	
	public Bitmap getBitmap(){

		return bitmap;

}
	public void cleanBitmap(){
		bitmap.recycle();
	}
		
}
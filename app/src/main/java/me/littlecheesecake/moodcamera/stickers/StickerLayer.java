package me.littlecheesecake.moodcamera.stickers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class StickerLayer extends View{
	private Paint   mPaint;
    private float   mOriginX;
    private float   mOriginY;
    private Bitmap  bitmap;
    private int 	stickerId = 0;
    
    public StickerLayer(Context context){
    	super(context);
    	setFocusable(true);
    	
    	mPaint = new Paint();
    	
    	//set top left corner of the sticker
    	//a bit off the left edge
    	mOriginX = 10.0f;
    	//set at the 2/3 of the screen(the bottom of the image)
    	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	Display display = wm.getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	int height = size.y;
    	
    	mOriginY = (1f/3f)*height;
    }
    
    public void setStickerId(int id){
    	stickerId = id;
    }
    public int getStickerId(){
    	return stickerId; 
    }
    public Bitmap getStickerBitmap(){
    	return bitmap;
    }
    public float getStickerPoX(){
    	return mOriginX;
    }
    public float getStickerPoY(){
    	if(bitmap!=null)
    		return bitmap.getHeight() + 10.0f;
    	else
    		return 0.0f;
    }
    
    @Override 
	protected void onDraw(Canvas canvas){
		canvas.translate(mOriginX, mOriginY);
		if(stickerId != 0){
			bitmap = BitmapFactory.decodeResource(getResources(),stickerId);
			float refineY = mOriginY - bitmap.getHeight() - 10.0f;
			canvas.drawBitmap(bitmap, mOriginX, refineY, mPaint);

		}
		postInvalidate();
	}
	

}

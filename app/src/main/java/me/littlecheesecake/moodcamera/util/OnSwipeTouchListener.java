package me.littlecheesecake.moodcamera.util;

import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeTouchListener implements OnTouchListener {
	//multi touch event
	private float oldDist = 1f;
	private PointF mid = new PointF(); 
	private int mode = 0;
	
    @SuppressWarnings("deprecation")
	private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

    public boolean onTouch(final View view, final MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        
        switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			onSingleActionDown();
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if(oldDist > 10f){
				midPoint(mid, event);
				mode = 1;
			}
			onSinglePointerDown();
			break;
			
	    case MotionEvent.ACTION_UP:
	    	 mode = 0;
	    	 onSingleActionUp();
		     break;
		     
		case MotionEvent.ACTION_POINTER_UP:
			 mode = 0;
			 onSinglePointerUp();
	         break;
	
		case MotionEvent.ACTION_MOVE:
			if(mode == 1){
				float newDist = spacing(event);
				if(newDist > 10f){
					float m = newDist - oldDist;
					onMultiTouch(m);
				}
			}
			break;
        }
        return true;
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    	
    }

    public void onSwipeLeft() {
    	
    }

    public void onSwipeTop() {
    	
    }

    public void onSwipeBottom() {
    	
    }
    
	public void onMultiTouch(float m){
		
	}
	
	public void onSingleActionDown(){
		
	}
	
	public void onSinglePointerDown(){
		
	}
	
	public void onSingleActionUp(){
		
	}
	
	public void onSinglePointerUp(){
		
	}
	
	 /** Determine the space between the first two fingers */
	 private float spacing(MotionEvent event) {
	    float x = event.getX(0) - event.getX(1);
	    float y = event.getY(0) - event.getY(1);
	    return (float)Math.sqrt(x * x + y * y);
	 }

	 /** Calculate the mid point of the first two fingers */
	 private void midPoint(PointF point, MotionEvent event) {
	    float x = event.getX(0) + event.getX(1);
	    float y = event.getY(0) + event.getY(1);
	    point.set(x / 2, y / 2);
	 }
    
    
}

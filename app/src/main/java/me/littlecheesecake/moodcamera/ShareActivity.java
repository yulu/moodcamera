package me.littlecheesecake.moodcamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import me.littlecheesecake.moodcamera.socialshare.MediaScannerNotifier;
import me.littlecheesecake.moodcamera.socialshare.WeiboUpload;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Util;

public class ShareActivity extends Activity{
	 private static String path; 
	 private static String tempName = ".temp_shot.jpg";
	 private String savedName = null;
	 private Bitmap bitmap;
	 private boolean saved = false;
	 
		//wechat send
		private static final int THUMB_SIZE = 150;
		
		private IWXAPI api;
	 
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	        
	     setContentView(R.layout.share);
	        
	     //display the image
	     path = Environment.getExternalStorageDirectory()
	             .toString() + "/Pictures/moodcam/"; 
	     String fileName = path + tempName;
	     bitmap = BitmapFactory.decodeFile(fileName);
	        
	     //scale the image
	     // Get current dimensions AND the desired bounding box
	     int width = bitmap.getWidth();
	     int height = bitmap.getHeight();
	     
	     //bound within the width of the display
	     Display display = getWindowManager().getDefaultDisplay();
	     Point size = new Point();
	     display.getSize(size);
	     int bounding = size.x;
	        
	     // Determine how much to scale: the dimension requiring less scaling is
	     // closer to the its side. This way the image always stays inside your
	     // bounding box AND either x/y axis touches it.  
	     float xScale = ((float) bounding) / width;
	     float yScale = ((float) bounding) / height;
	     float scale = (xScale <= yScale) ? xScale : yScale;
	        
	     // Create a matrix for the scaling and add the scaling data
	     Matrix matrix = new Matrix();
	     matrix.postScale(scale, scale);
	        
	     // Create a new bitmap and convert it to a format understood by the ImageView 
	     Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	     width = scaledBitmap.getWidth(); // re-use
	     height = scaledBitmap.getHeight(); // re-use
	     ImageView myImageView = (ImageView)findViewById(R.id.pending_img);
	     myImageView.setImageBitmap(scaledBitmap);
	                
	     buttons(this);
	     
	     try {
			reNameFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	     String app_id = getResources().getString(R.string.wc_app_id);
	     api = WXAPIFactory.createWXAPI(this, app_id, false);		
	     api.registerApp(app_id); 
	 }
	 
	 private void buttons(final Context context){
		 	ImageButton button;
		 	
		 	/**
		 	 * Save to local path
		 	 */
	    	button = (ImageButton)findViewById(R.id.local);  	
	        button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	if(savedName == null && saved == false)
						try {
							reNameFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	Toast.makeText(getApplicationContext(), "image saved to "+"("+ path +")", 
	            			Toast.LENGTH_LONG).show();
	            	
	            }
	        });
	        
	        /**
	         * Email the photo
	         */
	    	button = (ImageButton)findViewById(R.id.email);  	
	        button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	//check if saved local or not
	            	if(savedName == null&& saved == false)
						try {
							reNameFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	
	            	//open email intend
	            	sendMail();
	            }
	        });

	        /**
	         * Share the photo on weibo
	         */
	    	button = (ImageButton)findViewById(R.id.weibo);  	
	        button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	//check if saved local or not
	            	if(savedName == null&& saved == false)
						try {
							reNameFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	
	            	//weibo api
	            	uploadWeibo();
	            }
	        });
	        
	        
	        /**
	         * Share the photo on wechat
	         */
	    	button = (ImageButton)findViewById(R.id.wechat);  	
	        button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	//check if saved local or not
	            	if(savedName == null&& saved == false)
						try {
							reNameFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	
	            	//weibo api
	            	uploadWechat();
	            }
	        });
	        
	        /**
	         * cancel button
	         */
	        button = (ImageButton)findViewById(R.id.cancel_button);  	
	        button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	//cancel the activity
	            	finish();
	            }
	        });
	        
	 }
	 
	 private void reNameFile() throws IOException{
		 Date d = new Date();
		 CharSequence s  = DateFormat.format("yyyyMMddhms", d.getTime());
		 savedName = "MCIMG_" + s.toString()+".jpg";
		 
		 String fromPath = path + tempName;
		 
		 /*File from = new File(fromPath);
		 if(from.exists()){
			    File to = new File(path,savedName);
			    from.renameTo(to);
			}*/
		 File from = new File(fromPath);
		 File to = new File(path, savedName);
		 copy(from, to);
		 
		 new MediaScannerNotifier(this, path+savedName, "image/jpg");
	 }
	 
	 public void copy(File src, File dst) throws IOException {
		    InputStream in = new FileInputStream(src);
		    OutputStream out = new FileOutputStream(dst);

		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    in.close();
		    out.close();
		}
	 
	 private void sendMail(){
		 Intent intent = new Intent(Intent.ACTION_SEND);
		 intent.setType("text/plain");
		 intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"});
		 intent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
		 intent.putExtra(Intent.EXTRA_TEXT, "body text");

		 File file = new File(path, savedName);
		 if (!file.exists() || !file.canRead()) {
		     Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
		     finish();
		     return;
		 }
		 Uri uri = Uri.parse("file://" + file);
		 intent.putExtra(Intent.EXTRA_STREAM, uri);
		 startActivity(Intent.createChooser(intent, "Send email..."));
		 

	 }
	 
	 private void uploadWechat(){
		 //passing the path to the new intent
		 String fileName = path + savedName;
		 File file = new File(path, savedName);
		 if (!file.exists() || !file.canRead()) {
		     Toast.makeText(this, "Photo Saved Error", Toast.LENGTH_SHORT).show();
		     finish();
		     return;
		 }
		 
		 sendImage(fileName);
	 }

	 private void uploadWeibo(){
		 Intent launchWBIntent = new Intent(this, WeiboUpload.class);
		 
		 //passing the path to the new intent
		 String fileName = path + savedName;
		 File file = new File(path, savedName);
		 if (!file.exists() || !file.canRead()) {
		     Toast.makeText(this, "Photo Saved Error", Toast.LENGTH_SHORT).show();
		     finish();
		     return;
		 }
		 launchWBIntent.putExtra("bitmapPath", fileName);
		 
		 startActivity(launchWBIntent);
	 }
	 
	 	@Override
		public void onDestroy(){
			super.onDestroy();
			
		}
		
		@Override
		public void onPause(){
			super.onPause();
		}
		
		@Override
		public void onResume(){
			super.onResume();
			
		}
		@Override
		public void onRestart(){
			super.onRestart();
		}
		
		private void sendImage(String path){
			
			WXImageObject imgObj = new WXImageObject();
			imgObj.setImagePath(path);
			
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = imgObj;
			
			Bitmap bmp = BitmapFactory.decodeFile(path);
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
			bmp.recycle();
			msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
			
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("img");
			req.message = msg;
			api.sendReq(req);
			
			finish();
			
		}
		
		private String buildTransaction(final String type) {
			return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
		}

		
}

package me.littlecheesecake.moodcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class StickerActivity extends Activity{
	
	private int stickerId;
	private int stickerSize;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.sticker);
        
        ImageView imgView = (ImageView)findViewById(R.id.stickerTitleImg);
        Drawable drawable = getResources().getDrawable( R.drawable.title );
        imgView.setImageDrawable(drawable);
        
    	Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	int width = size.x;
    	stickerSize = (int)(width-150)/4;

        GridView g = (GridView) findViewById(R.id.myGrid);
        g.setAdapter(new ImageAdapter(this));

		g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		stickerId = mThumbIds[position];
        		
        		Intent returnIntent = new Intent();
        		returnIntent.putExtra("result",stickerId);
        		setResult(RESULT_OK, returnIntent);     

        		finish();
            }
        });
    }
	
	public class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(stickerSize, stickerSize));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);

            return imageView;
        }

        private Context mContext;

       
	}
	
	 private Integer[] mThumbIds = {
     		R.drawable.angry_pang, R.drawable.cute_pang,
     		R.drawable.eye_pang, R.drawable.helpless_pang,
     		R.drawable.sad_pang, R.drawable.sosad_pang, R.drawable.mooncake,
     		R.drawable.happy_pang, R.drawable.fengjie, 
     		R.drawable.shrug_jm, R.drawable.relax_jm, 
     		R.drawable.jm, R.drawable.no_jm,
     		R.drawable.zb_daoyan, R.drawable.true_daoyan,
     		R.drawable.miaomiao, R.drawable.ninja,
     		R.drawable.heart, R.drawable.heart_b
     };
}

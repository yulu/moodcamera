package me.littlecheesecake.moodcamera.util;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageButton;

import me.littlecheesecake.moodcamera.R;

public class GuideDialog extends Dialog implements View.OnClickListener{
	
	 public ImageButton button;
	
	public GuideDialog(Context context) {
		super(context, R.style.Theme_Transparent);
	}

	 @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);  

	    
	    Window window = this.getWindow();
	    window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    window.setGravity(Gravity.CENTER);
	    
	    setContentView(R.layout.guidedialog);
	    
	    button = (ImageButton) findViewById(R.id.guide_button);
	    button.setOnClickListener(this);

	  }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
	    case R.id.guide_button:
	      dismiss();
	      break;		
		}
	}
}

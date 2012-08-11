package kr.co.diary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class IntroActivity extends BaseActivity {
	String cellNum = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);

    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		  if ( event.getAction() == MotionEvent.ACTION_DOWN ){
			 Intent intent =  new Intent(IntroActivity.this, MyDiaryActivity.class);
			 startActivity(intent);
			 return true;
		  }
		  
		  return super.onTouchEvent(event);
		  
	}

        
}

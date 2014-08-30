package com.ericsson.leftpark;





import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class SettingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		fillHolder();	
		ViewHolder.getButtonPark().setOnClickListener(mButtonTabListener);
		ViewHolder.getButtonHistory().setOnClickListener(mButtonTabListener);
		ViewHolder.getButtonMap().setOnClickListener(mButtonTabListener);
		ViewHolder.getButtonTime().setOnClickListener(mButtonTabListener);
	}
	
	private OnClickListener mButtonTabListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent;
			switch(v.getId()) {
			case R.id.buttonHistory:
				intent = new Intent(getApplicationContext(), HistoryActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.buttonPark:
				intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.buttonMap:
				intent = new Intent(getApplicationContext(), MapsActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.buttonTime:
				intent = new Intent(getApplicationContext(), TimeActivity.class);
				startActivity(intent);
				finish();
				break;
			}
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == R.id.action_exit) {
			ParkingSlotApplication.Instence().onTerminate();
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	
	private void fillHolder() {
		ViewHolder.setButtonPark(findViewById(R.id.buttonPark));
		ViewHolder.setButtonHistory(findViewById(R.id.buttonHistory));
		ViewHolder.setButtonMap(findViewById(R.id.buttonMap));
		ViewHolder.setButtonTime(findViewById(R.id.buttonTime));
		ViewHolder.setTextviewNote(findViewById(R.id.textNote1));
		String str = ViewHolder.getTextviewNote().getText().toString();
		SpannableStringBuilder style=new SpannableStringBuilder(str);
		style.setSpan(new ForegroundColorSpan(Color.RED), 44, 48, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		ViewHolder.getTextviewNote().setText(style);
	}
	
	private static class ViewHolder {
		private static ImageButton buttonPark, buttonHistory, buttonMap,buttonTime;
		private static TextView textviewNote;

		public static ImageButton getButtonPark() {
			return buttonPark;
		}

		public static void setButtonPark(View buttonPark) {
			ViewHolder.buttonPark = (ImageButton)buttonPark;
		}

		public static ImageButton getButtonHistory() {
			return buttonHistory;
		}

		public static void setButtonHistory(View buttonHistory) {
			ViewHolder.buttonHistory = (ImageButton)buttonHistory;
		}

		public static TextView getTextviewNote() {
			return textviewNote;
		}

		public static void setTextviewNote(View textviewNote) {
			ViewHolder.textviewNote = (TextView)textviewNote;
		}

		public static ImageButton getButtonMap() {
			return buttonMap;
		}

		public static void setButtonMap(View buttonMap) {
			ViewHolder.buttonMap = (ImageButton)buttonMap;
		}
		public static ImageButton getButtonTime() {
			return buttonTime;
		}

		public static void setButtonTime(View buttonTime) {
			ViewHolder.buttonTime = (ImageButton)buttonTime;
		}
	}

}

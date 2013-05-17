package org.odk.collect.android.activities;

import java.text.DecimalFormat;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.odk.collect.android.R;

public class ReturnString extends Activity {
	private LinearLayout mLinearLayout;
	private final int mAnswerFontsize = 23;
	private String mAnswer;
	private Double mSeconds;
	private TextView mTimeView;
	private TextView mBlankLine;
	private TextView mBreathCountView;
	private Counter mHandler;
	
	private class Counter extends Handler {
		boolean stop = true;

		@Override
		public void handleMessage(Message msg) {
			updateSeconds();
		}

		public void count(long delayMillis) {
			if (!stop) {
				this.removeMessages(0);
				sendMessageDelayed(obtainMessage(0), delayMillis);
			}
		}

		public void stop() {
			stop = true;
		}

		public void start() {
			stop = false;
			updateSeconds();
		}

		public boolean running() {
			return !stop;
		}
	};
	private void updateSeconds() {
		if (mHandler.running()) {
			if (mSeconds <= 14.0) {
				mSeconds += .1;
				setSeconds(mSeconds);
				mHandler.count(100);
			}
			if (mSeconds > 13.9) {
				mHandler.stop();
				mAnswer = "pass";
				// sometimes stops at 59.9 or 60.1
				setSeconds(14.0);
				returnClearance();
			}
		}
	}

	private void setSeconds(double count) {
		mSeconds = count;
		DecimalFormat df = new DecimalFormat("0.0");
		mTimeView.setText("Seconds: " + df.format(mSeconds));
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer);
		mLinearLayout = (LinearLayout) findViewById(R.id.timer);
		
		mTimeView = new TextView(this);
		mBreathCountView = new TextView(this);
		mBlankLine = new TextView(this);
		
		mBreathCountView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
				mAnswerFontsize);
		
		mBreathCountView.setText("Time elapsed: ");
		
		mSeconds = 0.0;
		mHandler = new Counter();
		
		if (mAnswer == null && !mHandler.running()) {
			mHandler.start();
		}
		
		mLinearLayout.addView(mBreathCountView);
		mLinearLayout.addView(mBlankLine);
		mLinearLayout.addView(mTimeView);
		
		registerForContextMenu(mBreathCountView);
		registerForContextMenu(mBlankLine);
		registerForContextMenu(mTimeView);
	}
	
	private void returnClearance() {
		Intent intent = new Intent();
		intent.putExtra("value", mAnswer);
		setResult(RESULT_OK, intent);
		finish();
	}
}

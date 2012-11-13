package org.audiopulse.activities;

import org.audiopulse.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class AudioPulseRootActivity extends AudioPulseActivity{

	private Bundle audioResultsBundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				 
	}
	
	public void appendText(String str){
		TextView tv = getTextView(); 
		tv.setText(tv.getText() + "\n" + str);
	}
	
	public void emptyText(){
		TextView tv = getTextView();
		tv.setText("");
	}
	
	private TextView getTextView(){
		return (TextView)this.findViewById(R.id.text1);
	}
	
	public void plotSpectrum(Bundle audioResultsBundle) {
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		intent.putExtras(audioResultsBundle);
		this.audioResultsBundle=audioResultsBundle;
		startActivity(intent);
	}

	public void plotWaveform() {
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}
	
	public void plotAudiogram() {
		Intent intent = new Intent(this.getApplicationContext(), PlotAudiogramActivity.class);
		startActivity(intent);
	}

}

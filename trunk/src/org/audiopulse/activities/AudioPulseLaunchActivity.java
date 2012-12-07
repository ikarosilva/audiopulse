/* ===========================================================
 * SanaAudioPulse : a free platform for teleaudiology.
 *              
 * ===========================================================
 *
 * (C) Copyright 2012, by Sana AudioPulse
 *
 * Project Info:
 *    SanaAudioPulse: http://code.google.com/p/audiopulse/
 *    Sana: http://sana.mit.edu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * AudioPulseRootActivity.java 
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Andrew Schwartz
 * Contributor(s):   Ikaro Silva
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;

import org.audiopulse.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class AudioPulseLaunchActivity extends AudioPulseActivity{

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
		//TODO: Add check for not null audioResultsBundle (notify user that to run stimulus if they press this option before running anything).
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}
	
	public void plotAudiogram(Bundle DPOAEGramResultsBundle) {
		Intent intent = new Intent(this.getApplicationContext(), PlotAudiogramActivity.class);
		intent.putExtras(DPOAEGramResultsBundle);
		startActivity(intent);
	}

}

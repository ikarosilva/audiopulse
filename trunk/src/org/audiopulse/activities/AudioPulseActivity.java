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
 * AudioPulseCalibrationActivity.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Ikaro Silva
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;

import org.audiopulse.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

//Contains menu from which tests can be selected and run
public class AudioPulseActivity extends Activity
{
	public static final String TAG="AudioPulseActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_menu);

		// set actions for clickable menu items
		ListView menuList = (ListView) findViewById(R.id.menu_list);
		menuList.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {

						TextView item = (TextView) itemClicked;
						String itemText = item.getText().toString();

						//Use this Bundle to pass information to the secondary activiies
						Bundle tests = new Bundle();
						tests.putString(getResources().getString(R.string.testNameKey),
								itemText);

						if(itemText.equalsIgnoreCase(getResources().getString(R.string.DPOAE_RIGHT))) {
							tests.putString(getResources().getString(R.string.testEarKey),
									getResources().getString(R.string.RigtEarKey));
						} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.DPOAE_LEFT))) {
							tests.putString(getResources().getString(R.string.testEarKey),
									getResources().getString(R.string.LeftEarKey));
						}

						Log.v(TAG,"creating intent");
						Intent testIntent=null;
						if(itemText.equalsIgnoreCase(getResources().getString(R.string.USB_TEST))){
							testIntent = new Intent(AudioPulseActivity.this, UsbTestActivity.class);
						}else if(itemText.equalsIgnoreCase(getResources().getString(R.string.CAL_SPEC))){
							testIntent = new Intent(AudioPulseActivity.this, PlotWaveformActivity.class);	
						}else if(itemText.equalsIgnoreCase(getResources().getString(R.string.CAL_WAV))){
							testIntent = new Intent(AudioPulseActivity.this, PlotSpectralActivity.class);	
						}else{
							testIntent = new Intent(AudioPulseActivity.this, TestActivity.class);	
						}
						Log.v(TAG,"putting extras");
						testIntent.putExtras(tests);
						Log.v(TAG,"starting test activityfor :" + itemText);
						startActivity(testIntent);
					}
				}
				);
	}




}
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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

//Contains menu from which tests can be selected and run
public class CalibrationMenuActivity extends AudioPulseActivity 
{
	public static final String TAG="CalibrationMenuActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calibration);

		// set actions for clickable menu items
		ListView menuList = (ListView) findViewById(R.id.calibration_list);;
		menuList.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {

						TextView item = (TextView) itemClicked;
						String itemText = item.getText().toString();
						//item.getId(), R.id.??

								//TODO: Tests should return a Bundle containing the data they are suppose to provide,
						//and URLs to any file location they create
						if(itemText.equalsIgnoreCase(getResources().getString(R.string.tests_device_calibration))) {
							startActivity(new Intent(CalibrationMenuActivity.this, InputCalibrationActivity.class));	
						}else if(itemText.equalsIgnoreCase(getResources().getString(R.string.audio_calibration))) {
							startActivity(new Intent(CalibrationMenuActivity.this, AudioCalibrationActivity.class));	
						}else if(itemText.equalsIgnoreCase(getResources().getString(R.string.menu_debug))) {
							Intent testIntent = new Intent(CalibrationMenuActivity.this, TestActivity.class);
							startActivity(testIntent);
						}        			
					}
				}
				);
	}

}
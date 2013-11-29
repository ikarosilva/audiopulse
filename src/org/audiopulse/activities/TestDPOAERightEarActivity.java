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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

//The purpose of this class is to launch the DPOAE test remotely, but with similar 
//parameters as if launched from the APP menu
public class TestDPOAERightEarActivity extends AudioPulseActivity 
{
	public static final String TAG="TestDPOAERightEarActivity";
	public static final String BUNDLE_TESTNAME_KEY="testName";
	public static final String BUNDLE_TESTEAR_KEY="testEar";
	public static final String BUNDLE_TESTEAR_RIGHT="RE";
	public static final String BUNDLE_TESTEAR_LEFT="LE";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle tests = new Bundle();
			tests.putString(BUNDLE_TESTNAME_KEY,getResources().getString(R.string.TEST_DPOAE));
			tests.putString(BUNDLE_TESTEAR_KEY,BUNDLE_TESTEAR_RIGHT);
			Intent testIntent = new Intent(TestDPOAERightEarActivity.this, 
					TestDPOAEActivity.class);
			testIntent.putExtras(tests);
			startActivity(testIntent);
	}

}
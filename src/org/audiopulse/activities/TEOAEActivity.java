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
 * Original Author:  Andrew Schwartz
 * Contributor(s):   Ikaro Silva
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;

import org.audiopulse.tests.TEOAEProcedure;
import android.os.Handler;
import android.view.View;


//GeneralAudioTestActivity is a template for all tests; all test activities should extend GeneralAudioTestActivity.
//FIXME: for now implementing my debugging procedures here. FIgure out our structure, and make the appropriate structural changes.
public class TEOAEActivity extends TestActivity implements Handler.Callback
{
	public final String TAG="TEOAEActivity";
	
	//Begin test -- this function is called by the button in the default layout
	public void startTest(View callingView)
	{
		appendText("Starting TEOAE Procedure");
		if (testProcedure==null) {
//			appendText("No TestProecdure set!");
			//FIXME: don't do this. Just doing it now for convenience.
			//maybe define a DebuggingTestActivity?
			appendText("Starting TEOAE");
			testProcedure = new TEOAEProcedure(this);
			testProcedure.start();
		} else {
			testProcedure.start();
		}

	}	
}
package org.audiopulse.io;

import org.audiopulse.io.AcousticDevice.ioDevice;

public class HTCOne extends MobilePhone {

private double maxAmplitude; //Max amplitude of a 1 kHz that does not clip speaker
private int minAttenuation;
private final int calFreq=1000;
private final String name="HTCOne";
private AcousticDevice.ioDevice acousticDevice;
	
public enum deviceCalParameters{
	//Only include devices that were measured for attn 
	DUMMY(20),
	ER10C(40);
	
	public int attn;
	deviceCalParameters(int atten){
		this.attn=atten;
	}	
}

	public HTCOne(deviceCalParameters deviceParam, AcousticDevice.ioDevice acousticDevice){
	
		this.minAttenuation=deviceParam.attn;
		this.maxAmplitude=getMaxAmp(minAttenuation);
		this.acousticDevice= acousticDevice;
	}

	@Override
	public double getMaxAmplitude() {
		// TODO Auto-generated method stub
		return this.maxAmplitude;
	}

	@Override
	public int getMinAttenuation() {
		// TODO Auto-generated method stub
		return this.minAttenuation;
	}

	@Override
	public int getCalFreq() {
		// TODO Auto-generated method stub
		return this.calFreq;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public ioDevice getAcousticDevice() {
		// TODO Auto-generated method stub
		return this.acousticDevice;
	}

}

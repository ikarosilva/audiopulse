package org.audiopulse.hardware;

import org.audiopulse.hardware.AcousticDevice.ioDevice;

public class HTCOne extends MobilePhone {

private double maxAmplitude; //Max amplitude of a 1 kHz that does not clip speaker
private int minAttenuation;
private final int calFreq=1000;
private final String name="HTCOne";
private AcousticDevice.ioDevice acousticDevice;
	
public enum deviceCalParam{
	//Only include devices that were measured for attn 
	DUMMY(20),
	ER10C(40);
	
	public int attn;
	deviceCalParam(int atten){
		this.attn=atten;
	}	
}

	public HTCOne(deviceCalParam deviceParam, AcousticDevice.ioDevice acousticDevice){
	
		this.minAttenuation=deviceParam.attn;
		this.maxAmplitude=getMaxAmp(minAttenuation);
		this.acousticDevice= acousticDevice;
	}

	@Override
	public double getMaxAmplitude() {
		return this.maxAmplitude;
	}

	@Override
	public int getMinAttenuation() {
		return this.minAttenuation;
	}

	@Override
	public int getCalFreq() {
		return this.calFreq;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ioDevice getAcousticDevice() {
		return this.acousticDevice;
	}

}

package ksd.Data.report;

import java.util.List;

public class Data2Report {
	
	private String deviceEui;
	private int sequence;
	private int[] message;
	private List<Gateways> gateways;
	private Signal signal;
	
	public String getDeviceEui() {
		return deviceEui;
	}
	public void setDeviceEui(String deviceEui) {
		this.deviceEui = deviceEui;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public int[] getMessage() {
		return message;
	}
	public void setMessage(int[] message) {
		this.message = message;
	}

	public List<Gateways> getGateways() {
		return gateways;
	}
	public void setGateways(List<Gateways> gateways) {
		this.gateways = gateways;
	}
	public Signal getSignal() {
		return signal;
	}
	public void setSignal(Signal signal) {
		this.signal = signal;
	}
	
	

}

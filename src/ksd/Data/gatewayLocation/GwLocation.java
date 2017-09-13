package ksd.Data.gatewayLocation;

import java.util.List;

public class GwLocation {

	private String reortId = null;
	private String productId = null;
	private String deviceEui = null;
	
	private List<Gateways> gateways = null;
	private Signal signal = null;
	
	private int[] message = null;
	private int sequence = 0;
	private boolean handled ;
	private String handledBy = null;
	private int handledAt = 0;
	
	public String getReortId() {
		return reortId;
	}
	public void setReortId(String reortId) {
		this.reortId = reortId;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getDeviceEui() {
		return deviceEui;
	}
	public void setDeviceEui(String deviceEui) {
		this.deviceEui = deviceEui;
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
	public int[] getMessage() {
		return message;
	}
	public void setMessage(int[] message) {
		this.message = message;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public boolean isHandled() {
		return handled;
	}
	public void setHandled(boolean handled) {
		this.handled = handled;
	}
	public String getHandledBy() {
		return handledBy;
	}
	public void setHandledBy(String handledBy) {
		this.handledBy = handledBy;
	}
	public int getHandledAt() {
		return handledAt;
	}
	public void setHandledAt(int handledAt) {
		this.handledAt = handledAt;
	}
	
}

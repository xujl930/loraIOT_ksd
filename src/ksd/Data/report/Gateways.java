package ksd.Data.report;

public class Gateways {
	private int rssi;
	private String snr;
	private int channel;
	private int receivedAt;
	private String gatewayEui;
	
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int f) {
		this.rssi = f;
	}
	public String getSnr() {
		return snr;
	}
	public void setSnr(String snr) {
		this.snr = snr;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public int getReceivedAt() {
		return receivedAt;
	}
	public void setReceivedAt(int receivedAt) {
		this.receivedAt = receivedAt;
	}
	public String getGatewayEui() {
		return gatewayEui;
	}
	public void setGatewayEui(String gatewayEui) {
		this.gatewayEui = gatewayEui;
	}


}

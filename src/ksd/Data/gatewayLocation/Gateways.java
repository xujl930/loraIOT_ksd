package ksd.Data.gatewayLocation;

public class Gateways {
	private int rssi;
	private String snr;
	private int channel;
	private int receivedAt;
	
	private Info info = null;

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
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

	public Info getInfo() {
		return info;
	}

	public void setInfo(Info info) {
		this.info = info;
	}
		
}

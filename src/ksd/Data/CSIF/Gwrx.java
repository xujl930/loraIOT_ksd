package ksd.Data.CSIF;

public class Gwrx {
	private int chan;
	private String gweui ="";
	private  String lsnr ="";
	private  float rfch;
	private  int rssi;
	private  String time ="";
	private  boolean timefromgateway;
	
	public int getChan() {
		return chan;
	}
	public void setChan(int chan) {
		this.chan = chan;
	}
	public String getGweui() {
		return gweui;
	}
	public void setGweui(String gweui) {
		this.gweui = gweui;
	}
	public String getLsnr() {
		return lsnr;
	}
	public void setLsnr(String lsnr) {
		this.lsnr = lsnr;
	}
	public float getRfch() {
		return rfch;
	}
	public void setRfch(float rfch) {
		this.rfch = rfch;
	}
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public boolean isTimefromgateway() {
		return timefromgateway;
	}
	public void setTimefromgateway(boolean timefromgateway) {
		this.timefromgateway = timefromgateway;
	}
	




	
	
	
	
}

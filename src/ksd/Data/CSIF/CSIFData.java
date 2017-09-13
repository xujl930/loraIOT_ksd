package ksd.Data.CSIF;

public class CSIFData {
	private String AppEUI ="";
	private int CODE;
	private int CmdSeq;
	private String DevEUI ="";
	private String MSG ="";
	private int Port ;
	private String payload = "";
	
	private Detail detail = null;

	public String getAppEUI() {
		return AppEUI;
	}

	public void setAppEUI(String appEUI) {
		AppEUI = appEUI;
	}

	public int getCODE() {
		return CODE;
	}

	public void setCODE(int cODE) {
		CODE = cODE;
	}

	public int getCmdSeq() {
		return CmdSeq;
	}

	public void setCmdSeq(int cmdSeq) {
		CmdSeq = cmdSeq;
	}

	public String getDevEUI() {
		return DevEUI;
	}

	public void setDevEUI(String devEUI) {
		DevEUI = devEUI;
	}

	public String getMSG() {
		return MSG;
	}

	public void setMSG(String mSG) {
		MSG = mSG;
	}

	public int getPort() {
		return Port;
	}

	public void setPort(int port) {
		Port = port;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public Detail getDetail() {
		return detail;
	}

	public void setDetail(Detail detail) {
		this.detail = detail;
	}
	
	public void clear(){
		AppEUI ="";
		CODE =0;
		CmdSeq =0;
		DevEUI ="";
		MSG ="";
		Port=0 ;
		payload = "";
		detail = null;
	}
	
	
}

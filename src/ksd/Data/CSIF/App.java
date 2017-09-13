package ksd.Data.CSIF;

import java.util.List;

public class App {
	private String dir;
	private String moteeui;
	private int seqno;
	
	private List<Gwrx> gwrx ;
	private Motetx motetx;
	private Userdata userdata;
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getMoteeui() {
		return moteeui;
	}
	public void setMoteeui(String moteeui) {
		this.moteeui = moteeui;
	}
	public int getSeqno() {
		return seqno;
	}
	public void setSeqno(int seqno) {
		this.seqno = seqno;
	}
	public List<Gwrx> getGwrx() {
		return gwrx;
	}
	public void setGwrx(List<Gwrx> gwrx) {
		this.gwrx = gwrx;
	}
	public Motetx getMotetx() {
		return motetx;
	}
	public void setMotetx(Motetx motetx) {
		this.motetx = motetx;
	}
	public Userdata getUserdata() {
		return userdata;
	}
	public void setUserdata(Userdata userdata) {
		this.userdata = userdata;
	}
	
}

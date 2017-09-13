package ksd.Data.report;

public class Signal {
	private float frequency;
	private String dataRate;
	private String codeRate;
	private boolean adrSupported;
	public float getFrequency() {
		return frequency;
	}
	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}
	public String getDataRate() {
		return dataRate;
	}
	public void setDataRate(String dataRate) {
		this.dataRate = dataRate;
	}
	public String getCodeRate() {
		return codeRate;
	}
	public void setCodeRate(String codeRate) {
		this.codeRate = codeRate;
	}
	public boolean getAdrSupported() {
		return adrSupported;
	}
	public void setAdrSupported(boolean adrSupported) {
		this.adrSupported = adrSupported;
	}
	
	
	
}

package ksd.Data.gatewayLocation;

public class Signal {
	private Number frequency ;
	private String dataRate = null;
	private String codeRate = null;
	private boolean adrSupported;
	
	public Number getFrequency() {
		return frequency;
	}
	public void setFrequency(Number frequency) {
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
	public boolean isAdrSupported() {
		return adrSupported;
	}
	public void setAdrSupported(boolean adrSupported) {
		this.adrSupported = adrSupported;
	} 
	
}

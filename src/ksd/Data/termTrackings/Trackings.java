package ksd.Data.termTrackings;

public class Trackings {
	private int updated = 0;
	private String latitude = null;
	private String longitude = null;
	private Number drift ;
	
	public int getUpdated() {
		return updated;
	}
	public void setUpdated(int updated) {
		this.updated = updated;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public Number getDrift() {
		return drift;
	}
	public void setDrift(Number drift) {
		this.drift = drift;
	}
	@Override
	public String toString() {
		return "Trackings [updated=" + updated + ", latitude=" + latitude + ", longitude=" + longitude + ", drift="
				+ drift + "]";
	}
	
}

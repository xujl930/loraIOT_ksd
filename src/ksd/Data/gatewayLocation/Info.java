package ksd.Data.gatewayLocation;

public class Info {
    private String gatewayEui = null;
	private Location location = null;
	 
	public class Location{
		private int updated =0;
		private String latitude = null;
		private String longitude = null;
		private Number drift = 0;
		
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
	 }

	public String getGatewayEui() {
		return gatewayEui;
	}

	public void setGatewayEui(String gatewayEui) {
		this.gatewayEui = gatewayEui;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	 
}

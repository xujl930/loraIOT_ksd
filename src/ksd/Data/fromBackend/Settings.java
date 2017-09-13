package ksd.Data.fromBackend;

public class Settings {

	private int revision;
	private int created;
	private boolean stolen = false;
	
	public int getRevision() {
		return revision;
	}
	public void setRevision(int revision) {
		this.revision = revision;
	}
	public int getCreated() {
		return created;
	}
	public void setCreated(int created) {
		this.created = created;
	}
	public boolean isStolen() {
		return stolen;
	}
	public void setStolen(boolean stolen) {
		this.stolen = stolen;
	}
	
	
	
}

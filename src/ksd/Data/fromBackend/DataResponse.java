package ksd.Data.fromBackend;

public class DataResponse {

	private Data[] data;

	private int offset;
	private int limit;
	private int total;
	private boolean more;
	
	public Data[] getData() {
		return data;
	}
	public void setData(Data[] data) {
		this.data = data;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public boolean isMore() {
		return more;
	}
	public void setMore(boolean more) {
		this.more = more;
	}
	
}

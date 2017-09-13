package ksd.Data.token;

import java.io.Serializable;

public class Tokens implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String token =null;
	int expresAt =0;
	int issuedAt = 0;
	UserInfo userInfo =null;
	
	public UserInfo getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	public int getExpresAt() {
		return expresAt;
	}
	public void setExpresAt(int expresAt) {
		this.expresAt = expresAt;
	}
	public int getIssuedAt() {
		return issuedAt;
	}
	public void setIssuedAt(int issuedAt) {
		this.issuedAt = issuedAt;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	
	
	@Override
	public String toString() {
		return "token=" + token + ", expresAt=" + expresAt + ", issuedAt=" + issuedAt + ", userInfo=" + userInfo;
	}
	public Tokens newInstance(String src){
		
		return null;
		
	}
	
}

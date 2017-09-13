package ksd.Threads;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.loraiot.iot.service.Configure;
import com.loraiot.iot.service.MMCAgent;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import ksd.Data.gatewayLocation.GwLocation;
import ksd.Data.termTrackings.Trackings;
import ksd.Data.token.Tokens;
import ksd.myUtils.CalcLocationUtils;
import ksd.myUtils.ReportUtils;
import ksd.myUtils.TokenUtils;

public class CalcLocation implements Runnable{

	private GwLocation  gwLocat = null;
	private Trackings track = null;
	
	private boolean isGotGw = false;
	private volatile String gwLocationRsp = null;
	
	private String terminalId =null; 
	
	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public GwLocation getGwlocat() {
		return gwLocat;
	}

	public void setGwlocat(GwLocation gwlocat) {
		this.gwLocat = gwlocat;
	}

	public Trackings getTrack() {
		return track;
	}

	public void setTrack(Trackings track) {
		this.track = track;
	}

	public boolean isGotGw() {
		return isGotGw;
	}

	public void setGotGw(boolean isGotGw) {
		this.isGotGw = isGotGw;
	}

	public String getGwLocationRsp() {
		return gwLocationRsp;
	}

	public void setGwLocationRsp(String gwLocationRsp) {
		this.gwLocationRsp = gwLocationRsp;
	}

	@Override
	public void run() {
		while (true && !Thread.currentThread().isInterrupted()) {
			synchronized(MMCAgent.class){
				while(!isGotGw) {
					try {
						MMCAgent.class.wait();
					} catch (InterruptedException e) {
						//e.printStackTrace();
						System.out.println("calc location thread was stoped!");
						Thread.currentThread().interrupt();
					}
				}
				if (getGwLocationRsp()!=null){
					gwLocat = CalcLocationUtils.getGwLocation(gwLocationRsp);
					track = CalcLocationUtils.calcLocation(gwLocat);
					String id =  ReportUtils.getTerminalID(gwLocat.getDeviceEui(), Configure.token);
					if ("401".equals(id)||"403".equals(id)) {
						id = ReportUtils.getTerminalID(gwLocat.getDeviceEui(), Configure.token);
					}
					
					if (id !=null) {
						int trackCode = postTrackings(id,Configure.token);
						if (trackCode ==200 && !gwLocat.isHandled()) {
							patchHandledReport(id,gwLocat.getReortId(),Configure.token);
						}else {
							Configure.logger.error("post trackings error:terminal "+id+" null track info!");
							System.out.println("post trackings error:"+trackCode);
						}
					}else {
						System.out.println("get terminalid null");
					}
				}else {
					System.out.println("get gatewayInfo null");
				}
				this.setGotGw(false);
			}
		}
	}

	public int postTrackings(String terminalid,Tokens token) {
		if (this.track ==null) {
			return -1;
		}
		if (!TokenUtils.getLegalToken(token)) {
			System.out.println("get token failed!");
			return 0;
		}
		CloseableHttpClient httpClient = HttpClients.createDefault();

		StringEntity myEntity=null;
		try {	
			String trackJson = CalcLocationUtils.encapTrack2Report(this.track);
			myEntity = new StringEntity(trackJson,ContentType.create("application/json", "UTF-8"));
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		
		String url = Configure.DEFAULT_SERVER + "terminals/"+terminalid+"/trackings";
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("X-KSD-API-TOKEN", token.getToken());
		httpPost.setEntity(myEntity);
		System.out.println();
		System.out.println(url);
		
		CloseableHttpResponse response =null;
		try {
			response = httpClient.execute(httpPost);
			System.out.println(response.getStatusLine().toString());
			
			String resp = EntityUtils.toString(response.getEntity()).replace("\r\n", "");
			System.out.println(resp);
			System.out.println();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int status = response.getStatusLine().getStatusCode();
		return status;
	}

	public int patchHandledReport(String terminalid,String reportid,Tokens token){
		if (!TokenUtils.getLegalToken(token)) {
			System.out.println("get token failed!");
			return 0;
		}
		CloseableHttpClient httpClient = HttpClients.createDefault();

		StringEntity myEntity=null;
		try {	
			String json = "{\"handled\":true}"; 
			myEntity = new StringEntity(json,ContentType.create("application/json", "UTF-8"));
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		
		String url = Configure.DEFAULT_SERVER + "terminals/"+terminalid+"/reports/"+reportid;
		HttpPatch patch = new HttpPatch(url);
		patch.addHeader("X-KSD-API-TOKEN", token.getToken());
		patch.setEntity(myEntity);
		System.out.println(url);
		
		CloseableHttpResponse response =null;
		try {
			response = httpClient.execute(patch);
			System.out.println(response.getStatusLine().toString());
			
			String resp = EntityUtils.toString(response.getEntity()).replace("\r\n", "");
			System.out.println(resp);
			System.out.println();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int status = response.getStatusLine().getStatusCode();
		return status;
		
	}
}

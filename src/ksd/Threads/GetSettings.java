package ksd.Threads;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PipedWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loraiot.iot.comm.RespGetter;
import com.loraiot.iot.data.datagram.CSData2Dev;
import com.loraiot.iot.service.Configure;

import ksd.Data.fromBackend.Settings;
import ksd.Data.token.Tokens;
import ksd.myUtils.ReportUtils;
import ksd.myUtils.SetUtils;
import ksd.myUtils.TokenUtils;

public class GetSettings implements Runnable{

	private Settings sets;	
	private CSData2Dev cs2dev;
	private volatile RespGetter rg = null;

	
	private PipedWriter writer =null;
	BufferedWriter  bWriter = null;
	
	private static CloseableHttpClient httpClient = HttpClients.createDefault();
	private static CloseableHttpResponse httpResponse = null;
	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	private static HashMap<String, Integer> mapID;
	
	
	public PipedWriter getWriter() {
		return writer;
	}
	public void setWriter(PipedWriter writer) {
		this.writer = writer;
	}
	public RespGetter getRg() {
		return rg;
	}
	public void setRg(RespGetter rg) {
		this.rg = rg;
	}
	public Settings getSets() {
		return sets;
	}
	public void setSets(Settings sets) {
		this.sets = sets;
	}
	public CSData2Dev getCs2dev() {
		return cs2dev;
	}
	public void setCs2dev(CSData2Dev cs2dev) {
		this.cs2dev = cs2dev;
	}


	@Override
	public void run() {
 		bWriter = new BufferedWriter(writer);
		try {
			SetUtils.getDocument();
		} catch (DocumentException e) {
			Configure.logger.error(e.getMessage());
			e.printStackTrace();
			System.out.println("读取set.xml出错");
		}
		
		while (rg.isRunFlag() && !Thread.currentThread().isInterrupted()) {
			int statcode =0;
			mapID = SetUtils.getTerminalMap();
			if (mapID ==null) {
				continue;
			}
			Set<String> keySet = mapID.keySet();
			
			Iterator<String> it = keySet.iterator();
			boolean isModed = false;
			while (it.hasNext()) {
				String deveui = it.next();
				String id = ReportUtils.getTerminalID(deveui, Configure.token);
				if ("401".equals(id)||"403".equals(id)) {
					Configure.logger.error("wrong token: "+Configure.token+" when get terminal id!");
					id = ReportUtils.getTerminalID(deveui, Configure.token);
				}
				//通过id get对应的setting,带入toekn
				statcode = getSetData(id,Configure.token);
				if(statcode ==200) {
					int revision   = mapID.get(deveui);
					int serVersion = sets.getRevision();
					boolean stolen = sets.isStolen();
					int flag = (stolen == true)? 1:0;
					
					if(serVersion != revision){
						isModed = true;
						System.out.println("sets revision:"+sets.getRevision()+"  xml revision:"+revision);
						if (serVersion != revision) {
							SetUtils.modDevTable(id,serVersion,null,stolen);
							String conf = SetUtils.toDevConf(deveui, serVersion, flag);		
							send2Dev(conf);		//发送控制信息到设备
							System.out.println();
						}
					}else {
						continue;
					}
				}else if(statcode == 404){
					Configure.logger.error("指定主键: "+id+" 不存在！");
					System.out.println("指定主键不存在！");
				}else {
					Configure.logger.error("got set failed! "+statcode+" ERROR");
				}
			}
			if (isModed) {
				try {
					SetUtils.saveDocument();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e ) {
				//e.printStackTrace();
				Configure.logger.error("get setting thread was stoped!");
				System.out.println("get setting thread was stoped!");
				Thread.currentThread().interrupt();
				try {
					SetUtils.saveDocument();
					SetUtils.document = null;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void send2Dev(String conf) {
		conf = conf.trim();
		try {
			bWriter.write(conf);
			bWriter.newLine();
			bWriter.flush();
				
		} catch (IOException e) {
			e.printStackTrace();
			//System.out.println(e.getMessage());
		}
		System.out.println("set has sent to thread Dev!");
	}
	
	
	/**
	 * 通过id get到该设备的控制状态״̬
	 * @param terminalid
	 */
	public int getSetData(String terminalid,Tokens token){
		if (!TokenUtils.getLegalToken(token)) {
			Configure.logger.error("get token: "+token+" failed!");
			System.out.println("get token failed!");
			return -1;
		}
		
		String url = Configure.DEFAULT_SERVER+"terminals/"+terminalid+"/settings";
		HttpGet hGet = new HttpGet(url);
		hGet.addHeader("X-KSD-API-TOKEN", token.getToken());
		int statcode=-1;
		try {
			httpResponse = httpClient.execute(hGet);
			String resp = EntityUtils.toString(httpResponse.getEntity()).replace("\r\n", "").trim();
			System.out.println(resp);
			statcode = httpResponse.getStatusLine().getStatusCode();
			if(statcode == 200){
				sets = gson.fromJson(resp, Settings.class);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return statcode;
	}
	
}

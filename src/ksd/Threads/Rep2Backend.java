package ksd.Threads;

import com.loraiot.iot.comm.RespGetter;
import com.loraiot.iot.service.Configure;
import com.loraiot.iot.service.MMCAgent;
import ksd.Data.CSIF.CSIFData;
import ksd.Data.token.Tokens;
import ksd.myUtils.ReportUtils;
import ksd.myUtils.SetUtils;
import ksd.myUtils.TokenUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

//get date from scif and report to backend,
//meanwhile get response from backend
public class Rep2Backend implements Runnable{

	private volatile RespGetter rg = null;
	private CSIFData csifData = null;
//	public DataResponse data = null;
	private GetSettings settings = null;
	
	private CalcLocation calc = null;
	
	private byte[] message = null;
	private String deveui = null;
	private String terminalid = null;
//	private String csifRsp = null;
	
//	private String payload = null;

//	private static final Logger logger = LogManager.getLogger(Rep2Backend.class);
	
	public CSIFData getCfdata() {
		return csifData;
	}

	public CalcLocation getCalc() {
		return calc;
	}

	public void setCalc(CalcLocation calc) {
		this.calc = calc;
	}

	public GetSettings getSettings() {
		return settings;
	}

	public void setSettings(GetSettings settings) {
		this.settings = settings;
	}

	public void setCfdata(CSIFData cfdata) {
		this.csifData = cfdata;
	}

	public RespGetter getRg() {
		return rg;
	}

	public void setRg(RespGetter rg) {
		this.rg = rg;
	}

	public Rep2Backend() {
		
    }

	public String getDeveui() {
		return deveui;
	}

	public void setDeveui(String deveui) {
		this.deveui = deveui;
	}

	public String getTerminalid() {
		return terminalid;
	}

	public void setTerminalid(String terminalid) {
		this.terminalid = terminalid;
	}

	@Override
	public void run() {
		this.message = new byte[1024];
		int seqno = 0;
		int time = 0;
		int tempRev = 0;
		String tempDev = "";
		while (!Thread.currentThread().isInterrupted() && rg.isRunFlag()) {
			if(rg.isSbkFlag()) {
				message = rg.getMessage();
//				System.out.println("got answer:"+ new String(message));
				int revision = 0;
				int stolen = 0;
				if(message !=null){
					System.out.println("got answer:"+ new String(message));
					csifData = ReportUtils.parseCSIF(new String(message));
					
					
					if(csifData!=null && "UPLOAD".equals(csifData.getMSG())){
						String gweui = csifData.getDetail().getApp().getGwrx().get(0).getGweui();
						if ("6073BC0001000006".equals(gweui.trim())) {
							String deveui = csifData.getDevEUI();
							String seq = csifData.getDetail().getApp().getSeqno()+"";
							String load = ReportUtils.loggPayload(csifData.getPayload());
							String rssi = csifData.getDetail().getApp().getGwrx().get(0).getRssi()+"";
							String snr = csifData.getDetail().getApp().getGwrx().get(0).getLsnr();
							String freq = csifData.getDetail().getApp().getMotetx().getFreq()+"";
							String info = "deviceEui="+deveui+"  GWEui="+gweui+"  seqno="+seq+"   payload="+load+"  rssi="+rssi+"   snr="+snr+"   freq="+freq ;
							Configure.logger.debug(info);
						}else{
							System.out.println("================= gatewayEui = "+gweui+" =======================");
						}
					}

				}else{
					continue;
				}

				if (csifData!= null && "UPLOAD".equals(csifData.getMSG())){
					
					deveui = csifData.getDevEUI();
					terminalid =  ReportUtils.getTerminalID(deveui,Configure.token);
					if ("401".equals(terminalid)||"403".equals(terminalid)) {
						terminalid = ReportUtils.getTerminalID(deveui, Configure.token);
						Configure.logger.error("wrong token:"+Configure.token+" when get terminal id!");
					}
					
					String conf = ReportUtils.analysePayload(csifData.getPayload());	//解析得到revision和被盗标志位
					if (conf!="" && conf.length() >=4){
						int curSeqno = csifData.getDetail().getApp().getSeqno();
						int curTime = (int) (System.currentTimeMillis()/1000);
						System.out.println("时间差："+(curTime-time));
						System.out.println("序列差："+(curSeqno-seqno));
						if ((curTime-time)>60 || (Math.abs(curSeqno-seqno)>=1)) {
							revision = Integer.parseInt(conf.trim().substring(0, 2), 16);
							stolen	= Integer.parseInt(conf.charAt(conf.length()-1)+""); 
							
							//把获取到的terminal ID,revision加到控制表中,revision不一致则下发新版本到节点
							boolean stol= (stolen == 1);
							List<String> list = SetUtils.add2DevTable(terminalid,revision,deveui,stol);
							//下发版本到节点
							if(list!=null && list.size()>=1){
								int rev = Integer.parseInt(list.get(0));
								int sto = (list.get(1).equals("true"))?1:0;
//							if (rev !=tempRev && deveui != tempDev) {
								String configure = SetUtils.toDevConf(deveui,rev, sto);	
								this.getSettings().send2Dev(configure);
//							}else{
//								System.out.println("quit distributing,duplicate revision!");
//							}
								tempRev = rev;
								tempDev = deveui;
							}
							time = (int) (System.currentTimeMillis()/1000);
							seqno = csifData.getDetail().getApp().getSeqno();
						}
					}else {
//							Configure.logger.error("parse payload error!"+" payload is :"+conf);
//							System.out.println("parse payload error!");
							System.out.println("payload conf is:"+conf);
						}
					
					if(terminalid != null){
						int statusCode = postReport(new String(message),Configure.token);
						if (statusCode==-1) {
							Configure.logger.error("data to report is null,"+" data is:" +new String(message));
							System.out.println("data to report is null");
						}
					}else {
						Configure.logger.error("get terminalid failed! "+ "devEui is :"+deveui);
					}
//					csifData =null;
				}else{
					System.out.println("MSG is not upload,quit reporting!");
				}
				rg.setSbkFlag(false);
			}else{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Configure.logger.error("report thread was stoped!");
					System.out.println("report thread was stoped!");
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * //上报数据
	 * @param message 
	 * @return
	 * @throws IOException
	 */
	public  int postReport(String message,Tokens token){
		if (csifData ==null) {
			return -1;
		}
		//get token failed
		if (!TokenUtils.getLegalToken(token)) {
			Configure.logger.error("get token: "+token+" failed!");
			System.out.println("get token failed!");
			return 0;
		}
		
		CloseableHttpClient httpClient = HttpClients.createDefault();

		StringEntity myEntity=null;
		try {	
										//Encapsulator.encapsulateContent(ReportUtils.data)	    //"{\"type\":\"hello\"}"
			myEntity = new StringEntity(ReportUtils.encapsJsonRep(ReportUtils.encapsulatReport(csifData)),
					ContentType.create("application/json", "UTF-8"));
			
			System.out.println("report's: "+EntityUtils.toString(myEntity));
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		
		String url = Configure.DEFAULT_SERVER + "terminals/"+terminalid+"/reports";
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
//			System.out.println("report's response: "+resp);
			
			if (!resp.isEmpty()) {
				this.getCalc().setGwLocationRsp(resp);
				synchronized (MMCAgent.class) {
					this.getCalc().setGotGw(true);
					MMCAgent.class.notify();
				}
			}
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

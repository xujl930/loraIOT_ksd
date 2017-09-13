package ksd.myUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.loraiot.iot.data.Parser;
import com.loraiot.iot.service.CLIParser;
import com.loraiot.iot.service.Configure;

import ksd.Data.CSIF.CSIFData;
import ksd.Data.CSIF.Gwrx;
import ksd.Data.CSIF.Motetx;
import ksd.Data.fromBackend.DataResponse;
import ksd.Data.report.Data2Report;
import ksd.Data.report.Gateways;
import ksd.Data.report.Signal;
import ksd.Data.token.Tokens;

public class ReportUtils {

	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private static CSIFData csifData = null;

	public static DataResponse datarsp = null;

	public static DataResponse getDatarsp() {
		return datarsp;
	}

	public static void setDatarsp(DataResponse datarsp) {
		ReportUtils.datarsp = datarsp;
	}

	public static CSIFData getCsifData() {
		return csifData;
	}

	public static void setCsifData(CSIFData csifData) {
		ReportUtils.csifData = csifData;
	}

	
	/**
	 * 解析dev的payload字段
	 * @param payload
	 * @return	dev发送的配置信息
	 */
	public static String analysePayload(String payload) {
		String conf = "";
		int[] load = new int[20];
//		byte[] load = new byte[20];
		try {
			load = byte2Int(CLIParser.decodeBase64(payload.trim()));
//			load = CLIParser.decodeBase64(payload);
			if (load.length>4 && load[0] ==7) {
				for(int i=2;i<4;i++){
//					System.out.println(load[i]);
					String hex = Integer.toHexString(load[i]);
					if (hex.length()>1) {
						conf = conf+hex;
					}else{
						conf = conf+"0"+hex;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conf;
	}
	
	//测试用
	public static String loggPayload(String payload){
		String conf = "";
		int[] load = new int[20];
		try {
			load = byte2Int(CLIParser.decodeBase64(payload.trim()));
				for(int i=0;i<2;i++){
					String hex = Integer.toHexString(load[i]);
					if (hex.length()>1) {
						conf = conf+hex;
					}else{
						conf = conf+"0"+hex;
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conf;
	}
	
	
	/**
	 * 从csif对象数据封装到report对象
	 * @param csifData	封装好的CSIF数据对象
	 * @return			上报到后台服务器的数据对象
	 */
	public static Data2Report encapsulatReport(CSIFData csifData) {
		if (csifData == null || csifData.getDetail().getApp().getGwrx().isEmpty()) {
			return null;
		}
		Data2Report data2Report = new Data2Report();
		List<Gateways> gateways = new ArrayList<Gateways>();
		Signal signal = new Signal();

		Iterator<Gwrx> it = csifData.getDetail().getApp().getGwrx().iterator();
		Gateways gate = new Gateways();
		while (it.hasNext()) {
			Gwrx gr = it.next();
			gate.setRssi(gr.getRssi());        
			gate.setSnr(gr.getLsnr());      
			gate.setChannel(gr.getChan());     
			//转换时间                                                                         
			String time = gr.getTime();        
			gate.setReceivedAt(transTimetoInt(time));                                      
			                                                                               
			gate.setGatewayEui(gr.getGweui()); 
			gateways.add(gate);                                                            
		}
		
		Motetx mtx = csifData.getDetail().getApp().getMotetx();
		signal.setFrequency(mtx.getFreq());
		signal.setDataRate(mtx.getDatr());
		signal.setCodeRate(mtx.getCodr());
		signal.setAdrSupported(mtx.isAdr());
		
		data2Report.setDeviceEui(csifData.getDevEUI());
		data2Report.setSequence(csifData.getDetail().getApp().getSeqno());
		data2Report.setGateways(gateways);
		data2Report.setSignal(signal);
		try {
//			System.out.println("payload:"+csifData.getPayload());
			byte[] bytes = CLIParser.decodeBase64(csifData.getPayload());
			data2Report.setMessage(byte2Int(bytes));
//			String str = new String(CLIParser.decodeBase64(csifData.getPayload()));
//			System.out.println("message of payload:"+str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data2Report;
	}

	
	/**
	 * 字节数组转int
	 * @param bytes	字节数组
	 * @return		整型数组
	 */
	public static int[] byte2Int(byte[] bytes){

		int[] integer = new int[bytes.length];
		for(int i=0;i<bytes.length;i++){
			int b0 = bytes[i] & 0xFF;  
			integer[i] = b0;
		}
		return integer;
	}
	
	/**
	 * 转换UTC格式到秒值
	 * @param time	UTC格式时间
	 * @return		秒值
	 */
	public static int transTimetoInt(String time){
//		String time = "2017-03-21T02:22:19Z";
		SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
//		SimpleDateFormat sdfLocal = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.US);
		Date date=null;
		try {
//			String localTime = sdfUTC.parse(time).toString();
//			System.out.println(localTime);
			date = sdfUTC.parse(time);
//			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(date.getTime())));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (int)(date.getTime()/1000);
	}

	

	/**
	 * 通过deviceeui get得到terminalid
	 * @param devEui	设备eui号
	 * @param token		安全令牌
	 * @return			terminal ID
	 */
	public static String getTerminalID(String devEui,Tokens token){
		if (!TokenUtils.getLegalToken(token)) {
			System.out.println("get token failed!");
			return null;
		}
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String url = Configure.DEFAULT_SERVER + "terminals?deviceEui="+devEui;
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("X-KSD-API-TOKEN", token.getToken());
		CloseableHttpResponse response =null;
		System.out.println(url);
		
		String id =null;
		try {
			response = httpClient.execute(httpGet);
			System.out.println(response.getStatusLine().toString());
			String resp = EntityUtils.toString(response.getEntity()).replace("\r\n", "").trim();

			int status = response.getStatusLine().getStatusCode();
			if (status!=200) {
				if(status ==401 || status ==403){
					TokenUtils.requestToken();
				}else if(status ==404){
					System.out.println("查找对象不存在！");
				}else {
					System.out.println("get terminal id error!");
				}
				return status+"";
			}
//			System.out.println(resp);
			try {
				ReportUtils.datarsp = gson.fromJson(resp,DataResponse.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				System.out.println("from Json to DataResponse ERROR");
			}			
			
			if(datarsp.getData().length >0){
				id = datarsp.getData()[0].getTerminalId(); 
//				System.out.println("TerminalId:"+id);
			}else{
				id = null;
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	/**
	 * 将csif来的json数据封装成csifdata对象
	 * @param message
	 * @return csif对象
	 */
	public static CSIFData parseCSIF(String message){
		
		if (message.charAt(message.length()-1) =='\n') {
			message = message.substring(0, message.length());
		}
		List<String> list = Arrays.asList(message.split("\n"));
//		List<CSIFData> csifDatas = new ArrayList<CSIFData>();
		for(int i =0;i<list.size();i++){
			if (list.get(i).startsWith("{")) {
				if (list.get(i).contains("UPLOAD") && list.get(i).contains("detail")) {
					csifData =  gson.fromJson(list.get(i), CSIFData.class);
				}else{
					csifData = null;
				}
			}
		}
		
//		try {
//			if (message.charAt(message.length()-1) =='\n') {
//				message = message.substring(0, message.length());
//			}
//			message = message.replace("\n", "");
//			int i=0;
//			while (Character.isDigit(message.charAt(i))) {
//				i++;
//			}
//			message = message.substring(i).trim();
//			System.out.println("after trim: "+message);
//			ReportUtils.csifData = gson.fromJson(message, CSIFData.class); 
//			
//			if (csifData.getDevEUI()!="") {
//				System.out.println("csifData:"+message);
//			}
//		} catch (JsonSyntaxException e) {
//			e.printStackTrace();
//			System.out.println("from Json to csifData ERROR！");
//			return null;
//		}
		return ReportUtils.csifData;
	}
	

	/**
	 * 解析report数据对象到json格式
	 * @param obj 对象
	 * @return json格式对象数据
	 */
	public static String encapsJsonRep(Object obj){
		return gson.toJson(obj);
	}
	
	public static <T> Object fromJson2Obj(String json,Class<T> clazz){
		return gson.fromJson(json, clazz);
	}

	//解析CSIF返回的数据，得到deviceeui值
	public static String  parse4Deveui(byte[] message) {
		String deveui="";
		String[] csifRsp = null;
		try {
			csifRsp = Parser.parseRespBuf(message);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String str = csifRsp[0];
		String[] bloks = str.split(",");
		if("DevEUI".equals(bloks[3].split(":")[0])){
			deveui = bloks[3].split(":")[1];
		}else{
			deveui = null;
		}
		return deveui;
	}
}

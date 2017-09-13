package ksd.myUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loraiot.iot.service.CLIParser;
import com.loraiot.iot.service.Configure;

import ksd.Data.fromBackend.DataResponse;
import ksd.Data.gatewayLocation.Gateways;

public class JunitTest {
	
	@Test
	public void testLocation() {
		int rssi = -70;
		double a = -65.65;
		double n = 0.274;
		System.out.println((rssi-a)/(-10*n));
		double drift =Math.exp((rssi-a)/(-10*n));
		System.out.println(drift);
		System.out.println(Math.round(drift));
	}
	
	
	@Test
	public void testLoger(){
		Logger logger = LogManager.getLogger(JunitTest.class);
		int i = 10;
		while (i!=0) {
			logger.debug(i);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i--;
		}
	}
	
	@Test
	public void name() {
		String str = "MDgwMTEzMDE=";//"MDgwMTBjMDA=";
		
		String payload ="AAAAAAAAAAANtgAAAAD//w";//"MDgwMTBBMDE=";//"CAECAA==";// "BwEBAAAGAAAAAAAAAAAAAA";
		int[] load = new int[20];
		try {
			String num = "";
//			System.out.println(new String(CLIParser.decodeBase64(payload)));
			load = ReportUtils.byte2Int(CLIParser.decodeBase64(payload));
			int len = load.length;
			System.out.println(len);
			for(int i=0;i<2;i++){
				System.out.println(load[i]);
				String hex = Integer.toHexString(load[i]);
				if (hex.length()>1) {
					num = num+hex;
				}else{
					num = num+"0"+hex;
				}
			}
			System.out.println(num);
			
			
			
//			if (load.length>=4 ){//&& load[0] ==7) {
//				for(int i=0;i<load.length;i++){
//					System.out.println(load[i]);
//				}
//			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void name1(){
		String str = "0A01";
		int[] ints = new int[4];
		ints[0] = 8;
		ints[1] = 1;
		System.out.println(str.substring((str.charAt(0)=='0')?1:0, 2));
		String s2 = str.substring((str.charAt(0)=='0')?1:0, 2);
		String s3 = str.substring((str.charAt(2)=='0')?3:2);
		
		ints[2] = Integer.parseInt(s2, 16);
		ints[3] = Integer.parseInt(s3, 16);
		System.out.println(Arrays.toString(ints));
		byte[] bytes = new byte[50];
		String res =null;
		try {
			bytes = int2byte(ints);
			System.out.println(new String(bytes));
//			bytes = str.getBytes("UTF-8");
			String xx = Arrays.toString(ints);
			res = CLIParser.encodeBase64(bytes);
//			res = CLIParser.encodeBase64(str.getBytes());
			
			System.out.println(res);
			
			System.out.println(new String(CLIParser.decodeBase64(res)));
			ints = ReportUtils.byte2Int(CLIParser.decodeBase64("BwEBAAAGAAAAAAAAAAAAAA"));
			
			System.out.println(Arrays.toString(ints));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
//		System.out.println(new String(CLIParser.decodeBase64(str)));
	} catch (Exception e) {
		e.printStackTrace();
	}
	}
	
	public byte[] int2byte(int[] ins) {
		byte[] bytes = new byte[ins.length];
		for(int i=0;i<ins.length;i++){
			bytes[i] = (byte)(ins[i] & 0xFF);
		}
		return bytes;
	}
	
	
	@Test
	public void parseConf() {
//		String payload = "07010A01";
		String  eui = "004a7702bd000001";
		byte b0 = (byte)01 & 0xFF;
		System.out.println(b0);
		
		String rev = Integer.toHexString(12);
		String sto = Integer.toHexString(1);
		String conf  = eui+","+rev+sto;
		System.out.println(conf);
	}
	
	@Test
	public void  byte2Int(){
		byte[] bytes = null;
		try {
			String src =CLIParser.encodeBase64("08010A01".getBytes("UTF-8"));
			System.out.println(src);
			bytes = CLIParser.decodeBase64(src);
			System.out.println(new String(bytes,"UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}//.getBytes();
		
	
		for(int i=0;i<bytes.length;i++){
			int b0 = bytes[i] & 0xFF;  
//			System.out.println(b0);
//	        int b1 = bytes[i + 1] & 0xFF;  
//	        int b2 = bytes[i + 2] & 0xFF;  
//	        int b3 = bytes[i + 3] & 0xFF;
//	        System.out.println((b0 << 24) | (b1 << 16) | (b2 << 8) | b3);
		}
		
	}
	
	@Test
	public void  testJson(){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String resp = "{\"Data\": [{\"TerminalId\": 1,\"DeviceEui\": \"4a7702bd000001\"}],\"Offset\": 100,\"Limit\": 10,\"Total\": 110,\"More\": true}";
//		resp = resp.replace("\n", "").replace(" ",	"").trim();
		DataResponse  data = new DataResponse();
		data = gson.fromJson(resp,DataResponse.class);
	}
	
	@Test
	public void  testTime() {
		String time = "2017-04-25T01:21:19Z";
		SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
//		SimpleDateFormat sdfLocal = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.US);
		Date date=null;
		try {
			String localTime = sdfUTC.parse(time).toString();
			System.out.println(localTime);
			date = sdfUTC.parse(time);
			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println(date.getTime()/1000);
	}
	/**
	 * 
	 	Tue Mar 21 10:22:19 CST 2017
		2017-03-21 10:22:19
		1490062939
	 */
	
	@Test
	public void testIntTime() {
		long time = 1494919686;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		Date dt = new Date(Long.valueOf(time));
		Date dt = new Date(time*1000);
		String local = sdf.format(dt);
		System.out.println(local);
	}
}

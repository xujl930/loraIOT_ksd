package com.loraiot.iot.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.org.apache.xml.internal.security.keys.content.KeyValue;

import ksd.Data.token.Tokens;
import ksd.Data.token.User;
import ksd.Threads.Rep2Backend;
import ksd.myUtils.ReportUtils;
import ksd.myUtils.TokenUtils;

/**
 * This is the class which is configuring the whole system
 * @author 10028484
 *
 */
public class Configure {
	
	public final static String DEFAULT_APPEUI="2c26c502bd000001";//"2c26c502bd000001"; // 1112131415161718
	
	public final static String DEFAULT_NOPNCE="12345678";
	
	public final static String DEFAULT_KEY="1234";
	
	public final static String DEFAULT_PAYLOAD = "";
	
	public final static String DEFAULT_DEVEUI="004a7702bd000002";//"004a7702bd000003";//"4a770066fffe73"
	
	public final static String DEFAULT_SERVER= "http://127.0.0.1:1323/"; //"http://127.0.0.1:1323/" 前端服务器   "http://118.178.123.142:10551/mtp/interfaces/"
	
	public final static short APPEUI_LEN = 16;
	
	public final static short NONCE_LEN =8;
	
	public final static String DEFAULT_APPKEY="00112233445566778899aabbccddeeff";
	
	public final static int DEFAULT_CMDSEQ = 1;
	
	public final static int DEFAULT_PORT = 30002;
	
	public final static int DEFAULT_MESSAGE_PORT = 10;
	
	public final static String DEFAULT_HOSTIP="139.129.216.128";	

	public final static int MAX_HEADER=5;
	
	public final static String DEFAULT_IOTHOST="msp02.claaiot.com";
	
	public final static String DEFAULT_IOTHOSTIP = "139.129.216.128";
	
	public final static int DEFAULT_IOTSSLPort = 30001;
	
	public final static int DEFAULT_IOTPort = 30002;
	
	public static int cmdseq_counter = DEFAULT_CMDSEQ;
	
	public static String hostip=DEFAULT_HOSTIP;
	
	public static int port = DEFAULT_PORT;
	
	public static String comm_type="TCP";
	
	public static String cmd_appEui =  DEFAULT_APPEUI;		//获取console输入的appeui
	
	public static InetSocketAddress ADDRESS = new InetSocketAddress(DEFAULT_HOSTIP,DEFAULT_PORT);
	
	public static volatile Tokens token = null;
	
	public static String userId = "test";
	public static String password = "1234";
	
	public static String AUTO_JOIN = "join -appeui "+DEFAULT_APPEUI;
	
	public  static double VAR_A = -65.65;
	public  static double VAR_N = 0.274;
	
	public static final Logger logger = LogManager.getLogger(MMCAgent.class);
	
	/**
	 * Default constructor
	 */
	public Configure() {
		// TODO Auto-generated constructor stub
	}
	

	public boolean setSystem(){
		return false;
	}
	
	/**
	 * Get the address transfered to InetAddress.
	 * @return InetAddress format.
	 * @throws UnknownHostException
	 */
	public static InetAddress getADDRESS() throws UnknownHostException {
		return InetAddress.getByName(DEFAULT_HOSTIP);
	}

	public static void setADDRESS(InetSocketAddress dEFAULT_ADDRESS) {
		ADDRESS = dEFAULT_ADDRESS;
	}		
	
	/**
	 * Method to read conf.properties to acquire the ip and port of CSIF.
	 * @throws IOException
	 */
	public static void readConf() throws IOException{
		File file = new File("conf.properties");
		if ((!file.exists()) || (file.isDirectory())){
			throw new FileNotFoundException();
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		InputStream is = new FileInputStream(file);
		Properties prop = new Properties();
		prop.load(is);
		String line = null;
	    line = br.readLine();
	    Map<String,String> map = new HashMap<String,String>();
	    String[] keyval = null;
	    while((line=br.readLine())!=null){
	    	if (line.startsWith("#")) {
				continue;
			}else {
				keyval = line.split("=");
				map.put(keyval[0], prop.getProperty(keyval[0]));
			}
	    }
	    br.close();
	    is.close();
	    
	    if (map.get("CSIF_IP") != null){
	    	Configure.hostip = map.get("CSIF_IP");
	    }
	    if (map.get("CSIF_PORT") != null){
	    	Configure.port = Integer.parseInt(map.get("CSIF_PORT"));
	    }
	    if (map.get("COMM_TYPE") != null){
	    	Configure.comm_type = map.get("COMM_TYPE");
	    }
	    
	    if (map.get("TOKENS")!=null) {
			Configure.token = TokenUtils.readToken(map.get("TOKENS"));
			if (TokenUtils.isExpired(Configure.token)) {
				TokenUtils.requestToken();
			}
		}else {
			TokenUtils.requestToken();
		}
	    
	    if (map.get("A")!=null) {
			Configure.VAR_A = Double.parseDouble(map.get("A"));
		}
	    
	    if (map.get("N")!=null) {
	    	Configure.VAR_N = Double.parseDouble(map.get("N"));
	    }
	    
	    ADDRESS = new InetSocketAddress(hostip,port);
	}

	
}

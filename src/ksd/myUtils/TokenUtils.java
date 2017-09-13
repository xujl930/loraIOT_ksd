package ksd.myUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.loraiot.iot.service.Configure;
import ksd.Data.token.Tokens;
import ksd.Data.token.User;

public class TokenUtils {
	
	// check the token before every request to backend
	public static boolean getLegalToken(Tokens token) {
		if (!isExpired(token)) {
			return true;
		}else {
			if (TokenUtils.requestToken()) {
				return true;
			}else {
				return false;
			}
		}
	}
	
	// check if token expired 
	public static boolean isExpired(Tokens token){
		if (token==null) {
			return true;
		}
		int current = (int) (System.currentTimeMillis()/1000);
		if(current < token.getExpresAt()){
			return false;
		}else{
			return true;
		}
	}
	
	//get token object from backend
	public static Tokens getToken(User user){
		String url = Configure.DEFAULT_SERVER+"tokens";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		StringEntity myEntity=null;
		try {	
			myEntity = new StringEntity(ReportUtils.encapsJsonRep(user),
					ContentType.create("application/json", "UTF-8"));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(myEntity);
		System.out.println(url);
		
		CloseableHttpResponse response =null;
		String resp ="";
		try {
			response = httpClient.execute(httpPost);
			System.out.println(response.getStatusLine().toString());
			
			resp = EntityUtils.toString(response.getEntity()).replace("\r\n", "");
			System.out.println(resp);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return (Tokens) ReportUtils.fromJson2Obj(resp, Tokens.class);
	}
	
	//get token object from properties
	public static void saveToken2Conf(Tokens token) throws IOException{
		String path = "./conf.properties";
		File file = new File(path);
		
		if ((!file.exists()) || (file.isDirectory())){
			throw new FileNotFoundException();
		}
		Properties prop = new Properties();
		BufferedReader br = new  BufferedReader(new FileReader(file));
		InputStream is = new FileInputStream(file);
		
		String tokenJson = ReportUtils.encapsJsonRep(token);
		prop.load(is);
//		Map<String, String> conf = new HashMap<String, String>();
		String line =null;
		String[] keyValue =null;
		while ((line=br.readLine())!=null) {
			if (line.startsWith("#")) {
				continue;
			}else {
				keyValue = line.split("=");
				if (!"TOKENS".equals(keyValue[0])) {
					prop.setProperty(keyValue[0], keyValue[1]);
				}else{
					prop.setProperty("TOKENS", tokenJson);
				}
			}
		}
//		System.out.println(prop.toString());
		OutputStream os = new FileOutputStream(new File(path));
		prop.store(os,"Update token:"+token.getToken());
		is.close();
		os.close();
		br.close();
	}
	
	//read token from properties
	public static Tokens readToken(String tokensJson){
		return (Tokens) ReportUtils.fromJson2Obj(tokensJson, Tokens.class);
	}
	
	//request a token
	public static boolean requestToken() {
		User user = new User();
		user.setUserId(Configure.userId);
		user.setPassword(Configure.password);
		Configure.token =TokenUtils.getToken(user);
		if (Configure.token!=null) {
			try {
				TokenUtils.saveToken2Conf(Configure.token);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}else {
			return false;
		}
	}
}

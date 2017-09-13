package ksd.myUtils;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.loraiot.iot.service.Configure;

import ksd.Data.gatewayLocation.Gateways;
import ksd.Data.gatewayLocation.GwLocation;
import ksd.Data.termTrackings.Trackings;

public class CalcLocationUtils {

	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	public static GwLocation getGwLocation(String resp) throws JsonParseException{
		System.out.println("GWlocation Resp:"+resp);
		return gson.fromJson(resp, GwLocation.class);
	}
	
	//计算偏移
	public static Trackings calcLocation(GwLocation gw) {
		Trackings track = new Trackings();
		if (gw.getGateways().isEmpty()) {
			Configure.logger.error("got "+gw.getGateways().size()+" gateway!");
			return null;
		}

//		Configure.logger.debug("Configure.VAR_A= "+Configure.VAR_A+"  Configure.VAR_N= "+Configure.VAR_N);
		
		Gateways gateway = gw.getGateways().get(0);
		if (gateway.getInfo()==null) {
			Configure.logger.error("got null gateway info!");
			return null;
		}
		
		int rssi = gateway.getRssi();
		double a = Configure.VAR_A;
		double n = Configure.VAR_N;
		double drifts =Math.exp((rssi-a)/(-10*n));
		int drift=(int) Math.round(drifts);
		int time = getCurrentTime();
		String latitude = gateway.getInfo().getLocation().getLatitude();
		String longitude = gateway.getInfo().getLocation().getLongitude();
		
		track.setUpdated(time);
		track.setLatitude(latitude);
		track.setLongitude(longitude);
		track.setDrift(drift);
		System.out.println("post trackings:"+track.toString());
		return track;
	}
	
	//
	public static String encapTrack2Report(Trackings track)throws JsonParseException{
		return gson.toJson(track);
	}
	
	public static int getCurrentTime(){
		Date date = new Date();
		long time = date.getTime();
		return (int)time/1000;
	}
}

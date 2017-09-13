package ksd.Threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.net.InetAddress;
import com.loraiot.iot.comm.Connection;
import com.loraiot.iot.comm.ConnectionFactory;
import com.loraiot.iot.data.Encapsulator;
import com.loraiot.iot.data.datagram.CSData2Dev;
import com.loraiot.iot.service.CLIParser;
import com.loraiot.iot.service.Configure;

public class Send2DEV implements Runnable{
	private Connection conn = null;
	private InetAddress add = null;
	private CSData2Dev csData = null;
	
	private PipedReader reader = null;
	
	private static byte[] swap = new byte[2048];
	private static byte[] message = new byte[2048];
	
	public InetAddress getAdd() {
		return add;
	}
	public void setAdd(InetAddress add) {
		this.add = add;
	}
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	public PipedReader getReader() {
		return reader;
	}
	public void setReader(PipedReader reader) {
		this.reader = reader;
	}
	public CSData2Dev getCsData() {
		return csData;
	}
	public void setCsData(CSData2Dev csData) {
		this.csData = csData;
	}


	@Override
	public void run() {
		BufferedReader br  = new BufferedReader(reader);
//		try {
//			connect();
//		} catch (IOException e1) {
//			System.out.println("conn to csif:"+e1.getMessage());
//		}
		
		while (true && !Thread.currentThread().isInterrupted()) {
			try {
				String line =null;
				while ((line =br.readLine()) !=null) {
					System.out.println("got set:"+line);

					if (line !=null) {
						try {
							encapsutoDev(line);
						} catch (Exception e) {
							Configure.logger.error("封装set信息错误："+e.getMessage());
							System.out.println("封装set信息错误："+e.getMessage());
						}
					}
					
					if (this.getConn() !=null && !this.getConn().isClosed() && message.length >0) {
						try {
							System.out.println("put set is:");
							System.out.println(new String(message));
							
							this.getConn().putData(message);
							
							if (message !=null) {
							//	Configure.cmdseq_counter = Configure.cmdseq_counter + 2;
							}
						}catch (IOException e) {
							e.printStackTrace();
							//System.out.println("发送set信息错误："+e.getMessage());
						}
					}
				}
			}catch (IOException e) {
				Configure.logger.error("send to DEV thread was stopped");
					System.out.println("send to DEV thread was stopped");
				//e.printStackTrace();
					try {
						if (this.getConn() !=null) {
							this.getConn().disconnect();
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					this.setConn(null);
					Thread.currentThread().interrupt();

//					continue;  		//中断信号产生，还会继续执行本次循环，interrupt唤醒read上的阻塞
									//导致跳过read阻塞而执行下面步骤发送一次set
			}	
		}
	}

	/**
	 * 封装要发送到dev的setting数据
	 * @param buf 	获取set数据的线程发送来的set数据
	 * @return		
	 * @throws Exception
	 */
	public byte[] encapsutoDev(String conf) throws Exception {
		if (conf =="" || conf.split(",").length<2) {
			Configure.logger.error("conf info to set dev is not correct! "+ "info is: "+conf);
			System.out.println("conf info is not correct!");
			return message;
		}
		String deveui = conf.split(",")[0];
//		String payload = "0801" + conf.split(",")[1].trim();
		byte[] payload = pack2Payload(conf.split(",")[1].trim());
		
		csData = new CSData2Dev();
		csData.setCMD("SENDTO");
		csData.setAppEUI(Configure.cmd_appEui);
		csData.setCmdSeq(Configure.cmdseq_counter);
		csData.setDevEUI(deveui);
		csData.setAppkey(Configure.DEFAULT_APPKEY);
		csData.setConfirm(true);
		csData.setPort(Configure.DEFAULT_MESSAGE_PORT);
		
//		swap = payload.getBytes("UTF-8");
		swap = payload;
		String pltmp = CLIParser.encodeBase64(swap);
		csData.setPayload(pltmp);
		
		String body = Encapsulator.encapsulateContent(csData);
		csData.setHeader(Integer.toString(body.length()));
		csData.setContent(body);
		message = Encapsulator.composeMessage(body);
		return message;
	}
	
	
	/**
	 * 转换int型payload到byte数组
	 * @param conf
	 * @return
	 */
	public byte[] pack2Payload(String conf) {
		int[] ints = new int[4];
		ints[0] = 8;
		ints[1] = 1;
//		System.out.println(conf.substring((conf.charAt(0)=='0')?1:0, 2));
		String s2 = conf.substring((conf.charAt(0)=='0')?1:0, 2);
		String s3 = conf.substring((conf.charAt(2)=='0')?3:2);
		ints[2] = Integer.parseInt(s2, 16);
		ints[3] = Integer.parseInt(s3, 16);
		byte[] bytes = int2byte(ints);
		
		return bytes;
	}
	
	/**
	 * 整型数组转字节数组
	 * @param ins
	 * @return
	 */
	public byte[] int2byte(int[] ins) {
		byte[] bytes = new byte[ins.length];
		for(int i=0;i<ins.length;i++){
			bytes[i] = (byte)(ins[i] & 0xFF);
		}
		return bytes;
	}
	
	
	/**
	 * 创建sock连接
	 * @throws IOException
	 */
	public void connect() throws IOException{
		this.setAdd(Configure.getADDRESS());
		this.setConn(ConnectionFactory.getConnect(add, Configure.port, "TCP"));
	}
	
	
	
}

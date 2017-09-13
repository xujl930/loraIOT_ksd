/**
 * 
 */
package com.loraiot.iot.comm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.loraiot.iot.service.CLIParser;
import com.loraiot.iot.service.Configure;
import com.loraiot.iot.service.MMCAgent;

/**
 * 
 * @author 10028484
 * @version 0.0.1
 */
public class RespGetter implements Runnable {

	private volatile String dataIncoming = null;

	private byte[] message = null;
	private Connection conn = null;
	private Connection connSet = null;

	private volatile boolean runFlag = true;
	private volatile boolean sbkFlag = false;

	
	public Connection getConnSet() {
		return connSet;
	}

	public void setConnSet(Connection connSet) {
		this.connSet = connSet;
	}

	public boolean isSbkFlag() {
		return sbkFlag;
	}

	public void setSbkFlag(boolean sbkFlag) {
		this.sbkFlag = sbkFlag;
	}


	/**
	 * Default Constructor 
	 */
	public RespGetter() {
	}

	public void GetterServerStart() {
	}

	public String getDataIncoming() {
		return dataIncoming;
	}

	public void setDataIncoming(String dataIncoming) {
		this.dataIncoming = dataIncoming;
	}


	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	
	
	public void run() {
		this.message = new byte[2048];
		// Check if RespGetter is for running
		while (this.isRunFlag()) {
			// If the connection is closed or is null, reconnect for ready send
			// or receive message
			if ((this.getConn() == null) || (this.getConn().isClosed())) {
				Configure.cmdseq_counter = Configure.DEFAULT_CMDSEQ;
				reConnect();
				this.setConn(MMCAgent.getConn());
			} else{
				try {
					this.message = this.getConn().getData();
					
					// decode and print
					if (this.message != null) {
						
//						String[] strMessage = Parser.parseRespBuf(this.message);
						System.out.println("answer is:" + new String(this.message));
//						Parser.parseContent(new String(this.message));
						this.setSbkFlag(true);
					} else {
						System.out.println("read in message <= 0");
						//socket dead,close it and reconnect
						if (this.getConn() !=null) {
							if(this.getConn().disconnect()){
								this.reConnect();
								if (this.getConn() ==null || (this.getConn().isClosed())) {
									System.out.println("reconnect failed!");
								}else {
									System.out.println(new Date().toLocaleString());
									System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> socket has dead,reconnected succeed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
									MMCAgent.getS2dev().setConn(this.conn);
									try {
										byte[] joinCmd = new CLIParser().parseCmd(Configure.AUTO_JOIN.split(" "));	//自动重新入网
										MMCAgent.getConn().putData(joinCmd);
									} catch (Exception e) {
										e.printStackTrace();
										System.out.println("reauto_join failed!");
									}
//									this.setConn(MMCAgent.getConn());
								}
							}else {
								System.out.println("disconnect failed!");
							}
						}
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("Normal Interrupted Exception, can be beared,just keep go on");
					// this.setRunFlag(false);
					// Configure.cmdseq_counter=Configure.DEFAULT_CMDSEQ;
				} catch (IOException e) {
					e.printStackTrace();
					if ((this.getConn() == null) || (this.getConn().isClosed())) {
						reConnect();
					}
				} catch (NegativeArraySizeException e) {
					System.out.println("connection closed");
					this.setRunFlag(false);
					Configure.cmdseq_counter = Configure.DEFAULT_CMDSEQ;
				} finally {					

				}
			}
		}
	}

	//reconnect to csif server 
	public void reConnect(){
		InetAddress add;
		try {
			add = Configure.getADDRESS();
			this.conn = ConnectionFactory.getConnect(add, Configure.port, "TCP");
			MMCAgent.setConn(this.conn);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isRunFlag() {
		return runFlag;
	}

	public void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}

}

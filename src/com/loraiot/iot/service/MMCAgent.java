/**
 * 
 */
package com.loraiot.iot.service;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.loraiot.iot.comm.Connection;
import com.loraiot.iot.comm.ConnectionFactory;
import com.loraiot.iot.comm.RespGetter;

import ksd.Threads.CalcLocation;
import ksd.Threads.GetSettings;
import ksd.Threads.Rep2Backend;
import ksd.Threads.Send2DEV;

/**
 * 
 * @author 10028484
 * @version 0.0.1
 */
public class MMCAgent {

	private volatile static Connection conn;

	private static InetAddress add;

	private static Thread thread;
	private static Thread threadRbk;
	private static Thread threadSet;
	private static Thread threadDev;
	private static Thread threadCalcLo;
	
	//socket between threads of getting setting and sending setting 
	private static PipedWriter writer = new PipedWriter();
	private static PipedReader reader = new PipedReader();
	
	private volatile static RespGetter  rg  = new RespGetter(); 
	private volatile static Rep2Backend rbk = new Rep2Backend();
	private volatile static GetSettings set = new GetSettings();
	private volatile static Send2DEV  s2dev = new Send2DEV();  
	private volatile static CalcLocation calcLo = new CalcLocation();
	
//	public final Logger log = LogManager.getLogger(MMCAgent.class);
	
	public MMCAgent() {

	}

	/**
	 * Entry of whole process
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args){
		try {
			Configure.readConf();
			//first connect the socket
			connect();
		} catch (IOException e2) {
			Configure.logger.error(e2.getMessage());
		}
		//the pipe between get seting and sendto dev threads 
		try {
			reader.connect(writer);
			set.setWriter(writer);
			s2dev.setReader(reader);
		} catch (IOException e2) {
			Configure.logger.error(e2.getMessage());
		}
		
		@SuppressWarnings("resource")
		Scanner s = new Scanner(System.in);
		CLIParser cliparse = new CLIParser();
		
		byte[] data2send = null;
		//auto join the appeui  
		try {
			auto_Join(cliparse);
		} catch (Exception e3) {
			Configure.logger.error("auto join failed! "+e3.getMessage());
			System.out.println("auto join failed!");
		}
		
		rg.setConn(conn);
		s2dev.setConn(conn);
		
		rbk.setRg(rg);
		rbk.setSettings(set);
		rbk.setCalc(calcLo);
		set.setRg(rg);
		
		thread = new Thread(rg);
		thread.start();
		
		threadRbk = new Thread(rbk);
		threadRbk.start();

		threadSet = new Thread(set);
		threadSet.start();

		threadDev = new Thread(s2dev);
		threadDev.start();
		
		threadCalcLo = new Thread(calcLo);
		threadCalcLo.start();
		while (true) {
			String line = s.nextLine();

			if (line.equals("exit")) {
				if ((conn != null) && (!conn.isClosed())) {
					try {
						conn.disconnect();
					} catch (IOException e) {
						Configure.logger.error(e.getMessage());
						//System.out.println(e.getMessage());
						e.printStackTrace();
					}
					conn = null;
				} else {
					conn = null;
				}

				rg.setConn(conn);
				thread.interrupt();
				
				threadSet.interrupt();
				s2dev.setConn(conn);
				threadDev.interrupt();
				threadRbk.interrupt();
				 
				try {
					threadSet.join();	//等待exit前保存document
				} catch (InterruptedException e) {
					Configure.logger.error(e.getMessage());
					e.printStackTrace();
				}
				System.exit(0);
				break;
			}
			args = line.split(" ");
			try {
				data2send = cliparse.parseCmd(args);
			} catch (Exception e2) {
				Configure.logger.error(e2.getMessage());
				System.out.println(e2.getMessage());
			}
			
			if (!args[0].trim().equalsIgnoreCase("quit")) {
				if ((conn == null) || (conn.isClosed())) {
					conn = connect();
					rg = new RespGetter();
					rg.setConn(conn);
					
					s2dev.setConn(conn);
					
					thread = new Thread(rg);
					thread.start();
				}
			}
			if ((conn != null) && (!conn.isClosed())) {
				// server socket come data
				try {
					conn.putData(data2send);
				} catch (IOException e) {
					if ((conn == null) || (conn.isClosed())) {
						InetAddress add;
						try {
							add = Configure.getADDRESS();
							conn = ConnectionFactory.getConnect(add, Configure.port, "TCP");
							rg.setConn(conn);
							s2dev.setConn(conn);
							conn.putData(data2send);
						} catch (UnknownHostException e1) {
							Configure.logger.error(e1.getMessage());
							e1.printStackTrace();
						} catch (IOException e1) {
							System.out.println(
									"retry connect fail or connection problem, break out and restart the applicatoin");
							e1.printStackTrace();
							thread.interrupt();
							rg.setRunFlag(false);
							thread.interrupt();
							
							threadRbk.interrupt();
							threadSet.interrupt();
							threadDev.interrupt();
							threadCalcLo.interrupt();
							if ((conn != null) || (!conn.isClosed())) {
								try {
									conn.disconnect();
								} catch (IOException e2) {
									Configure.logger.error(e2.getMessage());
									System.out.println(e2.getMessage());
								}
								conn = null;
								rg.setConn(null);
								s2dev.setConn(null);
							}
							break;
						}
					}
				}
				if (data2send != null) {
					Configure.cmdseq_counter = Configure.cmdseq_counter + 2;
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Configure.logger.error(e.getMessage());
					System.out.println(e.getMessage());
				}
				if (args[0].trim().equalsIgnoreCase("quit")) {
					System.out.println("quit cmd has sent");
					Configure.cmdseq_counter = Configure.DEFAULT_CMDSEQ;
					rg.setRunFlag(false);
					thread.interrupt();
					
//					threadRbk.interrupt();
//					threadSet.interrupt();
//					threadDev.interrupt();
					
					if ((conn != null) || (!conn.isClosed())) {
						try {
							conn.disconnect();
						} catch (IOException e) {
							System.out.println(e.getMessage());
						}
						conn = null;
						rg.setConn(null);
						s2dev.setConn(null);
					}
				}
				
				//In fact, if application runs, the connection must be kept, if closed, open again
				if ((conn == null) || (conn.isClosed())) {
					
					conn = connect();
					rg.setConn(conn);
					
					thread = new Thread(rg);
					rg.setRunFlag(true);
					thread.start();

//					rbk.setRg(rg);
//					threadRbk = new Thread(rbk);
//					threadRbk.start();
					
					
//					set.setRg(rg);
//					threadSet = new Thread(set);
//					threadSet.start();
//					
//					threadDev = new Thread(s2dev);
//					threadDev.start();
					
					if (conn == null) {
						Configure.logger.error("Acquire connection failed, connecting error");
						System.out.println("Acquire connection failed, connecting error");
						break;
					}
				}
			}
		}
	}	
	

	public static void auto_Join(CLIParser parser) throws Exception{
		byte[] joinCmd = parser.parseCmd(Configure.AUTO_JOIN.split(" "));
		if (conn != null || !conn.isClosed()) {
			conn.putData(joinCmd);
		}
	}
	
	public static Connection getConn() {
		return conn;
	}

	public static void setConn(Connection conn) {
		MMCAgent.conn = conn;
	}

	/**
	 * Connect the socket to server.
	 * 
	 * @throws IOException
	 */
	public static Connection connect(){
		try {
			add = Configure.getADDRESS();
			conn = ConnectionFactory.getConnect(add, Configure.port, "TCP");
		} catch (Exception e) {
			Configure.logger.error(e.getMessage());
			//System.out.println(e.getMessage());
			e.printStackTrace();
			//System.exit(0);
		}
		return conn;
	}

	public static InetAddress getAdd() {
		return add;
	}

	public static void setAdd(InetAddress add) {
		MMCAgent.add = add;
	}

	public static void disconnect() throws IOException {
		if (conn != null) {
			conn.disconnect();
		}
	}

	public static Thread getThread() {
		return thread;
	}

	public static void setThread(Thread thread) {
		MMCAgent.thread = thread;
	}

	public static RespGetter getRg() {
		return rg;
	}

	public static void setRg(RespGetter rg) {
		MMCAgent.rg = rg;
	}

	public static Send2DEV getS2dev() {
		return s2dev;
	}

	public static void setS2dev(Send2DEV s2dev) {
		MMCAgent.s2dev = s2dev;
	}
	
}

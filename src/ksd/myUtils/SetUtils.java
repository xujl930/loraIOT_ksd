package ksd.myUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class SetUtils {

	public static volatile Document document=null;
	
	/**
	 *获取xml文件dom树
	 * @throws DocumentException 文件不存在
	 */
	public synchronized static void getDocument() throws DocumentException {
		try {
			BufferedReader br = new BufferedReader(new FileReader("./setting.xml"));
			SetUtils.document = new SAXReader().read(br);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
//		SetUtils.document = new SAXReader().read(new File("./setting.xml"));
//		System.out.println("reading document...."+"<"+document.selectNodes("/terminals/terminal").toString()+">");
		if (document.getRootElement() ==null) {
			document = DocumentHelper.createDocument();
			Element rootEle = document.addElement("terminals");
			document.setRootElement(rootEle);
		}
	}
	

	/**
	 * 回写xml文件
	 * @throws IOException 异常
	 */
	public synchronized static void saveDocument() throws IOException{
		if (document == null) {
			return;
		}
		System.out.println("....saving document...."+"<"+document.selectNodes("/terminals/terminal").size()+">");
		FileOutputStream fos = new FileOutputStream(new File("./setting.xml"));
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		XMLWriter writer;
		try {
			writer = new XMLWriter(fos,format);
			writer.write(document);
			writer.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println();
		}
	}
	

	/**
	 * 封装dev设置
	 * @param eui		deveui
	 * @param revision	配置版本号
	 * @param stolen	被盗模式位
	 * @return          conf
	 */
	public static String  toDevConf(String eui,int revision,int stolen) {
		String rev = Integer.toHexString(revision);
		rev = (rev.length()>1)?rev:"0"+rev;
		
		String sto = "0"+Integer.toHexString(stolen);
		return eui + "," + rev + sto;
	}
	
	/**
	 * 添加  设备的配置表
	 * @param terminalid	编号
	 * @param revision		版本号
	 * @param DevEUI		deveui
	 * @param stolen		被盗标志位
	 * @return				非空，下发新版本
	 */
	public static synchronized List<String> add2DevTable(String terminalid, int revision, String DevEUI,boolean stolen){
		if (document ==null) {
			try {
				getDocument();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		List<String> result = null;
		Element single;
		boolean exist = false;
		String xpath = "//terminal[@id='"+terminalid+"']";
		
		@SuppressWarnings("unchecked")
		List<Element> termElem = document.selectNodes(xpath);
		if(termElem.size()>=1){
			System.out.println("id:"+terminalid+" 已存在！");
			exist = true;
		}
		if (!exist) {
			Element rootElement = document.getRootElement();
			Element term = rootElement.addElement("terminal");
			term.addAttribute("id", terminalid);
			term.addElement("revision").setText(revision+"");
			term.addElement("deveui").setText(DevEUI);
			term.addElement("stolen").setText(stolen+"");
			System.out.println("terminal:"+terminalid+", revision:"+term.elementText("revision")+" 已添加到rev表中！");	
			
			//添加完 节点，保存内存中的document对象
			try {
				saveDocument();
				document = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(exist){
			single = (Element)document.selectSingleNode(xpath);
			int revTab = Integer.parseInt(single.elementText("revision"));  
			if(revision < revTab){					//需要向节点下发新的配置版本
				String sto = single.elementText("stolen");
				result = new ArrayList<>();
				result.add(revTab+"");
				result.add(sto);
			}
			if (revision > revTab) {
				single.element("revision").setText(revision+"");
				single.element("stolen").setText(stolen+"");
				System.out.println("terminal:"+terminalid+"  revision:"+single.elementText("revision"));
			}
		}
		return result;
	}
	
	/**
	 * 修改 设备的配置表
	 * @param terminalid	设备id号
	 * @param revision		版本号
	 * @param DevEUI		设备eui
	 * @param stolen		被偷标志位
	 */
	public static synchronized boolean modDevTable(String terminalid, int revision, String DevEUI,boolean stolen){
		if (document ==null) {
			try {
				getDocument();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}

		boolean isModed = false;
		String xpath = "//terminal[@id='"+terminalid+"']";
		
		Element single = (Element)document.selectSingleNode(xpath);
		if(single ==null){
			System.out.println("无此terminal!");
		}else{
			int revTab = Integer.parseInt(single.elementText("revision"));  
			if (revision != revTab) {
				single.element("revision").setText(revision+"");
				single.element("stolen").setText(stolen+"");
				System.out.println("terminal:"+terminalid+"  revision:"+single.elementText("revision"));
				isModed = true;
			}else {
				System.out.println("no need to update table!");
			}
		}
		return isModed;
	}
	
	/**
	 * 取出设备配置表信息，对比服务器配置
	 * @return 映射xml文件中所有的terminal到hashmap中
	 */
	public static HashMap<String,Integer> getTerminalMap(){
		if (document ==null || document.selectNodes("/terminals/terminal") ==null) {
			try {
				getDocument();
				if (document ==null || document.selectNodes("/terminals/terminal") ==null) {
					return null;
				}
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		
		String xpath = "/terminals/terminal";
		HashMap<String, Integer> terminalMap = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<Element> ids= document.selectNodes(xpath);
		for (Element term : ids) {
			//			int id = Integer.parseInt(term.attribute("id").getValue());
			String deveui = term.elementText("deveui");
			int revision = Integer.parseInt(term.elementText("revision"));
			terminalMap.put(deveui, revision);
		}
		return terminalMap;
	}

	
	/**
	 * 获取指定terminal的revision号
	 * @param terminalid	设备ID
	 * @return -2 xml文件中存在多个同样的ID
	 * 		   -1  没有指定的ID，需要setRevision新建，再给CSIF发数据
	 */
	public static int getRevision(int terminalid){
		String xpath = "//terminal[@id='"+terminalid+"']";
		
		@SuppressWarnings("unchecked")
		List<Element> termElem = document.selectNodes(xpath);
		if(termElem.size()>1){
	//			System.out.println("�ظ���id:"+terminalid+",��ȡrevisionʧ��");
			return -2;
		}
		int revision;
		if(termElem.size()==1){
			revision = Integer.parseInt(termElem.get(0).elementText("revision"));
		}else{
			revision = -1;
		}
		return revision;
	}
}
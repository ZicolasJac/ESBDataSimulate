package com.dc.esb;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dc.esb.bean.ConsumerSystem;
import com.dc.esb.bean.ESBException;
import com.dc.esb.bean.EsbApp;
import com.dc.esb.bean.ExptData;
import com.dc.esb.bean.ProviderSystem;
import com.dc.esb.bean.Service;
import com.dc.esb.conf.DataSimuConfig;
import com.dc.esb.task.InsertFlow;
import com.dc.esb.task.SendRsmInfo;

public class DataSimulate {

	private static Logger log = Logger.getLogger(DataSimulate.class);
	private static Document doc = null;

	public static void main(String[] args){
		// 加载log4j配置
		PropertyConfigurator.configure(System.getProperty("user.dir") + "/conf/log4j.properties");
		log.info("log4j加载完成！");
		
		//模拟六台资源采集器
		SendRsmInfo sendRsm = new SendRsmInfo();
		new Thread(sendRsm).start();
		
		//模拟交易流水
		simulateTransFlow();
	}
	
	/**
	 * 模拟交易流水模块
	 * @param args
	 */
	public static void simulateTransFlow() {
		// 加载配置文件
		loadConfig();
		
		// 获取监控周期
		int mon_cycle = Integer.parseInt(doc.getElementsByTagName("cycle").item(0).getTextContent());
		
		/* 获取esbapp应用配置 */
		NodeList esbappList = doc.getElementsByTagName("esbapp");
		ArrayList<EsbApp> esbAppList = getEsbAppList(esbappList);
		if (esbappList.getLength() == 0) {
			log.error("esb应用数为零，请检查配置文件！");
			System.exit(0);
		}
		log.info("获取esbapp应用结束，共" + esbAppList.size() + "个应用！");
		
		/* 获取调用方系统配置 */
		NodeList channelSysList = doc.getElementsByTagName("channelsystem");
		Map<String, ConsumerSystem> channelSysMap = getChannelSysParameter(channelSysList);
		if (channelSysMap == null || channelSysMap.size() == 0) {
			log.error("调用方系统数为零，请检查配置文件！");
			System.exit(0);
		}
		log.info("获取调用方系统结束，共" + channelSysMap.size() + "个系统！");
		
		Map<String,ArrayList<ESBException>> exptMap = new HashMap<>();
		/* 获取系统异常码及异常描述配置 */
		Node sysexpt = doc.getElementsByTagName("sysexpt").item(0);
		NodeList  seNodeList =  sysexpt.getChildNodes();
		// 去除sysexpt子节点中的空节点
		for (int m = seNodeList.getLength() - 1; m >= 0; m--) {
			Node child = seNodeList.item(m);
			if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.COMMENT_NODE) {
				child.getParentNode().removeChild(child);
			}
		}
		ArrayList<ESBException> sysExptList= getExptList(seNodeList);
		exptMap.put("se", sysExptList);
		
		/* 获取交易异常码及异常描述配置 */
		Node transexpt = doc.getElementsByTagName("transexpt").item(0);
		NodeList  beNodeList=  transexpt.getChildNodes();
		// 去除transexpt子节点中的空节点
		for (int m = beNodeList.getLength() - 1; m >= 0; m--) {
			Node child = beNodeList.item(m);
			if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.COMMENT_NODE) {
				child.getParentNode().removeChild(child);
			}
		}
		ArrayList<ESBException> bussExptList= getExptList(beNodeList);
		exptMap.put("be", bussExptList);
		
		/* 获取异常数据配置 */
		NodeList  edNodeList =  doc.getElementsByTagName("exptdata").item(0).getChildNodes();
		ArrayList<ExptData> edList = getExptDataList(edNodeList);
		log.info("获取异常历史数据配置结束，共" + edList.size() + "天异常历史数据配置！");
		
		/* 获取公共tps配置 */
		Node global_tps = doc.getElementsByTagName("allsys").item(0);
		NodeList  global_tpslist =  global_tps.getChildNodes();
		// 去除allsys子节点中的空节点
		for (int m = global_tpslist.getLength() - 1; m >= 0; m--) {
			Node child = global_tpslist.item(m);
			if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.COMMENT_NODE) {
				child.getParentNode().removeChild(child);
			}
		}
		
		/* 获取提供方系统及其旗下的服务配置 */
		NodeList psNodeList = doc.getElementsByTagName("system");
		ArrayList<ProviderSystem> provideSystemList = getPrvdSysList(psNodeList, global_tpslist);
		if (provideSystemList == null || provideSystemList.size() == 0) {
			log.error("提供方方系统数为零，请检查配置文件");
			System.exit(0);
		}
		log.info("获取提供方系统结束，共" + provideSystemList.size() + "个系统");

		/* 一个提供方开一个线程 */
		for (int i = 0; i < provideSystemList.size(); i++) {
			Thread insertThread = new Thread(new InsertFlow(mon_cycle, provideSystemList.get(i), channelSysMap, esbAppList, exptMap, edList ));
			log.info("开启提供方" + provideSystemList.get(i).getPrdId() + "的模拟数据线程...");
			insertThread.start();
		}
	}

	/**
	 * 获取异常历史数据配置
	 * @param edNodeList
	 * @return
	 */
	private static ArrayList<ExptData> getExptDataList(NodeList edNodeList) {
		// 去除exptdata子节点中的空节点
		for (int m = edNodeList.getLength() - 1; m >= 0; m--) {
			Node child = edNodeList.item(m);
			if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.COMMENT_NODE) {
				child.getParentNode().removeChild(child);
			}
		}
		
		ArrayList<ExptData> edList = new ArrayList<ExptData>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < edNodeList.getLength(); i++) {
			NodeList timeList = edNodeList.item(i).getChildNodes();
			//去除date直接中的空节点
			for (int n = timeList.getLength() - 1; n >= 0; n--) {
				Node child = timeList.item(n);
				if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.COMMENT_NODE) {
					child.getParentNode().removeChild(child);
				}
			}

			for (int j=0;j< timeList.getLength();j++){
				Node tnode = timeList.item(j);
				ExptData exptData = new ExptData();
				String date =edNodeList.item(i).getAttributes().getNamedItem("day").getTextContent();
				NamedNodeMap nnp = tnode.getAttributes();
				if(nnp!=null){
					String start_time = tnode.getAttributes().getNamedItem("startt").getTextContent();
					String stop_time = tnode.getAttributes().getNamedItem("stopt").getTextContent();
					try {
						exptData.setStarttime(sdf.parse(date+" "+start_time));
						exptData.setStoptime(sdf.parse(date+" "+stop_time));
					} catch (ParseException e) {
						log.error("字符串转日期失败！",e);
					}
					exptData.setSsrRange(nnp.getNamedItem("sysSuccRate").getTextContent());
					exptData.setTsrRange(nnp.getNamedItem("busSuccRate").getTextContent());
					exptData.setEtRange(nnp.getNamedItem("esbtime").getTextContent());
					exptData.setTtRange(nnp.getNamedItem("trantime").getTextContent());
					List<String[]> list = new ArrayList<>();
					String[] strr = {start_time, stop_time, nnp.getNamedItem("tps").getTextContent()};
					list.add(strr);
					exptData.setTpsRangeList(list);
//					exptData.setTpsRange(tnode.getAttributes().getNamedItem("tps").getTextContent());
					List<String> exptServices = new ArrayList<>();
					String[] serviceArray = tnode.getTextContent().split(",");
					for(String serId : serviceArray){
						exptServices.add(serId);
					}
					exptData.setExptServices(exptServices);
					edList.add(exptData);
				}
			}
		}
		
		return edList;
	}
	
	/**
	 * 获取异常列表
	 * @param exptNodeList
	 * @return
	 */
	private static ArrayList<ESBException> getExptList(NodeList exptNodeList) {
		List<ESBException> exptList = new ArrayList<ESBException>();
		for (int i = 0; i < exptNodeList.getLength(); i++) {
			ESBException expt = new ESBException();
			NamedNodeMap nnp = exptNodeList.item(i).getAttributes();
			if(nnp != null){
				expt.setCode(nnp.getNamedItem("code").getTextContent());
				expt.setDescription(nnp.getNamedItem("description").getTextContent());
				exptList.add(expt);
			}
		}
		return (ArrayList<ESBException>) exptList;
	}

	/**
	 * 获取提供方系统及其旗下服务的配置
	 * @param psNodeList 当个服务tps设置
	 * @param global_tpslist 默认tps配置
	 * @return
	 */
	private static ArrayList<ProviderSystem> getPrvdSysList(NodeList psNodeList, NodeList global_tpslist) {
		ArrayList<ProviderSystem> provideSystemList = new ArrayList<>();
		for (int i = 0; i < psNodeList.getLength(); i++) {
			// 保存每一只服务
			ProviderSystem provideSystem = new ProviderSystem();
			ArrayList<Service> serviceList = new ArrayList<Service>();
			if (psNodeList.item(i).hasChildNodes()) {
				// DOM是有严格的规范，子节点之间的空白回车换行也都是一个节点，这些节点的类型都是TEXT_NODE
				NodeList psClilds = psNodeList.item(i).getChildNodes();
				int count = psClilds.getLength();
				// 去除system子节点中的空节点
				for (int j = count - 1; j >= 0; j--) {
					Node child = psClilds.item(j);
					if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.COMMENT_NODE) {
						child.getParentNode().removeChild(child);
					}
				}
				String prdId = psNodeList.item(i).getAttributes().getNamedItem("id").getTextContent();
				String prdIps = psNodeList.item(i).getAttributes().getNamedItem("sysip").getTextContent();
				provideSystem.setPrdId(prdId);
				provideSystem.setPrdIps(prdIps);
				
				int realCount = psClilds.getLength();
				if (realCount > 0) {
					for (int j = 0; j < realCount; j++) {
						NamedNodeMap oneServAttris = psClilds.item(j).getAttributes();
						if ((oneServAttris.getNamedItem("runStatus").getTextContent()).equals("true")) {
							Service service = new Service();
							service.setServiceName(oneServAttris.getNamedItem("name").getTextContent());
							NodeList tpslist = psClilds.item(j).getChildNodes();
							// 去除service子节点中的空节点
							int count1 = tpslist.getLength();
							for (int k = count1 - 1; k >= 0; k--) {
								Node child = tpslist.item(k);
								if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.COMMENT_NODE) {
									child.getParentNode().removeChild(child);
								}
							}
							
							List<String[]> tps = getTpsFromConfig(tpslist, global_tpslist);
							service.setTpsRangeList(tps);
							oneServAttris.getNamedItem("runStatus").getTextContent();
							service.setSsrRange(oneServAttris.getNamedItem("sysSuccRate").getTextContent());
							service.setBsrRange(oneServAttris.getNamedItem("busSuccRate").getTextContent());
							service.setEsbTimeRange(oneServAttris.getNamedItem("esbtime").getTextContent());
							service.setBussTimeRange(oneServAttris.getNamedItem("trantime").getTextContent());
							service.setChannels(channelStr2List(oneServAttris.getNamedItem("channel").getTextContent()));
							serviceList.add(service);
						}
					}
					log.info("获取系统" + prdId + "内服务结束，共" + serviceList.size() + "个服务！");
				} else {
					log.warn("系统" + prdId + "没有配置服务，请检查配置！");
				}
			}
			provideSystem.setServiceList(serviceList);
			provideSystemList.add(provideSystem);
		}
		return provideSystemList;
	}

	/**
	 * 获取esbapp配置
	 * @param eappNodeList
	 * @return
	 */
	private static ArrayList<EsbApp> getEsbAppList(NodeList eappNodeList) {
		List<EsbApp> esbAppList = new ArrayList<EsbApp>();
		for (int i = 0; i < eappNodeList.getLength(); i++) {
			if ((eappNodeList.item(i).getAttributes().getNamedItem("runStatus").getTextContent()).equals("true")) {
				EsbApp esbapp = new EsbApp();
				esbapp.setLoop(eappNodeList.item(i).getAttributes().getNamedItem("id").getTextContent());
				esbAppList.add(esbapp);
			}
		}
		return (ArrayList<EsbApp>) esbAppList;
	}

	/**
	 * 加载配置信息
	 */
	private static void loadConfig() {
		try {
			// 加载系统配置文件
			DataSimuConfig.loadProperties();
			log.info("系统配置文件加载完成！");

			// 加载交易维度配置文件
			File xmlFile = new File("conf/serviceData.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(xmlFile);
			log.info("交易维度配置文件加载完成！");
		} catch (ParserConfigurationException e) {
			log.error("加载交易维度配置文件失败！文件格式异常！", e);
		} catch (Exception e) {
			log.error("工具启动失败！", e);
		}
	}
	
	/**
	 * 获取渠道系统参数
	 * @param channelSysList
	 * @return
	 */
	private static Map<String, ConsumerSystem> getChannelSysParameter(NodeList channelSysList) {
		Map<String, ConsumerSystem> channelSysParameterMap = new HashMap<String, ConsumerSystem>();
		for (int i = 0; i < channelSysList.getLength(); i++) {
			if ((channelSysList.item(i).getAttributes().getNamedItem("runStatus").getTextContent()).equals("true")) {
				ConsumerSystem channelSys = new ConsumerSystem();
				channelSys.setChannelId(channelSysList.item(i).getAttributes().getNamedItem("id").getTextContent());
				channelSys.setName(channelSysList.item(i).getAttributes().getNamedItem("name").getTextContent());
				String comIps = channelSysList.item(i).getAttributes().getNamedItem("channelIp").getTextContent();
				channelSys.setComIps(comIps);
				channelSysParameterMap.put(channelSys.getChannelId(), channelSys);
			}
		}
		return channelSysParameterMap;
	}
	
	/**
	 * 分时间段设置tps,优先采用具体配置，其次考虑共用配置
	 * @param strTps
	 * @return
	 */
	public static List<String[]> getTpsFromConfig(NodeList detail_tpslist, NodeList global_tpslist) {
		List<String[]> tpsRangeList = new ArrayList<>();
		int num2 = global_tpslist.getLength();
		while(num2 > 0){
			Node child2 = global_tpslist.item(num2 - 1);
			NamedNodeMap nnp2 = child2.getAttributes();
			String starttime2 = nnp2.getNamedItem("startt").getTextContent();
			String stoptime2 = nnp2.getNamedItem("stopt").getTextContent();
			boolean confirmTps = false;
			int num1 = detail_tpslist.getLength();
			while(num1 > 0){
				Node child1 = detail_tpslist.item(num1 - 1);
				NamedNodeMap nnp1 = child1.getAttributes();
				String starttime1 = nnp1.getNamedItem("startt").getTextContent();
				String stoptime1 = nnp1.getNamedItem("stopt").getTextContent();
				// 优先使用服务自定义
				if(starttime1 == starttime2 && stoptime1 == stoptime2){
					String tpsRange = nnp1.getNamedItem("tps").getTextContent();
					String[] timeArr= {starttime1, stoptime1, tpsRange}; 
					tpsRangeList.add(timeArr);
					confirmTps = true;
					break;
				}
				num1--;
			}
			if(!confirmTps){
				String tpsRange = nnp2.getNamedItem("tps").getTextContent();
				String[] timeArr= {starttime2, stoptime2, tpsRange}; 
				tpsRangeList.add(timeArr);
			}
			num2--;
		}
		return tpsRangeList;
	 }
	
	private static List<String> channelStr2List(String channelStr) {
		List<String> channelList = new ArrayList<String>();
		String[] array = channelStr.split(",");
		for(String item : array) {
			channelList.add(item);
		}
		return channelList;
	}
}
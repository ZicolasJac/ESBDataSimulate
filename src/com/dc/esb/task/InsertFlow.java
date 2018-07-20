package com.dc.esb.task;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.dc.esb.bean.ConsumerSystem;
import com.dc.esb.bean.ESBException;
import com.dc.esb.bean.EsbApp;
import com.dc.esb.bean.ExptData;
import com.dc.esb.bean.ProviderSystem;
import com.dc.esb.bean.Service;
import com.dc.esb.conf.DataSimuConfig;
import com.dc.esb.utils.DataSimuUtils;
import com.dc.esb.utils.JDBCTool;

public class InsertFlow implements Runnable {

	private static Logger log = Logger.getLogger(InsertFlow.class);
	private JDBCTool jdbc = new JDBCTool();
	//系统数据对象
	private ProviderSystem ProviderSystem;
	// 该系统提供的服务集合
	private ArrayList<Service> ServiceList = new ArrayList<Service>();
	// 调用该系统的渠道集合
	private Map<String, ConsumerSystem> ConsumerSystemMap = new HashMap<String, ConsumerSystem>();
	// esbapp应用集合
	private Queue<EsbApp> esbAppQueue = new LinkedList<EsbApp>();
	//异常情况集
	private Map<String,ArrayList<ESBException>> exptMap = new HashMap<>();
	//监控周期
	private int mon_cycle = 30;
	//异常历史数据配置
	private ArrayList<ExptData> edList;

	public InsertFlow(int mon_cycle, ProviderSystem ProviderSystem, Map<String, ConsumerSystem> ConsumerSystemMap, 
			ArrayList<EsbApp> esbAppList, Map<String,ArrayList<ESBException>> exptMap, ArrayList<ExptData> edList) {
		this.ProviderSystem = ProviderSystem;
		this.ConsumerSystemMap = ConsumerSystemMap;
		this.ServiceList = ProviderSystem.getServiceList();
		for(EsbApp esbapp : esbAppList) {
			esbAppQueue.offer(esbapp);
		}
		this.mon_cycle = mon_cycle;
		this.exptMap = exptMap;
		this.edList = edList;
	}

	public void run() {
		//esb内部流水号
		String ESBFLOWNO = "";
		 //esb应用标识
		String LOOP = "";
		// 交易状态码
		String RESPSTATUS = ""; 
		 // 交易响应码
		String RESPCODE = "";
		// 交易响应结果
		String RESPMSG = ""; 
		 // 消费方id
		String LOGICCHANNEL = "";
		// 消费方ip
		String CHANNELIP = ""; 
		// 提供方系统编号
		String prvdid ="";
		// 提供方系统ip
		String prvdip = "";
		// 计数器
		int loop = 0;
		
		while (true) {
			log.info("正在模拟当前周期数据...");
			loop = 0;
			
			long startTime = System.currentTimeMillis();
			boolean isTimeOut = false;
			jdbc.getConn();
			
			/** 获取提供方系统id */
			prvdid = ProviderSystem.getPrdId();
			
			/** 该提供方系统下所有服务 */
			for (Service service : ServiceList) {
				/* 判断当前周期是否为配置的异常周期 */
				boolean exptDataSign = false;
				ExptData exptData = new ExptData();
				for(ExptData ed : edList ){
					long currTime = new Date().getTime();
					long stat = ed.getStarttime().getTime();
					long stot = ed.getStoptime().getTime();
					if(currTime>=stat && currTime<=stot){
						exptDataSign = true;
						exptData = ed;
						break;
					}
				}
				
				/* 判断该服务是否被配置为异常服务 */
				if(exptDataSign && exptData.getExptServices().contains(service.getServiceName())){
					service = getExptService(exptData, service);
				}
				
				/* 当前服务的第一笔交易的起始时间 */
				long curr = System.currentTimeMillis();
				Date currDate = new Date(curr);
				
				/* 计算某个服务的执行次数 */
				double tps = 0;
				int excuteTime = 0;
				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				try {
					for(String[] tpsRR : service.getTpsRangeList()){
							Date dt_start = df.parse(tpsRR[0]);
							Date dt_stop = df.parse(tpsRR[1]);
							Date currTime = df.parse(df.format(currDate));
							if(currTime.getTime() >= dt_start.getTime() && currTime.getTime() < dt_stop.getTime()){
								tps = DataSimuUtils.getRandomFromRange(tpsRR[2]);
								break;
							}
					}
					// 执行次数
					excuteTime = (int) Math.floor(tps * mon_cycle);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				// 计算一笔交易的时间间隔
				long oneTransTcell = mon_cycle*1000 / excuteTime;
				
				//随机获取成功率
				float sysSuccRate = DataSimuUtils.getTwoBitFloat(DataSimuUtils.getRandomFromRange(service.getSsrRange()));
				float bussSuccRate = DataSimuUtils.getTwoBitFloat(DataSimuUtils.getRandomFromRange(service.getBsrRange()));
				
				for (int j = 0; j < excuteTime; j++) {
					//随机获取esbapp
					EsbApp esbapp = esbAppQueue.poll();
					LOOP = esbapp.getLoop();
					esbAppQueue.offer(esbapp);
					
					//随机获取该提供方系统的某台ip
					prvdip = ProviderSystem.getRandomPrdIps();
					
					//获取交易状态信息
					if (new Random().nextInt(100) < sysSuccRate) {
						// 系统成功
						RESPSTATUS = "1"; // 交易状态码
						if (new Random().nextInt(100) < bussSuccRate) {
							// 业务成功
							RESPCODE = "000000"; // 交易响应码
							RESPMSG = "交易成功"; // 交易响应结果
						} else {
							// 业务失败
							//随机获取交易异常响应码及响应信息
							ArrayList<ESBException> beList = exptMap.get("be"); 
							int rdnum1 = new Random().nextInt(beList.size()); 
							RESPCODE = beList.get(rdnum1).getCode(); 
							RESPMSG = beList.get(rdnum1).getDescription(); 
						}
					} else {
						//系统失败
						RESPSTATUS = "0"; // 交易状态码
						ArrayList<ESBException> seList = exptMap.get("se"); 
						int rdnum2 = new Random().nextInt(seList.size()); 
						RESPCODE = seList.get(rdnum2).getCode(); 
						RESPMSG = seList.get(rdnum2).getDescription(); 
					}

					// 按比例随机产生交易调用方
					int random = new Random().nextInt(service.getChannels().size());
					LOGICCHANNEL = service.getChannels().get(random);
					CHANNELIP = ConsumerSystemMap.get(service.getChannels().get(random)).getRandomComIps();

					/*计算四步时间*/
					//esb内部耗时
					int esb_interval = (int) DataSimuUtils.getRandomFromRange(service.getEsbTimeRange());
					//交易总耗时
					int buss_interval = (int) DataSimuUtils.getRandomFromRange(service.getBussTimeRange());
					//后端耗时
					int prvd_interval = buss_interval - esb_interval;
					
					int interval0_1 = (int) DataSimuUtils.getRandomFromRange("1-2");
					int interval1_2 = (int) DataSimuUtils.getRandomHalfValue(esb_interval);
					int interval3_4 = esb_interval-interval1_2;

					//交易发生时间
					String FLAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(curr));
					long tempCurr = curr;
					tempCurr += interval0_1;
					String time1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(tempCurr));
					tempCurr += interval1_2;
					String time2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(tempCurr));
					tempCurr += prvd_interval;
					String time3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(tempCurr));
					tempCurr += interval3_4;
					String time4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(tempCurr));
					tempCurr += new Random().nextInt(1);
					//入库时间
					String OPERSTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(tempCurr));
					
					//请求方流水号
					String reqflowNo = "req" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
					//全局流水号
					String preflowNo = "pre" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
					// esb内部流水号
					ESBFLOWNO = LOOP + "-" + UUID.randomUUID().toString();
					
					StringBuilder sql = new StringBuilder();
					String dbType = DataSimuConfig.getStringProperty("db.type","oracle");
					if (dbType.equals("oracle")) {
						sql.append("INSERT INTO esb2_trans_log");
						sql.append(" (ESBFLOWNO, FLOWSTEPID, ESBSERVICEFLOWNO, ESBSERVSEQU, REQFLOWNO, RESPFLOWNO, SERVICETYPE, TRANSTAMP1,TRANSTAMP2,TRANSTAMP3,TRANSTAMP4, LOCATIONID, CHANNELID, SERVICEID, RESPSTATUS, RESPCODE, RESPMSG, OPERSTAMP, PREFLOWNO, POSTFLOWNO, LOGICCHANNEL, REALCHANNEL, SERVICEFLOW, LOGICSYSTEM, REALSYSTEM, LOOP, FLAT_DATE, BUSINESSRESPSTATUS, BUSINESSRESPCODE, REQMSGLENTH, RESPMSGLENTH, CHANNELIP, SYSTEMIP, spare1, spare2, spare3)");
						sql.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					} else if (dbType.equals("mysql")) {
						String newdate = new SimpleDateFormat("yyyyMMdd").format(new Date());
						sql.append("INSERT INTO esb2_trans_log_" + newdate);
						sql.append(" (ESBFLOWNO, FLOWSTEPID, ESBSERVICEFLOWNO, ESBSERVSEQU, REQFLOWNO, RESPFLOWNO, SERVICETYPE, TRANSTAMP1,TRANSTAMP2,TRANSTAMP3,TRANSTAMP4, LOCATIONID, CHANNELID, SERVICEID, RESPSTATUS, RESPCODE, RESPMSG, OPERSTAMP, PREFLOWNO, POSTFLOWNO, LOGICCHANNEL, REALCHANNEL, SERVICEFLOW, LOGICSYSTEM, REALSYSTEM, `LOOP`, FLAT_DATE, BUSINESSRESPSTATUS, BUSINESSRESPCODE, REQMSGLENTH, RESPMSGLENTH, CHANNELIP, SYSTEMIP, spare1, spare2, spare3)");
						sql.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,str_to_date(?, '%Y-%m-%d %H:%i:%s'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					}
					String[] params = { ESBFLOWNO, "4", ESBFLOWNO, null,reqflowNo, null, "", time1, time2, time3, time4,
							"local_in", LOGICCHANNEL, service.getServiceName(), RESPSTATUS, RESPCODE, RESPMSG, OPERSTAMP, preflowNo,
							ESBFLOWNO, LOGICCHANNEL, LOGICCHANNEL, "", prvdid, prvdid, LOOP, FLAT_DATE,
							"0", "13", "823", "300", CHANNELIP, prvdip,null, null, null };
					jdbc.insert(sql.toString(), params);
					loop++;
					
					long endTime = System.currentTimeMillis();
					// 判断本次循环是否超过30秒
					if (endTime - startTime >= 30000) {
						log.warn("服务" + service.getServiceName() + "循环超时，即将进入下一个周期");
						isTimeOut = true;
						break;
					}
					log.debug("成功模拟一笔交易，交易流水号："+ESBFLOWNO+"，交易时间："+FLAT_DATE);
					curr += oneTransTcell;
				}

				if (isTimeOut) {
					break;
				}
				
				log.debug("服务"+service.getServiceName()+"的数据模拟完成！");
			}
			
			jdbc.closeAll();
			long endTime = System.currentTimeMillis();
			while (endTime - startTime <= 30000) {
				// 本周期循环结束，等待到下一周期
				try {
					long sleeptime = 30000 - endTime + startTime;
					log.info("当前周期数据模拟完毕,共模拟"+loop+"笔,睡眠"+sleeptime+"ms,等待下一周期");
					Thread.sleep(sleeptime);
					break;
				} catch (InterruptedException e) {
					log.error("线程中断！", e);
				}
			}
		}
	}

	/**
	 * 获取异常服务数据对象
	 * @param exptData
	 * @param oldService
	 * @return
	 */
	private Service getExptService(ExptData exptData, Service oldService) {
		Service newService = new Service();
		newService.setServiceName(oldService.getServiceName());
		newService.setEsbTimeRange(exptData.getEtRange());
		newService.setBussTimeRange(exptData.getTtRange());
		newService.setSsrRange(exptData.getSsrRange());
		newService.setBsrRange(exptData.getTsrRange());
		newService.setTpsRangeList(exptData.getTpsRangeList());
		return newService;
	}

}

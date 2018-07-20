package com.dc.esb.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import com.dc.esb.conf.DataSimuConfig;
import com.dc.esb.utils.TCPConnector;

public class SendRsmInfo implements Runnable {

	private static Logger log = Logger.getLogger(SendRsmInfo.class);
	public void run() {
		while (true) {
			
			for(int i=1;i<7;i++) {
				String loop = "AP"+i;
				new SendTask(parse(loop),DataSimuConfig.getStringProperty("mon.system.url", "127.0.0.1:9898"),3000,3000,"utf-8").run();
				log.info("服务器[" + loop + "]的资源数据模拟完成!");
			}

			try {
				log.info("开始睡眠10s!");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				log.error("模拟资源采集器的线程中断！");
			}
		}

	}
	
	private StringBuffer parse(String loop) {
		String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		int idle = new Random().nextInt(80);
		int rfmb = new Random().nextInt(500);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<esb_rsm servName=\"").append(loop).append("\" monTime=\"").
		append(time).append("\"><cpu><item user=\"7.7\" sys=\"10.2\" wait=\"0\" idle=\"")
		.append(idle).append("\" cpunum=\"8\"></item></cpu>")
		.append("<mem><item rfree=\"1.67861\" vfree=\"77.3613\" rfmb=\"").append(rfmb)
		.append("\" vfmb=\"8088.9\" rtmb=\"15950.7\" vtmb=\"10456\"></item></mem>")
		.append("</esb_rsm>");
		
		return sb;
	}

	class SendTask implements Runnable {
		private StringBuffer reqData;
		private String address;
		private int connTimeout;
		private int readTimeout;
		private String encoding;

		public SendTask(StringBuffer reqData, String address, int connTimeout, int readTimeout, String encoding) {
			this.reqData = reqData;
			this.address = address;
			this.connTimeout = connTimeout;
			this.readTimeout = readTimeout;
			this.encoding = encoding;
		}

		public void run() {
			byte[] rspData = null;
			String result = null;
			TCPConnector connector = new TCPConnector(address);
			
			try {
				connector.setConnTimeout(connTimeout);
				connector.setReadTimeout(readTimeout);

				rspData = connector.send(reqData.toString().getBytes(encoding).length,
						reqData.toString().getBytes(encoding));
				try {
					result = new String(rspData, encoding);
				} catch (UnsupportedEncodingException e) {
					result = new String(rspData);
				}
				
			} catch (ConnectException ce) {
				System.err.println(ce);
				log.error("连接监控失败,请检查配置与监控平台状态!");
				System.exit(1);
			} catch (IOException e) {
				System.err.println(e);
				log.error("数据发送异常!返回结果[" + result + "]");
				return;
			}

			log.debug("数据发送完成!返回结果[" + result + "]");
		}
	}
}

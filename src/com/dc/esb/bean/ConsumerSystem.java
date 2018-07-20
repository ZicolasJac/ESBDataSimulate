package com.dc.esb.bean;

import java.util.List;
import java.util.Random;

public class ConsumerSystem {

	//渠道id
	private String channelId;
	//渠道名
	private String name;
	//渠道ip
	private String[] channelIp;
	//调用方占比之比例区间
	private List<Integer> interval;
	
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRandomComIps() {
		return this.channelIp[new Random().nextInt(channelIp.length)];
	}
	public void setComIps(String comIps) {
		this.channelIp = comIps.split(",");
	}
	public String[] getChannelIp() {
		return channelIp;
	}
	public void setChannelIp(String[] channelIp) {
		this.channelIp = channelIp;
	}
	public List<Integer> getInterval() {
		return interval;
	}
	public void setInterval(List<Integer> interval) {
		this.interval = interval;
	}
	
}

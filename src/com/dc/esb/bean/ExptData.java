package com.dc.esb.bean;

import java.util.Date;
import java.util.List;

/**
 * 异常历史数据设置类
 * @author ZJC
 *
 */
public class ExptData {
	
	//异常时间段始
	private Date starttime;
	//异常时间段末
	private Date stoptime;
	//系统成功率范围
	private String ssrRange;
	//业务成功率范围
	private String tsrRange;
	//esb内部平均耗时范围
	private String etRange;
	//交易平均耗时范围
	private String ttRange;
	//tps范围
	private List<String[]> tpsRangeList;
	//异常服务id列表
	private List<String> exptServices;
	
	public Date getStarttime() {
		return starttime;
	}
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
	public Date getStoptime() {
		return stoptime;
	}
	public void setStoptime(Date stoptime) {
		this.stoptime = stoptime;
	}
	public String getSsrRange() {
		return ssrRange;
	}
	public void setSsrRange(String ssrRange) {
		this.ssrRange = ssrRange;
	}
	public String getTsrRange() {
		return tsrRange;
	}
	public void setTsrRange(String tsrRange) {
		this.tsrRange = tsrRange;
	}
	public String getEtRange() {
		return etRange;
	}
	public void setEtRange(String etRange) {
		this.etRange = etRange;
	}
	public String getTtRange() {
		return ttRange;
	}
	public void setTtRange(String ttRange) {
		this.ttRange = ttRange;
	}
	
	public List<String[]> getTpsRangeList() {
		return tpsRangeList;
	}
	public void setTpsRangeList(List<String[]> tpsRangeList) {
		this.tpsRangeList = tpsRangeList;
	}
	public List<String> getExptServices() {
		return exptServices;
	}
	public void setExptServices(List<String> exptServices) {
		this.exptServices = exptServices;
	}
	
	
}

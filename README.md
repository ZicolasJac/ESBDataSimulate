# ESBDataSimulate
ESB数据模拟工具

功能简介：实时模拟监控平台所需的ESB相关数据

模块：1.模拟服务器资源数据模块
	  2.模拟ESB交易流水数据模块
	  
使用向导：
	1.在/ESBDataSimulate/conf/datasimu.properties中配置数据库连接
	2.在/ESBDataSimulate/conf/datasimu.properties中配置监控平台地址
	3.在/ESBDataSimulate/conf/serviceData.xml中配置ESB的渠道、提供方系统、服务、TPS、异常数据等相关数据
	
	注：如果要部署至linux平台：1.使用ant编译执行/ESBDataSimulate/build.xml脚本，生成部署包（位于/ESBDataSimulate/dest下）
							   2.拷贝部署包至服务器，解压--授权
							   3.运行启动脚本start.sh
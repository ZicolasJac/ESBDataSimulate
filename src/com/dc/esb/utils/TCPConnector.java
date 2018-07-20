package com.dc.esb.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class TCPConnector {
	
	private String address;
	private String[] ip;
	private int[] port;
	private int readTimeout;
	private int connTimeout;
	private Random rand = new Random();
	
	public TCPConnector(String address) {
		this(address, 30000, 10000);
	}
	
	public TCPConnector(String address, int readTimeout) {
		this(address, readTimeout, 10000);
	}
	
	public TCPConnector(String address, int readTimeout, int connTimeout) {
		this.address = address.trim();
		String[] addresses = this.address.split(",");
		ip = new String[addresses.length];
		port = new int[addresses.length];
		for(int i=0;i<addresses.length;i++) {
			int index = addresses[i].indexOf(":");
			this.ip[i] = addresses[i].substring(0,index);
			this.port[i] = Integer.parseInt(addresses[i].substring(index+1));
		}
		this.readTimeout = readTimeout;
		this.connTimeout = connTimeout;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getConnTimeout() {
		return connTimeout;
	}

	public void setConnTimeout(int connTimeout) {
		this.connTimeout = connTimeout;
	}

	public byte[] send(byte[] data) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		ByteArrayOutputStream baos = null;
		Socket socket = new Socket();
		try {
			int index = 0;
			if(ip.length>1) {
				index = rand.nextInt(ip.length);
			}
			SocketAddress sa = new InetSocketAddress(ip[index],port[index]);
			socket.connect(sa, connTimeout);
			socket.setSoTimeout(readTimeout);
			
			os = socket.getOutputStream();
			is = socket.getInputStream();
			
			os.write(data);
			os.flush();
			socket.shutdownOutput();
			
			int len = 0;
			byte[] tmp = new byte[1024];
			baos = new ByteArrayOutputStream();
			while((len=is.read(tmp))!=-1) {
				baos.write(tmp, 0, len);
			}
			baos.close();
			socket.shutdownInput();
			
		} catch (UnknownHostException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if(os!=null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if(socket!=null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		
		return baos.toByteArray();
	}
	
	public byte[] send(int headLen, byte[] data) throws IOException {
		InputStream is = null;
		DataOutputStream os = null;
		ByteArrayOutputStream baos = null;
		Socket socket = new Socket();
		try {
			int index = 0;
			if(ip.length>1) {
				index = rand.nextInt(ip.length);
			}
			SocketAddress sa = new InetSocketAddress(ip[index],port[index]);
			socket.connect(sa, connTimeout);
			socket.setSoTimeout(readTimeout);
			
			os = new DataOutputStream(socket.getOutputStream());
			is = socket.getInputStream();

			os.writeInt(headLen);
			os.write(data,0,headLen);
			os.flush();
			
			int len = 0;
			byte[] tmp = new byte[1024];
			baos = new ByteArrayOutputStream();
			while((len=is.read(tmp))!=-1) {
				baos.write(tmp, 0, len);
			}
			baos.close();
			
		} catch (UnknownHostException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if(os!=null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if(socket!=null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		
		return "ok".getBytes();
	}
}

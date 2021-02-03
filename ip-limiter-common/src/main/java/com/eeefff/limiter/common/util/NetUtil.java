package com.eeefff.limiter.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fenglibin
 *
 */
@Slf4j
public class NetUtil {

	/***
	 * true:already in using false:not using
	 * 
	 * @param port
	 */
	public static boolean isLoclePortUsing(int port) {
		boolean flag = true;
		try {
			flag = isPortUsing("127.0.0.1", port);
		} catch (Exception e) {
		}
		return flag;
	}

	/***
	 * true:already in using false:not using
	 * 
	 * @param host
	 * @param port
	 * @throws UnknownHostException
	 */
	public static boolean isPortUsing(String host, int port) throws UnknownHostException {
		boolean flag = false;
		Socket socket = null;
		InetAddress theAddress = InetAddress.getByName(host);
		try {
			socket = new Socket(theAddress, port);
			if (socket.isBound()) {
				flag = true;
			}
		} catch (IOException e) {
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
			}
		}
		return flag;
	}

	/**
	 * 获取Linux下的IP地址
	 *
	 * @return IP地址
	 * @throws SocketException
	 */
	public static String getLinuxLocalIp() {
		String ip = "";
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				String name = intf.getName();
				if (!name.startsWith("docker") && !name.startsWith("lo") && !name.startsWith("br-")) {
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							String ipaddress = inetAddress.getHostAddress().toString();
							if (!ipaddress.contains("::") && !ipaddress.contains("0:0:")
									&& !ipaddress.contains("fe80")) {
								ip = ipaddress;
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			ip = "192.168.0.1";
			log.error("获取当前服务器的ＩＰ地址发生异常：" + e.getMessage(), e);
		}
		return ip;
	}

	/**
	 * 获取服务器的名称
	 * 
	 * @return
	 */
	public static String getHostName() {
		try {
			Process p = Runtime.getRuntime().exec("hostname");
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			if ((line = br.readLine()) != null) {
				return line;
			}
			br.close();
			p.destroy();
			return line;

		} catch (IOException e) {
			log.error("获取服务器名称发生异常:" + e.getMessage(), e);
			return "";
		}
	}
}

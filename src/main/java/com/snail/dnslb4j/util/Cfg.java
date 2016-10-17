package com.snail.dnslb4j.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author pengmeng
 */
public final class Cfg {

	public static ConfigLoader config;
	private static String prePath = "development";
	private static final String VERSION = "1.0.0";

	public static String getVersion() {
		return VERSION;
	}

	public static String prePath(String path) {
		return "config/" + prePath + "/" + path;
	}

	public static String config(String key) {
		if (config == null) {
			String path = !"classes".equals(new File(Jar.getPath(Cfg.class)).getName()) ? Jar.getPath(Cfg.class) + "/" + prePath("") : prePath("");
			path = new File(path).getAbsolutePath();
			Log.logger().info("use config file [ " + path + "/config.ini ]");
			config = new ConfigLoader(path, false, true)
				.setCfgFilename("config.ini", false);
		}

		String value = config.getValue(key);
		return value == null ? "" : value;
	}

	public static Integer configInt(String key) {
		return Integer.valueOf(config(key));
	}

	public static Boolean configBoolean(String key) {
		return Boolean.valueOf(config(key));
	}

	public static String getConfigIniFilePath() {
		return prePath("config.ini");
	}

	public static String getLogbackXmlFilePath() {
		return prePath("logback.xml");
	}

	public static String getErrorLogFilePath() {
		return "log/error.log";
	}

	public static String getLogFilePath(String name) {
		return "log/" + name + ".log";
	}

	public static void setPrePath(String aPrePath) {
		prePath = aPrePath;
	}

	public static void main(String[] args) {
		System.out.println(getBackendDns());
	}

	public static ArrayList<ConcurrentHashMap<String, String>> getBackendDns() {
		String[] dns0 = config("backend_dns").split(",");
		ArrayList<ConcurrentHashMap<String, String>> dns1 = new ArrayList<>();

		for (String string : dns0) {
			String[] dns2 = string.split(":");
			ConcurrentHashMap<String, String> dns3 = new ConcurrentHashMap<>();
			dns3.put("hostname", dns2[0]);
			dns3.put("port", dns2.length >= 2 ? dns2[1] : "53");
			dns1.add(dns3);
		}
		return dns1;
	}

	public static ArrayList<ConcurrentHashMap<String, String>> getBackupDns() {
		String[] dns0 = config("backup_dns").split(",");
		ArrayList<ConcurrentHashMap<String, String>> dns1 = new ArrayList<>();

		for (String string : dns0) {
			String[] dns2 = string.split(":");
			ConcurrentHashMap<String, String> dns3 = new ConcurrentHashMap<>();
			dns3.put("hostname", dns2[0]);
			dns3.put("port", dns2.length >= 2 ? dns2[1] : "53");
			dns1.add(dns3);
		}
		return dns1;
	}
}

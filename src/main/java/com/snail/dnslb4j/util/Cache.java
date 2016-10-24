package com.snail.dnslb4j.util;

import java.io.UnsupportedEncodingException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

public class Cache {

	private static final net.sf.ehcache.Cache CACHE;

	static {
		CacheConfiguration configuration = new CacheConfiguration();
		configuration.setName("test");
		configuration.setEternal(false);
		configuration.setLogging(false);
		configuration.setOverflowToDisk(false);
		configuration.setMaxBytesLocalHeap(50 * 1024 * 1024l);
		configuration.setTimeToLiveSeconds(Cfg.configInt("cache_time"));
		configuration.setTimeToIdleSeconds(0);
		CacheManager manager = new CacheManager();
		manager.addCache(new net.sf.ehcache.Cache(configuration));
		CACHE = manager.getCache("test");
	}

	public static void set(String key, String data) {
		if (!isCacheOn()) {
			return;
		}
		CACHE.put(new Element(key, data));
	}

	public static String get(String key) {
		if (!isCacheOn()) {
			return "";
		}
		Object val = CACHE.get(key);
		return val != null ? CACHE.get(key).getObjectValue().toString() : "";
	}

	public static void set(String key, byte[] data) {
		if (!isCacheOn()) {
			return;
		}
		Log.logger().trace("set cache key:[" + key + "] value:" + data.toString());
		CACHE.put(new Element(key, bytes2chars(data)));
	}

	public static byte[] getBytes(String key) {
		Object val = CACHE.get(key);
		Log.logger().trace("get cache key:[" + key + "] value:" + val);
		if (val != null) {
			System.err.println(CACHE.get(key).getObjectValue());
		}
		return val != null ? chars2bytes(CACHE.get(key).getObjectValue().toString()) : null;
	}

	private static boolean isCacheOn() {
		return Cfg.configBoolean("cache");
	}

	private static String bytes2chars(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length);
		for (int i = 0; i < bytes.length; ++i) {
			sb.append((char) bytes[i]);
		}
		return sb.toString();
	}

	private static byte[] chars2bytes(String chars) {
		try {
			return chars.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException ex) {
		}
		return null;
	}

	public static void main(String[] args) {
		set("test", "xxx");
		System.out.println(get("test"));
	}
}

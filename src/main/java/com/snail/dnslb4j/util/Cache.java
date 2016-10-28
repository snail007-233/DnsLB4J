package com.snail.dnslb4j.util;

import jodd.cache.TimedCache;

public class Cache {

	private static final TimedCache<String, String> cache = new TimedCache<>(600 * 1000);

	public static void set(String key, String data) {
		cache.put(key, data, Cfg.configInt("ttl_min_seconds") * 1000);

	}

	public static void set(String key, String data, int timeoutSeconds) {
		cache.put(key, data, timeoutSeconds * 1000);

	}

	public static String get(String key) {
		String val = cache.get(key);
		return val == null ? "" : val;
	}
}

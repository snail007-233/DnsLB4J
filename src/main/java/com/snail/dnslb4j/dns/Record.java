package com.snail.dnslb4j.dns;

public final class Record {

	public static final int TTL_MIN_SECONDS = 600;

	/**
	 * A 记录 类型
	 */
	public static final int TYPE_A = Packet.QUERY_TYPE_A;

	/**
	 * CName 类型
	 */
	public static final int TYPE_CNAME = Packet.QUERY_TYPE_CNAME;
	
	/**
	 * 具体的值，A 记录时为IP，CName时为指向的域名
	 */
	public final String value;

	/**
	 * 记录类型，A或者CName
	 */
	public final int type;

	/**
	 * TTL dns结果缓存时间
	 */
	public final int ttl;

	/**
	 * 时间戳，用来判断超时
	 */
	public final long timeStamp;

	public Record(String value, int type, int ttl, long timeStamp) {
		this.value = value;
		this.type = type;
		this.ttl = ttl < TTL_MIN_SECONDS ? TTL_MIN_SECONDS : ttl;
		this.timeStamp = timeStamp;
	}

	public boolean isA() {
		return type == TYPE_A;
	}

	public boolean isCname() {
		return type == TYPE_CNAME;
	}

	public boolean isExpired() {
		return isExpired(System.currentTimeMillis() / 1000);
	}

	public boolean isExpired(long time) {
		return timeStamp + ttl < time;
	}
}

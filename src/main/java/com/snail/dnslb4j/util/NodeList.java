
package com.snail.dnslb4j.util;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author snail
 */
public class NodeList {

	private final ArrayList<ConcurrentHashMap<String, String>> list;
	public static final int TYPE_BACKUP = 1;
	public static final int TYPE_BACKEND = 2;
	private int TYPE = 0;

	public NodeList(ArrayList<ConcurrentHashMap<String, String>> list, int type) {
		this.list=list;
		TYPE = type;
	}

	public ArrayList<ConcurrentHashMap<String, String>> getList() {
		return list;
	}

	public int getTYPE() {
		return TYPE;
	}

}

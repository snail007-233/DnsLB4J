package com.snail.dnslb4j.dns;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

public class Reader {

	public static Record[] parseResponse(byte[] response) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(response);
		DataInputStream dis = new DataInputStream(bis);
		int answerId = dis.readUnsignedShort();

		int header = dis.readUnsignedShort();

		int questionCount = dis.readUnsignedShort();
		int answerCount = dis.readUnsignedShort();
//        nameserver Count
		dis.readUnsignedShort();
//        additionalResourceRecordCount
		dis.readUnsignedShort();

//         ignore questions
		readQuestions(dis, response, questionCount);

		return readAnswers(dis, response, answerCount);
//        ignore auth
//        ignore additional
	}

	private static Record[] readAnswers(DataInputStream dis, byte[] data, int count) throws Exception {
		int offset = 0;
		Record[] ret = new Record[count];
		while (count-- > 0) {
			ret[offset++] = readRecord(dis, data);
		}
		return ret;
	}

	private static Record readRecord(DataInputStream dis, byte[] data) throws Exception {
		readName(dis, data);
		int type = dis.readUnsignedShort();
//            class
		dis.readUnsignedShort();

		long ttl = (((long) dis.readUnsignedShort()) << 16)
			+ dis.readUnsignedShort();
		int payloadLength = dis.readUnsignedShort();
		String payload = null;
		switch (type) {
			case Record.TYPE_A:
				byte[] ip = new byte[4];
				dis.readFully(ip);
				payload = InetAddress.getByAddress(ip).getHostAddress();
				break;
			case Record.TYPE_CNAME:
				payload = readName(dis, data);
				break;
			default:
				payload = null;
				for (int i = 0; i < payloadLength; i++) {
					dis.readByte();
				}
				break;
		}
		if (payload == null) {
			throw new UnknownHostException("no record");
		}
		return new Record(payload, type, (int) ttl, System.currentTimeMillis() / 1000);
	}

	private static void readQuestions(DataInputStream dis, byte[] data, int count) throws Exception {
		while (count-- > 0) {
			readName(dis, data);
//            type
			dis.readUnsignedShort();
//            class
			dis.readUnsignedShort();
		}
	}

	/**
	 * Parse a domain name starting at the current offset and moving the
	 * input stream pointer past this domain name (even if cross references
	 * occure).
	 *
	 * @param dis The input stream.
	 * @param data The raw data (for cross references).
	 * @return The domain name string.
	 * @throws IOException Should never happen.
	 */
	private static String readName(DataInputStream dis, byte[] data)
		throws Exception {
		int c = dis.readUnsignedByte();
		if ((c & 0xc0) == 0xc0) {
			c = ((c & 0x3f) << 8) + dis.readUnsignedByte();
			HashSet<Integer> jumps = new HashSet<Integer>();
			jumps.add(c);
			return readName(data, c, jumps);
		}
		if (c == 0) {
			return "";
		}
		byte[] b = new byte[c];
		dis.readFully(b);
		String s = IDN.toUnicode(new String(b));
		String t = readName(dis, data);
		if (t.length() > 0) {
			s = s + "." + t;
		}
		return s;
	}

	/**
	 * Parse a domain name starting at the given offset.
	 *
	 * @param data The raw data.
	 * @param offset The offset.
	 * @param jumps The list of jumps (by now).
	 * @return The parsed domain name.
	 * @throws IOException on cycles.
	 */
	private static String readName(
		byte[] data,
		int offset,
		HashSet<Integer> jumps
	) throws Exception {
		int c = data[offset] & 0xff;
		if ((c & 0xc0) == 0xc0) {
			c = ((c & 0x3f) << 8) + (data[offset + 1] & 0xff);
			if (jumps.contains(c)) {
				throw new Exception("Cyclic offsets detected.");
			}
			jumps.add(c);
			return readName(data, c, jumps);
		}
		if (c == 0) {
			return "";
		}
		String s = new String(data, offset + 1, c);
		String t = readName(data, offset + 1 + c, jumps);
		if (t.length() > 0) {
			s = s + "." + t;
		}
		return s;
	}

}

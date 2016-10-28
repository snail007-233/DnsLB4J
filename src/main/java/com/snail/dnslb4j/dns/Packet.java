package com.snail.dnslb4j.dns;

import com.snail.dnslb4j.util.DnsNodeManager;
import com.snail.dnslb4j.util.Misc;
import com.snail.dnslb4j.util.RequestSuccessCallback;
import com.snail.dnslb4j.util.RequestTimeoutCallback;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

public class Packet {

	private final int packetSize = 512 * 8;
	private BitSet packetBitSet = new BitSet(packetSize);
	private int QUESTION_END_OFFSET = 0;
	private int ANSWER_END_OFFSET = 0;

	private final int ID_OFFSET = 0;
	private final int ID_BIT_LENGTH = 16;
	private final int QUESTION_COUNT_OFFSET = 32;
	private final int QUESTION_COUNT_BIT_LENGTH = 16;
	private final int ANSWER_COUNT_OFFSET = 48;
	private final int ANSWER_COUNT_BIT_LENGTH = 16;
	private final int AUTHORITY_COUNT_OFFSET = 64;
	private final int AUTHORITY_COUNT_BIT_LENGTH = 16;
	private final int ADDITIONAL_COUNT_OFFSET = 80;
	private final int ADDITIONAL_COUNT_BIT_LENGTH = 16;
	private final int QUESTION_OFFSET = 96;
	private static final Random RNADOM = new Random();
	//header key infomation
	private static final int QR_OFFSET = 16;
	private static final int QR_BIT_LENGTH = 1;
	private static final int OPCODE_OFFSET = 17;
	private static final int OPCODE_BIT_LENGTH = 4;
	private static final int AA_OFFSET = 21;
	private static final int AA_BIT_LENGTH = 1;
	private static final int TC_OFFSET = 22;
	private static final int TC_BIT_LENGTH = 1;
	private static final int RD_OFFSET = 23;
	private static final int RD_BIT_LENGTH = 1;
	private static final int RA_OFFSET = 24;
	private static final int RA_BIT_LENGTH = 1;
	private static final int ZERO_OFFSET = 25;
	private static final int ZERO_BIT_LENGTH = 3;
	private static final int RCODE_OFFSET = 28;
	private static final int RCODE_BIT_LENGTH = 4;
	//header key's value
	public static final int QR_QUERY = 0;
	public static final int QR_ANSWER = 1;
	public static final int OPCODE_IP = 0;
	public static final int OPCODE_DOMAIN = 1;
	public static final int OPCODE_STATUS = 1;
	public static final int AA_AUTH = 1;
	public static final int AA_NON_AUTH = 0;
	public static final int TC_TRUNCATE = 1;
	public static final int TC_NON_TRUNCATE = 0;
	public static final int RD_RECURSION = 1;
	public static final int RD_NON_RECURSION = 0;
	public static final int RA_RECURSION_SUPPORT = 1;
	public static final int RA_NON_RECURSION_SUPPORT = 0;
	public static final int ZERO = 0;
	public static final int RCODE_OK = 0;
	public static final int RCODE_ERROR_FORMAT = 1;
	public static final int RCODE_ERROR_SERVER = 2;
	public static final int RCODE_ERROR_NAME = 3;
	public static final int RCODE_ERROR_NOT_IMPLEMENTED = 4;
	public static final int RCODE_ERROR_REFUSED = 5;
	//query class
	public static final int QUERY_CLASS_INTERNET = 1;
	public static final int QUERY_CLASS_CHAOS = 3;
	public static final int QUERY_CLASS_HESIOD = 4;
	public static final int QUERY_CLASS_ANY = 255;
	//query type
	public static final int QUERY_TYPE_A = 1;
	public static final int QUERY_TYPE_NS = 2;
	public static final int QUERY_TYPE_CNAME = 5;
	public static final int QUERY_TYPE_SOA = 6;
	public static final int QUERY_TYPE_WKS = 11;
	public static final int QUERY_TYPE_PTR = 12;
	public static final int QUERY_TYPE_HINFO = 13;
	public static final int QUERY_TYPE_MINFO = 14;
	public static final int QUERY_TYPE_MX = 15;
	public static final int QUERY_TYPE_TXT = 16;
	public static final int QUERY_TYPE_AAAA = 28;
	public static final int QUERY_TYPE_ALL = 255;

	public Packet() {

	}

	public Packet setQuery() {
		qr(QR_QUERY)
			.opcode(OPCODE_IP)
			.aa(AA_NON_AUTH)
			.tc(TC_NON_TRUNCATE)
			.rd(RD_RECURSION)
			.ra(RA_NON_RECURSION_SUPPORT)
			.zero(ZERO)
			.rcode(RCODE_OK)
			.questionCount(0)
			.answerCount(0)
			.authorityCount(0)
			.additonalCount(0);
		return this;
	}

	public Packet setAnswer() {
		qr(QR_ANSWER)
			.opcode(OPCODE_IP)
			.aa(AA_NON_AUTH)
			.tc(TC_NON_TRUNCATE)
			.rd(RD_RECURSION)
			.ra(RA_RECURSION_SUPPORT)
			.zero(ZERO)
			.rcode(RCODE_OK)
			.questionCount(0)
			.answerCount(0)
			.authorityCount(0)
			.additonalCount(0);
		return this;
	}

	public Packet(byte[] data) {
		packetBitSet = Misc.setBitsetBytes(data, packetBitSet, packetSize, ID_OFFSET);
		if (qr() == QR_QUERY) {
			QUESTION_END_OFFSET = data.length - 1;
		} else {
			ANSWER_END_OFFSET = data.length - 1;
		}
	}

	public int id() {
		return Misc.getBitsIntInBitset(packetBitSet, ID_OFFSET, ID_BIT_LENGTH);
	}

	public Packet id(int id) {
		Misc.setBitsIntInBitset(packetBitSet, id, ID_OFFSET, ID_BIT_LENGTH);
		return this;
	}

	public Packet randomId() {
		int id;
		synchronized (RNADOM) {
			id = RNADOM.nextInt(65535);
		}
		id(id);
		return this;
	}

	public int qr() {
		return Misc.getBitsIntInBitset(packetBitSet, QR_OFFSET, QR_BIT_LENGTH);
	}

	public Packet qr(int value) {
		Misc.setBitsIntInBitset(packetBitSet, value, QR_OFFSET, QR_BIT_LENGTH);
		return this;
	}

	public int opcode() {
		return Misc.getBitsIntInBitset(packetBitSet, OPCODE_OFFSET, OPCODE_BIT_LENGTH);
	}

	public Packet opcode(int value) {
		Misc.setBitsIntInBitset(packetBitSet, value, OPCODE_OFFSET, OPCODE_BIT_LENGTH);
		return this;
	}

	public int aa() {
		return Misc.getBitsIntInBitset(packetBitSet, AA_OFFSET, AA_BIT_LENGTH);
	}

	public Packet aa(int value) {
		Misc.setBitsIntInBitset(packetBitSet, value, AA_OFFSET, AA_BIT_LENGTH);
		return this;
	}

	public int tc() {
		return Misc.getBitsIntInBitset(packetBitSet, TC_OFFSET, TC_BIT_LENGTH);
	}

	public Packet tc(int value) {
		Misc.setBitsIntInBitset(packetBitSet, value, TC_OFFSET, TC_BIT_LENGTH);
		return this;
	}

	public int rd() {
		return Misc.getBitsIntInBitset(packetBitSet, RD_OFFSET, RD_BIT_LENGTH);
	}

	public Packet rd(int value) {
		Misc.setBitsIntInBitset(packetBitSet, value, RD_OFFSET, RD_BIT_LENGTH);
		return this;
	}

	public int ra() {
		return Misc.getBitsIntInBitset(packetBitSet, RA_OFFSET, RA_BIT_LENGTH);
	}

	public Packet ra(int value) {
		Misc.setBitsIntInBitset(packetBitSet, value, RA_OFFSET, RA_BIT_LENGTH);
		return this;
	}

	public int zero() {
		return Misc.getBitsIntInBitset(packetBitSet, ZERO_OFFSET, ZERO_BIT_LENGTH);
	}

	public Packet zero(int code) {
		Misc.setBitsIntInBitset(packetBitSet, code, ZERO_OFFSET, ZERO_BIT_LENGTH);
		return this;
	}

	public int rcode() {
		return Misc.getBitsIntInBitset(packetBitSet, RCODE_OFFSET, RCODE_BIT_LENGTH);
	}

	public Packet rcode(int value) {
		Misc.setBitsIntInBitset(packetBitSet, value, RCODE_OFFSET, RCODE_BIT_LENGTH);
		return this;
	}

	public byte[] getBytes() {
		int offset = qr() == QR_QUERY ? QUESTION_END_OFFSET : ANSWER_END_OFFSET;
		return Misc.getBitsetBytes(packetBitSet, packetSize, ID_OFFSET, (offset - ID_OFFSET + 1) / 8);
	}

	public int questionCount() {
		return Misc.getBitsIntInBitset(packetBitSet, QUESTION_COUNT_OFFSET, QUESTION_COUNT_BIT_LENGTH);
	}

	public Packet questionCount(int count) {
		Misc.setBitsIntInBitset(packetBitSet, count, QUESTION_COUNT_OFFSET, QUESTION_COUNT_BIT_LENGTH);
		return this;
	}

	public int answerCount() {
		return Misc.getBitsIntInBitset(packetBitSet, ANSWER_COUNT_OFFSET, ANSWER_COUNT_BIT_LENGTH);
	}

	public Packet answerCount(int count) {
		Misc.setBitsIntInBitset(packetBitSet, count, ANSWER_COUNT_OFFSET, ANSWER_COUNT_BIT_LENGTH);
		return this;
	}

	public int authorityCount() {
		return Misc.getBitsIntInBitset(packetBitSet, AUTHORITY_COUNT_OFFSET, AUTHORITY_COUNT_BIT_LENGTH);
	}

	public Packet authorityCount(int count) {
		Misc.setBitsIntInBitset(packetBitSet, count, AUTHORITY_COUNT_OFFSET, AUTHORITY_COUNT_BIT_LENGTH);
		return this;
	}

	public int additonalCount() {
		return Misc.getBitsIntInBitset(packetBitSet, ADDITIONAL_COUNT_OFFSET, ADDITIONAL_COUNT_BIT_LENGTH);
	}

	public Packet additonalCount(int count) {
		Misc.setBitsIntInBitset(packetBitSet, count, ADDITIONAL_COUNT_OFFSET, ADDITIONAL_COUNT_BIT_LENGTH);
		return this;
	}

	public Packet queryDomain(String domain) {
		return queryDomain(domain, QUERY_TYPE_A, QUERY_CLASS_INTERNET);
	}

	public String queryDomain() {
		int startOffset = QUESTION_OFFSET;
		int endOffset = findNameEndOffset(QUESTION_OFFSET);
		byte[] domainBytes = Misc.getBitsetBytes(packetBitSet, packetSize, startOffset, (endOffset - QUESTION_OFFSET) / 8);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < domainBytes.length; i++) {
			int count = domainBytes[i];
			if (count == 0) {
				break;
			}
			byte[] domainSectionBytes = Arrays.copyOfRange(domainBytes, i + 1, i + count + 1);
			for (byte domainSectionByte : domainSectionBytes) {
				sb.append((char) domainSectionByte);
			}
			i += count;
			sb.append(".");
		}
		String domain = sb.toString();
		return domain.substring(0, domain.length() - 1);
	}

	public Packet queryDomain(String domain, int queryType, int queryClass) {
		byte[] domainBytes = parseDomain(domain);
		//write domain
		Misc.setBitsetBytes(domainBytes, packetBitSet, packetSize, QUESTION_OFFSET);
		Misc.setBitsIntInBitset(packetBitSet, queryType, QUESTION_OFFSET + domainBytes.length * 8, 16);
		Misc.setBitsIntInBitset(packetBitSet, queryClass, QUESTION_OFFSET + domainBytes.length * 8 + 16, 16);
		//set question count
		questionCount(1);
		QUESTION_END_OFFSET = QUESTION_OFFSET + domainBytes.length * 8 - 1 + 32;
		return this;
	}

	public int queryType() {
		int endOffset = findNameEndOffset(QUESTION_OFFSET);
		return Misc.getBitsIntInBitset(packetBitSet, endOffset, 16);
	}

	public int queryClass() {
		int endOffset = findNameEndOffset(QUESTION_OFFSET);
		return Misc.getBitsIntInBitset(packetBitSet, endOffset + 16, 16);
	}

	private int findNameEndOffset(int offset) {
		//find offset
		byte[] bytesData = Misc.getBitsetBytes(packetBitSet, packetSize, offset);
		int length = 0;
		for (int j = 0; j < bytesData.length; j++) {
			int b = bytesData[j];
			length += 8;
			if (b == 0) {
				break;
			}
		}
		return offset + (length == 8 ? 0 : length);
	}

	private byte[] parseDomain(String domain) {
		//parse domain
		StringBuilder domainBuilder = new StringBuilder();
		String[] sections = domain.split("\\.");
		String domain0 = domain.replaceAll("\\.", "");
		byte[] domainBytes = new byte[sections.length + domain0.length() + 1];
		int i = 0;
		for (String section : sections) {
			domainBuilder.append(section.length()).append(section);
			domainBytes[i++] = Integer.valueOf(section.length()).byteValue();
			for (byte b : IDN.toASCII(section).getBytes()) {
				domainBytes[i++] = b;
			}
		}
		domainBytes[i] = 0x0;
		return domainBytes;
	}

	public Record[] answers() {
		Record[] records = new Record[0];
		try {
			records = Reader.parseResponse(Misc.getBitsetBytes(packetBitSet, packetSize, 0));
		} catch (Exception e) {
		}
		return records;
	}

	public Packet answer(String ip, int ttl) {
		byte[] ipBytes = new byte[4];
		try {
			ipBytes = InetAddress.getByName(ip).getAddress();
		} catch (UnknownHostException ex) {
		}
		byte[] ttlBytes = ByteBuffer.allocate(4).putInt(ttl).array();
		byte[] answerBytes = new byte[]{
			(byte) 0xc0, 0x0c//name offset
			, 0x00, 0x01//type
			, 0x00, 0x01//class
			, ttlBytes[0], ttlBytes[1], ttlBytes[2], ttlBytes[3]//ttl
			, 0x00, 0x04//playload length
			, ipBytes[0], ipBytes[1], ipBytes[2], ipBytes[3]
		};
		int answerOffset = findNameEndOffset(QUESTION_OFFSET) + 32;
		Misc.setBitsetBytes(answerBytes, packetBitSet, packetSize, answerOffset);
		answerCount(1);
		ANSWER_END_OFFSET = answerOffset + answerBytes.length * 8 - 1;
		return this;
	}

	public static void main(String[] args) {
		Packet query = new Packet();
		query.setQuery().randomId().queryDomain("www.baidu.com");
		byte[] bytes = query.getBytes();
		//System.out.println(Misc.bytes2BitString(bytes));
		DnsNodeManager.request(Misc.bytes2ByteBuf(bytes), "192.168.1.1", 53, 3000, new RequestSuccessCallback() {

			@Override
			public void onMessage(ChannelHandlerContext ctx1, DatagramPacket responsePacket, DatagramPacket requestPacket) {
				//System.out.println(Misc.bytes2BitString(Misc.byteBuf2bytes(responsePacket.copy().content())));
			}
		}, new RequestTimeoutCallback() {

			@Override
			public void onTimeout(Channel ch, DatagramPacket requestPacket, Integer timeout) {
				System.out.println("timeout");
			}
		});
	}
}

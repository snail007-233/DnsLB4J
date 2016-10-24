package com.snail.dnslb4j.dns;

import com.snail.dnslb4j.util.Misc;
import java.util.BitSet;

public class Header {

	private final int HEADER_BIT_SIZE = 16;
	private BitSet headerBitSet = new BitSet(HEADER_BIT_SIZE);
	//header key infomation
	private static final  int QR_OFFSET = 0;
	private static final  int QR_BIT_LENGTH = 1;
	private static final  int OPCODE_OFFSET = 1;
	private static final  int OPCODE_BIT_LENGTH = 4;
	private static final  int AA_OFFSET = 5;
	private static final  int AA_BIT_LENGTH = 1;
	private static final  int TC_OFFSET = 6;
	private static final  int TC_BIT_LENGTH = 1;
	private static final  int RD_OFFSET = 7;
	private static final  int RD_BIT_LENGTH = 1;
	private static final  int RA_OFFSET = 8;
	private static final  int RA_BIT_LENGTH = 1;
	private static final  int ZERO_OFFSET = 9;
	private static final  int ZERO_BIT_LENGTH = 3;
	private static final  int RCODE_OFFSET = 12;
	private static final  int RCODE_BIT_LENGTH = 4;
	//header key's value
	public static final int QR_QUERY = 0;
	public static final int QR_REPLY = 1;
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

	public Header(boolean isQueryPacket) {
		if (isQueryPacket) {
			qr(QR_QUERY)
				.opcode(OPCODE_IP)
				.aa(AA_NON_AUTH)
				.tc(TC_NON_TRUNCATE)
				.rd(RD_RECURSION)
				.ra(RA_NON_RECURSION_SUPPORT)
				.zero(ZERO)
				.rcode(RCODE_OK);
		} else {
			qr(QR_REPLY)
				.opcode(OPCODE_IP)
				.aa(AA_NON_AUTH)
				.tc(TC_NON_TRUNCATE)
				.rd(RD_RECURSION)
				.ra(RA_RECURSION_SUPPORT)
				.zero(ZERO)
				.rcode(RCODE_OK);
		}

	}

	public Header(byte[] header) {
		headerBitSet = Misc.bytes2bitset(header, HEADER_BIT_SIZE);
	}

	public int qr() {
		return Misc.getBitsIntInBitset(headerBitSet,QR_OFFSET, QR_BIT_LENGTH);
	}

	public Header qr(int value) {
		Misc.setBitsIntInBitset(headerBitSet,value, QR_OFFSET, QR_BIT_LENGTH);
		return this;
	}

	public int opcode() {
		return Misc.getBitsIntInBitset(headerBitSet,OPCODE_OFFSET, OPCODE_BIT_LENGTH);
	}

	public Header opcode(int value) {
		Misc.setBitsIntInBitset(headerBitSet,value, OPCODE_OFFSET, OPCODE_BIT_LENGTH);
		return this;
	}

	public int aa() {
		return Misc.getBitsIntInBitset(headerBitSet,AA_OFFSET, AA_BIT_LENGTH);
	}

	public Header aa(int value) {
		Misc.setBitsIntInBitset(headerBitSet,value, AA_OFFSET, AA_BIT_LENGTH);
		return this;
	}

	public int tc() {
		return Misc.getBitsIntInBitset(headerBitSet,TC_OFFSET, TC_BIT_LENGTH);
	}

	public Header tc(int value) {
		Misc.setBitsIntInBitset(headerBitSet,value, TC_OFFSET, TC_BIT_LENGTH);
		return this;
	}

	public int rd() {
		return Misc.getBitsIntInBitset(headerBitSet,RD_OFFSET, RD_BIT_LENGTH);
	}

	public Header rd(int value) {
		Misc.setBitsIntInBitset(headerBitSet,value, RD_OFFSET, RD_BIT_LENGTH);
		return this;
	}

	public int ra() {
		return Misc.getBitsIntInBitset(headerBitSet,RA_OFFSET, RA_BIT_LENGTH);
	}

	public Header ra(int value) {
		Misc.setBitsIntInBitset(headerBitSet,value, RA_OFFSET, RA_BIT_LENGTH);
		return this;
	}

	public int zero() {
		return Misc.getBitsIntInBitset(headerBitSet,ZERO_OFFSET, ZERO_BIT_LENGTH);
	}

	public Header zero(int code) {
		Misc.setBitsIntInBitset(headerBitSet,code, ZERO_OFFSET, ZERO_BIT_LENGTH);
		return this;
	}

	public int rcode() {
		return Misc.getBitsIntInBitset(headerBitSet,RCODE_OFFSET, RCODE_BIT_LENGTH);
	}

	public Header rcode(int value) {
		Misc.setBitsIntInBitset(headerBitSet,value, RCODE_OFFSET, RCODE_BIT_LENGTH);
		return this;
	}

	

	public byte[] getBytes() {
		return Misc.getBitsetBytes(headerBitSet, HEADER_BIT_SIZE, 0);
	}
}

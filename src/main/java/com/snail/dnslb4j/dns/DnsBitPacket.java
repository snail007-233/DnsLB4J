package com.snail.dnslb4j.dns;

import com.snail.dnslb4j.util.Misc;
import java.util.BitSet;
import java.util.Random;

public class DnsBitPacket {

	private final int packetSize = 512;
	private BitSet packetBitSet = new BitSet(packetSize);
	private final int ID_OFFSET = 0;
	private final int ID_BIT_LENGTH = 16;
	private final int HEADER_OFFSET = 16;
	private final int HEADER_BIT_LENGTH = 16;
	private final int QUESTION_COUNT_OFFSET = 32;
	private final int QUESTION_COUNT_BIT_LENGTH = 16;
	private final int ANSWER_COUNT_OFFSET = 48;
	private final int ANSWER_COUNT_BIT_LENGTH = 16;
	private static final Random RNADOM = new Random();

	public DnsBitPacket() {

	}

	public DnsBitPacket setDeafultQueryHeader() {
		setHeader(new Header(true));
		return this;
	}

	public DnsBitPacket setDeafultAnswerHeader() {
		setHeader(new Header(false));
		return this;
	}

	public DnsBitPacket(byte[] data) {
		packetBitSet = Misc.setBitsetBytes(data, packetBitSet, packetSize, ID_OFFSET);
	}

	public int id() {
		return Misc.getBitsIntInBitset(packetBitSet, ID_OFFSET, ID_BIT_LENGTH);
	}

	public DnsBitPacket id(int id) {
		Misc.setBitsIntInBitset(packetBitSet, id, ID_OFFSET, ID_BIT_LENGTH);
		return this;
	}

	public int questionCount() {
		return Misc.getBitsIntInBitset(packetBitSet, QUESTION_COUNT_OFFSET, QUESTION_COUNT_BIT_LENGTH);
	}

	public DnsBitPacket questionCount(int count) {
		Misc.setBitsIntInBitset(packetBitSet, count, QUESTION_COUNT_OFFSET, QUESTION_COUNT_BIT_LENGTH);
		return this;
	}

	public int answerCount() {
		return Misc.getBitsIntInBitset(packetBitSet, ANSWER_COUNT_OFFSET, ANSWER_COUNT_BIT_LENGTH);
	}

	public DnsBitPacket answerCount(int count) {
		Misc.setBitsIntInBitset(packetBitSet, count, ANSWER_COUNT_OFFSET, ANSWER_COUNT_BIT_LENGTH);
		return this;
	}

	private Header header() {
		return new Header(Misc.getBitsetBytes(packetBitSet, packetSize, HEADER_OFFSET, HEADER_BIT_LENGTH / 8));
	}

	private void setHeader(Header header) {
		Misc.setBitsetBytes(header.getBytes(), packetBitSet, packetSize, HEADER_OFFSET);
	}

	public int qr() {
		return header().qr();
	}

	public DnsBitPacket qr(int value) {
		setHeader(header().qr(value));
		return this;
	}

	public int opcode() {
		return header().opcode();
	}

	public DnsBitPacket opcode(int value) {
		setHeader(header().opcode(value));
		return this;
	}

	public int aa() {
		return header().aa();
	}

	public DnsBitPacket aa(int value) {
		setHeader(header().aa(value));
		return this;
	}

	public int tc() {
		return header().tc();
	}

	public DnsBitPacket tc(int value) {
		setHeader(header().tc(value));
		return this;
	}

	public int rd() {
		return header().rd();
	}

	public DnsBitPacket rd(int value) {
		setHeader(header().rd(value));
		return this;
	}

	public int ra() {
		return header().ra();
	}

	public DnsBitPacket ra(int value) {
		setHeader(header().ra(value));
		return this;
	}

	public int zero() {
		return header().zero();
	}

	public DnsBitPacket zero(int value) {
		setHeader(header().zero(value));
		return this;
	}

	public int rcode() {
		return header().rcode();
	}

	public DnsBitPacket rcode(int value) {
		setHeader(header().rcode(value));
		return this;
	}

	public byte[] getBytes() {
		return Misc.getBitsetBytes(packetBitSet, packetSize, ID_OFFSET);
	}

	public DnsBitPacket randomId() {
		int id;
		synchronized (RNADOM) {
			id = RNADOM.nextInt(65535);
		}
		id(id);
		return this;
	}

	public static void main(String[] args) {
		DnsBitPacket packet = new DnsBitPacket();
		packet.setDeafultQueryHeader().questionCount(6);
		System.out.println(Misc.bytes2BitString(packet.getBytes()));
	}
}

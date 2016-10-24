/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snail.dnslb4j.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.BitSet;
import java.util.Random;

/**
 *
 * @author pengmeng
 */
public class Misc {

	public static byte[] byteBuf2bytes(ByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		int readerIndex = buf.readerIndex();
		buf.getBytes(readerIndex, bytes);
		return bytes;
	}

	public static ByteBuf bytes2ByteBuf(byte[] bytes) {
		return Unpooled.copiedBuffer(bytes);
	}

	/**
	 * 将byte转换为一个长度为8的byte数组，数组每个值代表bit
	 *
	 * @param b
	 * @return
	 */
	public static byte[] getBooleanArray(byte b) {
		byte[] array = new byte[8];
		for (int i = 7; i >= 0; i--) {
			array[i] = (byte) (b & 1);
			b = (byte) (b >> 1);
		}
		return array;
	}

	/**
	 * 把byte转为字符串的bit
	 */
	public static String byteToBit(byte b) {
		return ""
			+ (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
			+ (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
			+ (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
			+ (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
	}

	public static void main(String[] args) {
		Random random = new Random();
		BitSet bitSet = new BitSet(512);
		for (int i = 0; i < 10; i++) {
			int k = (int) (random.nextFloat() * 65535);
			System.err.println(k);
			setBitsIntInBitset(bitSet, k, 0, 16);
			System.err.println(Misc.bytes2BitString(Misc.getBitsetBytes(bitSet, 512, 0)));
		}
	}

	public static BitSet bytes2bitset(byte[] data, int bitSetSize) {
		BitSet bitSet = new BitSet(bitSetSize);
		for (int i = 0; i < bitSetSize; i++) {
			int blockIndex = i / 8;
			int blockBitIndex = (7 - i % 8);
			bitSet.set(i, (blockIndex) > data.length - 1 ? false : getBit(data[blockIndex], blockBitIndex));
		}
		return bitSet;
	}

	public static BitSet setBitsetBytes(byte[] data, BitSet bitSet, int bitSetSize, int offset) {
		int endIndex = data.length * 8 + offset;
		int k = 0;
		for (int i = offset; i < endIndex; i++) {
			int blockIndex = k / 8;
			int blockBitIndex = (7 - k % 8);
			if (blockBitIndex >= endIndex) {
				break;
			}
			bitSet.set(i, getBit(data[blockIndex], blockBitIndex));
			k++;
		}
		return bitSet;
	}

	public static byte setBit(byte _byte, int bitPosition, boolean bitValue) {
		if (bitValue) {
			return (byte) (_byte | (1 << bitPosition));
		}
		return (byte) (_byte & ~(1 << bitPosition));
	}

	public static boolean getBit(byte _byte, int bitPosition) {
		return (_byte & (1 << bitPosition)) != 0;
	}

	public static String bytes2BitString(byte[] bytes) {
		int len = bytes.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < 8; j++) {
				sb.append((byte) (bytes[i] >> (7 - j) & 0x1));
			}
			sb.append(" ");
		}
		return sb.toString();
	}

	public static byte[] getBitsetBytes(BitSet packetBitSet, int packetSize, int offset, int bytesCount) {
		byte[] newbytes = new byte[bytesCount];
		int k = 0;
		for (int i = offset; i < packetSize; i++) {
			int blockIndex = k / 8;
			int blockBitIndex = 7 - (k % 8);
			if (blockIndex > bytesCount - 1) {
				break;
			}
			boolean value = packetBitSet.get(i);
			newbytes[blockIndex] = Misc.setBit(newbytes[blockIndex], blockBitIndex, value);
			k++;
		}
		return newbytes;
	}

	public static byte[] getBitsetBytes(BitSet packetBitSet, int packetSize, int offset) {
		int bytesCount = (packetSize - offset) / 8;
		return getBitsetBytes(packetBitSet, packetSize, offset, bytesCount);
	}

	public static int getBitsIntInBitset(BitSet headerBitSet, int offset, int length) {
		StringBuilder code = new StringBuilder();
		for (int i = offset; i < offset + length; i++) {
			code.append(headerBitSet.get(i) ? "1" : "0");
		}
		return Integer.parseUnsignedInt(Integer.valueOf(code.toString(), 2) + "");
	}

	public static void setBitsIntInBitset(BitSet bitSet, int value, int offset, int length) {
		value = Integer.parseUnsignedInt(value + "");
		String value0 = Integer.toBinaryString(value);
		int k = value0.length();
		--k;
		for (int i = offset + length - 1; i >= offset; i--) {
			int charIndex = k--;
			if (charIndex >= 0 && value0.charAt(charIndex) == '1') {
				bitSet.set(i);
			} else {
				bitSet.set(i, false);
			}
		}
	}
}

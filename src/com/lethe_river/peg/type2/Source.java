package com.lethe_river.peg.type2;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator.OfInt;

import com.lethe_river.util.primitive.ArrayIntList;

/**
 * Parserが読み取るためのデータ
 *
 * @author YuyaAizawa
 *
 */
public interface Source {

	/**
	 * このソースの大きさを返す．
	 * @return このソースの大きさ
	 */
	int length();

	/**
	 * このソースの現在の読み取り位置からさらに読み取れるかどうかを返す．
	 * @return 読み取ればtrue
	 */
	boolean hasNext();

	/**
	 * このソースの現在の読み取り位置から1文字読み取り，読み取り位置を1つ進める．
	 * @return 文字
	 * @throws NoSuchElementException
	 */
	char next();

	/**
	 * このソースの現在の読み取り位置を返す．
	 * @return 現在の読み取り位置
	 */
	int index();

	/**
	 * このソースの現在の読み取り位置の行番号を返す．
	 * @return 現在の読み取り位置の行番号(0-based)
	 */
	int lineNum();

	/**
	 * このソースの読み取り位置を指定する.
	 * @param index インデックス
	 * @throws IndexOutOfBoundsException 指定した位置が無効な場合
	 */
	void jump(int index);

	/**
	 * このソースの指定した区間の文字列Stringを生成する．
	 * @param from
	 * @param to
	 * @return
	 */
	String makeString(int from, int to);

	/**
	 * 指定した文字列をもとにソースを生成する
	 * @param src
	 * @return 生成した文字列
	 */
	public static Source from(CharSequence src) {
		return new ArraySource(src);
	}

	static class ArraySource implements Source {
		final char[] chars;
		final  int[] breakIndexes;
		int pos = 0;

		ArraySource(CharSequence src) {

			try {
				chars = new char[src.length()];
				OfInt itr = src.chars().iterator();
				for (int i = 0; i < chars.length; i++) {
					chars[i] = (char)(int)itr.next();
				}
			} catch(NoSuchElementException e) {
				throw new Error(e);
			}


			final int CR = 0x0D;
			final int LF = 0x0A;
			ArrayIntList brs = new ArrayIntList();
			for (int i = 0; i < chars.length; i++) {
				if(chars[i] == LF) {
					brs.add(i);
					continue;
				}
				if(chars[i] == CR) {
					if(i+1 < chars.length && chars[i+1] == LF) {
						brs.add(i+1);
						i++;
						continue;
					} else {
						brs.add(i);
						continue;
					}
				}
			}
			breakIndexes = brs.toArray();
		}

		@Override
		public int length() {
			return chars.length;
		}

		@Override
		public boolean hasNext() {
			return pos < chars.length;
		}

		@Override
		public char next() {
			try {
				return chars[pos++];
			} catch(IndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}

		@Override
		public int index() {
			return pos;
		}

		@Override
		public int lineNum() {
			int idx = Arrays.binarySearch(breakIndexes, pos);
			if(idx >= 0) {
				// on breakcode
				return idx + 1;
			} else {
				// otherwise
				return -idx;
			}
		}

		@Override
		public void jump(int index) {
			if(index < 0 || chars.length < index) {
				throw new IndexOutOfBoundsException("index: "+index+", length: "+chars.length);
			}
			pos = index;
		}

		@Override
		public String makeString(int from, int to) {
			return new String(chars, from, to - from);
		}
	}
}

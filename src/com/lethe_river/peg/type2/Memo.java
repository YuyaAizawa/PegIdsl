package com.lethe_river.peg.type2;

import java.util.HashMap;
import java.util.Map;

import com.lethe_river.util.primitive.LongIntMap;
import com.lethe_river.util.primitive.ScatterLongIntMap;

/**
 * 解析表現文法のパース結果をメモするクラス．
 * @author YuyaAizawa
 *
 */
interface Memo {

	public static final int ERROR = -1;
	public static final int NULL  = -2;

	/**
	 * パース終了位置を記録する
	 * @param rule 規則
	 * @param start 開始インデックス
	 * @param end 終了インデックス
	 */
	public void putEnd(Rule rule, int start, int end);

	/**
	 * パース失敗を記録する
	 * @param rule
	 * @param start
	 */
	public void putError(Rule rule, int start);

	/**
	 * パース結果のメモを取得する.
	 * 記録した結果を正確に返す代わりにNULLを返しても良い.
	 * @return 終了index，ERROR，またはNULL
	 */
	public int getEnd(Rule rule, int start);

	public static Memo noMemo() {
		return new Memo() {
			@Override
			public void putError(Rule rule, int start) {}
			@Override
			public void putEnd(Rule rule, int start, int end) {}
			@Override
			public int getEnd(Rule rule, int start) {
				return NULL;
			}
		};
	}

	public static Memo fullMemo() {
		return new Memo() {
			LongIntMap map = new ScatterLongIntMap();
			Map<Long, Object> valueMap = new HashMap<>();

			@Override
			public void putError(Rule rule, int start) {
				map.put(((long)rule.id()) << 32 | start, ERROR);
			}

			@Override
			public void putEnd(Rule rule, int start, int end) {
				map.put(((long)rule.id()) << 32 | start, end);
			}

			@Override
			public int getEnd(Rule rule, int start) {
				return map.getOrDefault(((long)rule.id()) << 32 | start, NULL);
			}
		};
	}
}

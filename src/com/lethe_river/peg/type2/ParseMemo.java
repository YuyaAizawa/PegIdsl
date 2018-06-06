package com.lethe_river.peg.type2;

import java.util.HashMap;
import java.util.Map;

class ParseMemo {

	Map<Long, Object> map = new HashMap<>();

	void put(Rule rule, int start, Object value) {
		map.put(((long)rule.id()) << 32 | start, value);
	}

	Object get(Rule rule, int start, Object value) {
		return map.get(((long)rule.id()) << 32 | start);
	}
}

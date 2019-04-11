package com.lethe_river.peg.type2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import com.lethe_river.util.primitive.collection.ArrayIntList;
import com.lethe_river.util.primitive.collection.IntList;

public class ParserTest {
	Parser<Integer> intParser =
			Parser.characters(c -> '0' <=c && c <= '9' , "[0-9]")
			.map(str -> Integer.parseInt(str));

	Parser<IntList> intSeqParser =
			intParser.then(
					(Parser.of(",")
							.then(Parser.of(" ").star(), (l, r) -> null)
							.then(intParser, (n, i) -> i)
					).star(), (l, r) -> {
						ArrayIntList list = new ArrayIntList();
						list.add(l);
						r.forEach(i -> list.add(i));
						return list;});

	Parser<IntList> intListParser =
			Parser.of("<")
			.then(intSeqParser, (a, seq) -> seq)
			.then(Parser.of(">"), (seq, a) -> seq);

	@Test
	public void intListTest() {
		IntList expected = ArrayIntList.of(1, 2, 3);
		IntList actual   = intListParser.parse(expected.toString());

		assertEquals(expected, actual);
	}
}

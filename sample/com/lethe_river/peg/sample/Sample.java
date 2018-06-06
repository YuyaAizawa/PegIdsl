package com.lethe_river.peg.sample;

import java.util.ArrayList;
import java.util.List;

import com.lethe_river.peg.type2.Parser;
import com.lethe_river.peg.type2.RuleAnalyzer;

public class Sample {
	public static void main(String[] args) {
		List<Integer> list = List.of(1, 2, 3);

		System.out.println(String.format("list = %s", list));



		Parser<Integer> intParser =
				Parser.characters(c -> '0' <=c && c <= '9' , "[0-9]")
				.map(str -> Integer.parseInt(str));

		Parser<Integer> commaSpaceIntParser =
				Parser.of(", ").then(intParser, (v, i) -> i);

		Parser<List<Integer>> intSeqParser =
				intParser.then(commaSpaceIntParser.star(),
						(first, rest) -> {
							List<Integer> l = new ArrayList<>();
							l.add(first);
							l.addAll(rest);
							return l;});

		Parser<List<Integer>> intListParser =
				Parser.of("[").then(intSeqParser).then("]");

		List<Integer> parsed = intListParser.parse(list.toString()).value;
		System.out.println(String.format("parsed = %s", parsed));



		new RuleAnalyzer().analyze(intListParser);
	}
}

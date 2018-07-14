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
				Parser.characters(c -> '0' <=c && c <= '9' , "[0-9]") // <int> ::= [0-9]+
				.map(str -> Integer.parseInt(str)); // 結果はparseIntでintにする

		Parser<Integer> commaSpaceIntParser =
				Parser.of(", ").then(intParser, // <commaSpaceInt> ::= ", " <int>
						(v, i) -> i); // 結果は<int>を反映

		Parser<List<Integer>> intSeqParser =
				intParser.then(commaSpaceIntParser.star(), // <intSeq> ::= <int> <commaSpaceInt>*
						(first, rest) -> { // 結果はListにまとめる
							List<Integer> l = new ArrayList<>();
							l.add(first);
							l.addAll(rest);
							return l;});

		Parser<List<Integer>> intListParser =
				Parser.of("[").then(intSeqParser).then("]"); // <intList> ::= "[" <intSeq> "]"

		List<Integer> parsed = intListParser.parse(list.toString()).value; // パース実行
		System.out.println(String.format("parsed = %s%n", parsed));



		// パーサの構造を確認
		RuleAnalyzer analyzer = new RuleAnalyzer();
		analyzer.setName(intListParser, "intSeqParser");
		analyzer.analyze(intListParser);

		// <intSeqParser> ::= "[" [0-9]+ <4>* "]"
		// <4> ::= ", " [0-9]+
	}
}

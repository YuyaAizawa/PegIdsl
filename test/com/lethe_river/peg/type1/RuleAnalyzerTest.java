package com.lethe_river.peg.type1;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.lethe_river.peg.type1.RuleAnalyzer;

public class RuleAnalyzerTest {


	@Test
	public void lambdaExpresstionTest() {
		LambdaExpressionRule rules = new LambdaExpressionRule();
		RuleAnalyzer analyzer = new RuleAnalyzer();

		analyzer.setName(rules.exp, "exp");
		analyzer.setName(rules.id , "id");
		analyzer.setName(rules.abs, "abs");
		analyzer.setName(rules.app, "app");

		StringBuilder sb = new StringBuilder();
		analyzer.analysis(rules.exp, sb);

		String expected =
				"<exp> ::= <app>/<7>\n" +
				"<app> ::= <7> <7>+\n" +
				"<7> ::= <id>/<abs>/<6>\n" +
				"<id> ::= [a-z]\n" +
				"<abs> ::= \"λ\" <id>+ \".\" <exp>\n" +
				"<6> ::= \"(\" <exp> \")\"\n";

		assertEquals(expected, sb.toString());
	}
}

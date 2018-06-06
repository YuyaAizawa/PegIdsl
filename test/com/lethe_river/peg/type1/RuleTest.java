package com.lethe_river.peg.type1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

public class RuleTest {

	@Test
	public void lambdaExpresstionNomemoTest() {
		LambdaExpressionRule rules = new LambdaExpressionRule();
		Memo memo = Memo.noMemo();

		Rule exp = rules.exp;
		Rule id  = rules.id;
		Rule abs = rules.abs;
		Rule app = rules.app;

		String str00 = "x";
		String str01 = "λx.x";
		String str02 = "(λf.ff)x";
		String str03 = "(λx.(λy.(λz.((x)(y))((y)(z)))))";
		String str04 = "λxyz.xy(yz)";

		assertTrue (exp.parse(Source.from(str00), memo));
		assertTrue ( id.parse(Source.from(str00), memo));
		assertFalse(abs.parse(Source.from(str00), memo));
		assertFalse(app.parse(Source.from(str00), memo));

		assertTrue (exp.parse(Source.from(str01), memo));
		assertFalse( id.parse(Source.from(str01), memo));
		assertTrue (abs.parse(Source.from(str01), memo));
		assertFalse(app.parse(Source.from(str01), memo));

		assertTrue (exp.parse(Source.from(str02), memo));
		assertFalse( id.parse(Source.from(str02), memo));
		assertFalse(abs.parse(Source.from(str02), memo));
		assertTrue (app.parse(Source.from(str02), memo));

		assertTrue (exp.parse(Source.from(str03), memo));
		assertFalse( id.parse(Source.from(str03), memo));
		assertFalse(abs.parse(Source.from(str03), memo));
		assertFalse(app.parse(Source.from(str03), memo));

		assertTrue (exp.parse(Source.from(str04), memo));
		assertFalse( id.parse(Source.from(str04), memo));
		assertTrue (abs.parse(Source.from(str04), memo));
		assertFalse(app.parse(Source.from(str04), memo));

	}

	@Test
	public void lambdaExpresstionFullmemoTest() {
		LambdaExpressionRule rules = new LambdaExpressionRule();

		Rule exp = rules.exp;
		Rule id  = rules.id;
		Rule abs = rules.abs;
		Rule app = rules.app;

		String str00 = "x";
		String str01 = "λx.x";
		String str02 = "(λf.ff)x";
		String str03 = "(λx.(λy.(λz.((x)(y))((y)(z)))))";
		String str04 = "λxyz.xy(yz)";

		assertTrue (exp.parse(Source.from(str00), Memo.fullMemo()));
		assertTrue ( id.parse(Source.from(str00), Memo.fullMemo()));
		assertFalse(abs.parse(Source.from(str00), Memo.fullMemo()));
		assertFalse(app.parse(Source.from(str00), Memo.fullMemo()));

		assertTrue (exp.parse(Source.from(str01), Memo.fullMemo()));
		assertFalse( id.parse(Source.from(str01), Memo.fullMemo()));
		assertTrue (abs.parse(Source.from(str01), Memo.fullMemo()));
		assertFalse(app.parse(Source.from(str01), Memo.fullMemo()));

		assertTrue (exp.parse(Source.from(str02), Memo.fullMemo()));
		assertFalse( id.parse(Source.from(str02), Memo.fullMemo()));
		assertFalse(abs.parse(Source.from(str02), Memo.fullMemo()));
		assertTrue (app.parse(Source.from(str02), Memo.fullMemo()));

		assertTrue (exp.parse(Source.from(str03), Memo.fullMemo()));
		assertFalse( id.parse(Source.from(str03), Memo.fullMemo()));
		assertFalse(abs.parse(Source.from(str03), Memo.fullMemo()));
		assertFalse(app.parse(Source.from(str03), Memo.fullMemo()));

		assertTrue (exp.parse(Source.from(str04), Memo.fullMemo()));
		assertFalse( id.parse(Source.from(str04), Memo.fullMemo()));
		assertTrue (abs.parse(Source.from(str04), Memo.fullMemo()));
		assertFalse(app.parse(Source.from(str04), Memo.fullMemo()));
	}
}

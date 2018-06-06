package com.lethe_river.peg.type1;

import com.lethe_river.peg.type1.Rule;

public class LambdaExpressionRule {
	private final String LAMBDA = "λ";
	private final String LPAREN = "(";
	private final String RPAREN = ")";
	private final String DOT = ".";

	public Rule lambda = new Rule.FullMatch(LAMBDA);
	public Rule dot    = new Rule.FullMatch(DOT);
	public Rule lParen = new Rule.FullMatch(LPAREN);
	public Rule rParen = new Rule.FullMatch(RPAREN);

	public Rule exp = new Rule.Choice(() -> this.app, () -> this.rhs);

	public Rule inParen = new Rule.Sequence(lParen, exp, rParen);

	public Rule rhs = new Rule.Choice(
			() -> this.id,
			() -> this.abs,
			() -> inParen);

	public Rule id = new Rule.PredicatedChar(i -> 'a' <= i && i <= 'z', "[a-z]");

	public Rule appTail = new Rule.Plus(rhs);

	public Rule app = new Rule.Sequence(
			rhs,
			appTail);

	public Rule abs = new Rule.Sequence(
			lambda,
			new Rule.Plus(id),
			dot,
			exp);
}

package com.lethe_river.peg.type2;

/**
 * 自明な引数を与えずに各種パーサ合成メソッド利用できる，有効な値を返さないパーサ.
 *
 * @author YuyaAizawa
 *
 */
public final class VoidParser extends Parser<Void> {

	protected VoidParser(Rule rule) {
		super(rule);
	}

	@Override
	protected Void eval(Source src, Memo memo) {
		return null;
	}

	public <T> Parser<T> then(Parser<T> following) {
		return super.then(following, (p, f) -> f);
	}
	@Override
	public VoidParser then(VoidParser following) {
		return new VoidParser(new Rule.Sequence(
				this.getRule(),
				following.getRule()));
	}
	@Override
	public VoidParser then(String str) {
		return then(Parser.of(str));
	}

}

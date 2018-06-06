package com.lethe_river.peg.type2;

/**
 * Ruleと直接対応するParser.
 *
 * parseは
 * 1. ruleが受理するか判定
 * 2. parserが対応するオブジェクトを返却
 * の順で行われる．この順でないとruleのparse終了位置が
 *
 * @author YuyaAizawa
 *
 * @param <T> ASTの型
 */
public abstract class BottomParser<T> extends Parser<T> {

	protected BottomParser(Rule rule) {
		super(rule);
	}

	@Override
	protected final ParseResult<T> parse(Source src, Memo memo) {
		int start = src.index();
		if(!getRule().parse(src, memo)) {
			src.jump(start);
			throw new ParseException();
		}
		int end = src.index();

		src.jump(start);
		T ast = eval(src, memo);
		src.jump(end);
		return new ParseResult<>(ast, new Location(src, start, end));
	}

	@Override
	protected abstract T eval(Source src, Memo memo);
}

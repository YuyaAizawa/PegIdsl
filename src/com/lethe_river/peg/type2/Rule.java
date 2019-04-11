package com.lethe_river.peg.type2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lethe_river.util.primitive.function.CharPredicate;

/**
 * 解析表現文法を表すクラス．解析結果のメモを利用できる．
 * 受理，拒否のみ判定
 *
 * @author YuyaAizawa
 *
 */
public abstract class Rule {

	public static enum Kind {
		TERM,
		SEQUENCE,
		CHOICE,
		STAR,
		PLUS,
		OPTION,
		AND_PREDICATE,
		NOT_PREDICATE;
	}

	/**
	 * この規則の種類を返す
	 * @return
	 */
	abstract Kind kind();

	/**
	 * メモを利用して指定したソースをパースし結果を返す．
	 * 成功した場合ソースの読み取り位置は進められる．
	 * 失敗した場合の読み取り位置は未定義．
	 * @param src ソース
	 * @param memo メモ
	 * @return 受理すればtrue
	 */
	public final boolean parse(Source src, Memo memo) {
		int start = src.index();
		int r = memo.getEnd(this, start);
		switch (r) {
		case Memo.ERROR:
			return false;
		case Memo.NULL:
			if(eval(src, memo)) {
				memo.putEnd(this, start, src.index());
				return true;
			} else {
				memo.putError(this, start);
				return false;
			}
		default:
			src.jump(r);
			return true;
		}
	}

	/**
	 * 指定したソースをパースし結果を返す．
	 * 成功した場合ソースの読み取り位置は進められる．
	 * 失敗した場合の読み取り位置は未定義．
	 * @param src ソース
	 * @return 受理すればtrue
	 */
	protected abstract boolean eval(Source src, Memo memo);

	/**
	 * 定義に利用される他の規則を返す
	 * @return
	 */
	abstract List<Rule> rules();

	private static final AtomicInteger idCounter = new AtomicInteger();
	private final int id = idCounter.incrementAndGet();

	/**
	 * この規則を表すidを返す
	 * @return
	 */
	public final int id() {
		return id;
	}

	@Override
	public final int hashCode() {
		return id();
	}

	public final boolean equals(Rule rule) {
		if(rule == this) {
			return true;
		}
		if(rule == null) {
			return false;
		}
		return this.id() == rule.id();
	}

	@Override
	public final boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof Rule)) {
			return false;
		}
		return this.id() == ((Rule) obj).id();
	}

	public static class Sequence extends Rule {
		private final List<Rule> rules;
		public Sequence(Rule first, Rule... rest) {
			this.rules = Stream.concat(
							Stream.of(first),
							Arrays.stream(rest))
					.flatMap(r -> r.kind() == Kind.SEQUENCE ?
							r.rules().stream():
							Stream.of(r))
					.collect(Collectors.toList());
		}

		@Override
		Kind kind() {
			return Kind.SEQUENCE;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			return rules.stream()
					.allMatch(r -> r.parse(src, memo));
		}

		@Override
		List<Rule> rules() {
			return rules;
		}
	}

	public static class Choice extends Rule {
		private final List<Supplier<Rule>> ruleSuppliers;
		private List<Rule> rules;
		@SafeVarargs
		public Choice(Supplier<Rule> first, Supplier<Rule>... rest) {
			this(Stream.concat(
							Stream.of(first),
							Arrays.stream(rest))
					.collect(Collectors.toList()));
		}

		public Choice(List<Supplier<Rule>> ruleSuppliers) {
			if(ruleSuppliers.size() < 1) {
				throw new IllegalArgumentException();
			}
			this.ruleSuppliers = ruleSuppliers;
		}

		@Override
		Kind kind() {
			return Kind.CHOICE;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			lazyInit();
			Iterator<Rule> i = rules.iterator();
			while(i.hasNext()) {
				int pos = src.index();
				if(i.next().parse(src, memo)) {
					return true;
				}
				src.jump(pos);
			}
			return false;
		}

		@Override
		List<Rule> rules() {
			lazyInit();
			return rules;
		}

		private void lazyInit() {
			if(rules == null) {
				rules = ruleSuppliers.stream()
						.map(Supplier::get)
						.collect(Collectors.toList());
			}
		}
	}

	public static class Star extends Rule {
		private final Rule rule;
		public Star(Rule rule) {
			this.rule = rule;
		}

		@Override
		Kind kind() {
			return Kind.STAR;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			while(true) {
				int pos = src.index();
				boolean result = rule.parse(src, memo);
				if(!result) {
					src.jump(pos);
					return true;
				}
			}
		}

		@Override
		List<Rule> rules() {
			return List.of(rule);
		}
	}

	public static class Plus extends Rule {
		private final Rule rule;
		public Plus(Rule rule) {
			this.rule = rule;
		}

		@Override
		Kind kind() {
			return Kind.PLUS;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			if(!rule.parse(src, memo)) {
				return false;
			}
			while(true) {
				int pos = src.index();
				boolean result = rule.parse(src, memo);
				if(!result) {
					src.jump(pos);
					return true;
				}
			}
		}

		@Override
		List<Rule> rules() {
			return List.of(rule);
		}
	}

	public static class Option extends Rule {
		private final Rule rule;
		public Option(Rule rule) {
			this.rule = rule;
		}

		@Override
		Kind kind() {
			return Kind.OPTION;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			int pos = src.index();
			boolean result = rule.parse(src, memo);
			if(result) {
				return true;
			}
			src.jump(pos);
			return true;
		}

		@Override
		List<Rule> rules() {
			return List.of(rule);
		}
	}

	public static class AndPredicate extends Rule {
		private final Rule rule;
		public AndPredicate(Rule rule) {
			this.rule = rule;
		}

		@Override
		Kind kind() {
			return Kind.AND_PREDICATE;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			int pos = src.index();
			boolean result = rule.parse(src, null);
			src.jump(pos);
			return result;
		}

		@Override
		List<Rule> rules() {
			return List.of(rule);
		}
	}

	public static class NotPredicate extends Rule {
		private final Rule rule;
		public NotPredicate(Rule rule) {
			this.rule = rule;
		}

		@Override
		Kind kind() {
			return Kind.NOT_PREDICATE;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			int pos = src.index();
			boolean result = rule.parse(src, memo);
			src.jump(pos);
			return !result;
		}

		@Override
		List<Rule> rules() {
			return List.of(rule);
		}
	}

	static abstract class RuleWithDescription extends Rule {
		public abstract void description(StringBuilder sb);
		public final String description() {
			StringBuilder sb = new StringBuilder();
			description(sb);
			return sb.toString();
		}
	}

	public static class FullMatch extends RuleWithDescription {
		private final String str;

		public FullMatch(String str) {
			this.str = str;
		}

		@Override
		Kind kind() {
			return Kind.TERM;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			try {
				for (int i = 0; i < str.length(); i++) {
					if(str.charAt(i) != src.next()) {
						return false;
					}
				}
				return true;
			} catch(NoSuchElementException e) {
				return false;
			}
		}

		@Override
		List<Rule> rules() {
			return List.of();
		}

		@Override
		public void description(StringBuilder sb) {
			sb.append('"').append(str).append('"');
		}
	}

	/**
	 * 文字を条件に従って受理する規則
	 * @author YuyaAizawa
	 */
	public static class PredicatedChar extends RuleWithDescription {
		private final CharPredicate predicate;
		private final String description;

		/**
		 * 指定した値の範囲の文字を受理する規則を生成する．範囲は境界を含む.
		 * @param start
		 * @param end
		 */
		public PredicatedChar(CharPredicate predicate, String description) {
			this.predicate = predicate;
			this.description = description;
		}

		@Override
		public void description(StringBuilder sb) {
			sb.append(description);
		}

		@Override
		Kind kind() {
			return Kind.TERM;
		}

		@Override
		public boolean eval(Source src, Memo memo) {
			try {
				return predicate.test(src.next());
			} catch(NoSuchElementException e) {
				return false;
			}
		}

		@Override
		List<Rule> rules() {
			return List.of();
		}
	}
}

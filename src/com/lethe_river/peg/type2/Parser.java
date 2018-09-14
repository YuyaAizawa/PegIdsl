package com.lethe_river.peg.type2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lethe_river.util.primitive.CharPredicate;

/**
 * ソースからオブジェクトを読み取る解析表現文法パーサ.
 * パーサは他のパーサと合成できる．
 *
 * @author YuyaAizawa
 *
 * @param <T> 解析結果オブジェクトの型
 */

public abstract class Parser<T> {

	/**
	 * 文字列からオブジェクトを読み取る
	 * @param src 文字列
	 * @return 結果オブジェクト(パース失敗時はempty)
	 */
	public final T parse(CharSequence src) {
		return parse(Source.from(src), Memo.fullMemo());
	}

	/**
	 * メモを利用してソースからオブジェクトを読み取る.
	 *
	 * パースはソースの現在の読出し開始位置から行わる．
	 * 呼出し後の読出し開始は，解析成功なら解析が終了した位置に進み，失敗なら解析開始位置に戻る．
	 *
	 * @param src
	 * @param memo
	 * @return 解析結果
	 * @throws ParseException 解析失敗の場合
	 */
	protected T parse(Source src, Memo memo) {
		int start = src.index();
		if(!getRule().parse(src, memo)) {
			src.jump(start);
			throw new ParseException();
		}
		int end = src.index();

		src.jump(start);
		T t = eval(src, memo);
		src.jump(end);

		return t;
	}

	/**
	 * ソースからオブジェクトを読み取って返す.
	 * ruleに対するparseが成功したときに，ソースの成功した読み取り開始位置に関して呼出される.
	 * @param src ソース
	 * @param memo 内部のParserのparseメソッドの引数に渡すための引数
	 */
	protected abstract T eval(Source src, Memo memo);

	public final Rule getRule() {
		return rule;
	}

	private final Rule rule;
	protected Parser(Rule rule) {
		this.rule = rule;
	}

	public <R, U> Parser<R> then(
			Parser<U> following,
			BiFunction<? super T, ? super U, ? extends R> combiner) {

		return new Parser<>(new Rule.Sequence(
				this.getRule(),
				following.getRule())) {
			@Override
			protected R eval(Source src, Memo memo) {
				return combiner.apply(
						Parser.this.parse(src, memo),
						following.parse(src, memo));
			}
		};
	}
	public Parser<T> then(VoidParser following) {
		return then(following, (p, f) -> p);
	}
	public Parser<T> then(String str) {
		return then(Parser.of(str));
	}

	@SafeVarargs
	public static <T> Parser<T> or(
			Supplier<Parser<? extends T>> first,
			Supplier<Parser<? extends T>>... rest) {

		List<Supplier<Parser<? extends T>>> parsers =
				Stream.concat(
						Stream.of(first),
						Arrays.stream(rest))
				.collect(Collectors.toList());

		List<Supplier<Rule>> rules = parsers
				.stream()
				.map(s -> ((Supplier<Rule>)() -> s.get().getRule()))
				.collect(Collectors.toList());

		return new Parser<>(new Rule.Choice(rules)) {
					@Override
					protected T eval(Source src, Memo memo) {
						int start = src.index();

						Iterator<Supplier<Parser<? extends T>>> ip = parsers.iterator();
						Iterator<Supplier<Rule>> ir = rules.iterator();

						for(;;) {
							Rule rule = ir.next().get();
							Parser<? extends T> parser = ip.next().get();
							if(rule.parse(src, memo)) {
								src.jump(start);
								return parser.parse(src, memo);
							}
							src.jump(start);
						}
					}
		};
	}

	public Parser<List<T>> star() {
		return new Parser<>(new Rule.Star(rule)) {
			@Override
			protected List<T> eval(Source src, Memo memo) {
				List<T> list = new ArrayList<>();
				int pos = src.index();
				while(Parser.this.rule.parse(src, memo)) {
					int next = src.index();
					src.jump(pos);
					list.add(Parser.this.parse(src, memo));
					pos = next;
				}
				return list;
			}
		};
	}

	public Parser<List<T>> plus() {
		return new Parser<>(new Rule.Plus(rule)) {
			@Override
			protected List<T> eval(Source src, Memo memo) {
				List<T> list = new ArrayList<>();
				int pos = src.index();
				while(Parser.this.rule.parse(src, memo)) {
					int next = src.index();
					src.jump(pos);
					list.add(Parser.this.parse(src, memo));
					pos = next;
				}
				return list;
			}
		};
	}

	public Parser<Optional<T>> option() {
		return new Parser<>(new Rule.Option(rule)) {
			@Override
			protected Optional<T> eval(Source src, Memo memo) {
				int start = src.index();
				if(Parser.this.rule.parse(src, memo)) {
					src.jump(start);
					return Optional.of(Parser.this.eval(src, memo));
				}
				return Optional.empty();
			}
		};
	}

	public static VoidParser and(Parser<?> original) {
		return new VoidParser(new Rule.AndPredicate(original.rule));
	}

	public static VoidParser not(Parser<?> original) {
		return new VoidParser(new Rule.NotPredicate(original.rule));
	}

	public <U> Parser<U> map(Function<? super T, ? extends U> mapper) {
		return new Parser<>(rule) {
			@Override
			protected U eval(Source src, Memo memo) {
				return mapper.apply(Parser.this.eval(src, memo));
			}
		};
	}

	public Parser<ResultWithLocation<T>> withLocation() {
		return new Parser<>(this.rule) {
			@Override
			protected ResultWithLocation<T> eval(Source src, Memo memo) {

				int start = src.index();
				T result = Parser.this.eval(src, memo);
				int end = src.index();

				Location location = new Location(src, start, end);
				ResultWithLocation<T> resultWithLocation = new ResultWithLocation<>(result, location);
				return resultWithLocation;
			}
		};
	}

	public VoidParser toVoid() {
		return new VoidParser(getRule());
	}

	public static VoidParser of(String str) {
		return new VoidParser(new Rule.FullMatch(str));
	}

	public static VoidParser of(Rule rule) {
		return new VoidParser(rule);
	}

	public static Parser<Character> character(CharPredicate predicate, String description) {
		return new Parser<>(new Rule.PredicatedChar(predicate, description)) {
			@Override
			protected Character eval(Source src, Memo memo) {
				return Character.valueOf(src.next());
			}
		};
	}

	public static Parser<String> characters(CharPredicate predicate, String description) {
		return new Parser<>(new Rule.Plus(new Rule.PredicatedChar(predicate, description))) {
			@Override
			protected String eval(Source src, Memo memo) {
				int from = src.index();
				getRule().parse(src, memo);
				int to = src.index();
				return src.makeString(from, to);
			}
		};
	}
}

/*
 * 結果オブジェクトの生成タイミング
 * - パース時に生成
 *   - シンプル
 * - 取得時に生成
 *   - 無駄なオブジェクト生成を防げる
 *   - メモは終了位置のみで十分
 * - 寧ろオブジェクトもメモを作る
 *   - 対応するRule.parseの呼出し後はメモは完成している
 *     - 開始位置を指定すれば終了位置は求まる
 *     - choiceのどの選択肢が受理されるかなどは不明
 *   - メモ不要
 *     - Ruleの2回目のパースは定数時間で完了
 *
 * 結論としては受理判定を行ってから
 *
 *
 *
 * パース失敗の表現
 * - 検査例外
 *   - 失敗時処理の必要性が明確 大域脱出の実装が楽
 *   - パース失敗は正常系な気がする ラムダ式と相性が悪い
 *   - 古き良き設計
 *
 * - 非検査例外
 *   - 大域脱出の実装が楽
 *   - パース失敗は正常系な気がする
 *   - NoSuchElementExceptionと同程度の異常感でバランスは良い
 *
 * - Optional.EMPTY
 *   - 失敗時処理の必要性が明確
 *   - 型の記述が面倒
 *   - 型の記述以外は完璧
 *
 * - 失敗を含めた独自型
 *   - 型の記述が楽
 *   - 設計しなきゃ...
 *   - 結果型を合成する気はないのでイマイチ
 *
 * - null
 *   - 処理が高速
 *   - この部分で高速にする意味はない
 *   - 論外
 *
 *
 *
 * ASTのメモは必要か
 *
 */

package com.lethe_river.peg.type2;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.lethe_river.peg.type2.Rule.Kind;

/**
 * 文法を可視化するための解析器
 *
 * @author YuyaAizawa
 */
public class RuleAnalyzer {
	private static final Set<Kind> EXPAND_RULE = EnumSet.of(
			Kind.STAR,
			Kind.PLUS,
			Kind.OPTION,
			Kind.AND_PREDICATE,
			Kind.NOT_PREDICATE);

	private final Map<Rule, String> ruleNames = new HashMap<>();

	public RuleAnalyzer() {

	}

	public void setName(Rule rule, String name) {
		ruleNames.put(rule, name);
	}
	public void setName(Parser<?> parser, String name) {
		setName(parser.getRule(), name);
	}

	public void analyze(Rule start, StringBuilder sb) {
		Queue<Rule> toAnalyze = new ArrayDeque<>();
		Set<Rule> analyzed = new HashSet<>();

		toAnalyze.add(start);
		while(!toAnalyze.isEmpty()) {
			Rule target = toAnalyze.poll();
			descrive(target, sb);
			analyzed.add(target);

			target.rules()
					.stream()
					.map(r -> EXPAND_RULE.contains(r.kind()) ? r.rules().get(0) : r)
					.filter(r -> ruleNames.containsKey(r) || (r.kind() != Kind.TERM && !EXPAND_RULE.contains(r.kind())))
//					.filter(r -> ruleNames.containsKey(r) || r.kind() != Kind.TERM)
					.filter(r -> !analyzed.contains(r))
					.filter(r -> !toAnalyze.contains(r))
					.forEach(r -> toAnalyze.add(r));
		}

	}
	public void analyze(Rule start) {
		StringBuilder sb = new StringBuilder();
		analyze(start, sb);
		System.out.print(sb.toString());
	}
	public void analyze(Parser<?> start) {
		analyze(start.getRule());
	}

	public void descrive(Rule rule, StringBuilder sb) {
		appendAsLhs(rule, sb);
		sb.append(" ::= ");

		if(rule.kind() == Kind.SEQUENCE ||
		   rule.kind() == Kind.CHOICE) {

			char sepalator = rule.kind() == Kind.SEQUENCE
					? ' ' : '/';

			Iterator<Rule> i = rule.rules().iterator();
			appendAsRhs(i.next(), sb);
			while(i.hasNext()) {
				sb.append(sepalator);
				appendAsRhs(i.next(), sb);
			}
		} else if(ruleNames.containsKey(rule)) {
			appendAsRhsNoName(rule, sb);
		} else {
			appendAsRhs(rule, sb);
		}
		sb.append('\n');
	}

	public void descrive(Rule rule) {
		StringBuilder sb = new StringBuilder();
		descrive(rule, sb);
		System.out.print(sb.toString());
	}

	private void appendAsLhs(Rule rule, StringBuilder sb) {
		sb.append('<')
		  .append(ruleNames.getOrDefault(
				rule,
				String.valueOf(rule.id())))
		  .append(">");
	}

	private void appendAsRhsNoName(Rule rule, StringBuilder sb) {
		// 説明用文字列を持つ場合
		if(rule instanceof Rule.RuleWithDescription) {
			((Rule.RuleWithDescription) rule).description(sb);
			return;
		}

		// 内部規則を展開する場合
		Kind ruleKind = rule.kind();
		if(EXPAND_RULE.contains(ruleKind)) {
			Rule inner = rule.rules().get(0);

			switch(ruleKind) {
			case STAR:
				appendAsRhs(inner, sb);
				sb.append('*');
				return;
			case PLUS:
				appendAsRhs(inner, sb);
				sb.append('+');
				return;
			case OPTION:
				appendAsRhs(inner, sb);
				sb.append('?');
				return;
			case AND_PREDICATE:
				sb.append('&');
				appendAsRhs(inner, sb);
				return;
			case NOT_PREDICATE:
				sb.append('!');
				appendAsRhs(inner, sb);
				return;
			default:
				throw new Error();
			}
		}

		// それ以外はid
		sb.append('<')
		  .append(rule.id())
		  .append(">");
	}
	private void appendAsRhs(Rule rule, StringBuilder sb) {
		// 解析器で名前をつけた場合
		if(ruleNames.containsKey(rule)) {
			sb.append('<')
			  .append(ruleNames.get(rule))
			  .append(">");
			return;
		}

		appendAsRhsNoName(rule, sb);
	}
}

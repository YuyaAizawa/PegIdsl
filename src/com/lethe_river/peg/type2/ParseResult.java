package com.lethe_river.peg.type2;

public final class ParseResult<T> {
	public final T value;
	public final Location location;

	public ParseResult(T ast, Location location) {
		this.value = ast;
		this.location = location;
	}
}

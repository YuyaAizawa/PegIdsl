package com.lethe_river.peg.type2;

public final class ResultWithLocation<T> {
	private final T result;
	private final Location location;

	public ResultWithLocation(T result, Location location) {
		super();
		this.result = result;
		this.location = location;
	}

	public T getResult() {
		return result;
	}

	public Location getLocation() {
		return location;
	}
}

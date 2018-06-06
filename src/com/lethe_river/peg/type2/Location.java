package com.lethe_river.peg.type2;

public final class Location {

	public final Source src;
	public final int from;
	public final int to;

	Location(Source src, int from, int to) {
		if(from < 0 || to < from || src.length() < to) {
			throw new IllegalArgumentException();
		}
		this.src = src;
		this.from = from;
		this.to = to;
	}

	public Location concat(Location location) {
		if(this.src != location.src || this.to != location.from) {
			throw new IllegalArgumentException();
		}
		return new Location(src, this.from, location.to);
	}

	public String makeString() {
		return src.makeString(from, to);
	}

	@Override
	public String toString() {
		return "from: "+from+", to: "+to;
	}
}
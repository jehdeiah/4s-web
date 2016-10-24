package com.googlecode._4s_web.shared;

import java.io.Serializable;

/**
 * 사건의 시작과 끝 시점을 나타내는 클래스
 * 
 */
public class Interval implements Comparable<Interval>, Serializable {

	private static final long serialVersionUID = 1L;

	public double getBegin() {
		return begin;
	}

	public void setBegin(double begin) {
		this.begin = begin;
	}

	public double getEnd() {
		return end;
	}

	public void setEnd(double end) {
		this.end = end;
	}

	double begin;
	double end;

	public Interval() {
	}

	public Interval(double begin, double end) {
		setRange(begin, end);
	}

	public void setRange(double begin, double end) {
		this.begin = begin;
		this.end = end;
	}

	public int compareTo(Interval o) {
		if (o != null) {
			return (begin < o.begin) ? -1 : ((begin > o.begin) ? 1 : 0);
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(begin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(end);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Interval other = (Interval) obj;
		if (Double.doubleToLongBits(begin) != Double
				.doubleToLongBits(other.begin))
			return false;
		if (Double.doubleToLongBits(end) != Double.doubleToLongBits(other.end))
			return false;
		return true;
	}

}

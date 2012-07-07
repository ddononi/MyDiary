package kr.co.diary.data;

import java.io.Serializable;

/**
 * 내 위치 로깅 정보를 담을 클래스
 */
public class Logging implements Serializable {
	private static final long serialVersionUID = 2454738663079620648L;
	private int idx; 	// 인덱스
	private double lat; // 위도
	private double lon; // 경도
	private String tag; // 태그명
	private String date; // 로깅 날짜

	public double getLat() {
		return lat;
	}

	public void setLat(final double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(final double lon) {
		this.lon = lon;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(final String tag) {
		this.tag = tag;
	}

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(final int idx) {
		this.idx = idx;
	}



}

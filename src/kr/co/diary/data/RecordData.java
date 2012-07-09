package kr.co.diary.data;

import java.io.Serializable;

/**
 * 음성녹음 데이터
 */
public class RecordData implements Serializable {
	private static final long serialVersionUID = 2454738663079620648L;
	private int idx; // 인덱스
	private String subject; // 태그명
	private String filePath; // 파일패스
	private String date; // 저장날짜

	public int getIdx() {
		return idx;
	}

	public void setIdx(final int idx) {
		this.idx = idx;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(final String filePath) {
		this.filePath = filePath;
	}

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}

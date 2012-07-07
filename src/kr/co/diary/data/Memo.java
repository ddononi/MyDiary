package kr.co.diary.data;

/**
 *	메모 데이터 저장 클래스
 */
public class Memo {
	private int idx;			// 인덱스 번호
	private String memo;	// 매모 내용
	private String date;		// 날짜

	public int getIdx() {
		return idx;
	}

	public void setIdx(final int idx) {
		this.idx = idx;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(final String memo) {
		this.memo = memo;
	}

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

}

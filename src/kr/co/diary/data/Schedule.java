package kr.co.diary.data;

/**
 * 스케쥴 저장 클래스
 */
public class Schedule {
	private int idx; // 인덱스
	private String todo; // 일정
	private String date; // 날짜
	private int tagColor; // 태그 칼라
	private int alarm; // 알람여부
	private int isDoneAlarm; // 알람발생여부
	private String startTime; // 시작시간
	private String endTime; // 종료시간

	private String name;	// 이름
	
	public int getIdx() {
		return idx;
	}

	public void setIdx(final int idx) {
		this.idx = idx;
	}

	public String getTodo() {
		return todo;
	}

	public void setTodo(final String todo) {
		this.todo = todo;
	}

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	public int getTagColor() {
		return tagColor;
	}

	public void setTagColor(final int tagColor) {
		this.tagColor = tagColor;
	}

	public int getAlarm() {
		return alarm;
	}

	public void setAlarm(final int alarm) {
		this.alarm = alarm;
	}

	public int getIsDoneAlarm() {
		return isDoneAlarm;
	}

	public void setIsDoneAlarm(final int isDoneAlarm) {
		this.isDoneAlarm = isDoneAlarm;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(final String endTime) {
		this.endTime = endTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(final String startTime) {
		this.startTime = startTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

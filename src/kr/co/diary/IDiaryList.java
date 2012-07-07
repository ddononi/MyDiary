package kr.co.diary;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 *	다이어리 메모 , 일정 리스트, 로깅 내역의 인터페이스
 */
public interface IDiaryList extends OnItemLongClickListener, OnItemClickListener {
	public void setArrayList(String date);	// ArrayList설정
	public int deleteItem(int index);		// 리스트 아이템 삭제
	public int addItem();					// 리스트 아이템 추가
	public void setListAnimation();			// 리스트 에니메이션
}

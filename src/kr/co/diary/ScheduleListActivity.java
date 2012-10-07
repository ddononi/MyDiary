package kr.co.diary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kr.co.diary.data.Schedule;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleListActivity extends MyActivity
		implements IDiaryList {

	private final ArrayList<Schedule> list = new ArrayList<Schedule>();
	private ScheduleAdapter adapter;	// 메모 커스텀 아답터

	private ListView scheduleList;		// 스케쥴 리스트뷰
    @Override
	public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_list);
		Calendar  cal = Calendar.getInstance();

		Intent intent = getIntent();
		// 이전 엑티비티에서 넘온 날짜 정보가 있으면 설정
        cal = Calendar.getInstance();
		if(intent.hasExtra("selectedDay")){
	        int[] selectedDay = intent.getIntArrayExtra("selectedDay");
	        // 날짜 설정
	        cal.set(Calendar.YEAR, selectedDay[0]);
	        cal.set(Calendar.MONTH, selectedDay[1]);
	        cal.set(Calendar.DAY_OF_MONTH, selectedDay[2]);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(cal.getTime());
        setArrayList(date);

        TextView titleTV = (TextView)findViewById(R.id.title);
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy년 MM월 dd일");
        titleTV.setText(sdf2.format(cal.getTime()) + " 일정");

        scheduleList = (ListView)findViewById(R.id.list);
        adapter = new ScheduleAdapter(list);
        scheduleList.setAdapter(adapter);
        // 메모 리스트 이벤트 처리
        scheduleList.setOnItemClickListener(this);
        scheduleList.setOnItemLongClickListener(this);
        setListAnimation();
    }

	/**
	 *	리스트 에니메이션 설정
	 */
    @Override
	public void setListAnimation() {
		AnimationSet set = new AnimationSet(true);
		Animation rtl = new TranslateAnimation(
		    Animation.RELATIVE_TO_SELF, 1.0f,Animation.RELATIVE_TO_SELF, 0.0f,
		    Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
		);
		rtl.setDuration(300);
		set.addAnimation(rtl);

		Animation alpha = new AlphaAnimation(0.0f, 0.5f);
		alpha.setDuration(300);
		set.addAnimation(alpha);

		LayoutAnimationController controller =
	        new LayoutAnimationController(set, 0.5f);
		scheduleList.setLayoutAnimation(controller);

	}


	@Override
	final public void setArrayList(final String date) {
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		Log.i(DEBUG_TAG, date);
		Schedule sc =null;
		// 년월일 조건검색 시작시간순으로
		cursor = db.query(DBHelper.SCHEDULE_TABLE, null,
				"substr(s_time, 1,10) = ? ", new String[]{date, }, null, null, "s_time asc");
		if(cursor.getCount() <= 0){	// 내역이 없으면 끝낸다.
			Toast.makeText(this, "내역이 없습니다.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if( cursor.moveToFirst() ){	// cursor가 있으면 처음으로 이동
			do{
				sc = new Schedule();
				// 데이터를 메모 객체에 넣어준다.
				sc.setIdx(cursor.getInt(cursor.getColumnIndex("no")));
				sc.setTodo(cursor.getString(cursor.getColumnIndex("todo")));
				sc.setStartTime(cursor.getString(cursor.getColumnIndex("s_time")));
				sc.setEndTime(cursor.getString(cursor.getColumnIndex("e_time")));
				sc.setAlarm(cursor.getInt(cursor.getColumnIndex("alarm")));
				// list에 추가
				list.add(sc);
				Log.i(DEBUG_TAG, "load schedule-->");
			}while(cursor.moveToNext());
		}
		// 디비를 닫아준다.
		cursor.close();
		db.close();
	}

	/**
	 * 메모 리스트 클릭시 메모 보기
	 */
	@Override
	public void onItemClick(final AdapterView<?> av, final View v, final int pos, final long arg3) {
		Adapter adpater = av.getAdapter();			// 아답터를 얻어와서
		Schedule sche = (Schedule)adpater.getItem(pos);		// 해당 메모 객체를 억는다.
		sche.getIdx();
	}

	/**
	 * Longclick시 해당 메모 삭제 처리
	 */
	@Override
	public boolean onItemLongClick(final AdapterView<?> av, final View v, final int pos,
			final long arg3) {
		// 삭제전 confirm 처리
		final AdapterView<?> tmpAv = av;
		final int position = pos;
		new AlertDialog.Builder(this)
		.setTitle("알림")
		.setMessage("삭제하시겠습니까?").setPositiveButton("삭제", new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				// TODO Auto-generated method stub
				Adapter adpater = tmpAv.getAdapter();			// 아답터를 얻어와서
				Schedule sche = (Schedule)adpater.getItem(position);		// 해당 메모 객체를 억는다.
				int index = sche.getIdx();					// 인덱스 번호
				if( deleteItem(index) > 0){	// 삭제가 정상적으로  처리되엇으면
					Toast.makeText(ScheduleListActivity.this,
							"삭제되었습니다.", Toast.LENGTH_SHORT).show();
					list.remove(position);				 // 리스트에서도 제거해줌
					adapter.notifyDataSetChanged();	 //	아답터에게 데이터 변경됨을 통지
				}

			}
		}).setNegativeButton("취소", null).show();

		return true;
	}

	/**
	 * 해당 일정를 삭제한다.
	 * @param index
	 * 	일정 인덱스 번호
	 * @return
	 * 	삭제된 수
	 */
	@Override
	final public int deleteItem(final int index) {
		int result = 0;	// 삭제된 row 갯수
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase(); // 쓰기 모드
		// 해당 인덱스번호로 검색하여 데이터를 지워준다.
		result = db.delete(DBHelper.SCHEDULE_TABLE, "no =?", new String[]{index+"",});
		db.close();
		dbhp.close();

		return result;
	}


	@Override
	public int addItem() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		startActivity(new Intent(this, MyDiaryActivity.class));
		finish();
	}




}

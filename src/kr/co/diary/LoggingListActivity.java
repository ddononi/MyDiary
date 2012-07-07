package kr.co.diary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kr.co.diary.data.Logging;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.widget.Toast;

/**
 * @author ddononi
 *
 */
public class LoggingListActivity extends BaseActivity
implements IDiaryList {
	private final ArrayList<Logging> list = new ArrayList<Logging>();
	private LoggingAdapter adapter;		// 로깅 커스텀 아답터
	private ListView loggingList;		// 로깅리스트
    @Override
	public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logging_list);
		Calendar  cal = Calendar.getInstance();

        // 날짜 설정
        cal = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(cal.getTime());
        setArrayList(date);

        loggingList = (ListView)findViewById(R.id.list);
        adapter = new LoggingAdapter(list);
        loggingList.setAdapter(adapter);
        // 메모 리스트 이벤트 처리
        loggingList.setOnItemClickListener(this);
        loggingList.setOnItemLongClickListener(this);
        setListAnimation();
    }
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
				Logging log = (Logging)adpater.getItem(position);		// 해당 메모 객체를 억는다.
				int index = log.getIdx();					// 인덱스 번호
				if( deleteItem(index) > 0){	// 삭제가 정상적으로  처리되엇으면
					Toast.makeText(LoggingListActivity.this,
							"삭제되었습니다.", Toast.LENGTH_SHORT).show();
					list.remove(position);				 // 리스트에서도 제거해줌
					adapter.notifyDataSetChanged();	 //	아답터에게 데이터 변경됨을 통지
				}

			}
		}).setNegativeButton("취소", null).show();

		return true;
	}

	@Override
	public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setArrayList(final String date) {
		// TODO Auto-generated method stub
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		Log.i(DEBUG_TAG, date);	//date 사용안함
		Logging log = null;
		// no 역 인덱스 순으로
		cursor = db.query(DBHelper.MY_PLACE_TABLE, null, null, null, null, null, "no desc");
		if(cursor.getCount() <= 0){	// 내역이 없으면 끝낸다.
			Toast.makeText(this, "내역이 없습니다.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		double lon, lat;
		String loggingDate;
		if( cursor.moveToFirst() ){
			do{
				log = new Logging();
				// 데이터를 메모 객체에 넣어준다.
				log.setIdx(cursor.getInt(cursor.getColumnIndex("no")));
				lat = Double.valueOf(cursor.getString(cursor.getColumnIndex("lat")));
				log.setLat(lat);
				lon = Double.valueOf(cursor.getString(cursor.getColumnIndex("lon")));
				log.setLat(lon);
				log.setTag(cursor.getString(cursor.getColumnIndex("tag")));
				loggingDate = cursor.getString(cursor.getColumnIndex("date"));
				log.setDate(loggingDate.substring(0, 10));
				// list에 추가
				list.add(log);
			}while(cursor.moveToNext());
		}
		// 디비를 닫아준다.
		cursor.close();
		db.close();
	}

	@Override
	public int deleteItem(final int index) {
		int result = 0;	// 삭제된 row 갯수
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase(); // 쓰기 모드
		// 해당 인덱스번호로 검색하여 데이터를 지워준다.
		result = db.delete(DBHelper.MY_PLACE_TABLE, "no =?", new String[]{index+"",});
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
		loggingList.setLayoutAnimation(controller);
	}

}

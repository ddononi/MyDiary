package kr.co.diary;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kr.co.diary.data.RecordData;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
 *	녹음 전체 리스트
 *	전체 메모 리스트 참고
 */
public class RecordListActivity extends BaseActivity
implements IDiaryList {
	private final ArrayList<RecordData> list = new ArrayList<RecordData>();
	private RecordAdapter adapter;		// 로깅 커스텀 아답터
	private ListView recordList;		// 로깅리스트
	private String delFilepath;			// 삭제시 파일경로
    @Override
	public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rec_list_layout);
		Calendar  cal = Calendar.getInstance();

        // 날짜 설정
        cal = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(cal.getTime());
        setArrayList(date);

        recordList = (ListView)findViewById(R.id.list);
        adapter = new RecordAdapter(list);
        recordList.setAdapter(adapter);
        // 메모 리스트 이벤트 처리
        recordList.setOnItemClickListener(this);
        recordList.setOnItemLongClickListener(this);
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
				RecordData rec = (RecordData)adpater.getItem(position);		// 해당 메모 객체를 억는다.
				int index = rec.getIdx();						// 인덱스 번호
				delFilepath = rec.getFilePath();
				if( deleteItem(index) > 0){	// 삭제가 정상적으로  처리되엇으면
					Toast.makeText(RecordListActivity.this,
							"삭제되었습니다.", Toast.LENGTH_SHORT).show();
					list.remove(position);				 // 리스트에서도 제거해줌
					adapter.notifyDataSetChanged();	 //	아답터에게 데이터 변경됨을 통지
				}

			}
		}).setNegativeButton("취소", null).show();

		return true;
	}

	/**
	 * 리스트를 선택시 녹음파일을 재생한다.
	 */
	@Override
	public void onItemClick(final AdapterView<?> arg0, final View v, final int pos, final long arg3) {
		// 녹음 데이터 객체 얻기
		RecordData rec = (RecordData)adapter.getItem(pos);
		// filepath로부터 uri 생성
		Uri uri = Uri.parse("file:///" + rec.getFilePath());
		// 암시적 인텐트 선언
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		// 타입 설정
		intent.setDataAndType(uri, "audio/mp3");
		startActivity(intent);
	}

	@Override
	public void setArrayList(final String date) {
		// TODO Auto-generated method stub
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		Log.i(DEBUG_TAG, date);	//date 사용안함
		RecordData rec = null;
		// no 역 인덱스 순으로
		cursor = db.query(DBHelper.REC_TABLE, null, null, null, null, null, "no desc");
		if(cursor.getCount() <= 0){	// 내역이 없으면 끝낸다.
			Toast.makeText(this, "내역이 없습니다.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		String loggingDate;
		if( cursor.moveToFirst() ){
			do{
				rec = new RecordData();
				// 데이터를 메모 객체에 넣어준다.
				rec.setIdx(cursor.getInt(cursor.getColumnIndex("no")));
				rec.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
				rec.setFilePath(cursor.getString(cursor.getColumnIndex("filepath")));
				loggingDate = cursor.getString(cursor.getColumnIndex("date"));
				rec.setDate(loggingDate.substring(0, 10));
				// list에 추가
				list.add(rec);
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
		result = db.delete(DBHelper.REC_TABLE, "no =?", new String[]{index+"",});
		if(result > 0){	// 디비에서 정상적으로 지워졌으면
			// 녹음 파일이 있는지 체크하고 파일을 지워준다.
			File file = new File(delFilepath);
			if(file.isFile()){	// 파일이 존재하면
				file.delete();	// 삭제 처리
			}
		}
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
		recordList.setLayoutAnimation(controller);
	}

}

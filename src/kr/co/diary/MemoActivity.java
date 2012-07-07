package kr.co.diary;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import kr.co.diary.data.Memo;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 *	메모 내용을 보여주는 엑티비티
 */
public class MemoActivity extends BaseActivity implements OnClickListener {
	private Calendar cal;				// 캘런더
	private final Memo memo = new Memo();		// 메모 객체
	// 엘리먼트
	private Button saveBtn;
	private EditText memoET;

	private int index = -1;	// 메모 인덱스
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo);

        TextView dateTV = (TextView)findViewById(R.id.date);
        saveBtn = (Button)findViewById(R.id.save_btn);
        memoET = (EditText)findViewById(R.id.memo);
        cal = Calendar.getInstance();	// 캘런더 가져오기

        Intent intent = getIntent();	// 인텐트 데이터 가져오기
        index = intent.getIntExtra("index", -1);
        Log.i(DEBUG_TAG, "index-->" + index);
        if(index != -1){	// 이전 엑티비티에서 넘오온 인덱스가 있으면
        	fillMemo(index);
        }else{
        	setDate();
        }

        String str = String.format("%04d년 %02d월 %02d일", cal.get(Calendar.YEAR),
        		cal.get(Calendar.MONDAY) + 1, cal.get(Calendar.DAY_OF_MONTH));
        dateTV.setText(str);

        saveBtn.setOnClickListener(this);
    }

	/**
	 * 메모 내용이 있으면 디비에서 내용을 불러와 메모를 채운다.
	 */
	private void fillMemo(final int index) {
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		cursor = db.query(DBHelper.MEMO_TABLE, null, "no = ?", new String[]{index+"", }, null, null, null);
		if( cursor.moveToFirst() ){
			// 데이터를 메모 객체에 넣어준다.
			memo.setIdx(cursor.getInt(cursor.getColumnIndex("no")));
			memo.setMemo(cursor.getString(cursor.getColumnIndex("memo")));
			memo.setDate(cursor.getString(cursor.getColumnIndex("date")));
			memoET.setText(memo.getMemo());
		}
		// 디비를 닫아준다.
		cursor.close();
		db.close();
		// 날짜를 설정
        int year = Integer.valueOf(memo.getDate().substring(0,4));
        int month = Integer.valueOf(memo.getDate().substring(5,7)) -1;
        int day = Integer.valueOf(memo.getDate().substring(8,10));
        // 가져온 데이터로 년 월일 설정
        cal.set(year, month, day);
	}

	/**
	 * 넘어온날짜로 설정
	 */
	private void setDate() {
		Intent intent = getIntent();
        int[] selectedDay = intent.getIntArrayExtra("selectedDay");
        // 날짜 설정
        if(selectedDay != null){
            cal.set(Calendar.YEAR, selectedDay[0]);
            cal.set(Calendar.MONTH, selectedDay[1]) ;
            cal.set(Calendar.DAY_OF_MONTH, selectedDay[2]);
        }
	}

	@Override
	public void onClick(final View v) {
		// TODO Auto-generated method stub
		String memo = memoET.getText().toString();
		if(TextUtils.isEmpty(memo)){
			Toast.makeText(this, "메모를 입력하세요", Toast.LENGTH_SHORT).show();
			return;
		}

		// 날짜를 저장할 데이터 형식
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long result;
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase(); // 쓰기 모드
		ContentValues cv = new ContentValues();
		cv.put("memo", memo);
		cv.put("date", sdf.format(cal.getTime()));
		String msg = "";
		if(index > 0){	// 인덱스가 있으면 업데이트 처리
			result = db.update(DBHelper.MEMO_TABLE, cv, "no = ?", new String[]{index+"",});
			msg = "수정되었습니다.";
			setResult(1);
		}else{
			result = db.insert(DBHelper.MEMO_TABLE, null, cv);
			msg = "저장되었습니다.";
		}

		if(result> 0){
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			finish();
		}
		db.close();
		dbhp.close();


	}
}

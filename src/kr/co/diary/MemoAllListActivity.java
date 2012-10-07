package kr.co.diary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kr.co.diary.data.Memo;
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

public class MemoAllListActivity extends MyActivity
		implements IDiaryList {

	private final ArrayList<Memo> list = new ArrayList<Memo>();
	private MemoAdapter adapter;	// 메모 커스텀 아답터
	private ListView memoList;		// 메모리스트
	private Calendar cal;			// 날짜설정을 위한 캘린더

	private static int DELETE_CODE = 1;	// 삭제 결과 코드
    @Override
	public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_list);
		this.cal = Calendar.getInstance();

		Intent intent = getIntent();
        setArrayList(null);
        TextView titleTV = (TextView)findViewById(R.id.title);
        titleTV.setText("전체메모 내역");

        memoList = (ListView)findViewById(R.id.list);
        adapter = new MemoAdapter(list);
        memoList.setAdapter(adapter);
        // 메모 리스트 이벤트 처리
        memoList.setOnItemClickListener(this);
        memoList.setOnItemLongClickListener(this);
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
		memoList.setLayoutAnimation(controller);

	}


	/**
	 * 메모 리스트 클릭시 메모 보기
	 */
	@Override
	public void onItemClick(final AdapterView<?> av, final View v, final int pos, final long arg3) {
		Adapter adpater = av.getAdapter();			// 아답터를 얻어와서
		Memo memo = (Memo)adpater.getItem(pos);		// 해당 메모 객체를 억는다.
		Intent intent = new Intent(this, MemoActivity.class);
		intent.putExtra("index", memo.getIdx());	// 메모 인덱스 번호를 싣어 보냄
		startActivityForResult(intent, 0);
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
				Memo memo = (Memo)adpater.getItem(position);		// 해당 메모 객체를 억는다.
				int index = memo.getIdx();					// 인덱스 번호
				if( deleteItem(index) > 0){	// 삭제가 정상적으로  처리되엇으면
					Toast.makeText(MemoAllListActivity.this,
							"삭제되었습니다.", Toast.LENGTH_SHORT).show();
					list.remove(position);				 // 리스트에서도 제거해줌
					adapter.notifyDataSetChanged();	 //	아답터에게 데이터 변경됨을 통지
				}

			}
		}).setNegativeButton("취소", null).show();

		return true;
	}

	@Override
	final public void setArrayList(String date) {
		// TODO Auto-generated method stub
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		Memo memo =null;
		// 년월일 조건검색
		cursor = db.query(DBHelper.MEMO_TABLE, null,
				null, null, null, null, null);
		if(cursor.getCount() <= 0){	// 내역이 없으면 끝낸다.
			Toast.makeText(this, "내역이 없습니다.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if( cursor.moveToFirst() ){
			do{
				memo = new Memo();
				// 데이터를 메모 객체에 넣어준다.
				memo.setIdx(cursor.getInt(cursor.getColumnIndex("no")));
				memo.setMemo(cursor.getString(cursor.getColumnIndex("memo")));
				memo.setDate(cursor.getString(cursor.getColumnIndex("date")));
				// list에 추가
				list.add(memo);
			}while(cursor.moveToNext());
		}
		// 디비를 닫아준다.
		cursor.close();
		db.close();
	}

	/**
	 * 해당 메모를 삭제한다.
	 * @param index
	 * 	메모 인덱스 번호
	 * @return
	 * 	삭제된 수
	 */
	@Override
	public int deleteItem(final int index) {
		int result = 0;	// 삭제된 row 갯수
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase(); // 쓰기 모드
		// 해당 인덱스번호로 검색하여 데이터를 지워준다.
		result = db.delete(DBHelper.MEMO_TABLE, "no =?", new String[]{index+"",});
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
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == DELETE_CODE){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(cal.getTime());
			list.clear();	// 리스트를 클리어한후 새로 리스트 갱신
	        setArrayList(date);
	        adapter.notifyDataSetChanged();	 //	아답터에게 데이터 변경됨을 통지
		}
	}


}

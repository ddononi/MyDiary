package kr.co.diary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *	SQLiteOpenHelper 재정의 클래스
 *	디비를 초기화 해주고 테이블을 생성한다.
 */
public class DBHelper extends SQLiteOpenHelper {
	public static final int DB_VER = 1;					// 디비버젼
	public static final String DB_FILE = "diary.db";	// 디비파일이음
	public static final String MEMO_TABLE = "diary_memo";	// 메모테이블
	public static final String SCHEDULE_TABLE = "diary_schedule";	// 일정테이블
	public static final String MY_PLACE_TABLE = "diary_my_place";	// 위치저장 테이블
	public static final String REC_TABLE = "diary_rec";	// 위치저장 테이블	
	public DBHelper(final Context context){
		super(context, DB_FILE, null, DB_VER);
	}


	/** 디비 생성시 테이블을 만들어준다. */
	@Override
	public void onCreate(final SQLiteDatabase db) {	// db가 생성될때 테이블도 생성
		// TODO Auto-generated method stub
		// 메모 테이블 생성
		// 인덱스  메모내용 날짜
		String sql = "CREATE TABLE "+ MEMO_TABLE +" (no INTEGER PRIMARY KEY," +
				     " memo TEXT NOT NULL, date TEXT NOT NULL );";
		db.execSQL(sql);
		// 일정 테이블 만들기
		// 인덱스, todo, 시작시간, 종료시간, 태그 칼라, 알람, 알람발생여부
        db.execSQL("create table "+ SCHEDULE_TABLE
        		+ " ( no integer primary key autoincrement, date TEXT, "
        		+ " todo TEXT, s_time TEXT, e_time TEXT, tag_color TEXT, alarm integer," +
        		" is_alarming integer );");
        // 위치저장 테이블 만들기
        // 인덱스, 위도, 경도, 로깅메세지, 날짜
        db.execSQL("create table "+ MY_PLACE_TABLE
        		+ " ( no integer primary key autoincrement, lat TEXT, "
        		+ " lon TEXT, tag TEXT, date TEXT d, alarm INTEGER);");
        
        // 음성 녹음 테이블
        // 인덱스, 파일경로, 제목
        db.execSQL("create table "+ REC_TABLE
        		+ " ( no integer primary key autoincrement, filepath TEXT, "
        		+ " subject TEXT, date TEXT);");        
	}

	@Override
	public void onOpen(final SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		// sqlite가 업그레이드 됐을경우 이전에 있던 테이블은 없앤다.
        db.execSQL("DROP TABLE IF EXISTS "+ MEMO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ SCHEDULE_TABLE);
        onCreate(db);
	}
}
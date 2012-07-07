package kr.co.diary;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *	녹음 액티비티
 *	MediaScannerConnectionClient를 구현하여 미디어 스캐너 동작상태를 체크한다.
 *	MediaScannerConnection class 를 사용하여 녹음파일 미디어 프로바이더에 등록한다.
 */
public class RecordDialog extends Dialog implements MediaScannerConnectionClient {
	private Context context;
	private LayoutInflater inflater;	// 레이아웃 전개를 위한 인플레이터	
	// 미디어 스캐닝 상태값
	private static final int STATE_IDLE = 0;
	private static final int STATE_RECORDING = 1;
	private static int RECORDING_STATE = STATE_IDLE;
	
	private MediaRecorder mRecorder;
	private Button recordButton;
	
	private String mediaPath = "";
	private File mediaFile = null;
	private MediaScannerConnection mMediaConnection;
	private EditText filenameEt;
	private String fileName;
	
	public RecordDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;	
		initLayout();
	}	

	/**
	 * 레이아웃 및 리스너 설정
	 */
	private void initLayout() {
		// 다이얼로그를 전개시킬 인플레이터 서비스를 얻어오자
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.rec_dialog, null);		
		this.setContentView(layout);	// 다이얼로그에 layout을 입힌다.				
		this.setTitle("녹음하기");		
		
		// 미디어 스캐너 
		mMediaConnection = new MediaScannerConnection(context, this);
		filenameEt = (EditText)layout.findViewById(R.id.rec_subject);
		// 녹음 버튼 설정
		recordButton = (Button)layout.findViewById(R.id.rec_btn);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procAudio(RECORDING_STATE);
			}
		});
		/* 
		recordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				procAudio(RECORDING_STATE);
			}
		});
		*/
	}

	@Override
	public void onMediaScannerConnected() {
		// 미디어 스캐너 스캔하기
		mMediaConnection.scanFile(mediaFile.getPath(), "audio/mpeg");
	}

	/**
	 * 스캔이 완료 되었을때
	 */
	@Override
	public void onScanCompleted(String path, Uri uri) {
		/*
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "파일 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show();
			}
		});
		*/
		mMediaConnection.disconnect();	// 미디어스캐너 연결 해제
		RECORDING_STATE = STATE_IDLE;	// 녹음상태가 아님으로 설정
	}
	
	/**
	 * 	녹음 상태에 따라 오디오 처리
	 */
	private void procAudio(int state){
		switch(state){
		case STATE_IDLE : 		// 녹음중이 아니면
			// 녹음 제목 입력 검증
			if(TextUtils.isEmpty(filenameEt.getText())){	// 내용이 없으면
				Toast.makeText(context, "제목을 입력하세요", Toast.LENGTH_SHORT).show();
				return;
			}
			
			initRecorder();				// 오디오 초기화
			mRecorder.start();			// 녹음 시작
			// 녹음 상태로 변경
			RECORDING_STATE = STATE_RECORDING;
			Toast.makeText(context, "녹음 시작", Toast.LENGTH_SHORT).show();
			filenameEt.setEnabled(false);	// 파일이름 비활성화
			recordButton.setText("녹음 중지");
			break;
		case STATE_RECORDING :	// 녹음중이면	
			if(mRecorder == null){
				return;
			}
			mRecorder.stop();		// 녹음 종료
			mRecorder.release();	// MediaRecorder  realease
			registerAudio();		// 미디어 스캐너에 등록
			insertDB();
			filenameEt.setText("");			// 파일이름 지워주기
			filenameEt.setEnabled(true);	// 파일이름 활성화
			recordButton.setText("녹음 시작");
			this.dismiss();
			break;
		}
	}
	
	/**
	 * 녹음된 파일을 디비에 저장한다.
	 */
	private void insertDB() {
		long result;
		// 날짜를 저장할 데이터 형식
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		DBHelper dbhp = new DBHelper(context);
		SQLiteDatabase db = dbhp.getWritableDatabase(); // 쓰기 모드
		ContentValues cv = new ContentValues();
		cv.put("filename", mediaPath);							// 파일이름
		cv.put("subject", filenameEt.getText().toString());	// 제목	
		cv.put("date", sdf.format(cal.getTime()));				// 저장 날짜
		result = db.insert(DBHelper. REC_TABLE, null, cv);	// 디비에 삽입
		db.close();
		dbhp.close();
	}

	/**
	 * 미디어 프로바이터에 등록하여 음악 플레이어가 인식 할수 있도록 한다.
	 */
	private void registerAudio() {
		if(mMediaConnection.isConnected()){
			mMediaConnection.disconnect();
		}
		mMediaConnection.connect();
	}

	/**
	 * MediaRecorder 초기화 하기
	 */
	private void initRecorder(){
		//	중복파일이 되지 않도록 현재 시간을 파일명으로 설정한다.
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		fileName = sdf.format(cal.getTime());
		// 파일경로및 파일명 설정
		mediaPath = Environment.getExternalStorageDirectory().getPath() + "/" +  fileName + ".mp3";
		mediaFile = new File(mediaPath);
		
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);			// 마이크
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);	// 녹음포맷설정
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);	// 
		mRecorder.setOutputFile(mediaFile.getAbsolutePath());	// 녹음파일 경로 설정
		
		try{
			mRecorder.prepare();		// 녹음 준비
		}catch(IllegalStateException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	

	
}

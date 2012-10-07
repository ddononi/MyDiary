package kr.co.diary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import kr.co.diary.map.MapActivity;
import kr.co.diary.map.RegMapActivity;

import org.apache.http.client.ClientProtocolException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

/**
 * 달력 메인 엑티비티
 */
public class MyDiaryActivity extends MyActivity implements OnClickListener {
	// var
	private ArrayList<Button> list; // 날짜 버튼들
	// date
	private Calendar cal; // 달력 설정을 위한 캘린더
	private int currentMonth, currentYear; // 현재달 및 년
	private final int[] selectedDay = new int[3]; // 선택한 년월일
	private final String[] dayWeek = { // 요일 배열
	"일", "월", "화", "수", "목", "금", "토" };
	// / element
	private TextView monthTV; // 상단 월 텍스트
	private ProgressBar loadingBar; // 날씨에 보여줄 로딩바
	private ViewSwitcher switcher; // 상단 월 에니메이션을 위한 뷰 스위쳐
	private View preBtn;			// 이전 선택 버튼

	// animation
	private Animation ani; // 버튼 에니메이션
	// map & geo
	private Location location;
	// 위치 리스너 처리
	private final LocationListener loclistener = new LocationListener() {
		// 위치가 변경되면
		@Override
		public void onLocationChanged(final Location location) {
			Log.w(DEBUG_TAG, "onLocationChanged");
			// getLocation();
			// 위치 수신
			MyDiaryActivity.this.location = location;
		}

		@Override
		public void onProviderDisabled(final String provider) {
			Log.w(DEBUG_TAG, "onProviderDisabled");
		}

		@Override
		public void onProviderEnabled(final String provider) {
			Log.w(DEBUG_TAG, "onProviderEnabled");
		}

		@Override
		public void onStatusChanged(final String provider, final int status,
				final Bundle extras) {
			Log.w(DEBUG_TAG, "onStatusChanged");
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		cal = Calendar.getInstance();
		// 시스템 시간으로 설정
		cal.setTimeInMillis(System.currentTimeMillis());
		currentYear = cal.get(Calendar.YEAR); // 현재 year
		currentMonth = cal.get(Calendar.MONTH) + 1; // 현재달을 저장
		ani = AnimationUtils.loadAnimation(this, R.anim.alpha); // 알파 애니메이션 설정
		// 상단 Month의 뷰 스위쳐를 얻은후 getCurrentView로 Month의 TextView를 얻는다.
		switcher = (ViewSwitcher) findViewById(R.id.switcher_month);
		monthTV = (TextView) switcher.getCurrentView();
		// 이전달 버튼
		ImageButton prevtBtn = (ImageButton) findViewById(R.id.prev);
		// 다음달 버튼
		ImageButton nextBtn = (ImageButton) findViewById(R.id.next);
		// 날씨 로딩바
		loadingBar = (ProgressBar) findViewById(R.id.progressBar);
		// 버튼 이벤트를 위해 스위쳐의 자식 텍스트뷰 후킹
		TextView swticherMonthTV = (TextView) findViewById(R.id.month);
		prevtBtn.setOnClickListener(this);
		nextBtn.setOnClickListener(this);
		swticherMonthTV.setOnClickListener(this);

		initElem();					 // 날짜 table 설정
		setDate();					 // 날짜 설정
		initCheckMemo(); 		 // 메모 체크
		initCheckSchedule();		 // 일정 체크
		ani = AnimationUtils.loadAnimation(this, R.anim.alpha); // 날씨 정보 가져오기
		AsyncTaskWeather asyncWeather = new AsyncTaskWeather();
		asyncWeather.execute();

	}

	/**
	 * 서비스 등록
	 */
	private void doStartService() {
		// TODO Auto-generated method stub
		// 서비스로 알람설정
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean isSetAlarm = sp.getBoolean("alarm", true);
		if (isSetAlarm) { // 서비스가 설정되어 있으면 서비스시작
			Intent serviceIntent = new Intent(this, AlarmService.class);
			stopService(serviceIntent); // 먼저 서비스를 중지한후 다시 시작
			startService(serviceIntent);
			Log.i(DEBUG_TAG, "service start!!");
		}

	}


	/**
	 * 달력 생성 및 초기화
	 */
	private void initElem() {
		list = new ArrayList<Button>(); // 달력 버튼 리스트
		TableLayout table = (TableLayout) findViewById(R.id.days_table);
		TableLayout.LayoutParams params = new TableLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		TableRow.LayoutParams btnParams = new TableRow.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		btnParams.setMargins(2, 2, 2, 2); // 마진 넣기
		for (int week = 0; week < 6; week++) { // 한주에 한 열씩
			TableRow tr = new TableRow(this);
			tr.setLayoutParams(params);
			for (int day = 0; day < 7; day++) { // 한주의 날짜 7일
				Button dayBtn = new Button(this);
				dayBtn.setBackgroundResource(R.drawable.selector);
				if (day == SUNDAY) { // 일요일이면 빨깡 색
					dayBtn.setTextColor(Color.parseColor("#FF4406"));
				} else if (day == SATURDAY) { // 토요일이면 파랑색
					dayBtn.setTextColor(Color.parseColor("#002765"));
				} else { // 평일이면 흰색
					dayBtn.setTextColor(Color.parseColor("#111111"));
				}
				// 날짜 버튼 레이아웃 설정
				dayBtn.setShadowLayer(1, 1, 1, Color.parseColor("#333333"));
				dayBtn.setTextSize(28);
				dayBtn.setPadding(2, 2, 2, 2);
				dayBtn.setText("0");
				dayBtn.setLayoutParams(btnParams);
				final int weekIndex = day;
				dayBtn.setOnClickListener(new OnClickListener() {

					/**
					 * 선택한 버튼 포커스 주기
					 */
					@Override
					public void onClick(final View v) {
						v.setBackgroundColor(R.color.select);
						// 이전 버튼 포커스 제거
						if(preBtn != null){
							if(preBtn.getTag() != null){
								preBtn.setBackgroundResource((Integer)preBtn.getTag());
							}else{
								preBtn.setBackgroundResource(R.drawable.selector);
							}
						}
						preBtn = v;
					}
				});

				// 날짜에 이벤트 설정
				dayBtn.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(final View v) {
						// 날짜를 클릭하면 선택한 년월일저장
						selectedDay[0] = cal.get(Calendar.YEAR);
						selectedDay[1] = cal.get(Calendar.MONTH);
						// 선택한 날 저장
						final String day = ((Button) v).getText().toString();
						selectedDay[2] = Integer.valueOf(day);
						int firstWeekDay = cal.get(Calendar.DAY_OF_WEEK);
						showDayDialog(weekIndex, day);
						return true;
					}

				});

				dayBtn.setGravity(Gravity.CENTER_HORIZONTAL);
				tr.addView(dayBtn);
				list.add(dayBtn);
			}
			table.addView(tr);
		}
	}

	/**
	 * 날짜 선택시 선택 다이얼로그 띄우기
	 * @param weekIndex
	 * @param day
	 */
	private void showDayDialog(final int weekIndex, final String day) {
		new AlertDialog.Builder(MyDiaryActivity.this)
				.setTitle(
						selectedDay[0] + "년 "
								+ Integer.valueOf(selectedDay[1] + 1) + "월 "
								+ day + "일 " + dayWeek[weekIndex] + "요일")
				// 메뉴 항목
				.setItems(R.array.todo_item,
						// 다이얼로그 메뉴 선택시
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int which) {
								Intent intent;
								switch (which) {
								case 0: // 메모하기로 이동
									intent = new Intent(MyDiaryActivity.this,
											MemoActivity.class);
									// 오늘 닐짜를 같이 보냄
									intent.putExtra("selectedDay", selectedDay);
									startActivity(intent);
									break;
								case 1: // 메모보기
									intent = new Intent(MyDiaryActivity.this,
											MemoListActivity.class);
									intent.putExtra("selectedDay", selectedDay);
									startActivity(intent);
									break;
								case 2: // 전체메모보기
									intent = new Intent(MyDiaryActivity.this,
											MemoAllListActivity.class);
									startActivity(intent);
									break;
								case 3: // 일정추가
									addSchedule(day);
									break;
								case 4: // 일정보기
									intent = new Intent(MyDiaryActivity.this,
											ScheduleListActivity.class);
									intent.putExtra("selectedDay", selectedDay);
									startActivity(intent);
									break;
								case 5: // 전체일정보기
									intent = new Intent(MyDiaryActivity.this,
											ScheduleAllListActivity.class);
									startActivity(intent);
									break;									
								case 6: // 녹음 하기
									RecordDialog recordDailog = new RecordDialog(
											MyDiaryActivity.this);

									recordDailog.show();
									break;
								case 7: // 녹음 리스트
									intent = new Intent(MyDiaryActivity.this,
											RecordListActivity.class);
									startActivity(intent);
									break;

								}
							}
						}).show();
	}

	/**
	 * 해당달의 날짜 설정
	 */
	private void setDate() {
		// year 와 moenth를 설정한다.
		// 월은 0~11까지 이다
		CharSequence month = String.format("%04d년 %02d월",
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
		monthTV.setText(month);
		// 첫번째 주의 시작일
		// int firstWeekDay =cal.getActualMinimum();
		int temp = cal.get(Calendar.DAY_OF_MONTH); // 오늘 날짜를 일단 저장
		// 첫째날의 요일순번을 저장하기 위해 임시로 1일로 설정
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int firstWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
		// 요일의 순번을 구했으면 다시 원래 날짜로
		cal.set(Calendar.DAY_OF_MONTH, temp);
		if (firstWeekDay > 6) {
			firstWeekDay = 0;
		}

		// 리스트를 초기화 시킨다.
		for (int i = 0; i < list.size(); i++) {
			Button b = list.get(i);
			b.setText("");
			b.setBackgroundResource(R.drawable.selector);
			b.setClickable(false);
		}

		Log.i(DEBUG_TAG, "firstWeekDay ->" + firstWeekDay + "");
		// 오늘 날짜
		int today = cal.get(Calendar.DAY_OF_MONTH);
		Log.i(DEBUG_TAG, "Today ->" + today + "");
		int selMonth = cal.get(Calendar.MONTH) + 1;
		int selYear = cal.get(Calendar.YEAR);
		// 날짜 설정

		// 첫번재 시작일부터 그 달의 월마지막 날과 첫번재 시작일을 더한값만큼 날짜를 추가해준다.
		int j = 1;
		for (int days = firstWeekDay; days < cal
				.getActualMaximum(Calendar.DAY_OF_MONTH) + firstWeekDay; days++) {
			list.get(days).setText(j + ""); // 날짜를 넣어준다.
			list.get(days).setClickable(true);
			list.get(days).startAnimation(ani); // 날짜버튼에 애니메이션 시작

			if (currentMonth == selMonth && today == j
					&& currentYear == selYear) { // 오늘날짜에 색 강조
				list.get(days).setBackgroundResource(R.color.today);
				list.get(days).setTag(R.color.today);
			} else {
				list.get(days).setBackgroundResource(R.drawable.selector);
			}
			j++;
		}
	}

	/**
	 * 메모날짜를 체크해 달력에 색 강조해 주기
	 */
	private void initCheckMemo() {
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		// 년월일 조건검색
		String date = cal.get(Calendar.YEAR) + "-"
				+ String.format("%02d", cal.get(Calendar.MONTH) + 1);
		Log.i(DEBUG_TAG, "date-->" + date);
		String memoDate;
		cursor = db.query(DBHelper.MEMO_TABLE, null, "substr(date, 1,7) = ? ",
				new String[] { date, }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				memoDate = cursor.getString(cursor.getColumnIndex("date"));
				// list에 추가
				String[] arr = memoDate.substring(0, 10).split("-");
				memoDate = arr[2]; // 메모한 날짜 뱨오기
				Log.i(DEBUG_TAG, "memoDate" + arr[2]);
				// 오늘날짜는 뺴고
				if (Integer.valueOf(memoDate) == cal.get(Calendar.DAY_OF_MONTH)) {
					continue;
				}
				int searchDay = Integer.valueOf(memoDate);
				for (Button btn : list) {
					// 메모가 있으면 색강조
					if (btn.getText().toString().length() > 0) { // 날짜가 있는 버튼만
						if (Integer.valueOf(btn.getText().toString()) == searchDay) {
							Log.i(DEBUG_TAG, "write memo day-->" + searchDay);
							btn.setBackgroundResource(R.drawable.has_memo_selector);
							btn.setTag(R.drawable.has_memo_selector);
						}
					}
				}
			} while (cursor.moveToNext());
		}
		// 디비를 닫아준다.
		cursor.close();
		db.close();

	}
	
	/**
	 * 메모날짜를 체크해 달력에 색 강조해 주기
	 */
	private void initCheckSchedule() {
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		// 년월일 조건검색
		String date = cal.get(Calendar.YEAR) + "-"
				+ String.format("%02d", cal.get(Calendar.MONTH) + 1);
		Log.i(DEBUG_TAG, "scheduleDate-->" + date);
		String scheduleDate;
		cursor = db.query(DBHelper.SCHEDULE_TABLE, null, "substr(yyyyMMdd, 1,7) = ? ",
				new String[] { date, }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				scheduleDate = cursor.getString(cursor.getColumnIndex("yyyyMMdd"));
				// list에 추가
				String[] arr = scheduleDate.substring(0, 10).split("-");
				scheduleDate = arr[2]; // 메모한 날짜 뱨오기
				Log.i(DEBUG_TAG, "schedule~~~~~~" + arr[2]);
				// 오늘날짜는 뺴고
				if (Integer.valueOf(scheduleDate) == cal.get(Calendar.DAY_OF_MONTH)) {
					continue;
				}
				int searchDay = Integer.valueOf(scheduleDate);
				for (Button btn : list) {
					Log.i(DEBUG_TAG, "schedulz22222zzz");			
					// 메모가 있으면 색강조
					if (btn.getText().toString().length() > 0) { // 날짜가 있는 버튼만
						Log.i(DEBUG_TAG, "schedulzzzz");						
						if (Integer.valueOf(btn.getText().toString()) == searchDay) {
							Log.i(DEBUG_TAG, "scheduleDate day-->" + searchDay);
							btn.setBackgroundResource(R.drawable.has_schedule_selector);
							btn.setTag(R.drawable.has_schedule_selector);
						}
					}
				}
			} while (cursor.moveToNext());
		}
		// 디비를 닫아준다.
		cursor.close();
		db.close();

	}	

	/**
	 * 년(year) 선택 다이얼로그
	 */
	private void selectYear() {
		// TODO Auto-generated method stub
		// 3년 전후로
		final CharSequence[] years = new CharSequence[] {
				cal.get(Calendar.YEAR) - 5 + "",
				cal.get(Calendar.YEAR) - 4 + "",
				cal.get(Calendar.YEAR) - 3 + "",
				cal.get(Calendar.YEAR) - 2 + "",
				cal.get(Calendar.YEAR) - 1 + "", cal.get(Calendar.YEAR) + "",
				cal.get(Calendar.YEAR) + 1 + "",
				cal.get(Calendar.YEAR) + 2 + "",
				cal.get(Calendar.YEAR) + 3 + "",
				cal.get(Calendar.YEAR) + 4 + "",
				cal.get(Calendar.YEAR) + 5 + "" };

		new AlertDialog.Builder(MyDiaryActivity.this).setTitle("년 선택하기")
				.setItems(years, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						int tmpYear = -(years.length / 2);
						tmpYear += which;
						cal.add(Calendar.YEAR, tmpYear);
						setDate();
						initCheckMemo();
					}
				}).show();
	}

	/**
	 * 일정 추가 다이얼로그
	 */
	private void addSchedule(final String day) {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.add_schedule);
		dialog.setTitle("일정 추가");
		// 다이얼로그 후킹
		final ScrollView sv = (ScrollView) dialog
				.findViewById(R.id.add_schedule_root);
		Button addBtn = (Button) dialog.findViewById(R.id.add_schedule_btn);
		final EditText scheduleET = (EditText) dialog
				.findViewById(R.id.schedule);
		// 일정추가 이벤트 처리
		addBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				// 일정을 입력했는지 체크
				if (TextUtils.isEmpty(scheduleET.getText().toString())) {
					Toast.makeText(MyDiaryActivity.this, "일정을 입력하세요",
							Toast.LENGTH_SHORT).show();
					return;
				}

				// 디비에 일정 저장 처리
				DBHelper dbhp = new DBHelper(MyDiaryActivity.this); // 도우미 클래스
				SQLiteDatabase db = dbhp.getWritableDatabase(); // 읽기모도로 해주자
				ContentValues cv = new ContentValues();

				// 스케쥴명
				String schedule = ((EditText) sv.findViewById(R.id.schedule))
						.getText().toString();
				// 시간가져오기
				int startHour = ((TimePicker) sv.findViewById(R.id.s_time))
						.getCurrentHour();
				int endHour = ((TimePicker) sv.findViewById(R.id.e_time))
						.getCurrentHour();
				int startMin = ((TimePicker) sv.findViewById(R.id.s_time))
						.getCurrentMinute();
				int endMin = ((TimePicker) sv.findViewById(R.id.e_time))
						.getCurrentMinute();
				ToggleButton tb = (ToggleButton) sv.findViewById(R.id.alarm);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal  = Calendar.getInstance();
	            cal.set(Calendar.YEAR, selectedDay[0]);
	            cal.set(Calendar.MONTH, selectedDay[1]) ;
	            cal.set(Calendar.DAY_OF_MONTH, selectedDay[2]);			
	            
				String date = sdf.format(cal.getTime());
				String startTime = date
						+ String.format("-%02d %02d:%02d",
								Integer.valueOf(day), startHour, startMin); // 시작
																			// 시간
				String endTime = date
						+ String.format("-%02d %02d:%02d",
								Integer.valueOf(day), endHour, endMin); // 종료 시간
				int alarm = tb.isChecked() ? 1 : 0;
	
				cv.put("yyyyMMdd", sdf.format(cal.getTime()));				
				cv.put("todo", schedule);
				cv.put("s_time", startTime);
				cv.put("e_time", endTime);
				cv.put("alarm", alarm);
				Log.i(DEBUG_TAG, "insert data-->" + schedule + "   "
						+ startTime + "  " + endTime + "   " + alarm);
				// db에 정상적으로 추가 되었으면 토스트를 굽는다.
				if (db.insert(DBHelper.SCHEDULE_TABLE, null, cv) > 0) {
					Toast.makeText(MyDiaryActivity.this, "일정이 추가되었습니다.",
							Toast.LENGTH_SHORT).show();
					dialog.dismiss(); // 정상적으로 처리되면 다이얼로그를 닫는다.
					initCheckSchedule();
				}
				db.close();
			}
		});

		dialog.show();
	}

	/**
	 * 내 위치 추가 하기
	 */
	private void addMyPlace() {
		// 다이얼로그 생성
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.add_myplace);
		dialog.setTitle("현재 위치 저장하기");
		final EditText titleET = (EditText) dialog.findViewById(R.id.title);
		final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.checkBox);
		Button addBtn = (Button) dialog.findViewById(R.id.add_myplace_btn);
		// 일정추가 이벤트 처리
		addBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (TextUtils.isEmpty(titleET.getText().toString())) {
					Toast.makeText(MyDiaryActivity.this, "로깅 메시지를 입력하세요",
							Toast.LENGTH_SHORT).show();
					return;
				}

				DBHelper dbhp = new DBHelper(MyDiaryActivity.this); // 도우미 클래스
				SQLiteDatabase db = dbhp.getWritableDatabase();
				ContentValues cv = new ContentValues();
				double[] geo = getLocation();
				if (geo == null) { // 수신된 자표가 없으면
					Toast.makeText(MyDiaryActivity.this, "현재 위치를 찾을수 없습니다.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				// 현재 시간 생성
				Calendar currentCal = Calendar.getInstance();
				currentCal.setTimeInMillis(System.currentTimeMillis());
				String date = sdf.format(currentCal.getTime());

				cv.put("lat", geo[0]);
				cv.put("lon", geo[1]);
				cv.put("tag", titleET.getText().toString());
				cv.put("date", date);
				cv.put("alarm", checkBox.isChecked() ? 1 : 0);
				// db에 정상적으로 추가 되었으면 토스트를 굽는다.
				if (db.insert(DBHelper.MY_PLACE_TABLE, null, cv) > 0) {
					Toast.makeText(MyDiaryActivity.this, "현재 위치가 추가되었습니다.",
							Toast.LENGTH_SHORT).show();
					dialog.dismiss(); // 정상적으로 처리되면 다이얼로그를 닫는다.
				}
				db.close();
			}
		});

		dialog.show();
	}

	/**
	 * 현재 위치를 폰의 위치 수신상태따라 gps>wifi>network 순으로 가져온다.
	 *
	 * @param msgFlag
	 * @return 위치 좌표 double[]
	 */
	private double[] getLocation() {

		LocationManager locationManager;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		/*
		 * Criteria criteria = new Criteria();
		 * criteria.setAccuracy(Criteria.ACCURACY_FINE);// 정확도
		 * criteria.setPowerRequirement(Criteria.POWER_HIGH); // 전원 소비량
		 * criteria.setAltitudeRequired(false); // 고도 사용여부
		 * criteria.setBearingRequired(false); //
		 * criteria.setSpeedRequired(false); // 속도
		 * criteria.setCostAllowed(true); // 금전적비용
		 */
		// String provider = locationManager.getBestProvider(criteria, true);
		String provider;
		// gps 가 켜져 있으면 gps로 먼저 수신
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
			locationManager.requestLocationUpdates(provider, 100, 0,
					loclistener);// 현재정보를 업데이트
			location = locationManager.getLastKnownLocation(provider);
		} else { // 없으면 null
			location = null;
		}

		if (location == null) {
			// 무선 네크워트를 통한 위치 설정이 안되어 있으면 그냥 null 처리
			if (!(locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
				return null;
			}

			// 네트워크로 위치를 가져옴
			provider = LocationManager.NETWORK_PROVIDER;
			// criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			// provider = locationManager.getBestProvider(criteria, true);
			location = locationManager.getLastKnownLocation(provider);
			locationManager.requestLocationUpdates(provider, 1000, 10,
					loclistener);
			/*
			 * Toast.makeText(this,
			 * "실내에 있거나 GPS를 이용할수 없어  네트워크를 통해 현재위치를 찾습니다.",
			 * Toast.LENGTH_SHORT).show();
			 */
			if (location == null) {
				return null;
			}
		}
		// 위도, 경도 가져오기
		double[] array = { location.getLatitude(), location.getLongitude() };
		return array;
	}

	/**
	 * resume시 달력날짜에 메모가 있는지 체크
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// 서비스 구동
		super.onResume();
		initCheckMemo();
		initCheckSchedule();
	}

	/**
	 * 포커스를 받으면 다시 알람 서비스 시작
	 */
	@Override
	public void onWindowFocusChanged(final boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		doStartService();
	}

	@Override
	public void onClick(final View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.prev:
			cal.add(Calendar.MONTH, -1);
			setDate();
			initCheckMemo();
			initCheckSchedule();
			switcher.showNext();
			break;

		case R.id.next:
			cal.add(Calendar.MONTH, 1);
			setDate();
			initCheckMemo();
			initCheckSchedule();
			switcher.showNext();
			break;
		case R.id.month:
			selectYear();
			switcher.showNext();
			break;
		}
	}

	/**
	 * 다이얼로그 생성
	 */
	@Override
	protected Dialog onCreateDialog(final int id) {
		// TODO Auto-generated method stub
		super.onCreateDialog(id);
		switch (id) {
		case DAY_DIALOG: // 파일 선택 다이얼로그
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}

		}

		return null;
	}

    /** 옵션 메뉴 만들기 */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu){
    	super.onCreateOptionsMenu(menu);
    	menu.add(0,1,0, "현재위치로깅");
    	menu.add(0,2,0, "지도위치로깅");
    	menu.add(0,3,0, "로깅지도보기");
    	menu.add(0,4,0, "로깅내역");
    	menu.add(0,5,0, "날씨보기");
    	menu.add(0,6,0, "설정");

    	//menu.add(0,4,0, "도움말");
    	//item.setIcon();
    	return true;
    }

    /** 옵션 메뉴 선택에 따라 해당 처리를 해줌 */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item){
    	Intent intent = null;
    	switch(item.getItemId()){
	    	case 1:	// 내 위치 추가
	    		addMyPlace();
	    		break;
	    	case 2:	// 지도에서 위치 추가
	    		intent = new Intent(MyDiaryActivity.this,
	    				RegMapActivity.class);
	    		double[] geo = getLocation();
	    		if(geo !=null){	// 현재좌표가 있으면 인텐트에 싣는다.
		    		intent.putExtra("lat", geo[0]);
		    		intent.putExtra("lon", geo[1]);
	    		}
	    		startActivity(intent);
	    		break;
	    	case 3:	// 내 위치 로깅 내역 지도  보기
	    		intent =new Intent(MyDiaryActivity.this,
	    				MapActivity.class);
	    		geo = getLocation();
	    		if(geo !=null){	// 현재좌표가 있으면 인텐트에 싣는다.
		    		intent.putExtra("lat", geo[0]);
		    		intent.putExtra("lon", geo[1]);
	    		}
	    		startActivity(intent);
	    		break;
    		case 4:		// 로깅내역들 보기
				intent = new Intent(getBaseContext(), LoggingListActivity.class);
				startActivity(intent);
				return true;
    		case 5:		// 날씨보기
				intent = new Intent(getBaseContext(), WeatherActivity.class);
				startActivity(intent);
				return true;
    		case 6:		// 설정 프리퍼런스엑티비티
				intent = new Intent(getBaseContext(), SettingActivity.class);
				startActivity(intent);
				return true;
				/*
			case 5:		// 도움말 액티비티로 이동
				intent = new Intent(getBaseContext(), HelpActivity.class);
				startActivity(intent);	// 특별한 요청코드는 필요없음
				return true;
				*/

    	}
    	return false;
    }

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		// doStartService();
		super.onStop();
	}

	/** 뒤로가기 버튼 클릭시 앱 종료 */
	@Override
	public void onBackPressed() {
		finishDialog(this);

	}

	/**
	 * 종료 confirm 다이얼로그 창
	 *
	 * @param context
	 */
	public void finishDialog(final Context context) {
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("").setMessage("프로그램을 종료하시겠습니까?")
				.setPositiveButton("종료", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// TODO Auto-generated method stub
						moveTaskToBack(true);
						moveTaskToBack(true);
						finish();
					}
				}).setNegativeButton("취소", null).show();
	}

	/**
	 * 서버에 쓰레드로 입력한 아이디를 전송한다.
	 */
	private class AsyncTaskWeather extends AsyncTask<Object, String, String> {
		/**
		 * 쓰레드 처리가 완료되면..
		 */
		@Override
		protected void onPostExecute(String weatherInfo) {
			if (weatherInfo != null) { // 서버 전송 결과에 따라 메세지를 보여준다.
				loadingBar.setVisibility(View.GONE); // progressbar를 숨겨주고
				ScrollView scroll = (ScrollView) findViewById(R.id.weather_scroll);
				TextView weatherTV = (TextView) findViewById(R.id.weather_info);
				weatherInfo = weatherInfo.trim().replace("<br />", "\n");
				scroll.setVisibility(View.VISIBLE);
				weatherTV.setText(weatherInfo); // 날씨정보를 보여준다.
				scroll.scrollTo(0, 0);
			} else {
				Toast.makeText(MyDiaryActivity.this, "날씨정보를 가져오기 못했습니다..",
						Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * 쓰레드 작업 처리전
		 */
		@Override
		protected void onPreExecute() { // 전송전 프로그래스 다이얼로그로 전송중임을 사용자에게 알린다.

			// mLockScreenRotation(); // 화면회전을 막는다.
		}

		@Override
		protected void onProgressUpdate(final String... values) {
		}

		@Override
		protected String doInBackground(final Object... params) { // 전송중

			// TODO Auto-generated method stub
			String result = null; // 전송 결과 처리
			// http 로 보낼 이름 값 쌍 컬랙션
			try {
				result = parseWeather();
			} catch (ClientProtocolException e) {
				Log.e(DEBUG_TAG, "Failed to register id (protocol): ", e);
			} catch (IOException e) {
				Log.e(DEBUG_TAG, "Failed to register  i (io): ", e);
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "파일 업로드 에러", e);
			}

			return result;
		}

		private String parseWeather() throws XmlPullParserException,
				IOException {

			URL url = new URL(WEATHER_URL + "?stnId=109");
			// url로부터 데이터를 읽어오기 위한 스트림
			InputStream in = url.openStream();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			// xml파서팩토리로부터 XmlPullParser얻어오기
			XmlPullParser parser = factory.newPullParser();
			// namespace 지원
			factory.setNamespaceAware(true);
			parser.setInput(in, "utf-8");
			int eventType = -1;
			String weatherInfo = null;
			while (eventType != XmlResourceParser.END_DOCUMENT) { // 문서의 마지막이
																	// 아닐때까지

				if (eventType == XmlResourceParser.START_TAG) { // 이벤트가 시작태그면
					String strName = parser.getName();
					if (strName.equals("wf")) { // message 시작이면 객체생성
						if (parser.getDepth() == 3) { // 주간 날씨 정보
							parser.next();
							weatherInfo = parser.getText();
							Log.i(DEBUG_TAG, parser.getText());
						}

					}

				}
				eventType = parser.next(); // 다음이벤트로..
			}
			in.close();
			return weatherInfo;
		}

	}

}
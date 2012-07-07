package kr.co.diary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kr.co.diary.data.Logging;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 공유설정에서 알람시간을 가져와 알람을 실행하고 알람시간이 되면 브로드캐스팅을 한다.
 */
public class AlarmService extends Service {
	private Calendar calendar = null; // 현재시간
	private AlarmManager am = null; // 알람 서비스
	private PendingIntent sender; // 알람 notification을 위한 팬딩인텐트
	private int alarmDistance; // 알람 발생 간격 미터
	private final List<Logging> list = new ArrayList<Logging>();

	/** 서비스가 실행될때 */
	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		// 공유환경 설정 가져오기 setting.xml 값
		SharedPreferences defaultSharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		// 미리 알림 기본 시간 가져오기 , 없으면 10분으로 설정
		int beforeMin = Integer.valueOf(defaultSharedPref.getString(
				"beforeAlarm", "10")); // minus
		// 알람을 발생해줄 거리 설정값 가져오기, 없으면 300미터
		alarmDistance = Integer.valueOf(defaultSharedPref.getString(
				"placeAlarm", "300"));
		// 거리 단위 변환 km -> m
		alarmDistance *= 0.001;

		// 알람을 발생할 로깅 리스트 가져오기
		getAlarmLoggingList();
		// 위치 수신하기
		getLocation();
		// 알람을 발생할 날짜 얻기
		calendar = getAlarmDate(beforeMin); // 알람시간이 설정된 calendar를 가져온다.

		if (calendar == null) { // 설정된 알람시간이 없으면 종료
			return 0;
		}
		
		Log.i(BaseActivity.DEBUG_TAG,
				"알람시간 : " + calendar.get(Calendar.HOUR_OF_DAY) + ":"
						+ calendar.get(Calendar.MINUTE));		

		// 시스템서비스에서 알람매니져를 얻어온다.
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		// 브로드케스트 리시버에 보낼 팬딩인텐트, 이전 팬딩인텐트가 있으면 취소하고 새로 실행
		Intent i = new Intent(getBaseContext(), AlarmReceiver.class);
		i.putExtra("beforeMin", beforeMin);
		// 팬딩 인텐트 설정
		// 현재 팬딩 인텐트가 있으면 취소하고 다시 설정
		sender = PendingIntent.getBroadcast(getBaseContext(), 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender); // 알람설정
		Log.i(BaseActivity.PREFERENCE, "onstartCommand");
		return 0;
	}

	/**
	 * 알람 로깅리스트 가져오기
	 */
	private void getAlarmLoggingList() {
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		// 알람 설정이 되어 있는 row 만 가져온다.
		cursor = db.query(DBHelper.MY_PLACE_TABLE, null, "alarm = 1", null,
				null, null, null);
		if (cursor.getCount() <= 0) { // 내역이 없으면 끝낸다.
			dbhp.close();
			return;
		}
		// 커서가 있으면
		if (cursor.moveToFirst()) {
			do {
				// 로깅 객체에 내용을 담아준다.
				Logging l = new Logging();
				l.setIdx(cursor.getInt(cursor.getColumnIndex("no")));
				l.setLat(Double.parseDouble(cursor.getString(cursor
						.getColumnIndex("lat"))));
				l.setLon(Double.parseDouble(cursor.getString(cursor
						.getColumnIndex("lon"))));
				l.setDate(cursor.getString(cursor.getColumnIndex("date")));
				l.setTag(cursor.getString(cursor.getColumnIndex("tag")));
				list.add(l); // arrayList 에 추가
			} while (cursor.moveToNext()); // 다음 커서로
		}
		// 디비를 닫아준다.
		cursor.close();
		db.close();
	}

	/**
	 * DB에서 가장 현재시간과 가까운 알람시간을 가져온다.
	 * 
	 * @return
	 */
	private Calendar getAlarmDate(final int beforeMin) {
		Calendar cal = Calendar.getInstance();
		// 단말기의 시간을 가져온다.
		cal.setTimeInMillis(System.currentTimeMillis());
		// 몇분전 알람시간 설정
		cal.add(Calendar.MINUTE, beforeMin);
		// 현재 시간 가져오기
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String date = sdf.format(cal.getTime());
		Log.i(BaseActivity.DEBUG_TAG, "현재 시간--->" + date);
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
		Cursor cursor = null;
		// 현재시간보다 큰 알람 시간
		cursor = db.query(DBHelper.SCHEDULE_TABLE, null,
				"s_time > ? and alarm = 1", new String[] { date, }, null, null,
				"s_time asc");
		if (cursor.getCount() <= 0) { // 내역이 없으면 끝낸다.
			db.close();
			return null;
		}
		if (cursor.moveToFirst()) { // 가장 최신 한개만 가져온다.
			date = cursor.getString(cursor.getColumnIndex("s_time"));
			date = date.substring(0, 16);
			Log.i(BaseActivity.DEBUG_TAG, "가져온 시간--->" + date);
		}
		// 불러낸 데이터로 calendar 셋팅
		try {
			cal.setTime(sdf.parse(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 디비를 닫아준다.
		cursor.close();
		db.close();
		// 몇분전 알람시간 설정
		cal.add(Calendar.MINUTE, -(beforeMin));
		Log.i(BaseActivity.DEBUG_TAG, "알람 시간--->" + sdf.format(cal.getTime()));
		return cal;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	/**
	 * 서비스가 종료될때 알람 수신 취소하고
	 */
	@Override
	public void onDestroy() {
		Log.i("dservice", "stop!");
		stopSelf();
		if (am != null) { // 알람이 설정되어 있으면 취소
			am.cancel(sender); // 알람 취소
		}

		// 위치 수신을 제거한다.
		if (loclistener != null) {
			// 위치수신 관리자 얻기
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			// 위치 수신 제거
			locationManager.removeUpdates(loclistener);
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	// 위치 리스너 처리
	private final LocationListener loclistener = new LocationListener() {

		/**
		 * 위치가 변경될때
		 */
		@Override
		public void onLocationChanged(final Location location) {
			Log.w(BaseActivity.DEBUG_TAG, "onLocationChanged");
			Log.i(BaseActivity.DEBUG_TAG, "알람 갯수-------->" + list.size());
			if (list.size() > 0) { // 알람이 설정된 위치로깅이 있으면 검색한다.
				// 거리 차이를 미터로 반환
				double lat = location.getLatitude();
				double lon = location.getLongitude();
				for (Logging l : list) { // 위치알람 리스트를 가져온다.
					// 현재위치와 알람위치와 거리차이를 계산한다.
					double distance = getDistance_arc(lat, lon, l.getLat(),
							l.getLon());
					Log.i(BaseActivity.DEBUG_TAG, "distance-------->"
							+ distance);
					// 거리차이가 알람 발생 거리보다 적으면 브로드캐스팅 발생
					if (distance < alarmDistance) {
						if (removeAlarmedPlace(l) > 0) { // 확인된 위치로깅 알람은 확인으로
															// 변경한후 브로드캐스팅처리
							LocationManager locationManager;
							locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
							locationManager.removeUpdates(this);
							// 브로드케스트 리시버에 보낼 팬딩인텐트, 이전 팬딩인텐트가 있으면 취소하고 새로 실행
							Intent i = new Intent(getBaseContext(),
									LoggingReceiver.class);
							Bundle bundle = new Bundle();
							bundle.putSerializable("alarmed_place", l);
							i.putExtras(bundle);
							// 브로드케스팅을 수신할 팬딩 인텐트 설정
							PendingIntent sender = PendingIntent.getBroadcast(
									getBaseContext(), 0, i,
									PendingIntent.FLAG_CANCEL_CURRENT);
							try {
								sender.send(); // 브로드케스팅
							} catch (CanceledException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		@Override
		public void onProviderDisabled(final String provider) {
			Log.w(BaseActivity.DEBUG_TAG, "onProviderDisabled");
		}

		@Override
		public void onProviderEnabled(final String provider) {
			Log.w(BaseActivity.DEBUG_TAG, "onProviderEnabled");
		}

		@Override
		public void onStatusChanged(final String provider, final int status,
				final Bundle extras) {
			Log.w(BaseActivity.DEBUG_TAG, "onStatusChanged");
		}
	};

	/**
	 * 위치 리스너 설정
	 */
	private void getLocation() {
		LocationManager locationManager;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// 최적의 위치 수신자를 가져온다.
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);// 정확도
		criteria.setPowerRequirement(Criteria.POWER_HIGH); // 전원 소비량
		criteria.setAltitudeRequired(false); 	// 고도 사용여부
		criteria.setBearingRequired(false); 	// 방위 반환
		criteria.setSpeedRequired(false); 	// 속도
		criteria.setCostAllowed(true); 		// 금전적비용

		String provider = locationManager.getBestProvider(criteria, true);
		// 위치 업데이트 설정
		// 1분, 10미터이동이 발생할때마다 위치 수신 리스너를 업데이트를 한다.
		locationManager.requestLocationUpdates(provider, 1000 * 60, 10,
				loclistener);
		/*
		 * String provider; // gps 가 켜져 있으면 gps로 먼저 수신 if
		 * (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		 * provider = LocationManager.GPS_PROVIDER;
		 * locationManager.requestLocationUpdates(provider, 0, 0,
		 * loclistener);// 현재정보를 업데이트 mLocation =
		 * locationManager.getLastKnownLocation(provider); } else { // 없으면 null
		 * mLocation = null; }
		 * 
		 * if (mLocation == null) { // 무선 네크워트를 통한 위치 설정이 안되어 있으면 그냥 null 처리 if
		 * (!(locationManager
		 * .isProviderEnabled(LocationManager.NETWORK_PROVIDER))) { }
		 * 
		 * // 네트워크로 위치를 가져옴 provider = LocationManager.NETWORK_PROVIDER; //
		 * criteria.setAccuracy(Criteria.ACCURACY_COARSE); // provider =
		 * locationManager.getBestProvider(criteria, true); mLocation =
		 * locationManager.getLastKnownLocation(provider);
		 * locationManager.requestLocationUpdates(provider, 0, 0, loclistener);
		 * /* Toast.makeText(this, "실내에 있거나 GPS를 이용할수 없어  네트워크를 통해 현재위치를 찾습니다.",
		 * Toast.LENGTH_SHORT).show();
		 */

		// }

	}

	/**
	 * 알람이 발생된 위치를 디비에서 제거한다.
	 * 
	 * @param log
	 *            위치알람 객체
	 * @return 처리결과값 1 or 0
	 */
	private int removeAlarmedPlace(final Logging log) {
		int result = -1;
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("alarm", 0);
		// 인덱스 번호를 이용하여 alarm 값을 0으로 주어 알람 설정이 안되도록 한다.
		result = db.update(DBHelper.MY_PLACE_TABLE, cv, "no = ?",
				new String[] { "" + log.getIdx(), });
		db.close();
		return result;
	}

	/**
	 * 두지점간의 거리 구하기
	 * 
	 * @param sLat
	 * @param sLong
	 * @param dLat
	 * @param dLong
	 * @return
	 */
	private static double getDistance_arc(final double sLat,
			final double sLong, final double dLat, final double dLong) {
		final int radius = 6371009;

		double uLat = Math.toRadians(sLat - dLat);
		double uLong = Math.toRadians(sLong - dLong);
		double a = Math.sin(uLat / 2) * Math.sin(uLat / 2)
				+ Math.cos(Math.toRadians(sLong))
				* Math.cos(Math.toRadians(dLong)) * Math.sin(uLong / 2)
				* Math.sin(uLong / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = radius * c;

		return Double.parseDouble(String.format("%.3f", distance / 1000));
	}

}

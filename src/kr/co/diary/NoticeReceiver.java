package kr.co.diary;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * AlarmService 의 위치알람갯수 처리 리시버 클래스
 */
public class NoticeReceiver extends BroadcastReceiver {
	private final int YOURAPP_NOTIFICATION_ID = 1; // 앱 아이디값
	private SharedPreferences sp; // 소리 및 진동 설정

	private int alarmCount;

	/**
	 * 브로드 캐스팅 수신
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.i("service", "notice broadcast catch!!");
		alarmCount = intent.getIntExtra("alarmCount", 1);
		sp = PreferenceManager.getDefaultSharedPreferences(context); // 환경설정값
																		// 가져오기
		showNotification(context, R.drawable.ico_place); // 통지하기
	}

	/**
	 * 상태바에 알람을 알리고 확인시 MyScheduleActivity로 이동시킨다.
	 * 
	 * @param context
	 * @param statusBarIconID
	 *            상태바에 나타낼 아이콘
	 */
	private void showNotification(final Context context,
			final int statusBarIconID) {
		// 통지바를 눌렀을경우 위치알람내역리스트 엑티비티로 이동하기 위한 설정
		Intent contentIntent = new Intent(context, LoggingListActivity.class);
		// 알림클릭시 이동할 엑티비티 설정
		PendingIntent theappIntent = PendingIntent.getActivity(context, 0,
				contentIntent, 0);
		CharSequence title = "위치 알림"; // 알림 타이틀
		CharSequence message = "위치 알림이 " + alarmCount + "개 있습니다.";

		// 통지바 설정
		Notification notif = new Notification(statusBarIconID, null,
				System.currentTimeMillis());

		notif.flags |= Notification.FLAG_AUTO_CANCEL; // 클릭시 사라지게
		notif.defaults |= Notification.DEFAULT_LIGHTS; // led도 키자
		// notif.defaults |= Notification.DEFAULT_SOUND; // 기본 sound도 키자

		// 진동알람을 설정했으면 진동을 울린다.
		if (sp.getBoolean("vibration", true)) {
			long[] vibrate = { 1000, 1000, 1000, 1000, 1000 }; // 1초간 5번
			notif.vibrate = vibrate;
		}

		// 소리알람을 설정했으면 소리를 울린다.
		if (sp.getBoolean("sound", true)) {
			notif.sound = Uri.parse("android.resource://"
					+ context.getPackageName() + "/raw/sound");
		}
		notif.flags |= Notification.FLAG_INSISTENT; // 계속 알람 발생
		notif.setLatestEventInfo(context, title, message, theappIntent); // 통지바
																			// 설정
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(this.YOURAPP_NOTIFICATION_ID, notif); // 통지하기
	}
}

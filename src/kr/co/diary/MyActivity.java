package kr.co.diary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 *	상수값을 정의한다 기본 엑티비티
 */
public class MyActivity extends Activity {
	// 요일을 나타내는 상수
	public final static int SUNDAY = 0;
	public final static int MONAY = 1;
	public final static int TUESDAY = 2;
	public final static int WEDNESDAY = 3;
	public final static int THURSDAY = 4;
	public final static int FRIDAY = 5;
	public final static int SATURDAY = 6;

	// tag
	public final static String DEBUG_TAG = "diary";

	// dialog
	public final static int DAY_DIALOG = 1;

	//url
	public final static String LOGIN_URL = "http://ddononi.cafe24.com/diary/server.php?method=login";
	public final static String JOIN_URL = "http://ddononi.cafe24.com/diary/server.php?method=join";	
	public final static String SHARE_SCHEDULE = "http://ddononi.cafe24.com/diary/server.php?method=shareTodo";		
	public final static String SHARE_CHECK_URL = "http://ddononi.cafe24.com/diary/server.php?method=checkShare";
	public final static String LOOK_CHECK_URL = "http://ddononi.cafe24.com/diary/server.php?method=look";
	
	
	//weather xml url
	public final static String WEATHER_URL =
			"http://www.kma.go.kr/weather/forecast/mid-term-xml.jsp";	
	
	public final static String USER_LIST_URL = 
			"http://ddononi.cafe24.com/diary/server.php?method=userList";

	// 연결시도 최대 시간
	public final static int CONNECTION_TIME_OUT = 5000;
	// preference
	public final static String PREFERENCE = "diary";

	public static int myIndex;
	
	/**
	 * 네트워크망을 사용가능한지 혹은 연결되어있는지 확인한다.
	 * msgFlag가 false이면 현재 연결되어 있는 네트워크를 알려준다.
	 * 네트워크망 연결 불가시 사용자 에게 다이얼로그창을 띄어 알린다.
	 * @param msgFlag
	 * 		Toast 메세지  사용여부
	 * @return
	 *		네트워크 사용가능 여부
	 */
	public boolean checkNetWork(final boolean msgFlag) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// boolean isWifiAvail = ni.isAvailable();
		boolean isWifiConn = ni.isConnectedOrConnecting();
		ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		// boolean isMobileAvail = ni.isAvailable();
		boolean isMobileConn = ni.isConnectedOrConnecting();
		if (isWifiConn) {
			if (msgFlag == false) {
				Toast.makeText(this, "Wi-Fi망에 접속중입니다.",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (msgFlag == false) {
				Toast.makeText(this, "3G망에 접속중입니다.",
						Toast.LENGTH_SHORT).show();
			}
		}

		if (!isMobileConn && !isWifiConn) {
			/*
			 * 네트워크 연결이 되지 않을경우 이전 화면으로 돌아간다.
			 */
			new AlertDialog.Builder(this)
			.setTitle("알림")
			.setMessage(
					"Wifi 혹은 3G망이 연결되지 않았거나 "
							+ "원활하지 않습니다.네트워크 확인후 다시 접속해 주세요!")
			.setPositiveButton("닫기",
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.dismiss(); // 닫기
							finish();
						}
					}).show();
			return false;
		}
		return true;

	}	

}

package kr.co.diary;

import kr.co.utils.BaseActivity;
import android.app.Activity;

/**
 *	상수값을 정의한다 기본 엑티비티
 */
public class MyActivity extends BaseActivity {
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
	//weather xml url
	public final static String WEATHER_URL =
			"http://www.kma.go.kr/weather/forecast/mid-term-xml.jsp";

	// yahoo weather xml url
	public final static String MSN_WEATHER_IMAGE_URL = "http://blu.stc.s-msn.com/as/wea3/i/en/";
	// yahoo weather xml url
	public final static String MSN_WEATHER_URL = "http://weather.service.msn.com/data.aspx?weadegreetype=C&culture=ko-KR&weasearchstr=";	
	
	// 연결시도 최대 시간
	public final static int CONNECTION_TIME_OUT = 5000;
	// preference
	public final static String PREFERENCE = "diary";


}

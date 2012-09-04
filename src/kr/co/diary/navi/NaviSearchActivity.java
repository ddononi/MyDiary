package kr.co.diary.navi;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import kr.co.diary.R;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *	네비게이션 출발 및 도착 위치 설정 엑티비티
 */
public class NaviSearchActivity extends Activity implements iConstant, OnClickListener {

	// element
	private EditText startPlaceEt;				// 출발 장소 입력창
	private EditText endPlaceEt;				// 도착 장소 입력창
	private Button searchBtn;					// 검색 버튼
	
	// location
	private Location startPoint = null;		//	출발 좌표
	private Location endPoint = null;		// 도착 좌표
	private String priorty = GeoRouteSearch.Params.PRIORITY_SHOTCUT;	// 우선 순위
	
	// 출발or 도착 검색 구분값
	private final int START_LOC_SEARCH = 1;
	private final int END_LOC_SEARCH = 2;	
	
	// map & geo
	private Location mLocation;
	private LocationManager locationManager;
	
	// 위치 리스너 처리
	private final LocationListener loclistener = new LocationListener() {
		// 위치가 변경되면
		@Override
		public void onLocationChanged(final Location location) {
			// getLocation();
			// 위치 수신
			mLocation = location;
		}

		@Override
		public void onProviderDisabled(final String provider) {
		}

		@Override
		public void onProviderEnabled(final String provider) {
		}

		@Override
		public void onStatusChanged(final String provider, final int status,
				final Bundle extras) {
		}
	};	
	
	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.search_layout);
		getLocation();
		// 앨리먼트
		startPlaceEt = (EditText) findViewById(R.id.start_input); 	// 시작 위치
		endPlaceEt = (EditText) findViewById(R.id.end_input); 	// 도착 위치
		Button searchStartBtn = (Button) findViewById(R.id.search_start_btn); 		// 위치 버튼
		Button searchEndBtn = (Button) findViewById(R.id.search_end_btn); 		// 위치 버튼
		
		Button locStartBtn = (Button) findViewById(R.id.loc_start_btn); 		// 출발 현재 위치 버튼
		Button locEndBtn = (Button) findViewById(R.id.loc_end_btn); 		// 출발 현재 위치 버튼

		searchBtn = (Button) findViewById(R.id.search_btn);					 // 검색 버튼
		Button priorityBtn = (Button) findViewById(R.id.priority_btn);		 //	우선 순위 버튼		
		
		// 시작 주소변환 검색 버튼
		searchStartBtn.setOnClickListener(this);		
		// 시작 주소변환 검색 버튼
		searchEndBtn.setOnClickListener(this);		
		locEndBtn.setOnClickListener(this);
		locStartBtn.setOnClickListener(this);

		searchBtn.setOnClickListener(this);				
		priorityBtn.setOnClickListener(this);

	}
	

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.search_start_btn : 		// 시작위치 주소값으로 찾기
			if(TextUtils.isEmpty(startPlaceEt.getText())){
				Toast.makeText(NaviSearchActivity.this, "시작 위치를 입력하세요", Toast.LENGTH_SHORT).show();
				return;
			}
			new GeocodeLoadTask(START_LOC_SEARCH).execute();
			break;
			
		case R.id.search_end_btn : 	// 도착위치 주소값으로 찾기
			if(TextUtils.isEmpty(endPlaceEt.getText())){
				Toast.makeText(NaviSearchActivity.this, "도착 위치를 입력하세요", Toast.LENGTH_SHORT).show();
				return;
			}
			new GeocodeLoadTask( END_LOC_SEARCH).execute();
			break;
		
		case R.id.loc_start_btn : 		// 출발 현재 위치로 하기
			// 좌표 얻기
			if(mLocation != null){
				int lat = (int)(mLocation.getLatitude() * 1E6);
				int lng = (int)(mLocation.getLongitude() * 1E6);
				// 좌표를 이용하여 주소로 변환한다.
				getAddressFromPoint(lat, lng, START_LOC_SEARCH);
			}else{
				Toast.makeText(NaviSearchActivity.this, "현재  위치를 찾을수 없습니다.", Toast.LENGTH_SHORT).show();					
			}		
			break;
			
		case R.id.loc_end_btn : 		// 도착 현재 위치로 하기
			// 좌표 얻기
			if(mLocation != null){
				int lat = (int)(mLocation.getLatitude() * 1E6);
				int lng = (int)(mLocation.getLongitude() * 1E6);
				// 좌표를 이용하여 주소로 변환한다.
				getAddressFromPoint(lat, lng, END_LOC_SEARCH);
			}else{
				Toast.makeText(NaviSearchActivity.this, "현재  위치를 찾을수 없습니다.", Toast.LENGTH_SHORT).show();					
			}		
			break;			
			
			
		case R.id.search_btn : 	// 검색 버튼
			// 시작위치나 도착위치 정보가 없으면 취소 처리
			if( startPoint == null || endPoint == null){
				setResult(RESULT_CANCELED);
			}else{				
				// 맵엑티비티에 출발 및 도착 위치정보를 보내 경로를 그려줄수 있도록 한다.
				Intent intent = new Intent();
				intent.putExtra("startPlace", startPlaceEt.getText().toString());	// 출발 위치명
				intent.putExtra("endPlace", endPlaceEt.getText().toString());		// 도착 위치명
				
				intent.putExtra("startLat", startPoint.getLatitude() );				// 출발 위도
				intent.putExtra("startLng", startPoint.getLongitude() );			// 출발 경도
				intent.putExtra("endLat", endPoint.getLatitude() );				// 도착 위도
				intent.putExtra("endLng", endPoint.getLongitude() );			// 도착 경도
				
				intent.putExtra("priority", priorty);										// 경로탐색방법					
				setResult(RESULT_OK, intent);	// 정상 검색 완료 처리
				finish();
			}
			break;
			
			
		case R.id.priority_btn :	// 우선 순위
			AlertDialog.Builder ad = new AlertDialog.Builder(NaviSearchActivity.this);
			ad.setTitle("").setSingleChoiceItems(R.array.pirority_label, -1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String[] arr = getResources().getStringArray(R.array.pirority_value);
					priorty = arr[which];			//우선 순위 설정
					dialog.dismiss();
				}
			}).setNegativeButton("취소",null).show();			
		}
	}	
	

	/**
	 * 좌표를 이용하여 주소정보를 얻는다.
	 * @param lat
	 * @param lng
	 * @param what
	 */
	private void getAddressFromPoint(int lat, int lng , int what) {
		Geocoder gc = new Geocoder(NaviSearchActivity.this,Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = gc.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String addressStr = "현위치";		// 디폴트 주소명
		if(addresses != null && addresses.size()>0) {	// 주소가 있으면
			// 첫번째 주소 컬렉션을 얻은후
			Address address = addresses.get(0);
			// 대한민국문자는 없애주고 실제 주소만 가져온다. 
			addressStr = address.getAddressLine(0).replace("대한민국", "").trim();
		}			
		if(what == START_LOC_SEARCH){	// 출발 검색이면
			startPoint = new GeoPoint(lat, lng);
			startPlaceEt.setText(addressStr);
		}else{
			endPoint = new GeoPoint(lat, lng);
			endPlaceEt.setText(addressStr);	
		}
	}	
	
	/*
	 * 위치 설정 초기화
	 */
	private void getLocation() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 적절한 위치기반공급자를 이용하여 리스너를 설정한다.
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);// 정확도
		criteria.setPowerRequirement(Criteria.POWER_HIGH); // 전원 소비량
		criteria.setAltitudeRequired(false); // 고도 사용여부
		criteria.setBearingRequired(false); //
		criteria.setSpeedRequired(true); // 속도
		criteria.setCostAllowed(true); // 금전적비용

		String provider = locationManager.getBestProvider(criteria, true);
		// 1분 이상 10미터 이상 
		locationManager.requestLocationUpdates(provider, 0, 0, loclistener);
		mLocation = locationManager.getLastKnownLocation(provider);
	}	
	

	/**
	 * 엑티비티가 stop시 위치 수신 제거
	 */
	@Override
	protected void onStop() {
		super.onStop();
		locationManager.removeUpdates(loclistener);
	}
	
	
	
	/**
	 * 주소-> 좌표 변환 처리 쓰레드 
	 */
	private class GeocodeLoadTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private String jsonBody;
		private int mode;
		public GeocodeLoadTask(int mode){
			this.mode = mode;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}

			if (result) {
				if( parseGeoCoderJson(jsonBody)){	// 좌표변환이 정상이면

				}
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = ProgressDialog.show(NaviSearchActivity.this, "주소변환",
					"잠시만 기다려 주세요\n  주소변환중입니다.");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			String address;
			if(mode == START_LOC_SEARCH){	// 출발 플래그이면 출발 장소검색 아니면 도착 장소 검색
				address = startPlaceEt.getText().toString();
			}else{
				address = endPlaceEt.getText().toString();
			}			
	        vars.add(new BasicNameValuePair("address", address));
	        // url get  파라미터 인코딩후 url 생성
            String url = GEOCODE_URL + URLEncodedUtils.format(vars, "UTF-8");
			// HTTP get 메서드를 이용하여 데이터 업로드 처리            
            HttpGet request = new HttpGet(url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpClient client = new DefaultHttpClient();
			try {
				jsonBody = client.execute(request, responseHandler);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;				
			}	
    		 Log.i("navi", "response : " + jsonBody);
			return true;
		}
		
		private boolean parseGeoCoderJson(String jsonText){
			try {
				JSONObject rootJson = (new JSONObject(jsonText));	// root json 얻기
				String status = rootJson.getString("status");		// 요청 결과 값
				if(status.equals(STATUS_OK) == false){
					Log.i("navi", "geocoder fail");
					return false;
				}
				JSONArray results = rootJson.getJSONArray("results");		// 결과 내용
				JSONObject result = results.getJSONObject(0);
				String  address = result.getString("formatted_address");		// 세부 주소 얻기
			
				JSONObject geometry = result.getJSONObject("geometry");	// 좌표 내용
				JSONObject location = geometry.getJSONObject("location");	// 좌표 내용
				
				// 좌표 얻기
				int lat = (int)(location.getDouble("lat") * 1E6);
				int lng = (int)(location.getDouble("lng") * 1E6);
				if(mode  ==  START_LOC_SEARCH){	// 출발 장소이면
					startPoint = new GeoPoint(lat, lng);
					startPlaceEt.setText(address);
				}else{
					endPoint = new GeoPoint(lat, lng);
					endPlaceEt.setText(address);
				}

				return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.i("navi", e.getMessage());
				e.printStackTrace();
				return false;
			}

		}		

	}
	
}

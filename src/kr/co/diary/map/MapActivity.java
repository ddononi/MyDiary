package kr.co.diary.map;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.co.diary.BaseActivity;
import kr.co.diary.DBHelper;
import kr.co.diary.R;
import kr.co.diary.data.GPoint;
import kr.co.diary.data.Logging;
import kr.co.diary.data.VerTexData;
import kr.co.diary.navi.GeoRouteSearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager.OnCalloutOverlayListener;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;

/**
 *	Naver open api 를 이용하여 지도를 보여준다.
 *	위치 로깅 내역을 오버레이 아이콘으로 나타낸다.
 */
public class MapActivity extends NMapActivity {
	// naver open api key
	private static final String API_KEY = "7b8d41389c90191b5a7dbfa0e8d71aac";
	private static final int MAP_LEVEL = 10;
	private static final String LOG_TAG = "NMapViewer";
	// mapview 설정
	private NMapView mMapView = null;
	private NMapController mMapController;
	private NMapMyLocationOverlay mMyLocationOverlay;
	private NMapOverlayManager mOverlayManager;
	private NMapLocationManager mMapLocationManager;
	private NMapCompassManager mMapCompassManager;
	private NMapViewerResourceProvider mMapViewerResourceProvider;
	private final ArrayList<String> addressList = new ArrayList<String>(); // 주소를 저장할 리스트
	
	
	//navi
	private VerTexData verTexData; 
	// location
	private GPoint startPoint;
	private GPoint endPoint;

	private String startAddress;
	private String endAddress;
	private String totalDistance;
	private String totalTime;	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		setContentView(R.layout.map_layout);

		/* mapView  설정  */
		mMapView = (NMapView)findViewById(R.id.mapView);
		mMapView.setApiKey(API_KEY);	// 키 설정
		mMapView.setClickable(true);	// 클릭 설정

		// register listener for map state changes
		mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
		mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);

		// use map controller to zoom in/out, pan and set map center, zoom level etc.
		mMapController = mMapView.getMapController();	// 컨트롤 얻어오기

		// use built in zoom controls
		NMapView.LayoutParams lp = new NMapView.LayoutParams(NMapView.LayoutParams.WRAP_CONTENT,
			NMapView.LayoutParams.WRAP_CONTENT, NMapView.LayoutParams.BOTTOM_RIGHT);
		mMapView.setBuiltInZoomControls(true, lp);

		// create resource provider
		mMapViewerResourceProvider = new NMapViewerResourceProvider(this);
		// create overlay manager
		mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
		// register callout overlay listener to customize it.
		mOverlayManager.setOnCalloutOverlayListener(new OnCalloutOverlayListener(){

			@Override
			public NMapCalloutOverlay onCreateCalloutOverlay(final NMapOverlay itemOverlay,final NMapOverlayItem overlayItem, final Rect itemBounds) {
				// set your callout overlay

				return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);
			}

		});

		// location manager
		mMapLocationManager = new NMapLocationManager(this);
		mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

		// compass manager
		mMapCompassManager = new NMapCompassManager(this);

		// create my location overlay
		mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);

		// set data provider listener
		// 지도 라이브러리에서 제공하는 서버 API 호출 시 응답에 대한 콜백 인터페이스
	    super.setMapDataProviderListener(new NMapActivity.OnDataProviderListener(){
			@Override
			public void onReverseGeocoderResponse(final NMapPlacemark arg0, final NMapError arg1) {
				// TODO Auto-generated method stub
			}
		});


	}

	/* MyLocation Listener */
	private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

		@Override
		public boolean onLocationChanged(final NMapLocationManager locationManager, final NGeoPoint myLocation) {

			if (mMapController != null) {
				mMapController.animateTo(myLocation);
			}

			return true;
		}

		@Override
		public void onLocationUpdateTimeout(final NMapLocationManager locationManager) {
			Toast.makeText(MapActivity.this, "현재 위치를 얻어올수 없습니다.", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onLocationUnavailableArea(final NMapLocationManager arg0,
				final NGeoPoint arg1) {
			// TODO Auto-generated method stub

		}

	};


	private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

		@Override
		public void onAnimationStateChange(final NMapView arg0, final int arg1, final int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMapCenterChange(final NMapView arg0, final NGeoPoint arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMapCenterChangeFine(final NMapView arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMapInitHandler(final NMapView arg0, final NMapError arg1) {
			Intent intent = getIntent();
			NGeoPoint point;
			// \로깅 위치 선택 액션인지 체크
			if(intent.getAction() != null && intent.getAction().equals("logging_place_view")){
				Logging data = (Logging)intent.getSerializableExtra("loggingData");
		    	mOverlayManager.clearOverlays();
				int markerId = NMapPOIflagType.PIN;
				NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
				point = new NGeoPoint(data.getLon(), data.getLat());
				poiData.beginPOIdata(1);	// 아이템 갯수 넣어주자
				poiData.addPOIitem(point, data.getTag(), markerId, 1); 	// 아이템 추가
				poiData.endPOIdata();	// 오버레이를 닫아주자
				// create POI data overlay
				mOverlayManager.createPOIdataOverlay(poiData, null);
			}else{
				// 이전 인텐트에서 위치좌표를 가져온다.
				double lat = intent.getDoubleExtra("lat", 37.571747);
				double lon = intent.getDoubleExtra("lon", 126.999158);
				point = new NGeoPoint(lon, lat);
				dockMyPlaces();	// 내 위치 처리
			}
			mMapController.setMapCenter( point, MAP_LEVEL);
			// Geocoder를 이용하여 좌표를 주소로 변환처리
			Geocoder gc = new Geocoder(MapActivity.this,Locale.getDefault());
			List<Address> addresses = null;
			try {
				addresses = gc.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String addressStr = "현위치";
			if(addresses != null && addresses.size()>0) {	// 주소가 있으면
				// 첫번째 주소 컬렉션을 얻은후
				Address address = addresses.get(0);
				// 실제 주소만 가져온다.
				addressStr = address.getAddressLine(0).replace("대한민국", "").trim();
				Toast.makeText(MapActivity.this, addressStr, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onZoomLevelChange(final NMapView arg0, final int arg1) {
			// TODO Auto-generated method stub

		}
	};

	private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

		@Override
		public void onLongPress(final NMapView arg0, final MotionEvent arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLongPressCanceled(final NMapView arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onScroll(final NMapView arg0, final MotionEvent arg1, final MotionEvent arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSingleTapUp(final NMapView arg0, final MotionEvent arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTouchDown(final NMapView arg0, final MotionEvent arg1) {
			// TODO Auto-generated method stub

		}
	};


    // 내 위치들 도킹
	private void dockMyPlaces() {
		// TODO Auto-generated method stub
		DBHelper dbhp = new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT lat, lon, tag, date FROM "
        			+ DBHelper.MY_PLACE_TABLE, null);
    	mOverlayManager.clearOverlays();
        int count = cursor.getCount();
		// Markers for POI item
        Log.i(BaseActivity.DEBUG_TAG, "count-->" +count);
		int markerId = NMapPOIflagType.PIN;
		// set POI data
		NMapPOIdata poiData = new NMapPOIdata(count, mMapViewerResourceProvider);

		poiData.beginPOIdata(count);	// 아이템 갯수 넣어주자

		int itemId = 0;
    	addressList.clear();	// 리스트가 있을지 모르니 지워준다.
        while (cursor.moveToNext()) {
			 double lat = Double.valueOf( cursor.getString(0) );	// 위도
			 double lon = Double.valueOf( cursor.getString(1) );	// 경도
			 String logMessage = cursor.getString(2);				// 이름
			 String date = cursor.getString(3);						// 로깅 날짜
			 addressList.add(" Tag : " + logMessage + "\n\n Date: " + date );				// 주소을 저장해 주고 말풍선 클릭시 보여준다.
			 poiData.addPOIitem(lon, lat, logMessage, markerId, itemId); 	// 아이템 추가
			 itemId++;
			 Log.i(BaseActivity.DEBUG_TAG, "lat-->" + lat + "  lon-->" + lon);
        }
		cursor.close();
		poiData.endPOIdata();	// 오버레이를 닫아주자

		// create POI data overlay
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		// set event listener to the overlay
		poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
        db.close();

	}


	/* POI data State Change Listener*/
	private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
		/**
		 * 오버레이 아이콘을 클릭시 로깅메시지를 띄운다.
		 */
		@Override
		public void onCalloutClick(final NMapPOIdataOverlay poiDataOverlay, final NMapPOIitem item) {
			Log.i(LOG_TAG, "onCalloutClick: title=" + addressList.get( item.getId() )  );
			// 로깅 메시지 를 보여준다.
			Toast.makeText(MapActivity.this, addressList.get( item.getId() ) , Toast.LENGTH_LONG).show();
		}

		@Override
		public void onFocusChanged(final NMapPOIdataOverlay poiDataOverlay, final NMapPOIitem item) {
			if (item != null) {
				Log.i(LOG_TAG, "onFocusChanged: " + item.toString());
			} else {
				Log.i(LOG_TAG, "onFocusChanged: ");
			}
		}
	};

	/* Menus */
	private static final int MENU_ITEM_CLEAR_MAP = 10;
	private static final int MENU_ITEM_MAP_MODE = 20;
	private static final int MENU_ITEM_MAP_MODE_SUB_VECTOR = MENU_ITEM_MAP_MODE + 1;
	private static final int MENU_ITEM_MAP_MODE_SUB_SATELLITE = MENU_ITEM_MAP_MODE + 2;
	private static final int MENU_ITEM_MAP_MODE_SUB_HYBRID = MENU_ITEM_MAP_MODE + 3;
	private static final int MENU_ITEM_MAP_MODE_SUB_TRAFFIC = MENU_ITEM_MAP_MODE + 4;
	private static final int MENU_ITEM_MAP_MODE_SUB_BICYCLE = MENU_ITEM_MAP_MODE + 5;
	private static final int MENU_ITEM_ZOOM_CONTROLS = 30;
	private static final int MENU_ITEM_MY_LOCATION = 40;

	/**
	 * Invoked during init to give the Activity a chance to set up its Menu.
	 *
	 * @param menu the Menu to which entries may be added
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem menuItem = null;
		SubMenu subMenu = null;

		menuItem = menu.add(Menu.NONE, MENU_ITEM_CLEAR_MAP, Menu.CATEGORY_SECONDARY, "맵 초기화");
		menuItem.setAlphabeticShortcut('c');
		//menuItem.setIcon(android.R.drawable.ic_menu_revert);

		subMenu = menu.addSubMenu(Menu.NONE, MENU_ITEM_MAP_MODE, Menu.CATEGORY_SECONDARY, "지도 모드");
		//subMenu.setIcon(android.R.drawable.ic_menu_mapmode);

		menuItem = subMenu.add(0, MENU_ITEM_MAP_MODE_SUB_VECTOR, Menu.NONE, "일반모드");
		menuItem.setAlphabeticShortcut('m');
		menuItem.setCheckable(true);
		menuItem.setChecked(false);

		menuItem = subMenu.add(0, MENU_ITEM_MAP_MODE_SUB_SATELLITE, Menu.NONE, "위성모드");
		menuItem.setAlphabeticShortcut('s');
		menuItem.setCheckable(true);
		menuItem.setChecked(false);

		menuItem = subMenu.add(0, MENU_ITEM_MAP_MODE_SUB_HYBRID, Menu.NONE, "혼합모드");
		menuItem.setAlphabeticShortcut('h');
		menuItem.setCheckable(true);
		menuItem.setChecked(false);

		menuItem = subMenu.add(0, MENU_ITEM_MAP_MODE_SUB_TRAFFIC, Menu.NONE, "교통");
		menuItem.setAlphabeticShortcut('t');
		menuItem.setCheckable(true);
		menuItem.setChecked(false);

		menuItem = subMenu.add(0, MENU_ITEM_MAP_MODE_SUB_BICYCLE, Menu.NONE, "자전거");
		menuItem.setAlphabeticShortcut('b');
		menuItem.setCheckable(true);
		menuItem.setChecked(false);

		menuItem = menu.add(0, MENU_ITEM_ZOOM_CONTROLS, Menu.CATEGORY_SECONDARY, "줌 컨트롤");
		menuItem.setAlphabeticShortcut('z');
		//menuItem.setIcon(android.R.drawable.ic_menu_zoom);

		menuItem = menu.add(0, MENU_ITEM_MY_LOCATION, Menu.CATEGORY_SECONDARY, "내위치 찾기");
		menuItem.setAlphabeticShortcut('l');
		//menuItem.setIcon(android.R.drawable.ic_menu_mylocation);


		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		super.onPrepareOptionsMenu(pMenu);

		int viewMode = mMapController.getMapViewMode();

		pMenu.findItem(MENU_ITEM_CLEAR_MAP).setEnabled(
			(viewMode != NMapView.VIEW_MODE_VECTOR) || mOverlayManager.sizeofOverlays() > 0);
		pMenu.findItem(MENU_ITEM_MAP_MODE_SUB_VECTOR).setChecked(viewMode == NMapView.VIEW_MODE_VECTOR);
		pMenu.findItem(MENU_ITEM_MAP_MODE_SUB_SATELLITE).setChecked(viewMode == NMapView.VIEW_MODE_SATELLITE);
		pMenu.findItem(MENU_ITEM_MAP_MODE_SUB_HYBRID).setChecked(viewMode == NMapView.VIEW_MODE_HYBRID);


		if (mMyLocationOverlay == null) {
			pMenu.findItem(MENU_ITEM_MY_LOCATION).setEnabled(false);
		}

		return true;
	}

	/**
	 * Invoked when the user selects an item from the Menu.
	 *
	 * @param item the Menu entry which was selected
	 * @return true if the Menu item was legit (and we consumed it), false
	 *         otherwise
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case MENU_ITEM_CLEAR_MAP:	// 맵 초기화
				if (mMyLocationOverlay != null) {
					stopMyLocation();
					mOverlayManager.removeOverlay(mMyLocationOverlay);
				}

				mMapController.setMapViewMode(NMapView.VIEW_MODE_VECTOR);
				mMapController.setMapViewTrafficMode(false);
				mMapController.setMapViewBicycleMode(false);

				return true;

			case MENU_ITEM_MAP_MODE_SUB_VECTOR:
				mMapController.setMapViewMode(NMapView.VIEW_MODE_VECTOR);
				return true;

			case MENU_ITEM_MAP_MODE_SUB_SATELLITE:
				mMapController.setMapViewMode(NMapView.VIEW_MODE_SATELLITE);
				return true;

			case MENU_ITEM_MAP_MODE_SUB_HYBRID:
				mMapController.setMapViewMode(NMapView.VIEW_MODE_HYBRID);
				return true;

			case MENU_ITEM_MAP_MODE_SUB_TRAFFIC:
				mMapController.setMapViewTrafficMode(!mMapController.getMapViewTrafficMode());
				return true;

			case MENU_ITEM_MAP_MODE_SUB_BICYCLE:
				mMapController.setMapViewBicycleMode(!mMapController.getMapViewBicycleMode());
				return true;

			case MENU_ITEM_ZOOM_CONTROLS:
				mMapView.displayZoomControls(true);
				return true;

			case MENU_ITEM_MY_LOCATION:
				startMyLocation();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}


	/**
	 * 내위치 찾기
	 */
	private void startMyLocation() {

		if (mMyLocationOverlay != null) {
			if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
				mOverlayManager.addOverlay(mMyLocationOverlay);
			}

			if (mMapLocationManager.isMyLocationEnabled()) {

				if (!mMapView.isAutoRotateEnabled()) {
					mMyLocationOverlay.setCompassHeadingVisible(true);

					mMapCompassManager.enableCompass();

					mMapView.setAutoRotateEnabled(true, false);

					mMapView.requestLayout();
				} else {
					stopMyLocation();
				}

				mMapView.postInvalidate();
			} else {
				boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(false);
				if (!isMyLocationEnabled) {
					Toast.makeText(MapActivity.this, "Please enable a My Location source in system settings",
						Toast.LENGTH_LONG).show();

					Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(goToSettings);

					return;
				}
			}
		}
	}

	/**
	 * 내 위치 찾기 중단
	 */
	private void stopMyLocation() {
		if (mMyLocationOverlay != null) {
			mMapLocationManager.disableMyLocation();

			if (mMapView.isAutoRotateEnabled()) {
				mMyLocationOverlay.setCompassHeadingVisible(false);

				mMapCompassManager.disableCompass();

				mMapView.setAutoRotateEnabled(false, false);

				mMapView.requestLayout();
			}
		}
	}
	
	

	/**
	 * 경로 받아오기 쓰레드 처리 클래스
	 */
	private class PathLoadTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private String jsonText;

		@Override
		protected void onPostExecute(Boolean result) {
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}

			if (result && jsonText != null) {	// 정상 수신
				Log.i("diary", jsonText);
				parseJson(jsonText.trim());
				
				// set path data points
				NMapPathData pathData = new NMapPathData(9);

				pathData.initPathData();
				pathData.addPathPoint(127.108099, 37.366034, NMapPathLineStyle.TYPE_SOLID);
				pathData.addPathPoint(127.108088, 37.366043, 0);
				pathData.addPathPoint(127.108079, 37.365619, 0);
				pathData.addPathPoint(127.107458, 37.365608, 0);
				pathData.addPathPoint(127.107232, 37.365608, 0);
				pathData.addPathPoint(127.106904, 37.365624, 0);
				pathData.addPathPoint(127.105933, 37.365621, NMapPathLineStyle.TYPE_DASH);
				pathData.addPathPoint(127.105929, 37.366378, 0);
				pathData.addPathPoint(127.106279, 37.366380, 0);
				pathData.endPathData();

				NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
				
				/*
				mapOverlays = mMapView.getOverlays();
				mapOverlays.clear();	// 이전 오버레이는 제거
				projection = mMapView.getProjection();
				mapOverlays.add(new MyOverlay());
				
				addPointOverlayItem();
				
				// 경로 정보 
				startPlaceTv.setText("출발 : " + startAddress.replace("대한민국", " "));
				endPlaceTv.setText("도착 : " +endAddress.replace("대한민국", " "));
				// 예상 시간 설정
				String hour = "";	// 60분 이상일경우 시간으로 변환처리
				long time = Math.round( Double.valueOf(totalTime)) ;
				if( time >  60){	// 1시간 이상이면 시간으로 변환
					hour = String.valueOf(time / 60);
					totalTime = hour +"시간 " + String.valueOf(time % 60) +"분";
				}else{
					totalTime = time + "분";
				}
				totalTimeTv.setText("예상시간 : 약 " +totalTime) ;
				totalDistanceTv.setText("거리 : 약" + Math.round( Double.valueOf(totalDistance) / 1000) + "Km");
				infoBox.setVisibility(View.VISIBLE);	// 주소정보 박스를 보여준다.
				 */				
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = ProgressDialog.show(MapActivity.this, "경로 탐색중",
					"잠시만 기다려 주세요 경로를 탐색중입니다.");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			jsonText = searchNaviPath();
			return true;
		}

		/**
		 * json을 파싱하여 경로정보를가져와 컬랙션에 좌표를 저장한다.
		 * 
		 * @param json
		 *            원격지에서 받은 json String
		 */
		private void parseJson(String json) {
			verTexData = new VerTexData();
			ArrayList<GPoint> list = new ArrayList<GPoint>();
			try {
				// json 이외 문자 제거
				json = json.replaceFirst("\"", "");
				json = json.substring(0, json.lastIndexOf("\"") + 1);

				JSONObject RESDATA = (new JSONObject(json))
						.getJSONObject("RESDATA");
				JSONObject SROUTE = RESDATA.getJSONObject("SROUTE");
				JSONObject ROUTE = SROUTE.getJSONObject("ROUTE");
				// 예상 시간
				totalTime =  ROUTE.getString("total_time");
				// 거리
				totalDistance = ROUTE.getString("total_dist");
				
				/*
				ROUTE":
				{
				"total_time":"14.28",
				"total_dist":"2956",
				"rg_count":"8",				
				*/
				JSONObject LINKS = SROUTE.getJSONObject("LINKS");
				JSONArray links = LINKS.getJSONArray("link");

				for (int i = 0; i < links.length(); i++) { // 경로 배열
					JSONObject arr = links.getJSONObject(i);
					JSONArray vertex = arr.getJSONArray("vertex"); // 좌표 버택스 배열
					for (int j = 0; j < vertex.length(); j++) {
						JSONObject obj = vertex.getJSONObject(j);

						String y = obj.getString("y"); // y 좌표 얻기
						String x = obj.getString("x"); // x 좌표 얻기
						// 좌표를 저장
						list.add(new GPoint(Double.valueOf(x), Double
								.valueOf(y)));
					}
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			verTexData.setList(list);
		}
		
		/**
		 * 경로 탐색 GeoRouteSearch 파라미터 설정후 경로 탐색 시작
		 * 
		 * @return 응답된 json 문자열
		 */
		private String searchNaviPath() {
			Log.i("naviApp", "start");
			// 경로 파라미터 객체
			GeoRouteSearch.Params parmas = new GeoRouteSearch.Params();
			//37.554857,127.168658
			// 출발점
			parmas.SX = "127.168658";// + // startPoint.getLongitudeE6() / 1E6;
			parmas.SY = "37.554857";// + // startPoint.getLatitudeE6() / 1E6;
			// 도착점
			//37.557813,127.144539
			parmas.EX = "127.144539";// + // endPoint.getLongitudeE6() / 1E6;
			parmas.EY = "37.557813";// + // endPoint.getLatitudeE6() / 1E6;
			// 이동 타입
			parmas.RPTYPE = "0";
			// 좌표 타입
			parmas.COORDTYPE = "0";
			// 경로 타입 설정
			parmas.PRIORITY = "0";
			// 현재 시각
			parmas.timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
					.format(new Date());
			// 경로 찾기 수행
			GeoRouteSearch t = new GeoRouteSearch(parmas);
			String jsonText = t.execute();

			return jsonText; // 경로 처리 json 응답값
		}
		

	}	


}

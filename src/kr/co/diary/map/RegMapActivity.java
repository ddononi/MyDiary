package kr.co.diary.map;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kr.co.diary.DBHelper;
import kr.co.diary.R;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager.OnCalloutOverlayListener;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay.OnFloatingItemChangeListener;

/**
 *	Naver open api 를 이용하여 지도를 보여준다.
 *	위치 로깅 내역을 오버레이 아이콘으로 나타낸다.
 */
public class RegMapActivity extends NMapActivity {
	// naver open api key
	private static final String API_KEY = "7b8d41389c90191b5a7dbfa0e8d71aac";
	private static final int MAP_LEVEL = 10;
	// mapview 설정
	private NMapView mMapView = null;
	private NMapController mMapController;
	private static final String LOG_TAG = "NMapViewer";

	private NMapMyLocationOverlay mMyLocationOverlay;
	private NMapOverlayManager mOverlayManager;
	private NMapLocationManager mMapLocationManager;
	private NMapCompassManager mMapCompassManager;
	private NMapViewerResourceProvider mMapViewerResourceProvider;
	private final ArrayList<String> addressList = new ArrayList<String>(); // 주소를 저장할 리스트

	private NGeoPoint mNgeoPoint = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		setContentView(R.layout.reg_map_layout);

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

	    initRegBoxLayout();

	}

	private void initRegBoxLayout() {
		findViewById(R.id.checkBox);
		final EditText titleET = (EditText)findViewById(R.id.title);
		final CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox);
		Button addBtn = (Button)findViewById(R.id.add_myplace_btn);
		// 일정추가 이벤트 처리
		addBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (TextUtils.isEmpty(titleET.getText().toString())) {
					Toast.makeText(RegMapActivity.this, "로깅 메시지를 입력하세요",
							Toast.LENGTH_SHORT).show();
					return;
				}

				DBHelper dbhp = new DBHelper(RegMapActivity.this); // 도우미 클래스
				SQLiteDatabase db = dbhp.getWritableDatabase();
				ContentValues cv = new ContentValues();
				if (mNgeoPoint == null) { // 수신된 자표가 없으면
					Toast.makeText(RegMapActivity.this, "현재 위치를 찾을수 없습니다.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				// 현재 시간 생성
				Calendar currentCal = Calendar.getInstance();
				currentCal.setTimeInMillis(System.currentTimeMillis());
				String date = sdf.format(currentCal.getTime());

				cv.put("lat", mNgeoPoint.latitude);
				cv.put("lon", mNgeoPoint.longitude);
				cv.put("tag", titleET.getText().toString());
				cv.put("date", date);
				cv.put("alarm", checkBox.isChecked() ? 1 : 0);
				// db에 정상적으로 추가 되었으면 토스트를 굽는다.
				if (db.insert(DBHelper.MY_PLACE_TABLE, null, cv) > 0) {
					Toast.makeText(RegMapActivity.this, "현재 위치가 추가되었습니다.",
							Toast.LENGTH_SHORT).show();
				}
				db.close();
				finish();
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
			Toast.makeText(RegMapActivity.this, "현재 위치를 얻어올수 없습니다.", Toast.LENGTH_LONG).show();
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
			// TODO Auto-generated method stub
			// 이전 인텐트에서 위치좌표를 가져온다.
			Intent intent = getIntent();
			double lat = intent.getDoubleExtra("lat", 37.571747);
			double lon = intent.getDoubleExtra("lon", 126.999158);
			NGeoPoint initPlace = new NGeoPoint(lon, lat);
			/*
			if(intent.hasExtra("lat")){	// 넘어온 좌표가 있으면
				Toast.makeText(MapActivity.this,
						"현재 위치로 부터 위치내역을 보여줍니다.", Toast.LENGTH_SHORT).show();
			}
			*/
			mMapController.setMapCenter( initPlace, MAP_LEVEL);
			initPoiItem(initPlace);

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

	 // 위치 선택 마커 설정
	private void initPoiItem(final NGeoPoint point) {
    	mOverlayManager.clearOverlays();
		int markerId = NMapPOIflagType.PIN;
		NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);

		poiData.beginPOIdata(1);	// 아이템 갯수 넣어주자
		poiData.addPOIitem(point, "위치를 선택하세요", markerId, 1); 	// 아이템 추가
		poiData.getPOIitem(0).setFloatingMode(NMapPOIitem.FLOATING_DRAG);
		poiData.endPOIdata();	// 오버레이를 닫아주자

		// create POI data overlay
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		// set event listener to the overlay
		poiDataOverlay.setOnFloatingItemChangeListener(onFloatingItemChangeListener);
	}

	/* POI data State Change Listener*/
	private final OnFloatingItemChangeListener onFloatingItemChangeListener = new NMapPOIdataOverlay.OnFloatingItemChangeListener() {

		@Override
		public void onPointChanged(final NMapPOIdataOverlay arg0, final NMapPOIitem POIitem) {
			mNgeoPoint = POIitem.getPoint();	// 마커 위치 좌표 저장
			// Geocoder를 이용하여 좌표를 주소로 변환처리
			Geocoder gc = new Geocoder(RegMapActivity.this,Locale.getDefault());
			List<Address> addresses = null;
			try {
				addresses = gc.getFromLocation(mNgeoPoint.getLatitude(), mNgeoPoint.getLongitude(), 1);
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
				Toast.makeText(RegMapActivity.this, addressStr, Toast.LENGTH_LONG).show();
				POIitem.setTitle(addressStr);
			}

		}

	};

	private static final int MENU_ITEM_MY_LOCATION = 40;
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
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
					Toast.makeText(RegMapActivity.this, "Please enable a My Location source in system settings",
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




}

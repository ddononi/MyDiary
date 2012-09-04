package kr.co.diary.navi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * 
 */
public class NaviMapActivity extends MapActivity implements iConstant {
	private MapView mMapView;
	private Drawable drawable;
	private CustomItemizedOverlay<CustomOverlayItem> itemizedOverlay;
	private List<Overlay> mapOverlays; // 오버레이 아이템 리스트
	private Projection projection;
	private VerTexData verTexData;
	
	private LinearLayout infoBox;			// 경로 정보 박스
	private TextView startPlaceTv;			// 출발 위치
	private TextView endPlaceTv;			// 도착 위치	
	private TextView totalDistanceTv;		// 총 거리
	private TextView totalTimeTv;			// 예상 시간	
	
	// location
	private GeoPoint startPoint;
	private GeoPoint endPoint;

	private String startAddress;
	private String endAddress;
	private String totalDistance;
	private String totalTime;	

	private String priority;
	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.map_layout);
		getIntent();
		initLayout();
		
		try {
			parseBuildingInfoJson();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 맵뷰 초기화 및 레이아웃 초기화
	 */
	private void initLayout() {
		// 맵뷰 설정
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true); // 줌 컨트롤
		mMapView.setTraffic(true);
		infoBox = (LinearLayout)findViewById(R.id.navi_info_box);
		// 상단 네비 정보 텍스트뷰 엘리먼트
		startPlaceTv = (TextView)findViewById(R.id.start_place);
		endPlaceTv = (TextView)findViewById(R.id.end_place);		
		totalTimeTv = (TextView)findViewById(R.id.total_time);				
		totalDistanceTv = (TextView)findViewById(R.id.distance);			

		mapOverlays = mMapView.getOverlays();
		// first overlay
		drawable = getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(
				drawable, mMapView);		
	}

	private void addPointOverlayItem() {
		CustomOverlayItem startPointOverlayItem = new CustomOverlayItem(
				startPoint, "출발장소", startAddress, "http://www.tjeju.kr/Helper/UC/ImgLink/noimg/NoImage356.jpg");
		itemizedOverlay.addOverlay(startPointOverlayItem);

		CustomOverlayItem endPointOverlayItem = new CustomOverlayItem(
				endPoint, "도착장소", endAddress, "http://www.tjeju.kr/Helper/UC/ImgLink/noimg/NoImage356.jpg");
		itemizedOverlay.addOverlay(endPointOverlayItem);

		itemizedOverlay.setOnItemClickListener(new OnOverlayItemClickListener() {

			public void onClick(CustomOverlayItem item,
					CustomItemizedOverlay cio) {
				cio.hideBalloon();
			}

		});

		mapOverlays.add(itemizedOverlay);
		final MapController mc = mMapView.getController();
		mc.animateTo(startPoint); // 시작점으로 좌표 이동
		mc.setZoom(16); // 줌 컨트롤 초기값
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			mMapView.setSatellite(false);
			break;
		case 1:
			mMapView.setSatellite(true);
			break;
		case 2:
			infoBox.setVisibility(View.GONE);
			Intent intent = new Intent(this, NaviSearchActivity.class);
			startActivityForResult(intent, 11);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "일반지도");
		menu.add(0, 1, 1, "위성지도");
		menu.add(0, 2, 2, "경로탐색");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * NaviSearchActivity에서 주소변환 결과 처리후 넘어온 결과 처리
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 정상 검색 처리면
		if (resultCode == RESULT_OK) {
			// 출발점 가져오기
			startPoint = new GeoPoint(data.getIntExtra("startLat", -1),
					data.getIntExtra("startLng", DEFAULT_LAT));
			// 도착점 가져오기
			endPoint = new GeoPoint(data.getIntExtra("endLat", -1),
					data.getIntExtra("endLng", DEFAULT_LNG));

			// 출발 주소명
			startAddress = data.getStringExtra("startPlace");
			// 도착 주소명
			endAddress = data.getStringExtra("endPlace");
			
			priority = data.getStringExtra("priority");
			new PathLoadTask().execute(); // 경로 찾기 실행
		}
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

		// 출발점
		parmas.SX = "" + startPoint.getLongitudeE6() / 1E6;
		parmas.SY = "" + startPoint.getLatitudeE6() / 1E6;
		// 도착점
		parmas.EX = "" + endPoint.getLongitudeE6() / 1E6;
		parmas.EY = "" + endPoint.getLatitudeE6() / 1E6;
		// 이동 타입
		parmas.RPTYPE = "0";
		// 좌표 타입
		parmas.COORDTYPE = "0";
		// 경로 타입 설정
		parmas.PRIORITY =priority;
		// 현재 시각
		parmas.timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
				.format(new Date());
		// 경로 찾기 수행
		GeoRouteSearch t = new GeoRouteSearch(parmas);
		String jsonText = t.execute();

		return jsonText; // 경로 처리 json 응답값
	}

	/**
	 * draw를 이용하여 지도에 경로를 그려준다.
	 */
	class MyOverlay extends Overlay {

		public MyOverlay() {

		}

		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
			super.draw(canvas, mapv, shadow);
			// 경로 선 속성
			Paint mPaint = new Paint();
			mPaint.setDither(true);
			mPaint.setColor(Color.RED);
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(6);

			ArrayList<GPoint> list = verTexData.getList();
			int latitude;
			int longitude;
			GPoint gPoint1, gPoint2;
			GeoPoint gP1, gP2;
			Point p1, p2;
			Path path = new Path();
			for (int i = 0; i < list.size() - 1; i++) {
				gPoint1 = list.get(i);
				latitude = (int) (gPoint1.y * 1E6);
				longitude = (int) (gPoint1.x * 1E6);
				gP1 = new GeoPoint(latitude, longitude);

				gPoint2 = list.get(i + 1);
				latitude = (int) (gPoint2.y * 1E6);
				longitude = (int) (gPoint2.x * 1E6);
				gP2 = new GeoPoint(latitude, longitude);

				p1 = new Point();
				p2 = new Point();

				// geopoint 좌표를 mapview projection 위치로 변환시킨다.
				projection.toPixels(gP1, p1);
				projection.toPixels(gP2, p2);

				// path 설정
				path.moveTo(p2.x, p2.y);
				path.lineTo(p1.x, p1.y);
			}
			// 캔버스에 그려준다.
			canvas.drawPath(path, mPaint);			
		}
	}
	
	/**
	 * asset에 저장된 학교정보 json을 파싱하여 
	 * 학교 정보 커스텀 오버레이 아이템으로 만들어 준다.
	 * @throws IOException
	 */
	private void parseBuildingInfoJson() throws IOException {
		AssetManager am = getResources().getAssets();
		InputStream is = am.open("building_info.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String data;
		while( (data = br.readLine()) != null){
			sb.append(data);
		}
		String json = sb.toString();
		
		try {
			// json 이외 문자 제거

			JSONArray jsonArray = (new JSONObject(json)).getJSONArray("buildings");
			CustomOverlayItem overLay;
			GeoPoint point;
			for (int i = 0; i < jsonArray.length(); i++) { // 경로 배열
				JSONObject obj = jsonArray.getJSONObject(i);

					String name = obj.getString("name"); // y 좌표 얻기
					String info = obj.getString("info"); 		// x 좌표 얻기
					String img = obj.getString("img"); 		// x 좌표 얻기
					int lat = (int)(Double.parseDouble(obj.getString("lat")) * 1E6); 		// x 좌표 얻기
					int lng = (int)(Double.parseDouble(obj.getString("lng")) *1E6); 		// x 좌표 얻기
					point = new GeoPoint(lat, lng);
					overLay = new CustomOverlayItem(point, name, info, img);
					itemizedOverlay.addOverlay(overLay);
					mapOverlays.add(itemizedOverlay);
					itemizedOverlay.setOnItemClickListener(new OnOverlayItemClickListener() {
						public void onClick(CustomOverlayItem item,
								CustomItemizedOverlay cio) {
							cio.hideBalloon();
						}
					});
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

			if (result) {	// 정상 수신
				parseJson(jsonText.trim());
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
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = ProgressDialog.show(NaviMapActivity.this, "경로 탐색중",
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

	}
}

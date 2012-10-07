package kr.co.diary;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kr.co.diary.data.ForecastData;
import kr.co.diary.data.WeatherData;
import kr.co.diary.widget.WebImageView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class WeatherActivity extends MyActivity {
	private TextView mCurrLocal;
	private TextView mCurrTemp;
	private TextView mCurrHumdity;
	private WebImageView mCurrCondImage;

	private ListView mListView;
	private Context mContext;

	private String selectedLocal = "서울";
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_layout);
		mContext = this;
		initLayout();

		new LoadWeatherTask().execute("seoul");
	}

	/**
	 * 레이아웃 hooking 및 이벤트 설정
	 */
	private void initLayout() {
		mCurrLocal = (TextView) findViewById(R.id.location);
		mCurrTemp = (TextView) findViewById(R.id.location_temp);
		mCurrHumdity = (TextView) findViewById(R.id.location_hum);
		mCurrCondImage = (WebImageView) findViewById(R.id.icon);

		mListView = (ListView) findViewById(R.id.list_view);
		Button localBtn = (Button) findViewById(R.id.local_btn);
		localBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				new AlertDialog.Builder(mContext)
				.setTitle("지역 선택하기")
				.setItems(R.array.local_list,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int which) {
								// 지역선택후 해당 지역 날씨 정보 가져오기
								String[] locals = mContext.getResources().getStringArray(R.array.local_list_value);
								selectedLocal =  mContext.getResources().getStringArray(R.array.local_list)[which]; 
								new LoadWeatherTask().execute(locals[which]);
							}
						}).show();
			}
		});
	}

	private class LoadWeatherTask extends AsyncTask<String, WeatherData, WeatherData> {
		private ProgressDialog progress = null;

		@Override
		protected WeatherData doInBackground(final String... params) {
			WeatherData data = null;
			try {
				data = requestWeatherData(params[0]);
			} catch (IOException ioe){
				Log.i(DEBUG_TAG , "" +ioe.getMessage());
			} catch (Exception e) {
				Log.i(DEBUG_TAG , "" +  e.getMessage());
			}
			return data;
		}

		/*
		 * 데이터 로딩 완료처리후
		 */
		@Override
		protected void onPostExecute(final WeatherData data) {
			if(data == null){	// 날씨 정보가 없을 경우
				Toast.makeText(mContext, "날씨 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
			}else{
				// 엘리먼트에 날씨정보 내용을 채워준다.
				mCurrLocal.setText(data.getLocal());
				mCurrTemp.setText(data.getCurrTemp());
				mCurrHumdity.setText(data.getCurrHumidify());
				WeatherListAdapter adapter = new WeatherListAdapter(data.getForecasts(), mContext);
				mListView.setAdapter(adapter);
			}

			// 로딩창 닫기
			if(progress != null && progress.isShowing()){
				progress.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progress = ProgressDialog.show(mContext, "", "날씨를 불러오는 중입니다.");
		}

	}

	/**
	 * 날씨 정보 요청
	 * 구글 api 를 이용하여 날씨정보를 xml형태로 수신하고
	 * XML 데이터를 파싱
	 * @param string
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public WeatherData requestWeatherData(String local) throws IOException, SAXException, ParserConfigurationException {
		// 구글 날씨 api 조립
		String weatherUrl  =MSN_WEATHER_URL + URLEncoder.encode(local, "UTF-8");
		URL url = new URL(weatherUrl);
		URLConnection conn = url.openConnection();
		// 연결시도 시간 설정
		conn.setConnectTimeout(CONNECTION_TIME_OUT);
		String line;
		StringBuilder sb = new StringBuilder();
		// 한줄 단위로 가져올수 있게 bufferedReader 로 가져온다.
		// 인코딩 euc-kr로 변환
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream() , "UTF-8"));
		while((line = reader.readLine()) != null){
			sb.append(line);
		}
		return parseXml(sb.toString());
	}

	/**
	 * 수신한 XML을 SAX 파싱처리
	 * @param string
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private WeatherData parseXml(final String xmlStr) throws SAXException, IOException, ParserConfigurationException {
		Log.i(DEBUG_TAG,  xmlStr);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
		Document document = builder.parse(is);
		Element weather = document.getDocumentElement();

		NodeList nodeList;
		Node node;

		// 날씨 root
		nodeList = weather.getElementsByTagName("weather");
		// 첫번째 지역만 가져온다.
		node = nodeList.item(0);
		WeatherData data =new WeatherData();		
		data.setLocal(selectedLocal);	// 지역 
		NodeList currentCondiList = nodeList.item(0).getChildNodes();

		// 날씨 가져오기
		data.setCurrTemp("현재 기온 : " + currentCondiList.item(0).getAttributes().getNamedItem("temperature").getNodeValue() +"℃" );
		Log.i(DEBUG_TAG,  "현재기온: " +  data.getCurrTemp());			
		// 습도 가져오기
		data.setCurrHumidify( currentCondiList.item(0).getAttributes().getNamedItem("humidity").getNodeValue() );
		Log.i(DEBUG_TAG,  "습도 : " +  data.getCurrTemp());			
		// 이미지 가져오기
		data.setCurrWeatherImgUrl(currentCondiList.item(0).getAttributes().getNamedItem("skycode").getNodeValue() );
		Log.i(DEBUG_TAG,  "이미지 : " +  data.getCurrWeatherImgUrl());			
		// 이미지 설정
		mCurrCondImage.setImasgeUrl(MSN_WEATHER_IMAGE_URL + data.getCurrWeatherImgUrl() +  ".gif");
		// 날씨상태

		ArrayList<ForecastData> list = new ArrayList<ForecastData>();
		// 마지막은 toobar이므로 제거 이다
		int size = currentCondiList.getLength()-1;
		for(int i =0; i< size;  i++){
			// 첫번째는 현재 날씨이므로 넘어간다.
			if(i == 0){
				continue;
			}
			Node forecastItems = currentCondiList.item(i);
			
			ForecastData forecastData = new ForecastData();
			//NamedNodeMap attrs = forecastItems.item(0).getAttributes();
			// 요일 가져오기
			forecastData.setDayOfWeek(forecastItems.getAttributes().getNamedItem("day").getNodeValue() );
			Log.i(DEBUG_TAG,  "날짜: " +  forecastData.getDayOfWeek());				
			// 최저기온 가져오기
			forecastData.setLowTemp(forecastItems.getAttributes().getNamedItem("low").getNodeValue() );
			// 최고기온 가져오기
			forecastData.setHighTemp(forecastItems.getAttributes().getNamedItem("high").getNodeValue() );
			// 날씨 이미지 가져오기
			forecastData.setWeatherImgUrl(forecastItems.getAttributes().getNamedItem("skycodeday").getNodeValue() );
			// 날씨  상태 가져오기
			forecastData.setCondition(forecastItems.getAttributes().getNamedItem("skytextday").getNodeValue() );
			// 리스트에 날씨 정보 담기
			list.add(forecastData);
		}
		data.setForecasts(list);

		return data;
	}

}

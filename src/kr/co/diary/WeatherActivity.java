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

/**
 * @author ddononi
 *
 */
public class WeatherActivity extends BaseActivity {
	private TextView mCurrLocal;
	private TextView mCurrTemp;
	private TextView mCurrHumdity;
	private WebImageView mCurrCondImage;

	private ListView mListView;
	private Context mContext;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_layout);
		mContext = this;
		initLayout();

		new LoadWeatherTask().execute("서울");
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
								String[] locals = mContext.getResources().getStringArray(R.array.local_list);
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

			} catch (Exception e) {
				// TODO: handle exception
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
	public WeatherData requestWeatherData(final String local) throws IOException, SAXException, ParserConfigurationException {
		// 구글 날씨 api 조립
		String weatherUrl  =GOOGLE_WEATHER_URL + URLEncoder.encode(local, "UTF-8");
		URL url = new URL(weatherUrl);
		URLConnection conn = url.openConnection();
		// 연결시도 시간 설정
		conn.setConnectTimeout(CONNECTION_TIME_OUT);
		String line;
		StringBuilder sb = new StringBuilder();
		// 한줄 단위로 가져올수 있게 bufferedReader 로 가져온다.
		// 인코딩 euc-kr로 변환
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream() , "EUC-KR"));
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
		Log.i("diary",  xmlStr);
		WeatherData data =new WeatherData();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
		Document document = builder.parse(is);
		Element weather = document.getDocumentElement();

		NodeList nodeList;
		Node node;

		nodeList = weather.getElementsByTagName("forecast_information");
		node = nodeList.item(0).getFirstChild();
		data.setLocal(node.getAttributes().getNamedItem("data").getNodeValue());	// 지역 가져오기
		nodeList = weather.getElementsByTagName("current_conditions");
		NodeList currentCondiList = nodeList.item(0).getChildNodes();

		// 날씨 가져오기
		data.setCurrTemp("현재 기온 : " + currentCondiList.item(2).getAttributes().getNamedItem("data").getNodeValue() +"℃" );
		// 습도 가져오기
		data.setCurrHumidify( currentCondiList.item(3).getAttributes().getNamedItem("data").getNodeValue() );
		// 이미지 가져오기
		data.setCurrWeatherImgUrl(currentCondiList.item(4).getAttributes().getNamedItem("data").getNodeValue() );
		mCurrCondImage.setImasgeUrl(GOOGLE_URL + data.getCurrWeatherImgUrl());
		// 날씨상태
		nodeList = weather.getElementsByTagName("forecast_conditions");
		ArrayList<ForecastData> list = new ArrayList<ForecastData>();
		for(int i =0; i<nodeList.getLength();  i++){
			NodeList forecastItems = nodeList.item(i).getChildNodes();
			ForecastData forecastData = new ForecastData();
			// 요일 가져오기
			forecastData.setDayOfWeek(forecastItems.item(0).getAttributes().getNamedItem("data").getNodeValue() );
			// 최저기온 가져오기
			forecastData.setLowTemp(forecastItems.item(1).getAttributes().getNamedItem("data").getNodeValue() );
			// 최고기온 가져오기
			forecastData.setHighTemp(forecastItems.item(2).getAttributes().getNamedItem("data").getNodeValue() );
			// 날씨 이미지 가져오기
			forecastData.setWeatherImgUrl(forecastItems.item(3).getAttributes().getNamedItem("data").getNodeValue() );
			// 날씨  상태 가져오기
			forecastData.setCondition(forecastItems.item(4).getAttributes().getNamedItem("data").getNodeValue() );
			// 리스트에 날씨 정보 담기
			list.add(forecastData);
		}
		data.setForecasts(list);

		return data;
	}

}

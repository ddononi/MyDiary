package kr.co.diary.data;

import java.util.ArrayList;

/**
 * 날씨정보를 담을 데이터 클래스
 */
public class WeatherData {
	private String local; // 지역
	private String currTemp; // 현재 온도
	private String currHumidify; // 현재 습도
	private String currWeatherImgUrl; //
	private ArrayList<ForecastData> forecasts = new ArrayList<ForecastData>();

	public String getCurrTemp() {
		return currTemp;
	}

	public void setCurrTemp(String currTemp) {
		this.currTemp = currTemp;
	}

	public String getCurrHumidify() {
		return currHumidify;
	}

	public void setCurrHumidify(String currHumidify) {
		this.currHumidify = currHumidify;
	}

	public String getCurrWeatherImgUrl() {
		return currWeatherImgUrl;
	}

	public void setCurrWeatherImgUrl(String currWeatherImgUrl) {
		this.currWeatherImgUrl = currWeatherImgUrl;
	}

	public ArrayList<ForecastData> getForecasts() {
		return forecasts;
	}

	public void setForecasts(ArrayList<ForecastData> forecasts) {
		this.forecasts = forecasts;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

}

package kr.co.diary.data;

public class ForecastData {
	private String dayOfWeek;
	private String condition;
	private String lowTemp;
	private String highTemp;
	private String weatherImgUrl;

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getLowTemp() {
		return lowTemp;
	}

	public void setLowTemp(String lowTemp) {
		this.lowTemp = lowTemp;
	}

	public String getHighTemp() {
		return highTemp;
	}

	public void setHighTemp(String highTemp) {
		this.highTemp = highTemp;
	}

	public String getWeatherImgUrl() {
		return weatherImgUrl;
	}

	public void setWeatherImgUrl(String weatherImgUrl) {
		this.weatherImgUrl = weatherImgUrl;
	}

}

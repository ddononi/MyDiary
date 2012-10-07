package kr.co.diary;

import java.util.ArrayList;

import kr.co.diary.data.ForecastData;
import kr.co.diary.widget.WebImageView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WeatherListAdapter extends BaseAdapter {
	private ArrayList<ForecastData> list = null;
	private final LayoutInflater inflater;
	// yahoo weather xml url
	public final static String MSN_WEATHER_IMAGE_URL = "http://blu.stc.s-msn.com/as/wea3/i/en/";
	
	public WeatherListAdapter(final ArrayList<ForecastData> list, final Context mContext) {
		this.list = list;
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(final int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(final int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** list 의 각 view 설정 */
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewGroup item = getViewGroup(convertView, parent);
		ForecastData data = (ForecastData)getItem(position);
		// 엘리먼트 후킹
		TextView dayOfWeekTv = (TextView) item.findViewById(R.id.list_dayofweek);
		TextView conditionTv = (TextView) item.findViewById(R.id.list_condition);
		TextView maxTempTv = (TextView) item.findViewById(R.id.list_max_temp);
		TextView minTempTv = (TextView) item.findViewById(R.id.list_min_temp);
		WebImageView imgTv = (WebImageView) item.findViewById(R.id.icon);
		// 엘리먼트에 값을 set해준다,
		dayOfWeekTv.setText(data.getDayOfWeek());
		conditionTv.setText(data.getCondition());
		maxTempTv.setText(data.getHighTemp() +"℃");
		minTempTv.setText(data.getLowTemp() +"℃");
		imgTv.setImasgeUrl(MSN_WEATHER_IMAGE_URL + data.getWeatherImgUrl() +  ".gif");
		return item;
	}

	/**
	 * 뷰의 재사용 체크후 custom list로 뷰 반환
	 *
	 * @param reuse
	 *            변환될 뷰
	 * @param parent
	 *            부모뷰
	 * @return 전개후 얻어진 뷰
	 */
	private ViewGroup getViewGroup(final View reuse, final ViewGroup parent) {
		/*
		 * if(reuse instanceof ViewGroup){ // 재사용이 가능하면 뷰를 재사용한다. return
		 * (ViewGroup)reuse; }
		 */
		//Context context = parent.getContext(); // 부모뷰로부터 컨택스트를 얻어온다.
		ViewGroup item = (ViewGroup) inflater.inflate(R.layout.weather_list_item, null);
		return item;
	}

}

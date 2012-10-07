package kr.co.diary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import kr.co.diary.data.UserListData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class UserListActivity  extends MyActivity implements OnItemClickListener, android.view.View.OnClickListener{
	private ListView listview;
	private ArrayList<UserListData> arrayList;
	private ArrayList<String> userIndexes = new ArrayList<String>();
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_list_layout);

		initLayout();
		
		new AsyncUserList().execute();
	}

	/**
	 * 레이아웃 초기화
	 */
	private void initLayout() {
		// 유저 리스트 뷰
		listview = (ListView)findViewById(R.id.list);
		listview.setOnItemClickListener(this);
		
		// 공유 확인 버튼
		Button btn  = (Button)findViewById(R.id.share_user_btn);
		btn.setOnClickListener(this);
	}

	@Override
	public void onItemClick(final AdapterView<?> av, final View v, final int pos, long arg3) {
		new AlertDialog.Builder(UserListActivity.this)
			.setTitle("공유요청")
			.setMessage("일정공유를 신청하시겠습니까?")
			.setPositiveButton("신청", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					UserListAdapter adapter = (UserListAdapter) av.getAdapter();
					UserListData data = (UserListData) adapter.getItem(pos);
					userIndexes.add(data.getIndex());
					Toast.makeText(UserListActivity.this, "신청되었습니다.", Toast.LENGTH_SHORT).show();
				}
			}).setNegativeButton("취소", null)
			.show();
	}
	

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.share_user_btn){
			Intent intent = new Intent();
			intent.putExtra("sharedUsers", userIndexes);
			setResult(RESULT_OK, intent);
			finish();
			
		}
	}	
	
	
	
	private class AsyncUserList extends AsyncTask<Void, Void, Boolean>{
		private ProgressDialog progress;
	
		@Override
		protected void onPostExecute(Boolean result) {
			// 로딩바를 닫아준다.
			if( progress != null && progress.isShowing() ){
				progress.dismiss();
			}
			
			// 리스트 내용이 있으면 리스트뷰에 붙여준다.
			if(arrayList != null && arrayList.size() > 0){
				UserListAdapter adapter = new UserListAdapter(arrayList);
				listview.setAdapter(adapter);
			}
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(UserListActivity.this, "로딩중", "사용자목록을 불러오는중입니다.");
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			if (!checkNetWork(true)) { // 네트워크 상태 체크
				return false;
			}

			try {
				// 서버에서 xml 을 받아와 파싱처리
				arrayList = (ArrayList<UserListData>) processUserListXML();
				return true;
			}catch (Exception e) {
				e.printStackTrace();
				return false;
			}

		}
	}
	
	/**
	 * 친구xml 을 파싱하여 list 에 담은후 handler에 list를 넣어준다.
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<UserListData> processUserListXML() throws XmlPullParserException, IOException {
	    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    XmlPullParser parser = factory.newPullParser();
	    InputStreamReader isr = null;
	    BufferedReader br = null;
		List<UserListData> list = new ArrayList<UserListData>();
	    // namespace 지원
	    factory.setNamespaceAware(true);

	    URL url = new URL(USER_LIST_URL + "&index=");
	    URLConnection conn = url.openConnection();
	    conn.setReadTimeout(2000);
	    conn.setConnectTimeout(2000);
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
		try{
		    isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
		    br = new BufferedReader(isr);
		    StringBuilder xml = new StringBuilder();
		    String line = "";
		    while((line = br.readLine()) != null){
		    	xml.append(line);
		    }
		    
		    String decodeXMl = URLDecoder.decode(xml.toString());
		    Log.i("decodeXMl", decodeXMl);
		    if(decodeXMl.contains("﻿no friend")){
		    	return null;
		    }

		    decodeXMl = decodeXMl.substring(decodeXMl.indexOf("<"), decodeXMl.lastIndexOf(">") + 1);
		    parser.setInput(new StringReader(decodeXMl));
			int eventType = -1;

			UserListData data = null;
			Log.i("dialy", "decodeXMl " + decodeXMl);
			while(eventType != XmlResourceParser.END_DOCUMENT){	// 문서의 마지막이 아닐때까지
				if(eventType == XmlResourceParser.START_TAG){	// 이벤트가 시작태그면
					String strName = parser.getName();
					if(strName.contains("friend")){				// userName 시작이면 객체생성
						data = new UserListData();
					}else if(strName.equals("index")){
						data.setIndex(parser.nextText());
					}else if(strName.equals("name")){
						data.setName(parser.nextText());
					}else if(strName.equals("id")){
						data.setId(parser.nextText());
						list.add(data);
						Log.i("dialy", "added");
					}
				}
				eventType = parser.next();	// 다음이벤트로..
			}
		}finally{
			if(isr != null){
				isr.close();
			}

			if(br != null){
				br.close();
			}
		}
		return list;
	}
	
}

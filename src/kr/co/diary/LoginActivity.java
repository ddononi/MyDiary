package kr.co.diary;

import java.io.IOException;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *	로그인 엑티비티
 */
public class LoginActivity extends MyActivity {
	private EditText idEt;
	private EditText pwdEt;
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		initLayout();
	}

	/**
	 * 레이아웃 설정
	 */
	private void initLayout() {
		idEt = (EditText)findViewById(R.id.user_id);
		pwdEt = (EditText)findViewById(R.id.user_pwd);
		Button btn = (Button)findViewById(R.id.login_btn);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				// TODO Auto-generated method stub
				//atl.cancel(false);
				// 로그인 정보를 서버에 전송
				AsyncTaskLogin atl = new AsyncTaskLogin();
				atl.execute(idEt.getText().toString(), pwdEt.getText().toString() );
			}
		});

		//	회원가입버튼
		Button joinBtn = (Button)findViewById(R.id.join_btn);
		joinBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
				startActivity(intent);
				//finish();
			}
		});

		setInfo();

	}

	/**
	 * 회원 가입후 넘어온 정보가 있으면 로그인 정보를 채워준다.
	 */
	private void setInfo() {
		// TODO Auto-generated method stub
		// 회원 가입후 넘어온 정보가 있으면 로그인 정보를 채워준다.
		Intent intent = getIntent();
		String userId = "";
		if(intent.hasExtra("user_id")){
			userId = intent.getStringExtra("user_id");
		}
		String userPwd = "";
		if(intent.hasExtra("user_pwd")){
			userPwd = intent.getStringExtra("user_pwd");
		}

		idEt.setText(userId);
		pwdEt.setText(userPwd);
	}

	private class AsyncTaskLogin extends
			AsyncTask<String, String, Boolean> {
		ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(final Boolean result) {	// 전송 완료후
			if(dialog != null && dialog.isShowing()){
				dialog.dismiss(); // 프로그레스 다이얼로그 닫기
			}

			if (result) {
				Intent intent = new Intent(LoginActivity.this, MyDiaryActivity.class);
				// 다음 엑티비티에 유저 정보를 넘겨준다.
				startActivity(intent);
				finish();

			} else {
				Toast.makeText(LoginActivity.this, "로그인 정보를 확인하세요",
						Toast.LENGTH_LONG).show();
				// 전송 실패 처리 해야됨
			}
			// 화면 고정 해제
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}

		/**
		 * @see android.os.AsyncTask#onPreExecute() 파일 전송중 로딩바 나타내기
		 */
		@Override
		protected void onPreExecute() {	// 전송전 프로그래스 다이얼로그로 전송중임을 사용자에게 알린다.
			dialog = ProgressDialog.show(LoginActivity.this, "전송중", "잠시 기다려주세요", true);
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(final String... values) {
		}

		/**
		 * @see android.os.AsyncTask#doInBackground(Params[]) 비동기 모드로 전송
		 */
		@Override
		protected Boolean doInBackground(final String... params) {	// 전송중

			// TODO Auto-generated method stub
			boolean result = false;
			if (!checkNetWork(true)) { // 네트워크 상태 체크
				return false;
			}
			// http 로 보낼 이름 값 쌍 컬랙션
			Vector<NameValuePair> vars = new
			Vector<NameValuePair>();
			try {
	            vars.add(new BasicNameValuePair("id", params[0]));							// 이름
	            vars.add(new BasicNameValuePair("pwd", params[1]));						// 비밀번호
	            HttpPost request = new HttpPost(LOGIN_URL);
				UrlEncodedFormEntity entity =  new UrlEncodedFormEntity(vars, "UTF-8");
				request.setEntity(entity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                HttpClient client = new DefaultHttpClient();
                String responseBody = client.execute(request, responseHandler);	// 전송
                responseBody = responseBody.substring(responseBody.indexOf("-") + 1);
        		 Log.i("debug",  "responseBody : " + responseBody);
        		 String[] arr =responseBody.trim().split(",");
                if (responseBody.contains("ok")  ) {	// 정상이면 내 인덱스 번호 가져오기
   				  	myIndex = Integer.valueOf(arr[1]);
   				  	result = true;
                }
            } catch (ClientProtocolException e) {

            } catch (IOException e) {

			} catch (Exception e) {

			}

			return result;
		}

	}

	@Override
	public void onBackPressed() {	//  뒤로 가기버튼 클릭시 종료 여부
	//	finishDialog(this);

	}

}

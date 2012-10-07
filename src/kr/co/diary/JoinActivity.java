package kr.co.diary;

import java.util.Vector;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 회원 가입 엑티비티
*/
public class JoinActivity extends MyActivity implements OnClickListener {
	// element
	private EditText nameEt;		// 이름
	private EditText pwdEt;			// 비밀번호
	private EditText rePwdEt;		// 비밀번호 확인
	private EditText idEt;			// 아이디

	private SharedPreferences settings;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_layout);

		initLayout();
	}

	/**
	 * 엘리먼트 설정
	 */
	private void initLayout(){
		// 엘리먼트 후킹
		nameEt = (EditText)findViewById(R.id.name);
		idEt = (EditText)findViewById(R.id.join_id);
		pwdEt = (EditText)findViewById(R.id.join_pwd);
		rePwdEt = (EditText)findViewById(R.id.join_re_pwd);

		Button regBtn = (Button)findViewById(R.id.register_btn);
		regBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(final View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case  R.id.register_btn :
			if(checkForm() != false){
				registerUser();
			}
			break;
		}
	}
	/**
	 * 폼 체크
	 */
	private boolean checkForm() {
		if(TextUtils.isEmpty(nameEt.getText())){
			Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(TextUtils.isEmpty(pwdEt.getText())){
			Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(TextUtils.isEmpty(rePwdEt.getText())){
			Toast.makeText(this, "비밀번호 확인을 입력하세요.", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(rePwdEt.getText().toString() == pwdEt.getText().toString() ){
			Toast.makeText(this, "비밀번호 입력이 같은지 확인 하세요.", Toast.LENGTH_SHORT).show();
			return false;
		}


		return true;
	}


	/**
	 * 서버에 유저를 등록한다.
	 */
	private void registerUser() {
		// TODO Auto-generated method stub
		new AsyncTaskUserInfoUpload().execute();
	}


	/**
	 *	 사용자 정보 업로드 클래스
	 */
	private class AsyncTaskUserInfoUpload extends AsyncTask<Void, String, Boolean> {
		ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(final Boolean result) {	// 전송 완료후
			// 전송이 완료되면 다이얼로그를 닫는다.
			if(dialog != null && dialog.isShowing()){
				dialog.dismiss(); // 프로그레스 다이얼로그 닫기
			}

			// 파일 전송 결과를 출력
			if (result) { // 파일 전송이 정상이면
				Toast.makeText(JoinActivity.this, "가입되었습니다~~♪",
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
				// 다음 엑티비티에 유저 정보를 넘겨준다.
				intent.putExtra("user_id", idEt.getText().toString());
				intent.putExtra("user_pwd", pwdEt.getText().toString());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();

			} else {
				Toast.makeText(JoinActivity.this, "회원 등록 실패!\n 네트워크 상태 및 서버상태를 체크하세요",
						Toast.LENGTH_LONG).show();
				// 전송 실패 처리 해야됨
			}
		}

		/**
		 * @see android.os.AsyncTask#onPreExecute() 파일 전송중 로딩바 나타내기
		 */
		@Override
		protected void onPreExecute() {	// 전송전 프로그래스 다이얼로그로 전송중임을 사용자에게 알린다.
			dialog = ProgressDialog.show(JoinActivity.this, "전송중", "사용자정보를 업로드중입니다.", true);
			 dialog.show();
		}

		/**
		 * 서버에 업로드
		 */
		@Override
		protected Boolean doInBackground(final Void... params) {	// 전송중

			boolean result = false;
			if (!checkNetWork(true)) { // 네트워크 상태 체크
				return false;
			}

			// http 로 보낼 이름 값 쌍 컬랙션
			Vector<NameValuePair> vars = new Vector<NameValuePair>();

			try {

				// HTTP post 메서드를 이용하여 데이터 업로드 처리
	            vars.add(new BasicNameValuePair("name", nameEt.getText().toString() ));
	            vars.add(new BasicNameValuePair("user_id", idEt.getText().toString() ));
	            vars.add(new BasicNameValuePair("pwd", pwdEt.getText().toString() ));

	            HttpPost request = new HttpPost(JOIN_URL);
	            // 한글깨짐을 방지하기 위해 utf-8 로 인코딩시키자
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(vars, "UTF-8");
				request.setEntity(entity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                HttpClient client = new DefaultHttpClient();
                final String responseBody = client.execute(request, responseHandler);	// 전송
                if (responseBody.trim().contains("ok")) {	// 정상 응답이면
    				  result = true;
                }
			}catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

	}


}

package kr.co.diary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadCastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		// �ܸ��� boot�� �Ϸ� �Ǿ�����
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Log.i("BOOTSVC", "Intent received");
			context.startService(new Intent(context, AlarmService.class));
			/*
			if (svcName == null) {
				Log.e("BOOTSVC", "Could not start service " + cn.toString());
			}
			*/

		}
	}
}

package com.example.evdog;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
	static final String action_boot="android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(action_boot)){
			//��������
			Intent it=new Intent(context,DogService.class);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(it);
        }
	}

}

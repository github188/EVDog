package com.example.evdog;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;

public class DogService extends Service {
	ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
	private int allopen=1;//1��ʾһֱ��Ӧ��,0��ʾ�رպ󲻴�Ӧ��	
	private int watchfeed=0;//ιʳ,0ʱ��������
	ActivityReceiver receiver;
	private int tempwatchfeed=0;//��ʱ����
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	//8.����activity�Ľ������㲥��������������
	public class ActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			Bundle bundle=intent.getExtras();
			int isallopen=bundle.getInt("isallopen");
			int feed=bundle.getInt("watchfeed");
			//ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź�isallopen="+isallopen+",feed="+feed,"dog.txt");
			setAllopen(isallopen,feed);	
		}

	}
			
	public int getAllopen() {
		return allopen;
	}
	public void setAllopen(int isallopen,int feed) {
		this.allopen = isallopen;
		this.watchfeed+=feed;
		ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź�setAllopen="+allopen+",watchfeed="+watchfeed,"dog.txt");
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź�����...","dog.txt");
		//9.ע�������
		//localBroadreceiver = LocalBroadcastManager.getInstance(this);
		receiver=new ActivityReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("android.intent.action.watchdog");
		//localBroadreceiver.registerReceiver(receiver,filter);	
		registerReceiver(receiver,filter);			
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		ToolClass.Log(ToolClass.INFO,"EV_SERVER","���Ź�ɾ��...","dog.txt");
		//���ע�������
		//localBroadreceiver.unregisterReceiver(receiver);
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź�����...","dog.txt");
		timer.scheduleWithFixedDelay(new Runnable() { 
	        @Override 
	        public void run() { 	    
	        	int restartNo=0,shutdownNo=0;
	        	int dogUart=0;
	        	if(allopen==1)
        		{
	        		if(tempwatchfeed!=watchfeed)
	    			{
	        			tempwatchfeed=watchfeed;
	    				dogUart=0;
	    				if(restartNo>0)
	    				{
		    				restartNo=0;
		        			shutdownNo=0;
		        			WriteSharedPreferences(restartNo,shutdownNo);
	    				}
	    				ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź�TaskDiff="+watchfeed+",dogUart="+dogUart,"dog.txt");
	    			}
	    			else
	    			{
	    				dogUart++;
	    				ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź�TaskSame="+watchfeed+",dogUart="+dogUart,"dog.txt");
	    			}
	        		//���ø�λ
	        		if(dogUart>2)
					{
	        			dogUart=0;
						Map<String,Integer> map=ReadSharedPreferences();
						
						if(map.containsKey("restartNo"))
						{
							restartNo=map.get("restartNo");								
						}
						if(map.containsKey("shutdownNo"))
						{
							shutdownNo=map.get("shutdownNo");							
						}
						//������3�ο��ܲ���������������
						if(restartNo<3)
						{
							restartNo++;
							WriteSharedPreferences(restartNo,shutdownNo);
							ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź���������restartNo="+restartNo+",shutdownNo="+shutdownNo,"dog.txt");
							String MY_PKG_NAME = "com.example.evconsole";
							Intent intent = new Intent();		        			 
		        			PackageManager packageManager = DogService.this.getPackageManager();
		        			intent = packageManager.getLaunchIntentForPackage(MY_PKG_NAME);
		        			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP) ;
		        			DogService.this.startActivity(intent);
						}
						//���У�����������3�ο��ܲ���ʹ��
						else if(shutdownNo<3)
						{
							shutdownNo++;
							WriteSharedPreferences(restartNo,shutdownNo);
							ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź���������restartNo="+restartNo+",shutdownNo="+shutdownNo,"dog.txt");
							try {
								Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","reboot now"});
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
						}
						//����������ʾ����
						else
						{
		        			ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź�ȷ��ϵͳ����...","dog.txt");
		        		}
						
					}
	        	}
	        	else
	        	{
	        		ToolClass.Log(ToolClass.INFO,"EV_DOG","���Ź�ֹͣ...","dog.txt");
        		}	        		        		        	
	        } 
	    },15,15,TimeUnit.SECONDS);       // timeTask 
	}
	
	//��ȡ�ļ���Ϣ
	Map<String,Integer>  ReadSharedPreferences()
	{
		Map<String,Integer> map=new HashMap<String, Integer>();
		//�ļ���˽�е�
		SharedPreferences  user = getSharedPreferences("restart_info",0);
		//��ȡ
		map.put("restartNo",user.getInt("restartNo",0));
		map.put("shutdownNo",user.getInt("shutdownNo",0));
		return map;
	}
	//д���ļ���Ϣ
	void  WriteSharedPreferences(int  restartNo,int shutdownNo)
	{
		//�ļ���˽�е�
		SharedPreferences  user = getSharedPreferences("restart_info",0);
		//��Ҫ�ӿڽ��б༭
		SharedPreferences.Editor edit=user.edit();
		//����
		edit.putInt("restartNo", restartNo);
		edit.putInt("shutdownNo" ,shutdownNo);
		//�ύ����
		edit.commit();
	}
	
	
	

}

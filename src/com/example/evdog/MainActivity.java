package com.example.evdog;

import java.io.File;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToolClass.SetDir();	//���ø�Ŀ¼
		ToolClass.Log(ToolClass.INFO,"EV_DOG","APP<<[���Ź�����...]·��:"+ToolClass.getEV_DIR()+File.separator+"logs","dog.txt");			
		//==========
		//Dog�������
		//==========
		//��������
		startService(new Intent(this,DogService.class));
		
		moveTaskToBack(true);//���ص�ǰactivityҳ��
    }
   
}

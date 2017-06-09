package com.bfy.movieplayerplus;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;

public class PlayerListActivity extends Activity implements OnItemClickListener{
	private static final int MENU_ACTION_ABOUT = 0;
	private static final int MENU_ACTION_SETTINGS = 1;
	
	public static final String MAIN_SETTINGS = "MAIN_SETTINGS";
	public static final String KEY_RECODER = "KEY_RECODER";
	
	//video columns key
	public static final String VIDEO_ID = MediaStore.Video.VideoColumns._ID;	
	public static final String VIDEO_NAME = MediaStore.Video.VideoColumns.DISPLAY_NAME;
	public static final String VIDEO_PATH = MediaStore.Video.VideoColumns.DATA;
	public static final String VIDEO_DURATION = MediaStore.Video.VideoColumns.DURATION;
	public static final String VIDEO_SIZE = MediaStore.Video.VideoColumns.SIZE;
	
	//ListView,GridView base array!
	public static final String[] VIDEO_INFO = {VIDEO_NAME,VIDEO_PATH};
	
	public static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	
	ListView lvplayer;
	
	SimpleAdapter adapter;
	
	List<Map<String,String>> data;

//	Toolbar mToolbar;
	
	
	private void init() {
		lvplayer = (ListView)findViewById(R.id.player_list);
		
		data = getData();
		
		adapter = new SimpleAdapter(this, data, R.layout.lv_movie_item, 
				new String[]{VIDEO_NAME}, new int[]{R.id.tv_title});
		lvplayer.setOnItemClickListener(this);
		lvplayer.setAdapter(adapter);
		
		
	}

	private List<Map<String, String>> getData() {
		List<Map<String,String>> videoList = new ArrayList<Map<String,String>>();
		Cursor cursor = null;
		 String selection = VIDEO_NAME + " not like ?";
		 String[] selectionArgs = {"%.dat"};
		 cursor = getContentResolver().query(VIDEO_URI, VIDEO_INFO, 
				 selection, selectionArgs, null);
		 if(cursor != null){
			 cursor.moveToFirst();
			 for(int i = 0; i < cursor.getCount();i++){
				 Map<String, String> map = new HashMap<String, String>();
				 String name = cursor.getString(cursor.getColumnIndex(VIDEO_NAME));
				 map.put(VIDEO_NAME, name);
				 String url = cursor.getString(cursor.getColumnIndex(VIDEO_PATH));
				 map.put(VIDEO_PATH, url);
				 videoList.add(map);
				 cursor.moveToNext();
			 }
		 }
		
		return videoList;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Map<String,String> item = data.get(position);
		Intent intent = new Intent(this,VideoPlayerActivity.class);
		Uri uri = null;
		uri = Uri.parse("file://"+item.get(VIDEO_PATH));
		
		
		intent.setDataAndType(uri, "video/*");
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_list);
//		mToolbar = (Toolbar) findViewById(R.id.toolbar);
//		mToolbar.setTitle("播放列表");
//		//设置导航图标要在setSupportActionBar方法之后
//		setSupportActionBar(mToolbar);
//		ActionBar actionBar = getSupportActionBar();
//		actionBar.setDisplayHomeAsUpEnabled(true);
//		actionBar.setDisplayShowHomeEnabled(true);
		init();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
			case MENU_ACTION_ABOUT:{
				AlertDialog d = new AlertDialog.Builder(this).create();
				d.setView(getLayoutInflater().inflate(R.layout.about_layout,null), 0, 0, 0, 0);
				
				return d;
			}
			case MENU_ACTION_SETTINGS:{
				AlertDialog d = new AlertDialog.Builder(this).setTitle(R.string.action_settings)
						.setPositiveButton(R.string.comfirm, null)
						.setNegativeButton(R.string.cancel, null).create();
				View root = getLayoutInflater().inflate(R.layout.settings_layout,null);
				d.setView(root, 0, 0, 0, 0);
				
				RadioGroup rg = (RadioGroup)root.findViewById(R.id.setting_rb_group);
				final SharedPreferences sp = getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);
				int recoder = sp.getInt(KEY_RECODER, 0);
				rg.check(recoder == 0 ? R.id.recoder_default : R.id.recoder_vlc);
				rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						
						if(checkedId == R.id.recoder_default){
							sp.edit().putInt(KEY_RECODER, 0).commit();
						}else{
							sp.edit().putInt(KEY_RECODER, 1).commit();
						}
						
					}
				});
				
				return d;
			}
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.width = window.getWindowManager().getDefaultDisplay().getWidth();
		window.setAttributes(lp);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_about:
				showDialog(MENU_ACTION_ABOUT);
				break;
			case R.id.action_settings:
				showDialog(MENU_ACTION_SETTINGS);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	
	
	
}

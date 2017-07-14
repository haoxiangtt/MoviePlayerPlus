package com.bfy.movieplayerplus.activity;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bfy.movieplayerplus.R;
import com.bfy.movieplayerplus.event.ContextReceiver;
import com.bfy.movieplayerplus.event.EventJsonObject;
import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.event.base.EventCallback;
import com.bfy.movieplayerplus.event.base.EventHandler;
import com.bfy.movieplayerplus.event.base.Schedulers;
import com.bfy.movieplayerplus.model.base.BaseModel;
import com.bfy.movieplayerplus.utils.Constant;
import com.bfy.movieplayerplus.utils.PackageUtil;
import com.bfy.movieplayerplus.utils.PermissionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class PlayerListActivity extends AppCompatActivity implements OnItemClickListener
		, View.OnClickListener{
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
	public static final String VIDEO_SOURCE = "_source";
	
	//ListView,GridView base array!
	public static final String[] VIDEO_INFO = {VIDEO_NAME,VIDEO_PATH};
	
	public static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	
	ListView lvplayer;
	
	SimpleAdapter adapter;
	
	List<Map<String,String>> data;

	EditText etSearch;

	Button btSearch;

	ProgressDialog pDialog;

	Toolbar mToolbar;
	
	
	private void init() {
		lvplayer = (ListView)findViewById(R.id.player_list);
		etSearch = (EditText)findViewById(R.id.et_search);
		btSearch = (Button)findViewById(R.id.bt_search);
		btSearch.setOnClickListener(this);

		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(true);
		pDialog.setCanceledOnTouchOutside(false);
		pDialog.setMessage("正在搜索");

		lvplayer.setOnItemClickListener(this);
		if (!PermissionUtils.isNeedRequestPermission() ||
				!PermissionUtils.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			data = getData();
			adapter = new SimpleAdapter(this, data, R.layout.lv_movie_item,
					new String[]{VIDEO_NAME}, new int[]{R.id.tv_title});
			lvplayer.setAdapter(adapter);
		}


		
	}

	private List<Map<String, String>> getData() {
		List<Map<String,String>> videoList = new ArrayList<Map<String,String>>();
		if (PermissionUtils.isNeedRequestPermission() &&
				PermissionUtils.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			requestPermission();
			return videoList;
		}
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
				 map.put(VIDEO_SOURCE, "local");
				 videoList.add(map);
				 cursor.moveToNext();
			 }
		 }
		
		return videoList;
	}

	private void getMV(String key) {
		if (PermissionUtils.isNeedRequestPermission() &&
				PermissionUtils.checkSelfPermission(this, Manifest.permission.INTERNET)) {
			requestPermission();
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putString("keyword", key);
		bundle.putString("page", "1");
		bundle.putString("pagesize", "30");
		bundle.putString("userid", "-1");
		bundle.putString("clientver", "");
		bundle.putString("platform", "WebFilter");
		bundle.putString("tag", "em");
		bundle.putString("filter", "2");
		bundle.putString("iscorrection", "1");
		bundle.putString("privilege_filter", "0");
		EventBuilder.Event<Bundle, EventJsonObject> event = new EventBuilder<Bundle, EventJsonObject>()
			.type(Constant.EVENT_TYPE_MODEL)
			.key(Constant.MAIN_MODEL)
			.requestId(0)
			.startTime(System.currentTimeMillis())
			.target(EventHandler.getInstance())
			.requestData(bundle)
			.callback(new EventCallback<Bundle, EventJsonObject>() {
				@Override
				public  void call(EventBuilder.Event<Bundle, EventJsonObject> event) {
					if (pDialog.isShowing()) {
						pDialog.dismiss();
					}
					parseData(event);
				}
			}).subscribeOn(Schedulers.cache())
			.observeOn(Schedulers.ui())
			.build();

		if (!pDialog.isShowing()) {
			pDialog.show();
		}

		event.send();
	}

	private void parseData(EventBuilder.Event<Bundle, EventJsonObject> event) {
		EventJsonObject result = event.responseData;
		if (Constant.ResponseCode.CODE_SUCCESSFULLY.equals(result.optString(BaseModel.KEY_RESULT_CODE))) {
            JSONObject json = result.optJSONObject("json");
            JSONObject data = json.optJSONObject("data");
            if (data != null) {
                JSONArray lists = data.optJSONArray("lists");
                if (lists != null) {
                    List<Map<String, String>> finalList = new ArrayList<>();
                    for (int i = 0; i < lists.length(); i++) {
                        JSONObject obj = lists.optJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        String fullName = obj.optString("MvName").replace("<em>", "").replace("</em>", "")
                                + " - " + obj.optString("Remark");
                        map.put(VIDEO_NAME, fullName);
                        map.put(VIDEO_PATH, obj.optString("MvHash"));
                        map.put(VIDEO_SOURCE, "kugou");
                        finalList.add(map);
                    }
                    PlayerListActivity.this.data = finalList;
                    updateView();
                }
            }
        } else {
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = result.optString(BaseModel.KEY_DESC);
            mHandler.sendMessage(msg);
        }
	}

	private void updateView() {
		adapter = new SimpleAdapter(this, data, R.layout.lv_movie_item,
				new String[]{VIDEO_NAME}, new int[]{R.id.tv_title});
		lvplayer.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Map<String,String> item = data.get(position);
		if ("kugou".equals(item.get(VIDEO_SOURCE))){
			getMVRealUrl(item);
		} else {
			Intent intent = new Intent(this, VideoPlayerActivity.class);
			Uri uri = Uri.parse("file://" + item.get(VIDEO_PATH));
			intent.setDataAndType(uri, "video/*");
			Bundle bundle = new Bundle();
			bundle.putParcelable(ContextReceiver.KEY_INTENT, intent);
			new EventBuilder<Bundle, EventJsonObject>()
				.type(Constant.EVENT_TYPE_CONTEXT)
				.requestId(ContextReceiver.REQUEST_GO_ACTIVITY)
				.target(EventHandler.getInstance())
				.reference(new WeakReference<Context>(PlayerListActivity.this))
				.requestData(bundle)
				.subscribeOn(Schedulers.ui())
				.build().send();
//			startActivity(intent);
		}
	}

	private void getMVRealUrl(Map<String, String> item) {
		if (PermissionUtils.isNeedRequestPermission() &&
				PermissionUtils.checkSelfPermission(this, Manifest.permission.INTERNET)) {
			requestPermission();
			return;
		}
		if (!pDialog.isShowing()) {
			pDialog.show();
		}
		Bundle bundle = new Bundle();
		bundle.putString("url", item.get(VIDEO_PATH));
		new EventBuilder<Bundle, EventJsonObject>()
			.type(Constant.EVENT_TYPE_MODEL)
			.key(Constant.MAIN_MODEL)
			.requestId(1)
			.startTime(System.currentTimeMillis())
			.target(EventHandler.getInstance())
			.requestData(bundle)
			.callback(new EventCallback<Bundle, EventJsonObject>() {
				@Override
				public void call(EventBuilder.Event<Bundle, EventJsonObject> event) {
					if (pDialog.isShowing()) {
						pDialog.dismiss();
					}
					EventJsonObject result = event.responseData;
					if (Constant.ResponseCode.CODE_SUCCESSFULLY.equals(
							result.optString(BaseModel.KEY_RESULT_CODE))) {
						EventJsonObject json = (EventJsonObject) result.optJSONObject("json");
						JSONObject mvdata = json.optJSONObject("mvdata");
						if (mvdata != null) {
							JSONObject sd = mvdata.optJSONObject("sd");
							if (sd != null) {
								String realUrl = sd.optString("downurl");
								goPlayerActivity(realUrl);
							}
						}
					}


				}
			}).subscribeOn(Schedulers.cache())
			.observeOn(Schedulers.ui())
			.build().send();

	}

	private void goPlayerActivity(String realUrl) {
		if (!TextUtils.isEmpty(realUrl)) {
			Bundle bundle = new Bundle();
			Intent intent = new Intent(PlayerListActivity.this, VideoPlayerActivity.class);
			Uri uri = Uri.parse(realUrl);
			intent.setDataAndType(uri, "video/*");
			bundle.putParcelable(ContextReceiver.KEY_INTENT, intent);
            new EventBuilder<Bundle, EventJsonObject>()
				.type(Constant.EVENT_TYPE_CONTEXT)
				.requestId(ContextReceiver.REQUEST_GO_ACTIVITY)
				.target(EventHandler.getInstance())
				.reference(new WeakReference<Context>(PlayerListActivity.this))
				.requestData(bundle)
				.subscribeOn(Schedulers.ui())
				.build().send();
        }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_list);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle("播放列表");
		//设置导航图标要在setSupportActionBar方法之后
		setSupportActionBar(mToolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		requestPermission();
		init();
	}

	private void requestPermission() {
		if (PermissionUtils.isNeedRequestPermission()) {
			List<String> list = new ArrayList<>();
			if (PermissionUtils.checkSelfPermission(
				this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
				list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			}
			if (PermissionUtils.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			}
			if (PermissionUtils.checkSelfPermission(this, Manifest.permission.VIBRATE)) {
				list.add(Manifest.permission.VIBRATE);
			}
			if (PermissionUtils.checkSelfPermission(this, Manifest.permission.INTERNET)) {
				list.add(Manifest.permission.INTERNET);
			}
			if (PermissionUtils.checkSelfPermission(this, Manifest.permission.WAKE_LOCK)) {
				list.add(Manifest.permission.WAKE_LOCK);
			}

			if (list.size() > 0) {
				String[] requests = list.toArray(new String[list.size()]);
				PermissionUtils.requestPermission(this, requests, 1001);
			}

		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
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
				View view = getLayoutInflater().inflate(R.layout.about_layout,null);
				TextView tvVersion = (TextView) view.findViewById(R.id.tv_version);
				tvVersion.setText("当前版本：" + PackageUtil.getInstance(this).getVersionName());
				d.setView(view, 0, 0, 0, 0);
				return d;
			}
			case MENU_ACTION_SETTINGS:{
				AlertDialog d = new AlertDialog.Builder(this)
						.setTitle(R.string.action_settings)
						.setPositiveButton(R.string.comfirm, null)
						.setNegativeButton(R.string.cancel, null).create();
				View root = getLayoutInflater().inflate(R.layout.settings_layout,null);
				d.setView(root);
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
			case android.R.id.home:
				onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.bt_search) {
			String key = etSearch.getText().toString();
			if (TextUtils.isEmpty(key)) {
//				Toast.makeText(this, "搜索内容不能为空!", Toast.LENGTH_SHORT).show();
				data = getData();
				updateView();
				return;
			}
			getMV(key);
		}
	}

	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0 : {
					Toast.makeText(PlayerListActivity.this,
							msg.obj.toString(), Toast.LENGTH_SHORT).show();
				}
			}
		}
	};
}

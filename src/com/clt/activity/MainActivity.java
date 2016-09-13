package com.clt.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.clt.ledservers.R;
import com.clt.service.MainService;
import com.clt.util.NetUtil;
import com.clt.util.SharedPreferenceUtil;
import com.clt.util.SharedPreferenceUtil.ShareKey;

public class MainActivity extends BaseActivity
{
	private static final boolean DEBUG = false;
	
	private String mTerminateName, mTerminatePassword;
	
	protected EditText etLedName, etPassword;
	
	private TextView tvWebsite;
	
	private SharedPreferenceUtil sharedPreferenceUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			init();
			initView();
			initListener();
		}
		catch (Exception e)
		{
		}
		
	}
	
	private void init()
	{
		sharedPreferenceUtil = SharedPreferenceUtil.getInstance(this, null);
		mTerminateName = sharedPreferenceUtil.getString(ShareKey.TerminateName,
				getResString(R.string.screen_name_default_val));
		mTerminatePassword = sharedPreferenceUtil.getString(
				ShareKey.TerminatePassword,
				getResString(R.string.screen_password_default_val));
		
		Intent intent = new Intent(this, MainService.class);
		startService(intent);
		
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	
	
	@Override
	protected void onPause()
	{
		//
		super.onPause();
	}
	
	public void initView()
	{
		etLedName = (EditText) findViewById(R.id.etLedName);
		etLedName.setText(mTerminateName);
		
		etPassword = (EditText) findViewById(R.id.et_password);
		etPassword.setText(mTerminatePassword);
		
		tvWebsite = (TextView) findViewById(R.id.tv_website);
		// if(NetUtil.isWifiConnect(this)){
		tvWebsite.setText("http://" + NetUtil.getIpAddress(this) + ":" + "8080"
				+ "/index");
		// }
		/**
		 * text
		 */
		TextView tvVersionCode = (TextView) findViewById(R.id.tv_version_code);
		tvVersionCode.setText(getVerCode(this) + "");
	}
	
	public void initListener()
	{
		etLedName.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count)
			{
				mTerminateName = etLedName.getText().toString();
				sharedPreferenceUtil.putString(ShareKey.TerminateName,
						mTerminateName);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after)
			{
				//
				
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				//
				
			}
		});
		
		etPassword.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count)
			{
				mTerminatePassword = etPassword.getText().toString();
				sharedPreferenceUtil.putString(ShareKey.TerminatePassword,
						mTerminatePassword);
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after)
			{
				//
				
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				//
				
			}
		});
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	/**
	 * 获得版本号
	 * 
	 * @param context
	 * @return
	 */
	public int getVerCode(Context context)
	{
		int verCode = -1;
		try
		{
			verCode = context.getPackageManager().getPackageInfo(
					"com.clt.ledserver", 0).versionCode;
		}
		catch (NameNotFoundException e)
		{
			Log.e("版本号获取异常", e.getMessage());
			e.printStackTrace();
		}
		
		return verCode;
	}
	
	public static class PlayingVsn {
        public String source;
        public String name;
    
        public PlayingVsn(String folder, String vsnName) {
            this.source = folder;
            this.name = vsnName;
        }
    }
}

package com.fmclient;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.fmclient.CMDExecute;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final int FILE_SELECT_CODE = 1;
	private static final double FREQTHRESHOLD = 0.01;
	private static final double FREQMAX = 7500;
	private static final double FREQMIN = 2500;
	private static final int STEPNUM = 19;
	private static final int CODEX = 16;
	private static final int CODEY = 17;
	private static final int CODEZ = 18;
	
	private EditText mTextTest;
	private EditText mTextOut;
	private EditText mimsi;
	private int mAudioMinBufSize;
	private short[] mAudioBuffer;
	private ArrayList<Double> mFreqBuffer;
	private int mSessionID;
	public static Context context ;
	public static TelephonyManager tm;
	String info = new String();
	private NativeMP3Decoder MP3Decoder;
    
	Handler handler;
	static {
		System.loadLibrary("mad");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextTest=(EditText)findViewById(R.id.texttest);
		mTextOut=(EditText)findViewById(R.id.textout);
		mimsi = (EditText)findViewById(R.id.e_imsi);
		context = MainActivity.this;
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		MP3Decoder = new NativeMP3Decoder();
		mFreqBuffer = new ArrayList<Double>();
	}

	public String fetch_imsi()
	{
		String result=new String();
		result = tm.getSubscriberId() +"\n";
	    return result;
	}
	
	public void BtnChofileOnClick(View view) {	
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("audio/*");
		startActivityForResult(Intent.createChooser(intent, "Select FM record file"), FILE_SELECT_CODE);
		mimsi.setText(fetch_imsi());
	}
	
	public void BtnTestOnClick(View view) {
		
		new Thread()
		{
			public void run()
			{
				Log.d("33333333","it is in the run");	
				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,5000);
				httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);

					HttpPost post = new HttpPost("http://121.248.49.96:80/phone_auth/cgi/post_data.php");//server url  121.248.49.96/phone_auth/cgi/post_data.php
					Log.d("11111111","after the post");
				
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					String ssid=mTextOut.getText().toString();//+info;
					Log.d("4444444444",ssid);
					params.add(new BasicNameValuePair("ssid","123"));
					params.add(new BasicNameValuePair("DeviceID(IMEI)", tm.getDeviceId()));
					params.add(new BasicNameValuePair("DevicesoftwareVersion",tm.getDeviceSoftwareVersion()));
					//params.add(new BasicNameValuePair("LineNumber",tm.getLine1Number()));
					params.add(new BasicNameValuePair("NetworkCountryIso",tm.getNetworkCountryIso()));
					params.add(new BasicNameValuePair("NetworkOperator",tm.getNetworkOperator()));
					params.add(new BasicNameValuePair("NetworkOperatorName",tm.getNetworkOperatorName()));
					params.add(new BasicNameValuePair("NetworkType",tm.getNetworkType()+"\n"));
					params.add(new BasicNameValuePair("PhoneType",tm.getPhoneType()+"\n"));	
					params.add(new BasicNameValuePair("SimCountryIso",tm.getSimCountryIso()));	
					params.add(new BasicNameValuePair("SimOperator ",tm.getSimOperator()));
				//	params.add(new BasicNameValuePair("SimOperatorName",tm.getSimOperatorName()));
					params.add(new BasicNameValuePair("SimSerialNumber",tm.getSimSerialNumber()));
					params.add(new BasicNameValuePair("SimState",tm.getSimState()+"\n"));
					params.add(new BasicNameValuePair("SubscriberId(IMSI)",tm.getSubscriberId()));
				//	params.add(new BasicNameValuePair("VoiceMailNumber",tm.getVoiceMailNumber()));
				
							
					Log.d("777777777","after the handlemessage");
				try{
					post.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
					Log.d("888888888","before the execute");
					HttpResponse response = httpClient.execute(post);// execute() has a return
					Log.d("999999999","after the execute");
					
				    if(response.getStatusLine().getStatusCode() == 200)  
				    	Log.d("666666666",  EntityUtils.toString(response.getEntity()));  
			    }
				catch(IOException e)
				{
					Log.d("555555555","it is in the catch");
					e.printStackTrace();
				}				
			}
		}.start();
		Log.d("22222222","after the start");		
	}

	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		String RecordfilePath = null;
		
		switch (requestCode) {
	    case FILE_SELECT_CODE:      
	        if (resultCode == RESULT_OK) {  
	            Uri uri = data.getData();
	            if ("content".equalsIgnoreCase(uri.getScheme())) {
	                String[] projection = { "_data" };
	                Cursor cursor = null;
	     
	                try {
	                    cursor = this.getContentResolver().query(uri, projection,null, null, null);
	                    int column_index = cursor.getColumnIndexOrThrow("_data");
	                    if (cursor.moveToFirst()) {
	                        RecordfilePath=cursor.getString(column_index);
	                    }
	                } catch (Exception e) {
	                	RecordfilePath="select file failed.";
	                }
	            }    
	            else if ("file".equalsIgnoreCase(uri.getScheme())) {
	            	RecordfilePath=uri.getPath();
	            }
	            else {
	            	RecordfilePath="select file failed.";
	            }
	            
	            mTextTest.setText(RecordfilePath);
	            try {
					DecodeFile(RecordfilePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	        }           
	        break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private int DecodeFile(String path) throws IOException {
		int i;
		int ret = MP3Decoder.initAudioPlayer(path, 0);
		if (ret == -1) {
			Log.i("FMClient", "Couldn't open file '" + path + "'");
			return -1;
		}
		mFreqBuffer.clear();
		int samplerate = MP3Decoder.getAudioSamplerate();
		//samplerate = samplerate / 2;
		//mAudioMinBufSize = AudioTrack.getMinBufferSize(samplerate/2,
		//		AudioFormat.CHANNEL_OUT_STEREO,
		//		AudioFormat.ENCODING_PCM_16BIT);
		mAudioMinBufSize = samplerate/8;
		mAudioBuffer = new short[mAudioMinBufSize];
		
		//FileWriter fp;
		//fp=new FileWriter("/sdcard/test.txt");

		
		while (0 < MP3Decoder.getAudioBuf(mAudioBuffer, mAudioMinBufSize)) {
			/*for (i=0; i<mAudioMinBufSize; i++) {
				fp.write(Short.toString(mAudioBuffer[i])+" ");
			}
			fp.flush();*/			
			FFT calculator=new FFT(mAudioBuffer, mAudioMinBufSize);
			double[] freqBuffer=calculator.transform();
			freqBuffer[0]=0;
			double sum = 0;
			int maxp=0;
			for (i=(int) (FREQMIN*freqBuffer.length/samplerate); i<freqBuffer.length/2; i++) {
				sum+=freqBuffer[i];
				if (freqBuffer[i]>freqBuffer[maxp]) maxp=i;
			}
			if (sum>0 && freqBuffer[maxp]>sum*FREQTHRESHOLD) 
				mFreqBuffer.add((double) (samplerate*maxp/freqBuffer.length));
			else
				mFreqBuffer.add((double) 0);
		}
		//fp.close();
		
		//数值标准化,去除无意义区段
		ArrayList<Integer> decodebuf=new ArrayList<Integer>();
		double loc,freq;
		for (i=0;i<mFreqBuffer.size();i++) {
			freq=mFreqBuffer.get(i);
			if (freq<FREQMIN || freq>FREQMAX) continue;
			loc=(STEPNUM-1)*(freq-FREQMIN)/(FREQMAX-FREQMIN);
			decodebuf.add((int) (Math.floor(loc)+Math.floor(loc*2)%2));
		}
		
		//合并重复采样
		int previous=-1,pos=0;
		while (pos<decodebuf.size()) {
			if (previous != decodebuf.get(pos)) {
				previous=decodebuf.get(pos);
				pos++;
			}
			else
				decodebuf.remove(pos);
		}
		
		//截去首尾多余数据
		pos=-1;
		while (true) {
			pos=decodebuf.indexOf(CODEX);
			if (pos<0) {
				mTextOut.setText("无效的数据源文件。");
				return -1;
			}
			if (decodebuf.get(pos+1)==CODEY && decodebuf.get(pos+2)==CODEX) {
				decodebuf.subList(0, pos+3).clear();
				break;
			}
			else 
				decodebuf.subList(0, pos+1).clear();
		}
		pos=-1;
		while (true) {
			pos=decodebuf.indexOf(CODEY);
			if (pos<0) {
				mTextOut.setText("不完整的数据源文件。");
				return -1;
			}
			if (decodebuf.get(pos+1)==CODEX && decodebuf.get(pos+2)==CODEY) {
				decodebuf.subList(pos, decodebuf.size()).clear();
				break;
			}
			else 
				decodebuf.remove(pos);
		}
		
		//移除分割符
		while (decodebuf.remove((Integer)CODEZ));
		
		//校验和
		int checksum=decodebuf.remove(decodebuf.size()-1);
		int sum=0;
		for (i=0;i<decodebuf.size();i++) 
			sum+=decodebuf.get(i);
		if (sum%(STEPNUM-3) != checksum) {
			mTextOut.setText("校验不匹配，数据无效。");
			return -2;
		}
		
		mSessionID=0;
		for (i=0; i<decodebuf.size(); i++)
			mSessionID=mSessionID*10+decodebuf.get(i);
		
		mTextOut.setText(Integer.toString(mSessionID));
		return 0;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		MP3Decoder.closeAduioFile();
		super.onDestroy();
	}
	
	
	
	
}

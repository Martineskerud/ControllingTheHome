package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.ServerEventListener;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.networking.ServerConnection;


public class FullscreenActivity extends Activity implements ServerEventListener {

	public static final String ON_STATE_RECEIVED = "no.hiof.skaalsveen.eskerud.olsen.prototype2.STATE_RECEIVE";
	protected static final String TAG = "FullscreenActivity";
	private CustomDrawableView surface;
	private boolean awaitingCallback = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		surface = new CustomDrawableView(this);
		surface.setServerEventListener(this);
        setContentView(surface);
        
        
       
	}

	@Override
	public void sendMessage(String message) {
		
		if(!awaitingCallback){
			awaitingCallback = true;
			Intent intent = new Intent(this, ServerConnection.class);
			intent.putExtra(ServerConnection.MESSAGE, message);
			startService(intent);
		}
	}
	
	@Override
	public void onResume() {

		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction(ON_STATE_RECEIVED);

		registerReceiver(broadcastReceiver, ifilter);
		super.onResume();
	}
	
	
	@Override
	public void onPause() {
		unregisterReceiver(broadcastReceiver);
		super.onPause();
	}
	
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String msg = intent.getStringExtra(ServerConnection.MESSAGE);
			if(msg.equals("done!")){
				awaitingCallback = false;
				Log.d(TAG, "got callback");
			}
			else{
				Log.d(TAG, "fail:"+msg);
			}
		}
	};
	
}

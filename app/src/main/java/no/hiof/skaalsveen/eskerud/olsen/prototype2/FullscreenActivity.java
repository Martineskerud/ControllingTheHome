package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.ActivityEventListener;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.ServerEventListener;


public class FullscreenActivity extends Activity implements ServerEventListener, ActivityEventListener {

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
        surface.setActivityEventListener(this);
        setContentView(surface);

	}

	@Override
	public void sendMessage(String message) {
		
		if(!awaitingCallback){
			awaitingCallback = true;

            Log.d(TAG, "PYTHON!!!!");
			pushMessage(message, ServerConnection.PYTHON);

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

    public void showDialog(){

        try {
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setTitle("Select connection type")
                    .setItems(R.array.connection_types, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                        }
                    });

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();

            dialog.show();
        }catch (Exception e){
            Log.e(TAG, "Shits went down!!!");
        }

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

    @Override
    public boolean onActivityEvent(ActivityEvent event) {

        //Log.d(TAG, "onActivityEvent");

        if(event.getType() == ActivityEvent.CONNECTION_ADDED){
            Log.d(TAG, "Event. Showing dialog");
            showDialog();

        } else if(event.getType() == ActivityEvent.PERFORM_HAPTIC_FEEDBACK){
            //Log.d(TAG, "BZZZ");
            ActivityEvent<Integer> e = event;
            surface.performHapticFeedback(e.getValue());


        } else if(event.getType() == ActivityEvent.REPORT_INTERACTION){

            ActivityEvent<String> e = event;
            pushMessage(e.getValue(), ServerConnection.HTTP);
            //Log.d(TAG, "HTTP!!!!");

        }

        return true;
    }

    private void pushMessage(String message, int con) {

        Intent intent = new Intent(this, ServerConnection.class);
        intent.putExtra(ServerConnection.MESSAGE, message);
        intent.putExtra(ServerConnection.CONNECTION_TYPE, con);
        startService(intent);

    }
}

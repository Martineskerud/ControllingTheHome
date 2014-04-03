package no.hiof.skaalsveen.eskerud.olsen.prototype2.networking;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.FullscreenActivity;

public class ServerConnection extends IntentService{

	public ServerConnection() {
		super("ServerConnection");

	}

	private static final String TAG = "ServerConnection";
	public static final String MESSAGE = "message";

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Log.d(TAG, "Initializing conneciton");

		Socket socket = null;
		try {
			socket = new Socket("192.168.0.192", 9998);

			DataOutputStream dataOutputStream = new DataOutputStream(
					socket.getOutputStream());

			dataOutputStream.writeUTF(intent.getStringExtra(MESSAGE));
			

			InputStream inputStream = socket.getInputStream();


			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
			    total.append(line);
			}
			
			
			final String msg = total.toString();
			Log.d(TAG, "received "+ msg);
			
			Intent callback = new Intent(FullscreenActivity.ON_STATE_RECEIVED);
			callback.putExtra(MESSAGE, msg);
			sendBroadcast(callback);
			
			Log.d(TAG, "message sent. socket closed...");

		} catch (Exception e) {
			Log.d(TAG, "Failed to send message");
			e.printStackTrace();
		} finally {
			
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}

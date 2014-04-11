package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class ServerConnection extends IntentService{

    public static final String CONNECTION_TYPE = "CONNECTION_TYPE";
    public static final int PYTHON = 1;
    public static final int HTTP = 2;
    public static final String IP = "192.168.43.141";
    public static final String PATH = "/proto3/register.php";

    public ServerConnection() {
		super("ServerConnection");

	}

	private static final String TAG = "ServerConnection";
	public static final String MESSAGE = "message";

	@Override
	protected void onHandleIntent(Intent intent) {

        //Log.d(TAG, "ServerConnection is workin!");

        if(intent.getIntExtra(CONNECTION_TYPE, 0) == PYTHON){

            connectToPy(intent);
        }
        else{

            try {
                method2(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

	}

    private void connectToHttp(Intent intent) throws IOException, URISyntaxException {

        //Log.d(TAG, "yoyoyohhhhhh");

        //instantiates clent to make request

        DefaultHttpClient clent = new DefaultHttpClient();

        //url with the post data
        HttpPost post = new HttpPost("http://"+IP + PATH);

        //passes the results to a string builder/entity
        String stringExtra = intent.getStringExtra(ServerConnection.MESSAGE);
//        Log.d(TAG, "connecting to server..."+IP+PATH+":\n"+stringExtra);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("data", stringExtra));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = clent.execute(post);

        BufferedReader in = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent()));

        String line = null;
        while((line = in.readLine()) != null){
            Log.d(TAG, line);
        }

    }

    private void method2(Intent intent) throws IOException {

        URL url = new URL("http://"+IP + PATH);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");

        OutputStreamWriter request = new OutputStreamWriter(connection.getOutputStream());

        String str = intent.getStringExtra(ServerConnection.MESSAGE);
        String parameters = "data="+ URLEncoder.encode(str, "UTF-8");

        request.write(parameters);
        request.flush();
        request.close();
        String line = "";
        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null)
        {
            sb.append(line + "\n");
        }
        // Response from server after login process will be stored in response variable.
        String response = sb.toString();
        // You can perform UI operations here
        //Log.d(TAG, "Message from Server: \n" + response);
        isr.close();
        reader.close();

    }


    private void connectToPy(Intent intent) {
        //Log.d(TAG, "Initializing conneciton");

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
            //Log.d(TAG, "received "+ msg);

            Intent callback = new Intent(FullscreenActivity.ON_STATE_RECEIVED);
            callback.putExtra(MESSAGE, msg);
            sendBroadcast(callback);

            //Log.d(TAG, "message sent. socket closed...");

        } catch (Exception e) {
            //Log.d(TAG, "Failed to send message");
            e.printStackTrace();
        } finally {

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }
    }

}

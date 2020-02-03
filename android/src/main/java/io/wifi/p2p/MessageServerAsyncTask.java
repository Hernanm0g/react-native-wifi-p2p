package io.wifi.p2p;

import android.os.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;

import static io.wifi.p2p.Utils.CHARSET;

/**
 * Created by kiryl on 18.7.18.
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class MessageServerAsyncTask extends AsyncTask<Void, Void, String> {
    private Callback callback;

    /**
     * @param callback
     */
    public MessageServerAsyncTask(Callback callback) {
        this.callback = callback;
    }

    protected String convertStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(Math.max(16, is.available()));
        char[] tmp = new char[4096];

        try {
            InputStreamReader reader = new InputStreamReader(is, CHARSET);
            for(int cnt; (cnt = reader.read(tmp)) > 0;)
                sb.append( tmp, 0, cnt );
        } finally {
            is.close();
        }
        return sb.toString();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            System.out.println("Server: Socket opened");
            Socket client = serverSocket.accept();
            System.out.println("Server: connection done");
            String clientIp = client.getInetAddress().getHostAddress();
            InputStream inputstream = client.getInputStream();
            String message = convertStreamToString(inputstream);
            WritableMap result = Arguments.createMap();
            result.putString("message", message);
            result.putString("clientIp", clientIp);
            callback.invoke(result);
            serverSocket.close();
            return message;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        System.out.println("Opening a server socket");
    }
}

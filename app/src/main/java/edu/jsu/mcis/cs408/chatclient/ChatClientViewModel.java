package edu.jsu.mcis.cs408.chatclient;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ChatClientViewModel extends ViewModel {
    private  static final String TAG = "ChatClientViewModel";

    private static final String URL = "http://ec2-3-143-211-101.us-east-2.compute.amazonaws.com/CS408_SimpleChat/Chat";

    private MutableLiveData<JSONObject> jsonData;

    private final ExecutorService requestThreadExecutor;
    private final Runnable httpGetRequestThread, httpPostRequestThread, httpClearRequestThread;
    private Future<?> pending;
    private final String NAME = "Altair";
    private String message;

    public ChatClientViewModel() {
        requestThreadExecutor = Executors.newSingleThreadExecutor();

        httpGetRequestThread = new Runnable() {
            @Override
            public void run() {

                if (pending != null) { pending.cancel(true);}

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("GET", URL));
                }
                catch(Exception e) {
                    Log.e(TAG, "Exception: ", e);}
            }
        };

        httpPostRequestThread = new Runnable() {
            @Override
            public void run() {
                if (pending != null){pending.cancel(true);}

                try{
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("POST", URL));
                }
                catch(Exception e){
                    Log.e(TAG, "Exception: ", e); }
            }
        };

        httpClearRequestThread = new Runnable() {
            @Override
            public void run() {
                if (pending != null){pending.cancel(true);}

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("DELETE", URL));
                }
                catch (Exception e){
                    Log.e(TAG, "Exception: ", e); }
            }
        };
    }

    public String jsonParse(JSONObject jsonObject) throws JSONException {
        String mess = (String) jsonObject.get("messages");
        return mess;
    }

    public void sendGetRequest(){httpGetRequestThread.run();}

    public void sendPostRequest() {httpPostRequestThread.run();}

    public void sendClearRequest() {httpClearRequestThread.run();}

    private void setJsonData(JSONObject json) {
        this.getJsonData().postValue(json);
    }

    public MutableLiveData<JSONObject> getJsonData(){
        if (jsonData == null){
            jsonData = new MutableLiveData<>();
        }
        return jsonData;
    }
    public void setMessage(String message){
        this.message = message;
    }

    private class HTTPRequestTask implements Runnable{

        private static final String TAG = "HTTPRequestTask";
        private final String method, urlString;

        HTTPRequestTask(String method, String urlString){
            this.method = method;
            this.urlString = urlString;
        }

        @Override
        public void run() {
            JSONObject results = doRequest(urlString);
            setJsonData(results);
        }

        private JSONObject doRequest(String urlString){

            StringBuilder r = new StringBuilder();
            String line;

            HttpURLConnection conn = null;
            JSONObject results = null;

            try{

                if (Thread.interrupted())
                    throw new InterruptedException();
                URL url = new URL(urlString);
                conn = (HttpURLConnection)url.openConnection();

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod(method);
                conn.setDoInput(true);

                if(method.equals("POST")){
                    conn.setDoOutput(true);

                    JSONObject j = new JSONObject();
                    j.put("name", NAME);
                    j.put("message", message);

                    OutputStream out = conn.getOutputStream();
                    out.write(j.toString().getBytes());
                    out.flush();
                    out.close();
                }

                conn.connect();

                if(Thread.interrupted())
                    throw new InterruptedException();

                int code = conn.getResponseCode();

                if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    do {
                        line = reader.readLine();
                        if (line != null)
                            r.append(line);
                    }while (line != null);

                }

                if (Thread.interrupted())
                    throw new InterruptedException();

                results = new JSONObject(r.toString());


            } catch (Exception e) {
                Log.e(TAG, "Exception: ", e);
            }
            finally {
                if (conn != null){
                    conn.disconnect();
                }
            }

            Log.d(TAG, "JSON: " + r.toString());

            return results;
        }
    }
}


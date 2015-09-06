package ua.pp.lab101.synthesizercontrol.service.network;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkNotifier {
    private static final String LOG_TAG = "Network notifier";
    private String mURI;
    private Integer mResultCode;

    /**
     * Constructor must unsure that all needed fields are filled
     * @param URL Server url with port
     * @param email owners email adress
     * @param token points access token
     * @param recourseId points key
     */
    public NetworkNotifier(String URL, String email, String token, String recourseId) {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("http://");
        uriBuilder.append(URL);
        uriBuilder.append("/service/v2/value?email=");
        uriBuilder.append(email);
        uriBuilder.append("&token=");
        uriBuilder.append(token);
        uriBuilder.append("&id=");
        uriBuilder.append(email);
        uriBuilder.append("/");
        uriBuilder.append(recourseId);
        mURI = uriBuilder.toString();


    }

    /**
     * Method sends the value and short note to the Nimbits server
     * @param frequencyValue value in MHz
     * @param note short not for generator run type
     * @return HTTP respond code
     */
    public int notifyServer(double frequencyValue, String note) {
        String frequency = String.valueOf(frequencyValue);
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("n", note);
            jsonData.put("d", frequencyValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, jsonData.toString());


        HTTPPostTask mt = new HTTPPostTask();
        mt.execute(mURI, jsonData.toString());

        return mResultCode;

    }

    private class HTTPPostTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {

            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpPost request = new HttpPost(params[0]);
                StringEntity params2 = new StringEntity(params[1]);
                request.addHeader("content-type", "application/json");
                request.setEntity(params2);
                HttpResponse response = httpClient.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();
                Log.d(LOG_TAG, "Response code: " + String.valueOf(responseCode));

                // handle response here...
            }catch (Exception ex) {
                ex.printStackTrace();
                Log.e(LOG_TAG,"Network exception occurred");
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            mResultCode = result;
        }
    }
}
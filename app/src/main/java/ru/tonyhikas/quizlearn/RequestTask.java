package ru.tonyhikas.quizlearn;


import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


class RequestTask extends AsyncTask<String, String, ResponseResult> {

    /**
     * @param params [0] - url, [1] - http method, [2] - data
     */
    @Override
    protected ResponseResult doInBackground(String... params) {
        ResponseResult result = new ResponseResult();
        try {
            URL url;
            String method = params[1];
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod(method);
                if (method.equals("POST")){
                    urlConnection.setDoOutput(true);
                    urlConnection.getOutputStream().write(params[2].getBytes());
                }
                InputStream in = urlConnection.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);

                int data = isw.read();

                while (data != -1) {
                    result.data += (char) data;
                    data = isw.read();
                }

                // return the data to onPostExecute method
                return result;

            } catch (Exception e) {
                e.printStackTrace();
                result.hasError = true;
                result.errorText = e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult("", 500, true, e.getMessage());
        }
        return result;
    }

}
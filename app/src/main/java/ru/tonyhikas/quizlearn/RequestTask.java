package ru.tonyhikas.quizlearn;


import android.os.AsyncTask;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setDoOutput(true);
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(params[2].getBytes("UTF-8"));
                    os.close();
                }
                InputStream in;
                try {
                    in = urlConnection.getInputStream();
                }catch (Exception e){
                    String error_text = "";
                    InputStreamReader isw = new InputStreamReader(urlConnection.getErrorStream());
                    int read_char = isw.read();
                    while (read_char != -1) {
                        error_text += (char) read_char;
                        read_char = isw.read();
                    }
                    return new ResponseResult(
                            "",
                            urlConnection.getResponseCode(),
                            false,
                            error_text);
                }
                InputStreamReader isw = new InputStreamReader(in);

                int data = isw.read();

                while (data != -1) {
                    result.data += (char) data;
                    data = isw.read();
                }
                result.statusCode = urlConnection.getResponseCode();
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
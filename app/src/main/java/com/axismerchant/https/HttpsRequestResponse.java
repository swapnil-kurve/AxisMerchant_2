package com.axismerchant.https;

import com.axismerchant.classes.Constants;

public class HttpsRequestResponse {

    private HttpsRequestResponse(){
    }
    private static HttpsRequestResponse instance = null;
    public static HttpsRequestResponse getInstance(){
        if(instance == null) {
            instance = new HttpsRequestResponse();
        }
        return instance;
    }

    public String postRequest(String content,String url) {
        String responseString = "";

        HttpsPostConnection httpsPostConnection = new HttpsPostConnection(url,content, Constants.ServiceRef,null);;

        try {
            responseString = httpsPostConnection.execute();
            }
        catch (Exception e) {
          e.getMessage();
        }
        return responseString;
    }
}

package com.axismerchant.classes;

/**
 * Created by sweta.shah on 4/20/16.
 */

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class HTTPUtils {
    /**
     * HttpClient
     * @param isHTTPS
     * @return
     */
    public HttpClient getNewHttpClient(boolean isHTTPS) {

        if(!isHTTPS){
            return getNewHttpClient();
        }else
            return null;
       /* File sdCard = Environment.getExternalStorageDirectory();
        try {
            if(!isHTTPS){
                return getNewHttpClient();
            }
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            //SSLSocketFactory sf = new CustomSSLSocketFactory_Old(trustStore);
            //SSLSocketFactory sf = new SSLSocketFactory(keyStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpParams params = new BasicHttpParams();
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", sf, 443));
            //Activity_EnterCardDetails.//textlog("\nHTTPUtils. testing CA");
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return null;
        }*/
    }






    /**
     * HttpClient for http request
     * @return
     */
    private static HttpClient getNewHttpClient(){
        HttpParams params = new BasicHttpParams();
        return new DefaultHttpClient(params);
    }
}


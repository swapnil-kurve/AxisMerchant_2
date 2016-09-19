package com.nxg.axismerchant.https;

import android.support.annotation.NonNull;
import android.util.Log;

import com.nxg.axismerchant.classes.Constants;

import org.apache.http.HttpException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsPostConnection {

    /**
     * Connection time out.
     */
    public static final int TIMEOUT = 30 * 1000;

    /**
     * HTTP Post method
     */
    public static final String HTTP_METHOD_POST = "POST";

    /**
     * URL
     */
    private String mUrl;
    /**
     * Input Data
     */
    private String mData;

    /**
     * Host Name
     */
    private String mHostname;

    /**
     * Certificate bytes
     */
    private byte[] mCertificateBytes;
    /**
     * Retry After Header
     */
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    public HttpsPostConnection(String url, String content, String ip, byte[] certificateChain)
    {
        this.mUrl = url;
        this.mData = content;
        this.mHostname = getHostName(ip);//"PCSMUMD1007";
        this.mCertificateBytes = certificateChain;
    }

    public HttpsPostConnection initializeData(String url, String content, String ip, byte[] certificateChain)
    {
        this.mUrl = url;
        this.mData = content;
        this.mHostname = getHostName(ip);
        this.mCertificateBytes = certificateChain;

        return this;
    }

    /**
     * This method takes request url
     *
     * @param url url of cms
     * @return Instance of HttpsPostConnection
     */
    public HttpsPostConnection withUrl(String url) {
        this.mUrl = url;
        return this;
    }

    /**
     * This method takes request data to send
     *
     * @param content data to send in request.
     * @return Instance of HttpsPostConnection
     */
    public HttpsPostConnection withRequestData(String content) {
        this.mData = content;
        return this;
    }
    /**
     * Creates URL object out of String url.
     *
     * @return URL Url
     */
    @NonNull
    private URL getServerUrl() throws MalformedURLException {
        return new URL(mUrl);
    }

    /**
     * This method takes host name to verify
     *
     * @param hostName Host Name
     * @return Instance of HttpsPostConnection
     */
    public HttpsPostConnection withHostName(String hostName) {
        this.mHostname = hostName;
        return this;
    }


    /**
     * This method takes certificate
     *
     * @param certificateBytes Byte Array of certificate
     * @return Instance of HttpsPostConnection
     */
    public HttpsPostConnection withCertificate(final byte[] certificateBytes) {
        this.mCertificateBytes = certificateBytes;
        return this;
    }


    /**
     * Read received input stream
     *
     * @param stream input data stream
     * @return byte[] of received data
     * @throws IOException
     */
    protected byte[] readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] chunk = new byte[2048];
        int length;
        while ((length = stream.read(chunk)) != -1) {
            if (length == chunk.length) {
                outputStream.write(chunk);
            } else {
                byte[] lastChunk = new byte[length];
                System.arraycopy(chunk, 0, lastChunk, 0, length);
                outputStream.write(lastChunk);
            }
        }
        return outputStream.toByteArray();
    }

    /**
     * Initialize ssl context as TLS.
     *
     * @return SSLContext Instance of SSLContext
     */
    @NonNull
    private SSLContext initializePermissiveSslContext() throws NoSuchAlgorithmException,
            KeyManagementException, IOException, HttpException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        try {
            KeyManager[] keyManagers = null;
            TrustManager[] customTrustManager = null;
            if (mCertificateBytes != null && mCertificateBytes.length > 0) {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream mInputStream = new ByteArrayInputStream(mCertificateBytes);
                Certificate certificate = cf.generateCertificate(mInputStream);

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", certificate);
                customTrustManager = new TrustManager[]{new CustomTrustManager(keyStore)};
                kmf.init(keyStore, null);
                keyManagers = kmf.getKeyManagers();
            }
            else //trust all certificate
            {
                TrustManager tm = new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                customTrustManager = new TrustManager[]{tm};
            }

            sslContext.init(keyManagers, customTrustManager, new java.security.SecureRandom());

        } catch (CertificateException e) {

            throw new HttpException("Error in Certificate");
        } catch (Exception e) {
            if (e.getCause() != null) {

            } else {

            }
            throw new HttpException("Error in ssl context preparation");
        }
        return sslContext;
    }

    /**
     * Set HTTPs url connection.
     *
     * @return HttpsURLConnection Instance of HttpsURLConnection
     */
    private HttpsURLConnection setupHttpsUrlConnection(URL serverUrl) throws
            KeyManagementException, NoSuchAlgorithmException, IllegalArgumentException,
            IOException, HttpException {
        SSLContext sslContext = initializePermissiveSslContext();
        HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) serverUrl.openConnection();

        if (Constants.FORCE_TLS_PROTOCOL != null
                && Constants.FORCE_TLS_PROTOCOL.length != 0) {
            //Only protocols supported mentioned in Build config.
            SSLSocketFactory tslOnlySocketFactory =
                    new CustomSSLSocketFactory(sslContext.getSocketFactory());
            httpsUrlConnection.setSSLSocketFactory(tslOnlySocketFactory);
        } else {
            // #MCBP_LOG_BEGIN
            //mLogger.d("No protocol found in Build config");
            // #MCBP_LOG_END

            // Note that this requires at least API level 16
            httpsUrlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        }

        if (mHostname == null || mHostname.isEmpty()) {
            throw new HttpException("No host name found");
        }

        httpsUrlConnection.setRequestMethod(HTTP_METHOD_POST);
        httpsUrlConnection.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        /*httpsUrlConnection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(final String hostname, final SSLSession session) {
                Boolean test=verifyHost(session);
                return test;
            }
        });*/
        httpsUrlConnection.setDoInput(true);
        httpsUrlConnection.setDoOutput(true);
        httpsUrlConnection.setConnectTimeout(TIMEOUT);
        httpsUrlConnection.setReadTimeout(TIMEOUT);
        httpsUrlConnection.setRequestProperty("Content-Type", "application/json");
        httpsUrlConnection.setRequestProperty("Accept", "application/json");
        return httpsUrlConnection;
    }


    /**
     * Set HTTP url connection.
     *
     * @return HttpURLConnection Instance of HttpURLConnection
     */

    private HttpURLConnection setupHttpUrlConnection(URL serverUrl) throws IOException {

        HttpURLConnection httpURLConnection = (HttpURLConnection) serverUrl.openConnection();
        httpURLConnection.setRequestMethod(HTTP_METHOD_POST);

        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setConnectTimeout(TIMEOUT);
        httpURLConnection.setReadTimeout(TIMEOUT);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        return httpURLConnection;
    }



    private boolean verifyHost(final SSLSession sslSession) {
        X509Certificate[] certificateChain;
        Boolean resp =false;

        try {
            certificateChain = (X509Certificate[]) sslSession.getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {

            resp=false;
            return resp;
        }

        if (certificateChain != null) {
            for (int i = 0; i <= certificateChain.length; i++) {
                try {
                    certificateChain[0].checkValidity();
                } catch (CertificateExpiredException e) {

                    return resp;
                } catch (CertificateNotYetValidException e) {

                    return resp;
                }
            }

            String clientDN = (certificateChain[0].getSubjectDN()).getName();
            int clientNameIndex = clientDN.indexOf("CN");
            int clientNameEndIndex = clientDN.indexOf(',', clientNameIndex);

            String attributeName;
            if (clientNameEndIndex == -1) {
                attributeName = clientDN.substring(clientNameIndex);
            } else {
                attributeName = clientDN.substring(clientNameIndex, clientNameEndIndex);
            }

            String commonName = attributeName.substring(3);
            if (commonName.equals(mHostname)) {
                resp=true;
            }
        }
        Log.e("before returning resp = ", resp + "");
            return resp;
    }


    /**
     * Return error message
     *
     * @param httpUrlConnection httpUrlConnection
     * @return Error message
     * @throws IOException
     */
    private String getErrorStream(HttpURLConnection httpUrlConnection) throws IOException {

        InputStream errorStream = httpUrlConnection.getErrorStream();
        if (errorStream != null) {
            return new String(readAll(errorStream));
        }
        return null;
    }


    public String execute() throws HttpException {
        //mLogger.d("----------HTTP " + mRequestMethod + " START------------");
        String responseString="";
        HttpURLConnection httpUrlConnection = null;
        String errorMessage;
        int responseCode;
        int retryAfterValue;
        try {
            InputStream inputStream;
            URL serverUrl = getServerUrl();

            if (serverUrl.getProtocol().equalsIgnoreCase("https")) {
                httpUrlConnection = setupHttpsUrlConnection(serverUrl);
            } else {
                httpUrlConnection = setupHttpUrlConnection(serverUrl);
            }
            //In case of HttpGetRequest, data will not be there.
            if (this.mData != null) {
                httpUrlConnection.getOutputStream().write(this.mData.getBytes());
            }
            httpUrlConnection.connect();
            responseCode = httpUrlConnection.getResponseCode();
            errorMessage = getErrorStream(httpUrlConnection);

            if (!((responseCode == HttpURLConnection.HTTP_OK) ||
                    (responseCode == HttpURLConnection.HTTP_NO_CONTENT))) {
                //throw new HttpException(errorMessage);
                   responseString = errorMessage;
            }
            else {

                   // Try to build a response
                   inputStream = httpUrlConnection.getInputStream();
                   //byte[] reponse =readAll(inputStream);
                   responseString = Constants.convertStreamToString(inputStream);
               }
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new HttpException(e.getMessage(), e);
        } catch (SocketTimeoutException e) {
            throw new HttpException(e.getMessage());
        } catch (SSLException e) {
            throw new HttpException(e.getMessage());
        } catch (IOException e) {
            throw new HttpException(e.getMessage(), e);
        }catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }  finally {
            if (httpUrlConnection != null) {
                httpUrlConnection.disconnect();
            }
        }

        return responseString;
    }

    public String getHostName(String ip) {
        String hostname="";
        try {
            if(ip.startsWith("https"))
            ip=ip.substring(8,ip.length()-1);
            else
            ip=ip.substring(7,ip.length()-1);

            InetAddress inetAddr = InetAddress.getByName(ip);
            // Get the host name
          hostname = inetAddr.getHostName();

            // Get canonical host name
            String canonicalHostname = inetAddr.getCanonicalHostName();

            /*System.out.println("Hostname: " + hostname);
            System.out.println("Canonical Hostname: " + canonicalHostname);*/

        } catch (UnknownHostException e) {
            e.getMessage();
           /* System.out.println("Host not found: " + e.getMessage());*/
        }
        catch (Exception e) {

        }
        return hostname;
    }
}
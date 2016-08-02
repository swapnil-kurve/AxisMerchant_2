package com.nxg.axismerchant.https;

import com.nxg.axismerchant.classes.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class CustomSSLSocketFactory extends SSLSocketFactory {
    private final SSLSocketFactory mSSLSocketFactory;

    public CustomSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.mSSLSocketFactory = sslSocketFactory;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return mSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return mSSLSocketFactory.getSupportedCipherSuites();
    }

    private Socket makeSocketSafe(Socket socket) throws IOException {
        if (socket instanceof SSLSocket) {
            socket = new CustomSSLSocket((SSLSocket) socket);
            if (Constants.FORCE_TLS_PROTOCOL != null &&
                    Constants.FORCE_TLS_PROTOCOL.length > 0) {
                ((SSLSocket) socket).setEnabledProtocols(Constants.FORCE_TLS_PROTOCOL);
            }

        }
        return socket;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        return makeSocketSafe(mSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return makeSocketSafe(mSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
        return makeSocketSafe(mSSLSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return makeSocketSafe(mSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address,
                               int port,
                               InetAddress localAddress,
                               int localPort) throws IOException {
        return makeSocketSafe(
                mSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }


    public class CustomSSLSocket extends SSLSocket {

        protected final SSLSocket mSSLSocket;

        CustomSSLSocket(SSLSocket sslSocket) {
            this.mSSLSocket = sslSocket;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return mSSLSocket.getSupportedCipherSuites();
        }

        @Override
        public String[] getEnabledCipherSuites() {
            return mSSLSocket.getEnabledCipherSuites();
        }

        @Override
        public void setEnabledCipherSuites(String[] suites) {
            mSSLSocket.setEnabledCipherSuites(suites);
        }

        @Override
        public String[] getSupportedProtocols() {
            return mSSLSocket.getSupportedProtocols();
        }

        @Override
        public String[] getEnabledProtocols() {
            return mSSLSocket.getEnabledProtocols();
        }

        @Override
        public void setEnabledProtocols(String[] protocols) {
            mSSLSocket.setEnabledProtocols(protocols);
        }

        @Override
        public SSLSession getSession() {
            return mSSLSocket.getSession();
        }

        @Override
        public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
            mSSLSocket.addHandshakeCompletedListener(listener);
        }

        @Override
        public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
            mSSLSocket.removeHandshakeCompletedListener(listener);
        }

        @Override
        public void startHandshake() throws IOException {
            mSSLSocket.startHandshake();
        }

        @Override
        public void setUseClientMode(boolean mode) {
            mSSLSocket.setUseClientMode(mode);
        }

        @Override
        public boolean getUseClientMode() {
            return mSSLSocket.getUseClientMode();
        }

        @Override
        public void setNeedClientAuth(boolean need) {
            mSSLSocket.setNeedClientAuth(need);
        }

        @Override
        public void setWantClientAuth(boolean want) {
            mSSLSocket.setWantClientAuth(want);
        }

        @Override
        public boolean getNeedClientAuth() {
            return mSSLSocket.getNeedClientAuth();
        }

        @Override
        public boolean getWantClientAuth() {
            return mSSLSocket.getWantClientAuth();
        }

        @Override
        public void setEnableSessionCreation(boolean flag) {
            mSSLSocket.setEnableSessionCreation(flag);
        }

        @Override
        public boolean getEnableSessionCreation() {
            return mSSLSocket.getEnableSessionCreation();
        }

        @Override
        public void bind(SocketAddress localAddr) throws IOException {
            mSSLSocket.bind(localAddr);
        }

        @Override
        public synchronized void close() throws IOException {
            mSSLSocket.close();
        }

        @Override
        public void connect(SocketAddress remoteAddr) throws IOException {
            mSSLSocket.connect(remoteAddr);
        }

        @Override
        public void connect(SocketAddress remoteAddr, int timeout) throws IOException {
            mSSLSocket.connect(remoteAddr, timeout);
        }

        @Override
        public SocketChannel getChannel() {
            return mSSLSocket.getChannel();
        }

        @Override
        public InetAddress getInetAddress() {
            return mSSLSocket.getInetAddress();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return mSSLSocket.getInputStream();
        }

        @Override
        public boolean getKeepAlive() throws SocketException {
            return mSSLSocket.getKeepAlive();
        }

        @Override
        public InetAddress getLocalAddress() {
            return mSSLSocket.getLocalAddress();
        }

        @Override
        public int getLocalPort() {
            return mSSLSocket.getLocalPort();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return mSSLSocket.getLocalSocketAddress();
        }

        @Override
        public boolean getOOBInline() throws SocketException {
            return mSSLSocket.getOOBInline();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return mSSLSocket.getOutputStream();
        }

        @Override
        public int getPort() {
            return mSSLSocket.getPort();
        }

        @Override
        public synchronized int getReceiveBufferSize() throws SocketException {
            return mSSLSocket.getReceiveBufferSize();
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return mSSLSocket.getRemoteSocketAddress();
        }

        @Override
        public boolean getReuseAddress() throws SocketException {
            return mSSLSocket.getReuseAddress();
        }

        @Override
        public synchronized int getSendBufferSize() throws SocketException {
            return mSSLSocket.getSendBufferSize();
        }

        @Override
        public int getSoLinger() throws SocketException {
            return mSSLSocket.getSoLinger();
        }

        @Override
        public synchronized int getSoTimeout() throws SocketException {
            return mSSLSocket.getSoTimeout();
        }

        @Override
        public boolean getTcpNoDelay() throws SocketException {
            return mSSLSocket.getTcpNoDelay();
        }

        @Override
        public int getTrafficClass() throws SocketException {
            return mSSLSocket.getTrafficClass();
        }

        @Override
        public boolean isBound() {
            return mSSLSocket.isBound();
        }

        @Override
        public boolean isClosed() {
            return mSSLSocket.isClosed();
        }

        @Override
        public boolean isConnected() {
            return mSSLSocket.isConnected();
        }

        @Override
        public boolean isInputShutdown() {
            return mSSLSocket.isInputShutdown();
        }

        @Override
        public boolean isOutputShutdown() {
            return mSSLSocket.isOutputShutdown();
        }

        @Override
        public void sendUrgentData(int value) throws IOException {
            mSSLSocket.sendUrgentData(value);
        }

        @Override
        public void setKeepAlive(boolean keepAlive) throws SocketException {
            mSSLSocket.setKeepAlive(keepAlive);
        }

        @Override
        public void setOOBInline(boolean oobinline) throws SocketException {
            mSSLSocket.setOOBInline(oobinline);
        }

        @Override
        public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
            mSSLSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        @Override
        public synchronized void setReceiveBufferSize(int size) throws SocketException {
            mSSLSocket.setReceiveBufferSize(size);
        }

        @Override
        public void setReuseAddress(boolean reuse) throws SocketException {
            mSSLSocket.setReuseAddress(reuse);
        }

        @Override
        public synchronized void setSendBufferSize(int size) throws SocketException {
            mSSLSocket.setSendBufferSize(size);
        }

        @Override
        public void setSoLinger(boolean on, int timeout) throws SocketException {
            mSSLSocket.setSoLinger(on, timeout);
        }

        @Override
        public synchronized void setSoTimeout(int timeout) throws SocketException {
            mSSLSocket.setSoTimeout(timeout);
        }

        @Override
        public void setTcpNoDelay(boolean on) throws SocketException {
            mSSLSocket.setTcpNoDelay(on);
        }

        @Override
        public void setTrafficClass(int value) throws SocketException {
            mSSLSocket.setTrafficClass(value);
        }

        @Override
        public void shutdownInput() throws IOException {
            mSSLSocket.shutdownInput();
        }

        @Override
        public void shutdownOutput() throws IOException {
            mSSLSocket.shutdownOutput();
        }

        @Override
        public String toString() {
            return mSSLSocket.toString();
        }

        @Override
        public boolean equals(Object o) {
            return mSSLSocket.equals(o);
        }
    }
}

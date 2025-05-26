package com.whisper.network;

import com.whisper.HttpClientWrapper;
import java.util.concurrent.BlockingQueue;

public class NetworkStream {

    public BlockingQueue<byte[]> receivedBytes = HttpClientWrapper.getReceivingQueue();

    public void send(byte[] bytes) {
        HttpClientWrapper.stream(bytes);
    }

    public void startStreaming() {
       try {
           HttpClientWrapper.startStreamingBlocking();
       } catch (Exception e) {
           System.out.println("NetworkStream -> Connection error. \n" + e);
       }
    }

    public boolean isConnected() {
        return HttpClientWrapper.isStreaming();
    }

    public void setUrl(String url) {
        HttpClientWrapper.setUrl(url);
    }

    public String getUrl() {
        return HttpClientWrapper.getUrl();
    }

    public void stop() {
        HttpClientWrapper.stopSendingAndReceiving();
    }
}

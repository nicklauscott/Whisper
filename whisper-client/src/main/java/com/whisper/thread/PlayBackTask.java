package com.whisper.thread;

import com.whisper.audio.AudioPlayBack;
import com.whisper.network.NetworkStream;

public class PlayBackTask implements Runnable{

    private final NetworkStream networkStream;
    public PlayBackTask(NetworkStream networkStream) {
        this.networkStream = networkStream;
    }

    @Override
    public void run() {
        if (networkStream.isConnected()) {
            System.out.println("Audio playback started");
            AudioPlayBack playBack = new AudioPlayBack(networkStream);
            playBack.start();
        } else {
            System.out.println("PlayBackTask -> Error server not connected");
        }
    }
}

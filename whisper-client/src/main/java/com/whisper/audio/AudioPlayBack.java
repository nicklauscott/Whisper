package com.whisper.audio;

import com.whisper.network.NetworkStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayBack {

    private final AudioFormat format;
    private SourceDataLine speaker;
    private final NetworkStream networkStream;

    public AudioPlayBack(NetworkStream networkStream) {
        this.networkStream = networkStream;
        this.format = new AudioFormat(44100, 16, 1, true, false);
    }

    public void start() {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);
            speaker.start();

            while (true) {
                byte[] audioChunk = networkStream.receivedBytes.take(); // blocks until data available
                speaker.write(audioChunk, 0, audioChunk.length);
            }

        } catch (Exception e) {
            System.out.println("AudioPlayBack -> Error playing audio: \n" + e);
        } finally {
            if (speaker != null) {
                speaker.drain();
                speaker.close();
            }
        }
    }
}

package com.whisper;

import com.whisper.audio.AudioRecorder;
import com.whisper.network.NetworkStream;
import com.whisper.thread.PlayBackTask;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Whisper {
    public static final NetworkStream httpClient = new NetworkStream();
    public static final AudioRecorder recorder = new AudioRecorder();
    public static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException {

        ExecutorService executorService = Executors.newCachedThreadPool();
        Thread start = new Thread(httpClient::startStreaming);
        Thread playback = new Thread(new PlayBackTask(httpClient));
        executorService.execute(start);

        var text = new StringBuilder();
        for (int i = 0; i < 5; i ++) {
            text.append(".");
            System.out.println("\rWaiting for server" + text);
            Thread.sleep(10);
            if (httpClient.isConnected()) break;
        }

        if (httpClient.isConnected()) {
            System.out.println("Connected to Whisper server");
        } else {
            httpClient.stop();
            executorService.close();
            System.exit(0);
        }

        while (true) {
            System.out.println("Enter 's' or 'start' to start whispering");
            var input = scanner.next();
            if (input.equals("s") || input.equals("start")) break;
        }
        executorService.execute(playback);
        recorder.record(httpClient::send);

    }
}


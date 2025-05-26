package com.whisper.audio;

import com.whisper.thread.Callback;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class AudioRecorder {

    //public static final AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
    public static final AudioFormat format1 = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            16000.0f, // sample rate
            16,       // sample size in bits
            1,        // channels (mono)
            2,        // frame size
            16000.0f, // frame rate
            false     // little endian
    );
    public static final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);;
    public static final DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    public static TargetDataLine microphone;

    public void record() throws IOException {

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not Supported");
            System.exit(0);
        }

        try (TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info)) {
            mic.open(format);
            mic.start();

            System.out.println("Recording...");
            AudioInputStream stream = new AudioInputStream(mic);

            // Create a separate thread to stop recording after 5 seconds
            Thread stopper = new Thread(() -> {
                try {
                    for (int i = 5; i > 0; i--) {
                        System.out.println("Stopping in " + i + "...");
                        Thread.sleep(1000);
                    }
                    mic.stop();
                    mic.close();
                    System.out.println("Recording stopped.");
                } catch (InterruptedException e) {
                    System.out.println("Error occurred when counting down. \n" + e);
                }
            });

            stopper.start();

            File output = generateTempFile();
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, output);

        } catch (Exception e) {
            System.out.println("Error occurred when recording. \n" + e);
        }

    }

    public void record(Callback callback) throws IOException {
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not Supported");
            System.exit(0);
        }

        try (TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info)) {
            mic.open(format);
            mic.start();
            byte[] buffer = new byte[4096];
            try (AudioInputStream stream = new AudioInputStream(mic)) {
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                    callback.onComplete(chunk); // Send each chunk
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred when recording. \n" + e);
        }
    }

    public static byte[]audioInputStreamToBytes(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != 1) {
            out.write(buffer, 0, bytesRead);
        }
        return out.toByteArray();
    }

    private File generateTempFile() {
        String prefix = "Whisper_Recording";
        String fileExtension = ".wav";
        String id = UUID.randomUUID().toString();
        return new File(prefix + id + fileExtension);
    }



}

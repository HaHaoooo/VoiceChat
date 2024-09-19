package com.haha.modules;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class AudioPlayer extends Thread {

    private final SourceDataLine speakers;
    private final InputStream in;

    public AudioPlayer(Socket socket) throws LineUnavailableException, IOException {
        AudioFormat format = getAudioFormat();
        speakers = AudioSystem.getSourceDataLine(format);
        speakers.open(format);
        in = socket.getInputStream();
    }

    @Override
    public void run() {
        speakers.start();
        byte[] buffer = new byte[1024];

        while (!isInterrupted()) {
            try {
                int bytesRead = in.read(buffer, 0, buffer.length);
                if (bytesRead == -1) {
                    break;
                }
                speakers.write(buffer, 0, bytesRead);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        speakers.drain();
        speakers.stop();
        speakers.close();
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
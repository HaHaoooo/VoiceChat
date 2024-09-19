package com.haha.modules;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class AudioCapture extends Thread {

    private final TargetDataLine microphone;
    private final OutputStream out;

    public AudioCapture(Socket socket) throws LineUnavailableException, IOException {
        AudioFormat format = getAudioFormat();
        microphone = AudioSystem.getTargetDataLine(format);
        microphone.open(format);
        out = socket.getOutputStream();
    }

    @Override
    public void run() {
        microphone.start();
        byte[] buffer = new byte[1024];

        while (!isInterrupted()) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            try {
                out.write(buffer, 0, bytesRead);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        microphone.stop();
        microphone.close();
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
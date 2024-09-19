package com.haha.clients.base;

import com.haha.modules.AudioCapture;
import com.haha.modules.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class UserClientBase extends JFrame {

    protected GridBagConstraints gbc = new GridBagConstraints();
    protected JButton toggleCallButton;
    protected JTextField serverIpField;
    protected JTextField serverPortField;
    protected Socket clientSocket;
    protected AudioCapture audioCapture;
    protected AudioPlayer audioPlayer;
    protected boolean callActive = false;
    protected String username;

    public UserClientBase(String title, int width, int height, String username) {
        this.username = username;
        setTitle(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    // 初始化通用界面
    protected void initUI() {
        setLayout(new GridBagLayout());
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel serverIpLabel = new JLabel("Server IP:");
        serverIpField = new JTextField("localhost", 20);
        JLabel serverPortLabel = new JLabel("Server Port:");
        serverPortField = new JTextField("12345", 20);

        toggleCallButton = new JButton("Start Call");
        toggleCallButton.addActionListener(e -> toggleCall());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(serverIpLabel, gbc);
        gbc.gridx = 1;
        add(serverIpField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(serverPortLabel, gbc);
        gbc.gridx = 1;
        add(serverPortField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(toggleCallButton, gbc);
    }

    protected void toggleCall() {
        if (!callActive) {
            startCall();
        } else {
            stopCall();
        }
    }

    // 通用通话开启逻辑
    protected void startCall() {
        SwingUtilities.invokeLater(() -> toggleCallButton.setText("End Call"));

        new Thread(() -> {
            try {
                String address = serverIpField.getText();
                int port = Integer.parseInt(serverPortField.getText());
                clientSocket = new Socket(address, port);

                // 发送用户名到服务器
                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                outputStream.writeUTF(username);

                audioCapture = new AudioCapture(clientSocket);
                audioPlayer = new AudioPlayer(clientSocket);
                audioCapture.start();
                audioPlayer.start();
            } catch (IOException | LineUnavailableException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> toggleCallButton.setText("Start Call"));
            }
        }).start();

        callActive = true;
    }

    // 通用通话关闭逻辑
    protected void stopCall() {
        SwingUtilities.invokeLater(() -> toggleCallButton.setText("Start Call"));

        new Thread(() -> {
            try {
                callActive = false;
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                if (audioCapture != null) {
                    audioCapture.interrupt();
                }
                if (audioPlayer != null) {
                    audioPlayer.interrupt();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
package com.haha.clients;

import com.haha.clients.base.UserClientBase;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntegratedClient extends UserClientBase {

    private JTextArea serverInfoArea;
    private JButton toggleServerButton;
    private JLabel serverStatusLabel;
    private JLabel onlineCountLabel;

    private boolean serverRunning = false;
    private ServerSocket serverSocket;
    private final List<Socket> clients = new ArrayList<>();
    private final Map<Socket, String> clientNames = new HashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public IntegratedClient(String username) {
        super("Integrated Client - " + username.substring(0, 1).toUpperCase() + username.substring(1), 800, 600, username);
    }

    @Override
    public void initUI() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel serverIpLabel = new JLabel("Server IP:");
        serverIpField = new JTextField("localhost", 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(serverIpLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        controlPanel.add(serverIpField, gbc);

        JLabel serverPortLabel = new JLabel("Server Port:");
        serverPortField = new JTextField("12345", 20);

        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(serverPortLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        controlPanel.add(serverPortField, gbc);

        serverStatusLabel = new JLabel("Server Status: Stopped");

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        controlPanel.add(serverStatusLabel, gbc);

        toggleServerButton = new JButton("Start Server");
        toggleServerButton.addActionListener(e -> toggleServer());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        controlPanel.add(toggleServerButton, gbc);

        toggleCallButton = new JButton("Start Call");
        toggleCallButton.addActionListener(e -> toggleCall());

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        controlPanel.add(toggleCallButton, gbc);

        onlineCountLabel = new JLabel("Online: 0");

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        controlPanel.add(onlineCountLabel, gbc);

        controlPanel.setPreferredSize(new Dimension(350, getHeight()));

        serverInfoArea = new JTextArea(15, 30);
        serverInfoArea.setEditable(false);
        serverInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane serverInfoScrollPane = new JScrollPane(serverInfoArea);
        serverInfoScrollPane.setBorder(BorderFactory.createTitledBorder("Server Info"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, serverInfoScrollPane);
        splitPane.setDividerLocation(350);

        add(splitPane, BorderLayout.CENTER);
    }

    private void toggleServer() {
        if (!serverRunning) {
            try {
                int port = Integer.parseInt(serverPortField.getText());
                serverSocket = new ServerSocket(port);
                serverRunning = true;
                serverStatusLabel.setText("Server Status: Running");
                toggleServerButton.setText("Stop Server");

                threadPool.execute(() -> {
                    while (serverRunning) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                            String clientName = inputStream.readUTF();
                            clients.add(clientSocket);
                            clientNames.put(clientSocket, clientName);
                            appendToServerInfo(clientName + " connected: " + clientSocket.getRemoteSocketAddress());
                            updateOnlineCount();
                            threadPool.execute(() -> handleClient(clientSocket));
                        } catch (IOException e) {
                            appendToServerInfo("Error accepting client: " + e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                appendToServerInfo("Failed to start server: " + e.getMessage());
            }
        } else {
            try {
                serverRunning = false;
                if (serverSocket != null) {
                    serverSocket.close();
                }
                serverStatusLabel.setText("Server Status: Stopped");
                toggleServerButton.setText("Start Server");
                appendToServerInfo("Server stopped.");
            } catch (IOException e) {
                appendToServerInfo("Error stopping server: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        String clientName = clientNames.get(clientSocket); // 获取客户端的用户名

        try {
            InputStream inputStream = clientSocket.getInputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                broadcastToClients(buffer, bytesRead, clientSocket);
            }
        } catch (IOException e) {
            appendToServerInfo("Error handling client: " + e.getMessage());
        } finally {
            clients.remove(clientSocket);
            clientNames.remove(clientSocket);
            updateOnlineCount();
            appendToServerInfo(clientName + " disconnected: " + clientSocket.getRemoteSocketAddress());
            try {
                clientSocket.close();
            } catch (IOException e) {
                appendToServerInfo("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void broadcastToClients(byte[] data, int length, Socket senderSocket) {
        for (Socket client : clients) {
            if (client != senderSocket) {
                try {
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write(data, 0, length);
                } catch (IOException e) {
                    appendToServerInfo("Error broadcasting to client: " + e.getMessage());
                }
            }
        }
    }

    private void updateOnlineCount() {
        SwingUtilities.invokeLater(() -> onlineCountLabel.setText("Online: " + clients.size()));
    }

    private void appendToServerInfo(String message) {
        SwingUtilities.invokeLater(() -> {
            serverInfoArea.append(message + "\n");
            serverInfoArea.setCaretPosition(serverInfoArea.getDocument().getLength());
        });
    }
}
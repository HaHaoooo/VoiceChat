package com.haha.clients;

import com.haha.clients.base.ClientBase;

import javax.swing.*;
import java.awt.*;

public class NormalClient extends ClientBase {

    public NormalClient(String username) {
        super("User - " + username.substring(0, 1).toUpperCase() + username.substring(1), 400, 200, username);
    }

    @Override
    protected void intiUI() {
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
}
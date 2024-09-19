package com.haha.panel;

import com.haha.clients.IntegratedClient;
import com.haha.clients.NormalClient;
import com.haha.util.PasswordUtils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginPanel extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private final Map<String, User> userCredentials;

    public LoginPanel() {
        setTitle("Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        userCredentials = new HashMap<>();
        loadUsers();
        initUI();
    }

    // 初始化界面
    private void initUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名输入
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        // 密码输入
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        // 登录按钮
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> authenticate());

        // 添加组件到布局
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(usernameLabel, gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(loginButton, gbc);
    }

    // 从 resources 文件夹加载用户
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("user.txt"))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String role = parts[0]; // 获取用户类型 (integrated 或 normal)
                    String[] credentials = parts[1].split(":");
                    if (credentials.length == 2) {
                        String username = credentials[0]; // 用户名
                        String password = credentials[1]; // 密码
                        userCredentials.put(username, new User(password, role));
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading user credentials.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 验证用户
    private void authenticate() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        User user = userCredentials.get(username);
        if (PasswordUtils.hashPassword(password).equals(user.getPassword())) {
            JOptionPane.showMessageDialog(this, "Login successful!");

            // 根据用户类型跳转
            switch (user.getRole()) {
                case "integrated" -> SwingUtilities.invokeLater(() -> {
                    this.dispose();
                    IntegratedClient integratedClient = new IntegratedClient(username);
                    integratedClient.setVisible(true);
                });
                case "normal" -> SwingUtilities.invokeLater(() -> {
                    this.dispose();
                    NormalClient normalClient = new NormalClient(username);
                    normalClient.setVisible(true);
                });
                default -> JOptionPane.showMessageDialog(this, "Invalid user role.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 用户类
    private record User(String password, String role) {
        public String getPassword() {
            return password;
        }

        public String getRole() {
            return role;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPanel loginPanel = new LoginPanel();
            loginPanel.setVisible(true);
        });
    }
}
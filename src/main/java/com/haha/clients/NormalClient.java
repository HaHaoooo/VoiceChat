package com.haha.clients;

import com.haha.clients.base.UserClientBase;

public class NormalClient extends UserClientBase {

    public NormalClient(String username) {
        super("Normal Client - " + username.substring(0, 1).toUpperCase() + username.substring(1), 400, 200, username);
    }
}
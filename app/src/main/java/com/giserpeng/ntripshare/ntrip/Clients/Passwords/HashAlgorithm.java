package com.giserpeng.ntripshare.ntrip.Clients.Passwords;

public enum HashAlgorithm {
    None(new None()), BCrypt(new BCrypt()), SHA256(new SHA256());

    public PasswordHandler passwordHandler;

    HashAlgorithm(PasswordHandler passwordHandler) {
        this.passwordHandler = passwordHandler;
    }
}

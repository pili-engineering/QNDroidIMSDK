package com.qiniu.qndroidimsdk.mode;


import java.io.Serializable;

public class LoginToken implements Serializable {

    private String loginToken;
    private String accountId;


    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

}

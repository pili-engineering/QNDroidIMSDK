package com.qiniu.qndroidimsdk.util;


import android.util.Base64;


public final class Utils {
    /**
     * Decode base64 string
     */
    public static String base64Decode(String msg) {
        try {
            return new String(Base64.decode(msg.getBytes(), Base64.DEFAULT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }



}

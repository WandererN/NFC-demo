package com.app.jh.nfcdemo;

/**
 * Created by Странник on 26.07.2017.
 */

public class StringUtils {
    public static String bytesToHex(byte[] in)
    {
        StringBuilder builder = new StringBuilder();
        for(byte b:in)
            builder.append(String.format("%02x",b));
        return builder.toString();
    }}

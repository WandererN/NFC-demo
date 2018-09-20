package com.app.jh.nfcdemo;

import android.content.Intent;
import android.nfc.NdefMessage;

import java.io.IOException;

/**
 * Created by Странник on 23.07.2017.
 */

public interface NfcReadWriteManager {
    void connect(Intent intent) throws IOException;
    String readHexData();
    int getSectionsCount();
    void format();
    void writeNdef(NdefMessage message);
    NdefMessage readNdef();
    byte[] readAllData();
    String readAll();
    void writeAll(String string);
    void close();
}

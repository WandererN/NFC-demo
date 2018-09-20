package com.app.jh.nfcdemo;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Странник on 26.07.2017.
 */

public class NfcForumType2Manager implements NfcReadWriteManager {
    private ForumType2Card forumType2Card;
    private int headerSection = 7;

    @Override
    public void connect(Intent intent) throws IOException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NfcA nfcA = NfcA.get(tag);
        forumType2Card = new ForumType2Card(nfcA);
        forumType2Card.connect();
    }

    @Override
    public String readHexData() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 41; i++)
            try {
                builder.append(String.format(Locale.getDefault(), "%d : %s \n", i, StringUtils.bytesToHex(Arrays.copyOf(forumType2Card.read((byte) i), 4))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        return builder.toString();
    }

    @Override
    public int getSectionsCount() {
        return 0;
    }

    @Override
    public void format() {
        forumType2Card.tryFormat();
    }

    @Override
    public void writeNdef(NdefMessage message) {
        try {
            byte[] data = message.toByteArray();
            forumType2Card.writeDataFromSection(12, data);
            forumType2Card.writeDataFromSection(headerSection, new byte[]{(byte) data.length, 0, 0, 0});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NdefMessage readNdef() {
        try {
            byte[] wholeData = readAllData();
            return new NdefMessage(Arrays.copyOf(wholeData, forumType2Card.read((byte) headerSection)[0]));
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] readAllData() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        for (byte i = 12; i < 36; i++)
            try {
                bs.write(Arrays.copyOfRange(forumType2Card.read(i), 0, 4));
            } catch (IOException e) {
                e.printStackTrace();
            }
        return bs.toByteArray();
    }

    @Override
    public String readAll() {

        return null;
    }

    @Override
    public void writeAll(String string) {

    }

    @Override
    public void close() {

    }
}

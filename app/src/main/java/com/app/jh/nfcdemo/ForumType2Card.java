package com.app.jh.nfcdemo;


import android.nfc.tech.NfcA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Странник on 24.07.2017.
 */

public class ForumType2Card {
    private NfcA mgr;
    private static byte READ_CMD = 0x30 & 0xFF;
    private static byte WRITE_CMD = (byte) (0xA2 & 0xFF);
    private static final int START_SECTION = 4;
    private static final int END_SECTION = 35;

    private int sectionCount = 36;

    public ForumType2Card(NfcA mgr) {
        this.mgr = mgr;
    }

    public void connect() throws IOException {
        mgr.connect();
    }

    public byte[] read(byte blockNum) throws IOException {
        return mgr.transceive(new byte[]{READ_CMD, blockNum});
    }

    public boolean write(int section, byte[] bytes) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bs.write(WRITE_CMD);
        bs.write((byte) section);
        byte[] lbytes = bytes;
        if (lbytes.length < 4)
            lbytes = Arrays.copyOfRange(bytes, 0, 4);
        bs.write(lbytes);
        byte[] res = mgr.transceive(bs.toByteArray());

        bs.close();
        return true;//TODO: make ack/nack handling
    }

    public boolean writeDataFromSection(int startSection, byte[] bytes) throws IOException {
        ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
        for (int i = 0; i < bytes.length / 4; i++) {
            int pos = i * 4;
            byte[] buf = new byte[4];
            bs.read(buf, 0, 4);
            if (!write(startSection + pos / 4, buf)) {
                bs.close();
                return false;
            }
        }
        bs.close();
        return true;
    }

    public void tryFormat() {
        byte[] zeros = {0, 0, 0, 0};
        for (int i = START_SECTION; i < END_SECTION; i++) {
            try {
                write(i, zeros);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

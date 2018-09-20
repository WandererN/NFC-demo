package com.app.jh.nfcdemo;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by Странник on 23.07.2017.
 */

public class NfcUltralightManager implements NfcReadWriteManager {

    private MifareUltralight mfc;
    private Charset charset = Charset.forName("windows-1251");
    private byte[] locks = new byte[2];
    private void readLocksInfo()
    {
        try {
            byte[] readPages=mfc.readPages(2);
            locks[0]=readPages[2];
            locks[1]=readPages[3];

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean isPageLocked(int num)//for classic mifare cards
    {
        if (num<8)
            return (locks[0]&(1<<num))==1;
        else
            return (locks[1]&(1<<num%8))==1;
    }
    @Override
    public void connect(Intent intent) throws IOException
    {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        mfc = MifareUltralight.get(tagFromIntent);
        mfc.connect();
        readLocksInfo();
    }

    @Override
    public String readHexData() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <getSectionsCount(); i++)
            try {
                builder.append(String.format(Locale.getDefault(),"%d : %s \n",i, StringUtils.bytesToHex(Arrays.copyOf(mfc.readPages(i),4))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        return builder.toString();
    }

    @Override
    public int getSectionsCount() {
        int res = 0;
        try{
            while(true)
            {
                mfc.readPages(res);
                res++;
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        return res;
    }

    @Override
    public void format() {

    }

    @Override
    public void writeNdef(NdefMessage message) {

    }

    @Override
    public NdefMessage readNdef() {
        return null;
    }

    @Override
    public byte[] readAllData() {
        return new byte[0];
    }

    private byte[] rawRead()
    {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        try {
            for (int i = 4; i <16; i++)
            if(!isPageLocked(i))
                    bs.write(Arrays.copyOf(mfc.readPages(i),4));
            } catch (IOException e) {
            e.printStackTrace();
        }
        return bs.toByteArray();
    }
    @Override
    public String readAll() {
        byte[] bytes = rawRead();
        byte len = bytes[0];
        byte[] strBytes=Arrays.copyOfRange(bytes,1,len+1);
        return new String(strBytes, charset);
    }

    private void writeSplitted(byte[] data) throws IOException {
        int offs = 0;
        int currSection = 4;
        while (offs<data.length)
        {

            int size = data.length-4>offs?4:data.length-4;
            byte[] nextPortion = Arrays.copyOfRange(data,offs,offs+size);
            while (isPageLocked(currSection))
                currSection++;
            mfc.writePage(currSection, Arrays.copyOf(nextPortion,4));
            offs+=size;
            currSection++;
        }
    }
    @Override
    public void writeAll(String string) {
        byte len = (byte) string.length();
        byte[] strToWriteBytes = string.getBytes(charset);
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        oStream.write(len);
        try {
            oStream.write(strToWriteBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] dataToWrite = oStream.toByteArray();
        try {
            writeSplitted(dataToWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            mfc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

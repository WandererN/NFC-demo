package com.app.jh.nfcdemo;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    Button readNFCButton, writeNFCButton, formatNFCButton, readNdefButton;
    EditText editNFCText;
    EditText dataNFC;
    NfcReadWriteManager activeManager;
    ForumType2Card forumType2Card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        activeManager = new NfcForumType2Manager();
        readNFCButton = (Button) findViewById(R.id.readBtn);
        readNdefButton = (Button) findViewById(R.id.readNdefBtn);
        writeNFCButton = (Button) findViewById(R.id.writeBtn);
        formatNFCButton = (Button) findViewById(R.id.formatBtn);
        editNFCText = (EditText) findViewById(R.id.nfcData);
        dataNFC = (EditText) findViewById(R.id.nfcDataText);

        readNFCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //editNFCText.setText(activeManager.readAll());
                dataNFC.setText(activeManager.readHexData());
            }
        });
        readNdefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NdefMessage message = activeManager.readNdef();
                if (message != null) {
                    byte[] payload = message.getRecords()[0].getPayload();
                    byte status = payload[0];
                    int enc = status & 0x80; // Bit mask 7th bit 1
                    String encString = null;
                    if (enc == 0)
                        encString = "UTF-8";
                    else
                        encString = "UTF-16";
                    int ianaLength = status & 0x3F;
                    try {
                        Log.d("DEBUG::",new String(payload, ianaLength + 1, payload.length - 1 - ianaLength, encString));
                        dataNFC.setText(new String(payload, ianaLength + 1, payload.length - 1 - ianaLength, encString));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        formatNFCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeManager.format();
            }
        });
        writeNFCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NdefRecord ndefRecord;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ndefRecord = NdefRecord.createTextRecord("en", editNFCText.getText().toString());
                    NdefMessage ndefMessage = new NdefMessage(ndefRecord);
                    activeManager.writeNdef(ndefMessage);
                }
            }
        });
        if ("android.nfc.action.TECH_DISCOVERED".equals(intent.getAction())) {
            try {
                activeManager.connect(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // forumType2Card = new ForumType2Card(nfcA);

        }
    }
}

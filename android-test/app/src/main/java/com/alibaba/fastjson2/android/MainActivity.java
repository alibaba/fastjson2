package com.alibaba.fastjson2.android;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.android.eishay.MediaContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    static final int SERDE_LOOP_COUNT = 10_000;
    static final int PARSE_LOOP_COUNT = 1000;

    Gson g = new Gson();
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CART_STR = readString("cart.json");
        HOMEPAGE_STR = readString("homepage.json");
        H5API_STR = readString("h5api.json");

        setContentView(R.layout.activity_main);
    }

    private String readString(String path) {
        StringBuffer buffer = new StringBuffer();
        AssetManager assets = getApplicationContext().getAssets();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(path), "utf-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            Log.d("fastjson", e.getMessage());
        }
        return buffer.toString();
    }

    private void appendInfo(String str) {
        TextView textView10 = findViewById(R.id.textView);
        String text = textView10.getText().toString();
        if (text.isEmpty()) {
            text = str;
        } else {
            text = text + "\n" + str;
        }
        textView10.setText(text);
    }


    public void fastjson1EishaySerde() {
        MediaContent mediaContent = JSON.parseObject(EISHAY_STR, MediaContent.class);
        long toStringTotal;
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
                com.alibaba.fastjson.JSON.toJSONString(mediaContent);
            }
            toStringTotal = System.currentTimeMillis() - start;
        }

        long parseObjectTotal;
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
                com.alibaba.fastjson.JSON.parseObject(EISHAY_STR, MediaContent.class);
            }
            parseObjectTotal = System.currentTimeMillis() - start;
        }

        appendInfo("fastjson1 serde : " + toStringTotal + ", " + parseObjectTotal);
    }


    public void fastjson2EishaySerde() {
        MediaContent mediaContent = JSON.parseObject(EISHAY_STR, MediaContent.class);

        long toStringTotal;
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
                JSON.toJSONString(mediaContent);
            }
            toStringTotal = System.currentTimeMillis() - start;
        }

        long parseObjectTotal;
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
                JSON.parseObject(EISHAY_STR, MediaContent.class);
            }
            parseObjectTotal = System.currentTimeMillis() - start;
        }

        appendInfo("fastjson2 serde : " + toStringTotal + ", " + parseObjectTotal);
    }


    public void gsonEishaySerde() {
        MediaContent mediaContent = JSON.parseObject(EISHAY_STR, MediaContent.class);

        long start = System.currentTimeMillis();
        for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
            g.toJson(mediaContent);
        }
        long toStringTotal = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
            g.fromJson(EISHAY_STR, MediaContent.class);
        }
        long parseObjectTotal = System.currentTimeMillis() - start;

        appendInfo("gson serde : " + toStringTotal + ", " + parseObjectTotal);
    }

    public void jacksonEishaySerde() {
        MediaContent mediaContent = JSON.parseObject(EISHAY_STR, MediaContent.class);

        try {
            long toStringTotal;
            {
                long start = System.currentTimeMillis();
                for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
                    objectMapper.writeValueAsString(mediaContent);
                }
                toStringTotal = System.currentTimeMillis() - start;
            }

            long parseObjectTotal;
            {
                long start = System.currentTimeMillis();
                for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
                    objectMapper.readValue(EISHAY_STR, MediaContent.class);
                }
                parseObjectTotal = System.currentTimeMillis() - start;
            }

            appendInfo("jackson serde : " + toStringTotal + ", " + parseObjectTotal);
        } catch (Exception e) {
            Log.d("fastjson", e.getMessage());
        }
    }

    public void fastjson1Serde(View view) {
        if (((RadioButton) findViewById(R.id.eishaySerde)).isChecked()) {
            fastjson1EishaySerde();
        }
    }

    public void fastjson2Serde(View view) {
        if (((RadioButton) findViewById(R.id.eishaySerde)).isChecked()) {
            fastjson2EishaySerde();
        }
    }

    public void gsonSerde(View view) {
        if (((RadioButton) findViewById(R.id.eishaySerde)).isChecked()) {
            gsonEishaySerde();
        }
    }

    public void jacksonSerde(View view) {
        if (((RadioButton) findViewById(R.id.eishaySerde)).isChecked()) {
            jacksonEishaySerde();
        }
    }

    public void fastjson1Parse(String str, String name) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < PARSE_LOOP_COUNT; i++) {
            com.alibaba.fastjson.JSON.parseObject(str);
        }
        long millis = System.currentTimeMillis() - start;
        appendInfo("fastjson1 parse " + name + " : " + millis);
    }

    public void fastjson2Parse(String str, String name) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < PARSE_LOOP_COUNT; i++) {
            JSON.parseObject(str);
        }
        long millis = System.currentTimeMillis() - start;
        appendInfo("fastjson2 parse " + name + " : " + millis);
    }

    public void orgjsonParse(String str, String name) {
        try {
            long start = System.currentTimeMillis();
            for (int i = 0; i < PARSE_LOOP_COUNT; i++) {
                new org.json.JSONObject(str);
            }
            long millis = System.currentTimeMillis() - start;
            appendInfo("orgjson parse " + name + " : " + millis);
        } catch (Exception e) {
            Log.d("fastjson", e.getMessage());
        }
    }

    public void fastjson1Parse(View view) {
        String str = null, name = null;
        if (((RadioButton) findViewById(R.id.eishayParse)).isChecked()) {
            str = EISHAY_STR;
            name = "eishay";
        } else if (((RadioButton) findViewById(R.id.cartParse)).isChecked()) {
            str = CART_STR;
            name = "cart";
        } else if (((RadioButton) findViewById(R.id.homepageParse)).isChecked()) {
            str = HOMEPAGE_STR;
            name = "homepage";
        } else if (((RadioButton) findViewById(R.id.h5apiParse)).isChecked()) {
            str = H5API_STR;
            name = "h5api";
        }
        if (str != null) {
            fastjson1Parse(str, name);
        }
    }

    public void fastjson2Parse(View view) {
        String str = null, name = null;
        if (((RadioButton) findViewById(R.id.eishayParse)).isChecked()) {
            str = EISHAY_STR;
            name = "eishay";
        } else if (((RadioButton) findViewById(R.id.cartParse)).isChecked()) {
            str = CART_STR;
            name = "cart";
        } else if (((RadioButton) findViewById(R.id.homepageParse)).isChecked()) {
            str = HOMEPAGE_STR;
            name = "homepage";
        } else if (((RadioButton) findViewById(R.id.h5apiParse)).isChecked()) {
            str = H5API_STR;
            name = "h5api";
        }
        if (str != null) {
            fastjson2Parse(str, name);
        }
    }

    public void orgjsonParse(View view) {
        String str = null, name = null;
        if (((RadioButton) findViewById(R.id.eishayParse)).isChecked()) {
            str = EISHAY_STR;
            name = "eishay";
        } else if (((RadioButton) findViewById(R.id.cartParse)).isChecked()) {
            str = CART_STR;
            name = "cart";
        } else if (((RadioButton) findViewById(R.id.homepageParse)).isChecked()) {
            str = HOMEPAGE_STR;
            name = "homepage";
        } else if (((RadioButton) findViewById(R.id.h5apiParse)).isChecked()) {
            str = H5API_STR;
            name = "h5api";
        }
        if (str != null) {
            orgjsonParse(str, name);
        }
    }

    public void clear(View view) {
        TextView textView10 = findViewById(R.id.textView);
        textView10.setText("");
    }

    static final String EISHAY_STR = "{\"images\": [{\n" +
            "      \"height\":768,\n" +
            "      \"size\":\"LARGE\",\n" +
            "      \"title\":\"Javaone Keynote\",\n" +
            "      \"uri\":\"http://javaone.com/keynote_large.jpg\",\n" +
            "      \"width\":1024\n" +
            "    }, {\n" +
            "      \"height\":240,\n" +
            "      \"size\":\"SMALL\",\n" +
            "      \"title\":\"Javaone Keynote\",\n" +
            "      \"uri\":\"http://javaone.com/keynote_small.jpg\",\n" +
            "      \"width\":320\n" +
            "    }\n" +
            "  ],\n" +
            "  \"media\": {\n" +
            "    \"bitrate\":262144,\n" +
            "    \"duration\":18000000,\n" +
            "    \"format\":\"video/mpg4\",\n" +
            "    \"height\":480,\n" +
            "    \"persons\": [\n" +
            "      \"Bill Gates\",\n" +
            "      \"Steve Jobs\"\n" +
            "    ],\n" +
            "    \"player\":\"JAVA\",\n" +
            "    \"size\":58982400,\n" +
            "    \"title\":\"Javaone Keynote\",\n" +
            "    \"uri\":\"http://javaone.com/keynote.mpg\",\n" +
            "    \"width\":640\n" +
            "  }\n" +
            "}";

    static String CART_STR;
    static String HOMEPAGE_STR;
    static String H5API_STR;
}
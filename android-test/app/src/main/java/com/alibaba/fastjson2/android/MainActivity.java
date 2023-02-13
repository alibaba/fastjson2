package com.alibaba.fastjson2.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.android.eishay.MediaContent;
import com.alibaba.fastjson2.android.databinding.ActivityMainBinding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {
    static final int SERDE_LOOP_COUNT = 10_000;
    static final int PARSE_LOOP_COUNT = 1000;

    static final String[] LIB_NAMES = {
            "fastjson2", "fastjson1", "gson", "jackson"
    };

    static final String[] TAG_NAMES = {
            "SERDE", "PARSE"
    };

    static final String[] ITEM_NAMES = {
            "eishay", "cart", "homepage", "h5 api"
    };

    private Gson gson;
    private ObjectMapper mapper;

    private final String[] texts = new String[ITEM_NAMES.length];
    private final StringBuilder result = new StringBuilder(128);
    private Map<Object, Map<Object, Map<Object, Runnable>>> tester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding inflate =
                ActivityMainBinding.inflate(
                        getLayoutInflater()
                );
        setContentView(inflate.getRoot());

        int white = getResources().getColor(
                R.color.white, null
        );

        CheckBox[] libBoxes = new CheckBox[LIB_NAMES.length];
        for (int i = 0; i < LIB_NAMES.length; i++) {
            CheckBox box = new CheckBox(this);
            libBoxes[i] = box;
            box.setChecked(i == 0);
            box.setTag(LIB_NAMES[i]);
            box.setText(LIB_NAMES[i]);
            box.setTextColor(white);
            inflate.libs.addView(box);
        }

        RadioButton[] tagRadios = new RadioButton[TAG_NAMES.length];
        for (int i = 0; i < TAG_NAMES.length; i++) {
            RadioButton radio = new RadioButton(this);
            tagRadios[i] = radio;
            radio.setTag(TAG_NAMES[i]);
            radio.setText(TAG_NAMES[i]);
            radio.setTextColor(white);
            inflate.tags.addView(radio);
        }

        RadioButton[] itemRadios = new RadioButton[ITEM_NAMES.length];
        for (int i = 0; i < ITEM_NAMES.length; i++) {
            RadioButton radio = new RadioButton(this);
            itemRadios[i] = radio;
            radio.setTag(ITEM_NAMES[i]);
            radio.setText(ITEM_NAMES[i]);
            radio.setTextColor(white);
            inflate.items.addView(radio);
        }

        byte[] buffer = new byte[2048];
        ByteArrayOutputStream stream = new ByteArrayOutputStream(5120);

        String[] paths = {
                "eishay.json", "cart.json", "homepage.json", "h5api.json"
        };
        for (int i = 0; i < texts.length; i++) {
            try (InputStream in = getResources().getAssets().open(paths[i])
            ) {
                int size;
                stream.reset();
                while ((size = in.read(buffer)) != -1) {
                    stream.write(buffer, 0, size);
                }
            } catch (Exception e) {
                Log.d("Error getting resource", e.getMessage());
            } finally {
                try {
                    texts[i] = stream.toString("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.d("Error getting resource", e.getMessage());
                }
            }
        }

        gson = new Gson();
        tester = new HashMap<>();
        mapper = new ObjectMapper();

        {
            Map<Object, Map<Object, Runnable>> tags = new HashMap<>();
            tester.put("fastjson2", tags);
            {
                Map<Object, Runnable> items = new HashMap<>();
                tags.put("SERDE", items);
                items.put("eishay", this::fastjson2EishaySerde);
            }
            {
                Map<Object, Runnable> items = new HashMap<>();
                tags.put("PARSE", items);
                for (int i = 0; i < ITEM_NAMES.length; i++) {
                    String text = texts[i];
                    String item = ITEM_NAMES[i];
                    items.put(item, () -> fastjson2Parse(text, item));
                }
            }
        }

        {
            Map<Object, Map<Object, Runnable>> tags = new HashMap<>();
            tester.put("fastjson1", tags);
            {
                Map<Object, Runnable> items = new HashMap<>();
                tags.put("SERDE", items);
                items.put("eishay", this::fastjson1EishaySerde);
            }
            {
                Map<Object, Runnable> items = new HashMap<>();
                tags.put("PARSE", items);
                for (int i = 0; i < ITEM_NAMES.length; i++) {
                    String text = texts[i];
                    String item = ITEM_NAMES[i];
                    items.put(item, () -> fastjson1Parse(text, item));
                }
            }
        }

        {
            Map<Object, Map<Object, Runnable>> tags = new HashMap<>();
            tester.put("gson", tags);
            {
                Map<Object, Runnable> items = new HashMap<>();
                tags.put("SERDE", items);
                items.put("eishay", this::gsonEishaySerde);
            }
        }

        {
            Map<Object, Map<Object, Runnable>> tags = new HashMap<>();
            tester.put("jackson", tags);
            {
                Map<Object, Runnable> items = new HashMap<>();
                tags.put("SERDE", items);
                items.put("eishay", this::jacksonEishaySerde);
            }
        }

        Runnable updater = () -> inflate.result.setText(result);
        ExecutorService executor = Executors.newCachedThreadPool();

        Consumer<Runnable> consumer =
                runnable -> executor.execute(() -> {
                    runnable.run();
                    runOnUiThread(updater);
                });
        Function<RadioButton[], Object> target = radios -> {
            for (RadioButton btn : radios) {
                if (btn.isChecked()) {
                    return btn.getTag();

                }
            }
            return null;
        };

        inflate.submit.setOnClickListener(v -> {
            final Object tag = target.apply(tagRadios);
            final Object item = target.apply(itemRadios);

            if (tag == null || item == null) {
                Toast.makeText(this, "Unselected", Toast.LENGTH_SHORT).show();
                return;
            }

            result.setLength(0);
            inflate.result.setText(
                    result.append(tag).append("  ").append(item).append("\n\n")
            );

            Toast.makeText(this, "Testing", Toast.LENGTH_SHORT).show();
            for (CheckBox box : libBoxes) {
                if (box.isChecked()) {
                    Optional.ofNullable(tester.get(box.getTag()))
                            .map((tags -> tags.get(tag))).map((items) -> items.get(item)).ifPresent(consumer);
                }
            }
        });
    }

    public void fastjson1EishaySerde() {
        MediaContent mediaContent = JSON.parseObject(texts[0], MediaContent.class);
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
                com.alibaba.fastjson.JSON.parseObject(texts[0], MediaContent.class);
            }
            parseObjectTotal = System.currentTimeMillis() - start;
        }

        synchronized (result) {
            result.append("fastjson1 serde : ").append(toStringTotal).append(", ").append(parseObjectTotal).append("\n");
        }
    }


    public void fastjson2EishaySerde() {
        MediaContent mediaContent = JSON.parseObject(texts[0], MediaContent.class);

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
                JSON.parseObject(texts[0], MediaContent.class);
            }
            parseObjectTotal = System.currentTimeMillis() - start;
        }

        synchronized (result) {
            result.append("fastjson2 serde : ").append(toStringTotal).append(", ").append(parseObjectTotal).append("\n");
        }
    }


    public void gsonEishaySerde() {
        MediaContent mediaContent = JSON.parseObject(texts[0], MediaContent.class);

        long start = System.currentTimeMillis();
        for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
            gson.toJson(mediaContent);
        }
        long toStringTotal = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
            gson.fromJson(texts[0], MediaContent.class);
        }
        long parseObjectTotal = System.currentTimeMillis() - start;

        synchronized (result) {
            result.append("gson serde : ").append(toStringTotal).append(", ").append(parseObjectTotal).append("\n");
        }
    }

    public void jacksonEishaySerde() {
        MediaContent mediaContent = JSON.parseObject(texts[0], MediaContent.class);

        try {
            long toStringTotal;
            {
                long start = System.currentTimeMillis();
                for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
                    mapper.writeValueAsString(mediaContent);
                }
                toStringTotal = System.currentTimeMillis() - start;
            }

            long parseObjectTotal;
            {
                long start = System.currentTimeMillis();
                for (int i = 0; i < SERDE_LOOP_COUNT; i++) {
                    mapper.readValue(texts[0], MediaContent.class);
                }
                parseObjectTotal = System.currentTimeMillis() - start;
            }

            synchronized (result) {
                result.append("jackson serde : ").append(toStringTotal).append(", ").append(parseObjectTotal).append("\n");
            }
        } catch (Exception e) {
            synchronized (result) {
                result.append("jackson error : ").append(e.getMessage()).append("\n");
            }
        }
    }

    public void fastjson1Parse(String str, String name) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < PARSE_LOOP_COUNT; i++) {
            com.alibaba.fastjson.JSON.parseObject(str);
        }
        long millis = System.currentTimeMillis() - start;
        synchronized (result) {
            result.append("fastjson1 parse ").append(name).append(" : ").append(millis).append("\n");
        }
    }

    public void fastjson2Parse(String str, String name) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < PARSE_LOOP_COUNT; i++) {
            JSON.parseObject(str);
        }
        long millis = System.currentTimeMillis() - start;
        synchronized (result) {
            result.append("fastjson2 parse ").append(name).append(" : ").append(millis).append("\n");
        }
    }

    public void orgjsonParse(String str, String name) {
        try {
            long start = System.currentTimeMillis();
            for (int i = 0; i < PARSE_LOOP_COUNT; i++) {
                new org.json.JSONObject(str);
            }
            long millis = System.currentTimeMillis() - start;
            synchronized (result) {
                result.append("orgjson parse ").append(name).append(" : ").append(millis).append("\n");
            }
        } catch (Exception e) {
            synchronized (result) {
                result.append("orgjson error : ").append(e.getMessage()).append("\n");
            }
        }
    }
}

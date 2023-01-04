package com.alibaba.fastjson2.benchmark.eishay.mixin;

import com.alibaba.fastjson2.annotation.JSONAutowired;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.reader.ObjectReaders.*;

@JSONAutowired
public class MediaMixin {
    public static final ObjectWriter<Media> objectWriter = ObjectWriters.objectWriter(
            Media.class,
            ObjectWriters.fieldWriter("bitrate", (Media e) -> e.bitrate),
            ObjectWriters.fieldWriter("duration", (Media e) -> e.duration),
            ObjectWriters.fieldWriter("format", (Media e) -> e.format),
            ObjectWriters.fieldWriter("height", (Media e) -> e.height),
            ObjectWriters.fieldWriterList("persons", String.class, (Media e) -> e.persons),
            ObjectWriters.fieldWriter("player", Media.Player.class, (Media e) -> e.player),
            ObjectWriters.fieldWriter("size", (Media e) -> e.size),
            ObjectWriters.fieldWriter("title", (Media e) -> e.title),
            ObjectWriters.fieldWriter("uri", (Media e) -> e.uri),
            ObjectWriters.fieldWriter("width", (Media e) -> e.width),
            ObjectWriters.fieldWriter("copyright", (Media e) -> e.copyright)
    );

    public static final ObjectReader<Media> objectReader = ObjectReaders.of(
            Media::new,
            fieldReaderInt("bitrate", (Media o, int v) -> o.bitrate = v),
            fieldReaderLong("duration", (Media o, long v) -> o.duration = v),
            fieldReaderString("format", (Media o, String v) -> o.format = v),
            fieldReaderInt("height", (Media o, int v) -> o.height = v),
            fieldReaderList("persons", String.class, ArrayList::new, (Media o, List<String> v) -> o.persons = v),
            fieldReader("player", Media.Player.class, (Media o, Media.Player v) -> o.player = v),
            fieldReaderLong("size", (Media o, long v) -> o.size = v),
            fieldReaderString("title", (Media o, String v) -> o.title = v),
            fieldReaderString("uri", (Media o, String v) -> o.uri = v),
            fieldReaderInt("width", (Media o, int v) -> o.width = v),
            fieldReaderString("copyright", (Media o, String v) -> o.copyright = v)
    );
}

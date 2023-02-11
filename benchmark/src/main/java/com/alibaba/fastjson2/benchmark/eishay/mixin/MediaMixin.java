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
            ObjectWriters.fieldWriter("bitrate", (Media e) -> e.getBitrate()),
            ObjectWriters.fieldWriter("duration", (Media e) -> e.getDuration()),
            ObjectWriters.fieldWriter("format", (Media e) -> e.getFormat()),
            ObjectWriters.fieldWriter("height", (Media e) -> e.getHeight()),
            ObjectWriters.fieldWriterList("persons", String.class, (Media e) -> e.getPersons()),
            ObjectWriters.fieldWriter("player", Media.Player.class, (Media e) -> e.getPlayer()),
            ObjectWriters.fieldWriter("size", (Media e) -> e.getSize()),
            ObjectWriters.fieldWriter("title", (Media e) -> e.getTitle()),
            ObjectWriters.fieldWriter("uri", (Media e) -> e.getUri()),
            ObjectWriters.fieldWriter("width", (Media e) -> e.getWidth()),
            ObjectWriters.fieldWriter("copyright", (Media e) -> e.getCopyright())
    );

    public static final ObjectReader<Media> objectReader = ObjectReaders.of(
            Media::new,
            fieldReaderInt("bitrate", Media::setBitrate),
            fieldReaderLong("duration", Media::setDuration),
            fieldReaderString("format", Media::setFormat),
            fieldReaderInt("height", Media::setHeight),
            fieldReaderList("persons", String.class, ArrayList::new, (Media o, List<String> v) -> o.setPersons(v)),
            fieldReader("player", Media.Player.class, (Media o, Media.Player v) -> o.setPlayer(v)),
            fieldReaderLong("size", Media::setSize),
            fieldReaderString("title", Media::setTitle),
            fieldReaderString("uri", Media::setUri),
            fieldReaderInt("width", Media::setWidth),
            fieldReaderString("copyright", Media::setCopyright)
    );
}

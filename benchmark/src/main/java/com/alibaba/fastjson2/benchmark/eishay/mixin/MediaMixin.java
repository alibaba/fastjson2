package com.alibaba.fastjson2.benchmark.eishay.mixin;

import com.alibaba.fastjson2.annotation.JSONAutowired;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import static com.alibaba.fastjson2.reader.ObjectReaders.*;

@JSONAutowired
public class MediaMixin {
    public static final ObjectWriter<Media> objectWriter = ObjectWriters.objectWriter(
            Media.class,
            ObjectWriters.fieldWriter("bitrate", Media::getBitrate),
            ObjectWriters.fieldWriter("duration", Media::getDuration),
            ObjectWriters.fieldWriter("format", Media::getFormat),
            ObjectWriters.fieldWriter("height", Media::getHeight),
            ObjectWriters.fieldWriterListString("persons", Media::getPersons),
            ObjectWriters.fieldWriter("player", Media.Player.class, Media::getPlayer),
            ObjectWriters.fieldWriter("size", Media::getSize),
            ObjectWriters.fieldWriter("title", Media::getTitle),
            ObjectWriters.fieldWriter("uri", Media::getUri),
            ObjectWriters.fieldWriter("width", Media::getWidth),
            ObjectWriters.fieldWriter("copyright", Media::getCopyright)
    );

    public static final ObjectReader<Media> objectReader = ObjectReaders.of(
            Media::new,
            fieldReaderInt("bitrate", Media::setBitrate),
            fieldReaderLong("duration", Media::setDuration),
            fieldReaderString("format", Media::setFormat),
            fieldReaderInt("height", Media::setHeight),
            fieldReaderListStr("persons", Media::setPersons),
            fieldReader("player", Media.Player.class, Media::setPlayer),
            fieldReaderLong("size", Media::setSize),
            fieldReaderString("title", Media::setTitle),
            fieldReaderString("uri", Media::setUri),
            fieldReaderInt("width", Media::setWidth),
            fieldReaderString("copyright", Media::setCopyright)
    );
}

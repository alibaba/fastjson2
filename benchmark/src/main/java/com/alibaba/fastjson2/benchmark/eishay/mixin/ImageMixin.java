package com.alibaba.fastjson2.benchmark.eishay.mixin;

import com.alibaba.fastjson2.annotation.JSONAutowired;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import static com.alibaba.fastjson2.reader.ObjectReaders.fieldReader;
import static com.alibaba.fastjson2.reader.ObjectReaders.fieldReaderInt;

@JSONAutowired
public class ImageMixin {
    public static final ObjectWriter<Image> objectWriter = ObjectWriters.objectWriter(
            Image.class,
            ObjectWriters.fieldWriter("height", Image::getHeight),
            ObjectWriters.fieldWriter("size", Image.Size.class, (Image e) -> e.getSize()),
            ObjectWriters.fieldWriter("title", Image::getTitle),
            ObjectWriters.fieldWriter("uri", Image::getUri),
            ObjectWriters.fieldWriter("width", Image::getWidth)
    );

    public static final ObjectReader<Image> objectReader = ObjectReaders.of(
            Image::new,
            fieldReaderInt("height", Image::setHeight),
            fieldReader("size", Image.Size.class, (Image o, Image.Size v) -> o.setSize(v)),
            fieldReader("title", String.class, (Image o, String v) -> o.setTitle(v)),
            fieldReader("uri", String.class, (Image o, String v) -> o.setUri(v)),
            fieldReaderInt("width", Image::setWidth)
    );
}

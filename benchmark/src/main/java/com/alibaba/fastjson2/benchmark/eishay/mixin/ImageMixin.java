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
            ObjectWriters.fieldWriter("height", (Image e) -> e.height),
            ObjectWriters.fieldWriter("size", Image.Size.class, (Image e) -> e.size),
            ObjectWriters.fieldWriter("title", (Image e) -> e.title),
            ObjectWriters.fieldWriter("uri", (Image e) -> e.uri),
            ObjectWriters.fieldWriter("width", (Image e) -> e.width)
    );

    public static final ObjectReader<Image> objectReader = ObjectReaders.of(
            Image::new,
            fieldReaderInt("height", (Image o, int v) -> o.height = v),
            fieldReader("size", Image.Size.class, (Image o, Image.Size v) -> o.size = v),
            fieldReader("title", String.class, (Image o, String v) -> o.title = v),
            fieldReader("uri", String.class, (Image o, String v) -> o.uri = v),
            fieldReaderInt("width", (Image o, int v) -> o.width = v)
    );
}

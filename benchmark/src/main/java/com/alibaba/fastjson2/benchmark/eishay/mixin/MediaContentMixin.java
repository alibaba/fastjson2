package com.alibaba.fastjson2.benchmark.eishay.mixin;

import com.alibaba.fastjson2.annotation.JSONAutowired;
import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.reader.ObjectReaders.*;

@JSONAutowired
public class MediaContentMixin {
    public static final ObjectWriter<MediaContent> objectWriter = ObjectWriters.objectWriter(
            MediaContent.class,
            ObjectWriters.fieldWriter("media", Media.class, (MediaContent e) -> e.media),
            ObjectWriters.fieldWriterList("images", Image.class, (MediaContent e) -> e.images)
    );

    public static final ObjectReader<MediaContent> objectReader = ObjectReaders.of(
            MediaContent::new,
            fieldReader("media", Media.class, (MediaContent o, Media v) -> o.media = v),
            fieldReaderList("images", Image.class, ArrayList::new, (MediaContent o, List<Image> v) -> o.images = v)
    );
}

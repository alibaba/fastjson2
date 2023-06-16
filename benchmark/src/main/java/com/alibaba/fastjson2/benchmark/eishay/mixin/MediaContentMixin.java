package com.alibaba.fastjson2.benchmark.eishay.mixin;

import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.util.ArrayList;

import static com.alibaba.fastjson2.reader.ObjectReaders.*;

public class MediaContentMixin {
    public static final ObjectWriter<MediaContent> objectWriter = ObjectWriters.objectWriter(
            MediaContent.class,
            ObjectWriters.fieldWriter("media", Media.class, MediaContent::getMedia),
            ObjectWriters.fieldWriterList("images", Image.class, MediaContent::getImages)
    );

    public static final ObjectReader<MediaContent> objectReader = ObjectReaders.of(
            MediaContent::new,
            fieldReader("media", Media.class, MediaContent::setMedia),
            fieldReaderList("images", Image.class, ArrayList::new, MediaContent::setImages)
    );
}

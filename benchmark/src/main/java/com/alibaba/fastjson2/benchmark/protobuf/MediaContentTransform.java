package com.alibaba.fastjson2.benchmark.protobuf;

import com.alibaba.fastjson2.benchmark.eishay.vo.Image;
import com.alibaba.fastjson2.benchmark.eishay.vo.Media;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;

import java.util.ArrayList;
import java.util.List;

public class MediaContentTransform {
    public static MediaContent reverse(MediaContentHolder.MediaContent mc) {
        List<Image> images = new ArrayList<Image>(mc.getImageCount());

        for (MediaContentHolder.Image image : mc.getImageList()) {
            images.add(reverseImage(image));
        }

        return new MediaContent(reverseMedia(mc.getMedia()), images);
    }

    public static Media reverseMedia(MediaContentHolder.Media media) {
        return new Media(
                media.getUri(),
                media.hasTitle() ? media.getTitle() : null,
                media.getWidth(),
                media.getHeight(),
                media.getFormat(),
                media.getDuration(),
                media.getSize(),
                media.hasBitrate() ? media.getBitrate() : 0,
                new ArrayList<String>(media.getPersonList()),
                reversePlayer(media.getPlayer()),
                media.hasCopyright() ? media.getCopyright() : null
        );
    }

    public static Image reverseImage(MediaContentHolder.Image image)
    {
        return new Image(
                image.getUri(),
                image.getTitle(),
                image.getWidth(),
                image.getHeight(),
                reverseSize(image.getSize()));
    }

    public static Image.Size reverseSize(MediaContentHolder.Image.Size s)
    {
        switch (s) {
            case SMALL: return Image.Size.SMALL;
            case LARGE: return Image.Size.LARGE;
            default:
                throw new AssertionError("invalid case: " + s);
        }
    }

    public static Media.Player reversePlayer(MediaContentHolder.Media.Player p) {
        switch (p) {
            case JAVA:
                return Media.Player.JAVA;
            case FLASH:
                return Media.Player.FLASH;
            default:
                throw new AssertionError("invalid case: " + p);
        }
    }

    public static MediaContentHolder.MediaContent forward(MediaContent mc) {
        MediaContentHolder.MediaContent.Builder cb = MediaContentHolder.MediaContent.newBuilder();
        cb.setMedia(forwardMedia(mc.getMedia()));
        for (Image image : mc.getImages()) {
            cb.addImage(forwardImage(image));
        }

        return cb.build();
    }

    public static MediaContentHolder.Media forwardMedia(Media media) {
        MediaContentHolder.Media.Builder mb = MediaContentHolder.Media.newBuilder();
        mb.setUri(media.getUri());
        String title = media.getTitle();
        if (title != null) {
            mb.setTitle(title);
        }
        mb.setWidth(media.getWidth());
        mb.setHeight(media.getHeight());
        mb.setFormat(media.getFormat());
        mb.setDuration(media.getDuration());
        mb.setSize(media.getSize());
        mb.setBitrate(media.getBitrate());
        for (String person : media.getPersons()) {
            mb.addPerson(person);
        }
        mb.setPlayer(forwardPlayer(media.getPlayer()));
        String copyright = media.getCopyright();
        if (copyright != null) {
            mb.setCopyright(copyright);
        }

        return mb.build();
    }

    public static MediaContentHolder.Media.Player forwardPlayer(Media.Player p) {
        switch (p) {
            case JAVA:
                return MediaContentHolder.Media.Player.JAVA;
            case FLASH:
                return MediaContentHolder.Media.Player.FLASH;
            default:
                throw new AssertionError("invalid case: " + p);
        }
    }

    public static MediaContentHolder.Image forwardImage(Image image) {
        MediaContentHolder.Image.Builder ib = MediaContentHolder.Image.newBuilder();
        ib.setUri(image.getUri());
        String title = image.getTitle();
        if (title != null) {
            ib.setTitle(title);
        }
        ib.setWidth(image.getWidth());
        ib.setHeight(image.getHeight());
        ib.setSize(forwardSize(image.getSize()));
        return ib.build();
    }

    public static MediaContentHolder.Image.Size forwardSize(Image.Size s) {
        switch (s) {
            case SMALL:
                return MediaContentHolder.Image.Size.SMALL;
            case LARGE:
                return MediaContentHolder.Image.Size.LARGE;
            default:
                throw new AssertionError("invalid case: " + s);
        }
    }
}

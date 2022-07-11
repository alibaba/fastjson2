package com.alibaba.fastjson2.benchmark.eishay.vo;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("serial")
public class Media
        implements java.io.Serializable {
    public enum Player {
        JAVA, FLASH
    }

    public int bitrate;   // Can be unset.
    @JSONField(serialize = false, deserialize = false)
    public boolean hasBitrate;

    public long duration;
    public String format;
    public int height;
    public List<String> persons;
    public Player player;
    public long size;
    public String title;     // Can be unset.
    public String uri;
    public int width;

    // msgpack requires this
    public String copyright; // Can be unset.

    public Media() {
    }

    public Media(String uri, String title, int width, int height, String format, long duration, long size, int bitrate,
                 boolean hasBitrate, List<String> persons, Player player, String copyright) {
        this.uri = uri;
        this.title = title;
        this.width = width;
        this.height = height;
        this.format = format;
        this.duration = duration;
        this.size = size;
        this.bitrate = bitrate;
        this.hasBitrate = hasBitrate;
        this.persons = persons;
        this.player = player;
        this.copyright = copyright;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public List<String> getPersons() {
        return persons;
    }

    public void setPersons(List<String> persons) {
        this.persons = persons;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Media media = (Media) o;
        return bitrate == media.bitrate && hasBitrate == media.hasBitrate && duration == media.duration && height == media.height && size == media.size && width == media.width && Objects.equals(format, media.format) && Objects.equals(persons, media.persons) && player == media.player && Objects.equals(title, media.title) && Objects.equals(uri, media.uri) && Objects.equals(copyright, media.copyright);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitrate, hasBitrate, duration, format, height, persons, player, size, title, uri, width, copyright);
    }
}

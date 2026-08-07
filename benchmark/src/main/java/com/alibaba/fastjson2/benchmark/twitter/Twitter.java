package com.alibaba.fastjson2.benchmark.twitter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Twitter {
    public String text;
    public Boolean truncated;
    @JsonProperty("in_reply_to_user_id")
    public String inReplyToUserId;
    @JsonProperty("in_reply_to_status_id")
    public String inReplyToStatusId;
    public Boolean favorited;
    public String source;
    @JsonProperty("in_reply_to_screen_name")
    public String inReplyToScreenName;
    @JsonProperty("in_reply_to_status_id_str")
    public String inReplyToStatusIdStr;
    @JsonProperty("id_str")
    public String idStr;
    public Entities entities;
    public String contributors;
    public Boolean retweeted;
    @JsonProperty("in_reply_to_user_id_str")
    public String inReplyToUserIdStr;
    public String place;
    @JsonProperty("retweet_count")
    public Integer retweetCount;
    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("retweeted_status")
    public RetweetedStatus retweetedStatus;
    public User user;
    public Long id;
    public String coordinates;
    public String geo;

    public static class UserMention {
        public List<Integer> indices;
        @JsonProperty("screen_name")
        public String screenName;
        @JsonProperty("id_str")
        public String idStr;
        public String name;
        public Integer id;
    }

    public static class Entities {
        @JsonProperty("user_mentions")
        public List<UserMention> userMentions;
        public List urls;
        public List hashtags;
    }

    public static class Hashtag {
        public String text;
        public List<Integer> indices;
    }

    public static class User {
        public String notifications;
        @JsonProperty("profile_use_background_image")
        public Boolean profileUseBackgroundImage;
        @JsonProperty("statuses_count")
        public Integer statusesCount;
        @JsonProperty("profile_background_color")
        public String profileBackgroundColor;
        @JsonProperty("followers_count")
        public Integer followersCount;
        @JsonProperty("profile_image_url")
        public String profileImageUrl;
        @JsonProperty("listed_count")
        public Integer listedCount;
        @JsonProperty("profile_background_image_url")
        public String profileBackgroundImageUrl;
        public String description;
        @JsonProperty("screen_name")
        public String screenName;
        @JsonProperty("default_profile")
        public Boolean defaultProfile;
        public Boolean verified;
        @JsonProperty("time_zone")
        public String timeZone;
        @JsonProperty("profile_text_color")
        public String profileTextColor;
        @JsonProperty("is_translator")
        public Boolean isTranslator;
        @JsonProperty("profile_sidebar_fill_color")
        public String profileSidebarFillColor;
        public String location;
        @JsonProperty("id_str")
        public String idStr;
        @JsonProperty("default_profile_image")
        public Boolean defaultProfileImage;
        @JsonProperty("profile_background_tile")
        public Boolean profileBackgroundTile;
        public String lang;
        @JsonProperty("friends_count")
        public Integer friendsCount;
        @JsonProperty("protected")
        public Boolean isProtected;
        @JsonProperty("favourites_count")
        public Integer favouritesCount;
        @JsonProperty("created_at")
        public String createdAt;
        @JsonProperty("profile_link_color")
        public String profileLinkColor;
        public String name;
        @JsonProperty("show_all_inline_media")
        public Boolean showAllInlineMedia;
        @JsonProperty("follow_request_sent")
        public String followRequestSent;
        @JsonProperty("geo_enabled")
        public Boolean geoEnabled;
        @JsonProperty("profile_sidebar_border_color")
        public String profileSidebarBorderColor;
        public String url;
        public Integer id;
        @JsonProperty("contributors_enabled")
        public Boolean contributorsEnabled;
        public String following;
        @JsonProperty("utc_offset")
        public String utcOffset;
    }

    public static class RetweetedStatus {
        public String text;
        public Boolean truncated;
        @JsonProperty("in_reply_to_user_id")
        public String inReplyToUserId;
        @JsonProperty("in_reply_to_status_id")
        public String inReplyToStatusId;
        public Boolean favorited;
        public String source;
        @JsonProperty("in_reply_to_screen_name")
        public String inReplyToScreenName;
        @JsonProperty("in_reply_to_status_id_str")
        public String inReplyToStatusIdStr;
        @JsonProperty("id_str")
        public String idStr;
        public Entities entities;
        public String contributors;
        public Boolean retweeted;
        @JsonProperty("in_reply_to_user_id_str")
        public String inReplyToUserIdStr;
        public String place;
        @JsonProperty("retweet_count")
        public Integer retweetCount;
        @JsonProperty("created_at")
        public String createdAt;
        public User user;
        public Long id;
        public String coordinates;
        public String geo;
    }
}

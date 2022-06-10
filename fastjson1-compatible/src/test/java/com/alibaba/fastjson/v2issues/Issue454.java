package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue454 {
    @Test
    public void test() {
        String text = "[{\"child_count\":8,\"del_state\":0,\"display_level\":2,\"id\":54,\"item_index\":46,\"parent_id\":1,\"path\":\"/1/54/\",\"phase_id\":1,\"title\":\"八年级上\",\"update_time\":1596423978669}]";
        List<User> list1 = JSON.parseArray(text, User.class);
        if (list1 != null && list1.size() > 0) {
            User user = list1.get(0);
            String str = JSON.toJSONString(user);
            assertEquals("{\"child_count\":8,\"del_state\":0,\"display_level\":2,\"id\":54,\"item_index\":46,\"parent_id\":1,\"path\":\"/1/54/\",\"phase_id\":1,\"title\":\"八年级上\",\"update_time\":1596423978669}", str);
        }
    }

    @Test
    public void test1() {
        String text = "[{\"child_count\":8,\"del_state\":0,\"display_level\":2,\"id\":54,\"item_index\":46,\"parent_id\":1,\"path\":\"/1/54/\",\"phase_id\":1,\"title\":\"八年级上\",\"update_time\":1596423978669}]";
        List<User2> list2 = JSON.parseArray(text, User2.class);
        if (list2 != null && list2.size() > 0) {
            User2 user2 = list2.get(0);
            String str = JSON.toJSONString(user2);
            assertEquals("{\"child_count\":8,\"del_state\":0,\"display_level\":2,\"id\":54,\"item_index\":46,\"parent_id\":1,\"path\":\"/1/54/\",\"phase_id\":1,\"title\":\"八年级上\",\"update_time\":1596423978669}", str);
        }
    }

    public class User
            implements Serializable {
        @JSONField(name = "id")
        private int id;
        @JSONField(name = "title")
        private String title;
        @JSONField(name = "parent_id")
        private Integer parentId;
        @JSONField(name = "display_level")
        private Integer displayLevel;
        @JSONField(name = "item_index")
        private Integer itemIndex;
        @JSONField(name = "child_count")
        private Integer childCount;
        @JSONField(name = "phase_id")
        private Integer phaseId;
        @JSONField(name = "del_state")
        private int delState;
        @JSONField(name = "path")
        private String path;
        @JSONField(name = "update_time")
        private Long updateTime;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }

        public Integer getDisplayLevel() {
            return displayLevel;
        }

        public void setDisplayLevel(Integer displayLevel) {
            this.displayLevel = displayLevel;
        }

        public Integer getItemIndex() {
            return itemIndex;
        }

        public void setItemIndex(Integer itemIndex) {
            this.itemIndex = itemIndex;
        }

        public Integer getChildCount() {
            return childCount;
        }

        public void setChildCount(Integer childCount) {
            this.childCount = childCount;
        }

        public Integer getPhaseId() {
            return phaseId;
        }

        public void setPhaseId(Integer phaseId) {
            this.phaseId = phaseId;
        }

        public int getDelState() {
            return delState;
        }

        public void setDelState(int delState) {
            this.delState = delState;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }
    }

    public class User2
            implements Serializable {
        private int id;
        private String title;
        private Integer parentId;
        private Integer displayLevel;
        private Integer itemIndex;
        private Integer childCount;
        private Integer phaseId;
        private int delState;
        private String path;
        private Long updateTime;

        @JSONField(name = "id")
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @JSONField(name = "title")
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @JSONField(name = "parent_id")
        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }

        @JSONField(name = "display_level")
        public Integer getDisplayLevel() {
            return displayLevel;
        }

        public void setDisplayLevel(Integer displayLevel) {
            this.displayLevel = displayLevel;
        }

        @JSONField(name = "item_index")
        public Integer getItemIndex() {
            return itemIndex;
        }

        public void setItemIndex(Integer itemIndex) {
            this.itemIndex = itemIndex;
        }

        @JSONField(name = "child_count")
        public Integer getChildCount() {
            return childCount;
        }

        public void setChildCount(Integer childCount) {
            this.childCount = childCount;
        }

        @JSONField(name = "phase_id")
        public Integer getPhaseId() {
            return phaseId;
        }

        public void setPhaseId(Integer phaseId) {
            this.phaseId = phaseId;
        }

        @JSONField(name = "del_state")
        public int getDelState() {
            return delState;
        }

        public void setDelState(int delState) {
            this.delState = delState;
        }

        @JSONField(name = "path")
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @JSONField(name = "update_time")
        public Long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }
    }
}

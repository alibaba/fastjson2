package com.alibaba.fastjson2.issues.ae;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KejinjinTest {
    @Test
    public void test() {
        Component component1 = new DataSetComponent();
        Component component2 = new DataSetComponent() {
            {
                // empty
            }
        };
        String s1 = JSON.toJSONString(component1);
        assertEquals("{\"type\":\"DataSetComponent\"}", s1);
        String s2 = JSON.toJSONString(component2);
        assertEquals("{\"type\":\"DataSetComponent\"}", s2);
    }

    @EqualsAndHashCode
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TextComponent.class, name = "TextComponent"),
            @JsonSubTypes.Type(value = LinkComponent.class, name = "LinkComponent"),
            @JsonSubTypes.Type(value = ImageComponent.class, name = "ImageComponent"),
            @JsonSubTypes.Type(value = TableComponent.class, name = "TableComponent"),
            @JsonSubTypes.Type(value = DataSetComponent.class, name = "DataSetComponent"),
            @JsonSubTypes.Type(value = TabComponent.class, name = "TabComponent")
    })
    @JSONType(seeAlso = {
            TextComponent.class,
            LinkComponent.class,
            ImageComponent.class,
            TableComponent.class,
            DataSetComponent.class,
            TabComponent.class},
            typeKey = "type"
    )
    public abstract static class Component {
        /**
         * 组件标题
         */
        protected String caption;

        static final String COMPONENT_TYPE_FIELD = "type";

        /**
         * @return 组件名称
         */
        @JsonProperty(COMPONENT_TYPE_FIELD)
        @JSONField(name = COMPONENT_TYPE_FIELD)
        public String name() {
            return this.getClass().getSimpleName();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public abstract static class CombinedComponent
            extends Component {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @JSONType(typeName = "DataSetComponent")
    public static class DataSetComponent
            extends CombinedComponent {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public abstract class PrimitiveComponent
            extends Component {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @JSONType(typeName = "TextComponent")
    public class TextComponent
            extends PrimitiveComponent {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @JSONType(typeName = "TabComponent")
    public class TabComponent
            extends CombinedComponent {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @JSONType(typeName = "LinkComponent")
    public class LinkComponent
            extends PrimitiveComponent {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @JSONType(typeName = "ImageComponent")
    public class ImageComponent
            extends PrimitiveComponent {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @JSONType(typeName = "TableComponent")
    public class TableComponent
            extends CombinedComponent {
    }
}

package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest51 {
    @Test
    public void test() {
        String data = "{\n" +
                "  \"expression\": {\n" +
                "    \"type\": \"logicNot\",\n" +
                "    \"expression\": {\n" +
                "      \"type\": \"logicTrue\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        RuleCompositionInstance condition = JSON.parseObject(data, RuleCompositionInstance.class);
        assertEquals(NotLogicalExpression.class, condition.expression.getClass());
    }

    public abstract static class OneLogicalExpression
            extends LogicalExpression {
    }

    public abstract static class LogicalExpression
            implements IExpression {
    }

    public static class RuleCompositionInstance {
        private IExpression expression;

        public IExpression getExpression() {
            return expression;
        }

        public void setExpression(IExpression expression) {
            this.expression = expression;
        }
    }

    @JSONType(typeName = "logicNot")
    public class NotLogicalExpression
            extends OneLogicalExpression {
        private IExpression expression;

        public IExpression getExpression() {
            return expression;
        }

        public void setExpression(IExpression expression) {
            this.expression = expression;
        }

        @Override
        public ExpressionEnum type() {
            return ExpressionEnum.logicNot;
        }
    }

    @JSONType(typeKey = "type", seeAlso = {TrueLogicalExpression.class, NotLogicalExpression.class})
    public static interface IExpression {
        ExpressionEnum type();

        default List<IExpression> getSubExpressions() {
            return new ArrayList<>();
        }
    }

    @JSONType(typeName = "logicTrue")
    public static class TrueLogicalExpression
            extends OneLogicalExpression {
        @Override
        public ExpressionEnum type() {
            return ExpressionEnum.logicTrue;
        }
    }

    public enum ExpressionEnum {
        logicAnd,
        logicNot,
        logicOr,
        logicTrue,
        logicFalse,
        rule
    }
}

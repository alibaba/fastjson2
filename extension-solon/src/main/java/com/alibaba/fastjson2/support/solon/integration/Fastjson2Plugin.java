package com.alibaba.fastjson2.support.solon.integration;

import com.alibaba.fastjson2.support.solon.Fastjson2ActionExecutor;
import com.alibaba.fastjson2.support.solon.Fastjson2RenderFactory;
import com.alibaba.fastjson2.support.solon.Fastjson2RenderTypedFactory;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.serialization.prop.JsonProps;

/**
 * Fastjson2 for solon extension integration plugin
 *
 * @author noear
 * */
public class Fastjson2Plugin implements Plugin {

    @Override
    public void start(AppContext context) {
        JsonProps jsonProps = JsonProps.create(context);
        if(jsonProps == null){
            jsonProps = new JsonProps();
            jsonProps.dateAsTicks = true;
        }

        //::renderFactory
        Fastjson2RenderFactory renderFactory = new Fastjson2RenderFactory(jsonProps); //绑定属性
        context.wrapAndPut(Fastjson2RenderFactory.class, renderFactory); //推入容器，用于扩展
        Solon.app().renderManager().register(renderFactory);

        //::renderTypedFactory
        Fastjson2RenderTypedFactory renderTypedFactory = new Fastjson2RenderTypedFactory();
        context.wrapAndPut(Fastjson2RenderTypedFactory.class, renderTypedFactory); //推入容器，用于扩展
        Solon.app().renderManager().register(renderTypedFactory);

        //::actionExecutor
        Fastjson2ActionExecutor actionExecutor = new Fastjson2ActionExecutor(); //支持 json 内容类型执行
        context.wrapAndPut(Fastjson2ActionExecutor.class, actionExecutor); //推入容器，用于扩展
        Solon.app().chainManager().addExecuteHandler(actionExecutor);
    }
}

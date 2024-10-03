package com.alibaba.fastjson2.example.solontest.controller;

import org.noear.solon.annotation.Component;
import org.noear.solon.core.exception.StatusException;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.noear.solon.core.handle.Result;

/**
 * @author noear
 * @since 2024-10-01
 */
@Component
public class DemoErrorFilter implements Filter {
    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        try {
            chain.doFilter(ctx);
        } catch (StatusException e) {
            ctx.render(Result.failure(e.getCode(), e.getMessage()));
        } catch (Throwable e) {
            ctx.render(Result.failure(500));
        }
    }
}

package com.bfy.movieplayerplus.model.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 网络请求行为注解（可扩展）
 * 该注解主要保存此次请求的一些相关信息，比如请求行为代号或者请求错误代号等
 * @author ouyangjinfu
 * @data on 2016/5/27.
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestAction {
    String action() default "";//请求行为代号
    String noNetWorkStatus() default "";//无网络状态代号
    String noNetWorkPrompt() default "";//无网络提示语
}

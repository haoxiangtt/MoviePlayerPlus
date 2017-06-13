package com.bfy.movieplayerplus.model.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 过滤器注解，用于标记验证某些特殊操作
 * 比如需要验证token,在需要验证的方法上加入该注解，赋值type=Type.TOKEN
 * 若token与网络都要验证，则赋值type=Type.TOKEN | Type=NETWORK
 * @author ouyangjinfu
 * @data on 2016/5/27.
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateFilter{
    /**
     * 验证类型
     * 利用二进制位数记录各种验证类型，例如验证token为1，验证网络就为10，
     * 以此类推其他类型就为100、1000、10000等
     */
    public class Type{
        public static final int NETWORK = 1;//验证网络(1)
    }

    public int type() default 0;

}



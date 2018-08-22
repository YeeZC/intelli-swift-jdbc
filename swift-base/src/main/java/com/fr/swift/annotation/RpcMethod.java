package com.fr.swift.annotation;

import com.fr.third.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class created on 2018/6/12
 *
 * @author Lucifer
 * @description Allow method in allowed services to call RPC
 * @since Advanced FineBI 5.0
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcMethod {
    String methodName();
}

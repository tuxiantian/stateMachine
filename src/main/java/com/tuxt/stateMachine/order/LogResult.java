package com.tuxt.stateMachine.order;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LogResult {
    /**
     * 执行的业务key
     *
     * @return String
     */
    String key();
}

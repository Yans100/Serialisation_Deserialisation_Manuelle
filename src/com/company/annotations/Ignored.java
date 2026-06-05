package com.company.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Ignored {
	boolean Ignore() default false;
}

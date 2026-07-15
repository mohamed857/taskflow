package com.taskflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // معناها دي هتتحط فوق الدوال بس
@Retention(RetentionPolicy.RUNTIME) // معناها تشتغل وقت الـ Run
public @interface LogExecutionTime {
}
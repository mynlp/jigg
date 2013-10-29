package enju.ccg.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Katsuhiko Hayashi
 * 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option {
  String name();

  String usage() default "";

  boolean required() default false;

  String defaultValue() default "";
}

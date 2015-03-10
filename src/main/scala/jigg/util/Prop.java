package jigg.util;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Prop {
  // String name() default "";
  String gloss() default "";
  boolean required() default false;
}

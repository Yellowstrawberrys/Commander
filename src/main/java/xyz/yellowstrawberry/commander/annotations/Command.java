package xyz.yellowstrawberry.commander.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
    String value();
    String description() default "";
    String usageMessage() default "";
    String[] aliases() default {};
}
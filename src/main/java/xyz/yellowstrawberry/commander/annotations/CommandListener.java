package xyz.yellowstrawberry.commander.annotations;

import xyz.yellowstrawberry.commander.enums.CommandEvent;
import xyz.yellowstrawberry.commander.enums.CommandTarget;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandListener {
    CommandEvent listeningTo() default CommandEvent.COMMAND;
    CommandTarget target() default CommandTarget.ALL;
}

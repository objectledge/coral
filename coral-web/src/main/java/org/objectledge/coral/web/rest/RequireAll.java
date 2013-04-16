package org.objectledge.coral.web.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ METHOD, TYPE })
@NameBinding
public @interface RequireAll
{
    RequireCoralPermission[] permissions() default {};

    RequireCoralRole[] roles() default {};
}

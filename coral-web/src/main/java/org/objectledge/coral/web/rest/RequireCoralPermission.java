package org.objectledge.coral.web.rest;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ PARAMETER, TYPE })
@NameBinding
public @interface RequireCoralPermission
{
    String value();
}

package org.objectledge.coral.web.rest;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * SecurityContraint is base annotation for other security related annotations
 * 
 * @author Marek Lewandowski
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ TYPE })
public @interface SecurityConstraint
{
    /**
     * type of security constraint
     * 
     * <pre>
     * Supported types:
     * - role 
     * - permission
     * </pre>
     * 
     * @return
     */
    String value();
}

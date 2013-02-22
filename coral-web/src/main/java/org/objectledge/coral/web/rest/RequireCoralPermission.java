package org.objectledge.coral.web.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

/**
 * Annotation to mark method with required permissions.
 * 
 * <pre>
 * Examples:
 * {@code
 *  @RequireCoralPermission(permission="some.permission") 
 * 
 * Checks permission "some.permission" on resource which Id is taken from Path parameter named "id"
 * }
 * {@code
 *  @RequireCoralPermission(permission="some.permission", pathParam="pathParamName")
 * }
 * Checks permission "some.permission" on resource which Id is taken from Path parameter named "pathParamName"
 * 
 * {@code
 *  @RequireCoralPermission(permission="some.permission", queryParam="queryParamName")
 * } 
 *  Checks permission "some.permission" on resource which Id is taken from Query parameter named "queryParamName"
 * 
 * </pre>
 * 
 * @author Marek Lewandowski
 * @see CoralResourceId
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD })
@NameBinding
public @interface RequireCoralPermission
{
    String permission();

    String pathParam() default "id";

    String queryParam() default "";
}

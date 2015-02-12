package org.jboss.as.console.spi;

/**
 * Access control meta data for dialogs (presenter).
 *
 * @author Heiko Braun
 * @date 3/26/12
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
public @interface RequiredResources {

    /**
     * Set of required resource to operate on (addressable privilege) within the dialog
     * @return
     */
    String[] resources();

    /**
     * Set of required operations (execution privileges) upon initialisation of the dialog
     * @return
     */
    String[] operations() default {};

    /**
     * Recursively parse child resources
     *
     * @return
     */
    boolean recursive() default true;
}

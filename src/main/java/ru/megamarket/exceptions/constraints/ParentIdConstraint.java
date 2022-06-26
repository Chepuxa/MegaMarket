package ru.megamarket.exceptions.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ParentIdValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParentIdConstraint {

    String message() default "Entity cant be a parent of itself";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

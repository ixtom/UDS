package com.ixtom.uds.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ixtom.uds.utils.SocialLink;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/*
* allows null and blank strings
* use @NotBlank for non optional fields
*/
@Constraint(validatedBy = OptionalSocialLinkValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalSocialLink {

    SocialLink type();

    String message() default "Invalid link";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

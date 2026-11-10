package com.ixtom.uds.validation;

import com.ixtom.uds.utils.SocialLink;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalSocialLinkValidator implements ConstraintValidator<OptionalSocialLink, String> {

    SocialLink type;

    @Override
    public void initialize(OptionalSocialLink anotation) {
        this.type = anotation.type();
	}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isEmpty() || this.type.matches(value);
    }
}

package com.ixtom.uds.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ixtom.uds.service.TurnstileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class CfTurnstileValidator implements ConstraintValidator<ValidCfTurnstile, Object> {

    @Autowired
    private TurnstileService turnstileService;

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return false;
        }

        HttpServletRequest request = attributes.getRequest();

        String token = request.getParameter("cf-turnstile-response");
        String remoteIp = request.getRemoteAddr();

        return turnstileService.isValidToken(token, remoteIp);
    }
}

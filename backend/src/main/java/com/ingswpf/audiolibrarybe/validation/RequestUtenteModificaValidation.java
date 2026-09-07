package com.ingswpf.audiolibrarybe.validation;

import com.ingswpf.audiolibrarybe.utils.Utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * interfaccia implementabile nel dto RequestUtenteModifica per la validazione customizzata
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = RequestUtenteModificaValidator.class)
public @interface RequestUtenteModificaValidation {
    String message() default Utils.ERR_JSON_REQUEST;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package com.ingswpf.audiolibrarybe.validation;

import com.ingswpf.audiolibrarybe.dto.request.RequestUtenteModifica;
import com.ingswpf.audiolibrarybe.utils.Utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * classe contenente la logica effettiva che effettua la validazione del dto RequestUtenteModifica
 */
public class RequestUtenteModificaValidator implements ConstraintValidator<RequestUtenteModificaValidation, RequestUtenteModifica> {
    @Override
    public void initialize(RequestUtenteModificaValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * metodo contenente la logica per la validazione
     * @param requestUtenteModifica object to validate
     * @param context context in which the constraint is evaluated
     */
    @Override
    public boolean isValid(RequestUtenteModifica requestUtenteModifica, ConstraintValidatorContext context) {
        String email = requestUtenteModifica.getEmail();
        String password = requestUtenteModifica.getPassword();
        if(email == null|| password == null) return false;
        if(!email.isEmpty() && !email.matches(Utils.REGEX_EMAIL)) return false;
        if(!password.isEmpty() && !password.matches(Utils.REGEX_PASSWORD)) return false;
        return true;
    }
}

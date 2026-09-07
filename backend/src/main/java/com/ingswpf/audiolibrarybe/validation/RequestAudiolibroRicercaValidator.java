package com.ingswpf.audiolibrarybe.validation;

import com.ingswpf.audiolibrarybe.dto.request.RequestAudiolibroRicerca;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

/**
 * classe contenente la logica effettiva che effettua la validazione del dto RequestAudiolibroRicerca
 */
public class RequestAudiolibroRicercaValidator implements ConstraintValidator<RequestAudiolibroRicercaValidation, RequestAudiolibroRicerca> {
    @Override
    public void initialize(RequestAudiolibroRicercaValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * metodo contenente la logica per la validazione
     * @param requestAudiolibroRicerca object to validate
     * @param context context in which the constraint is evaluated
     */
    @Override
    public boolean isValid(RequestAudiolibroRicerca requestAudiolibroRicerca, ConstraintValidatorContext context) {
        if (requestAudiolibroRicerca == null) return true;
        String titolo = requestAudiolibroRicerca.getTitolo();
        String dataInserimento = requestAudiolibroRicerca.getDataInserimento();
        Integer tipo = requestAudiolibroRicerca.getTipo();
        if(titolo == null && dataInserimento == null) return false;
        if(titolo != null && titolo.isBlank()) return false;
        if(dataInserimento != null && dataInserimento.isEmpty()) return false;
        if(tipo == null || tipo < 1 || tipo > 3) return false;
        if(dataInserimento != null) {
            try {
                LocalDate.parse(dataInserimento);
            } catch(Exception e) {
                return false;
            }
        }
        return true;
    }
}
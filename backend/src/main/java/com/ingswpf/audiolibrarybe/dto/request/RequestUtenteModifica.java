package com.ingswpf.audiolibrarybe.dto.request;

import com.ingswpf.audiolibrarybe.utils.Utils;
import com.ingswpf.audiolibrarybe.validation.RequestUtenteModificaValidation;

import jakarta.validation.constraints.Pattern;

/**
 * classe rappresentante il DTO per la request di "utente/modifica"
 */
@RequestUtenteModificaValidation
public class RequestUtenteModifica {
    private String email;

    @Pattern(regexp = "^$|" + Utils.REGEX_PASSWORD, message = Utils.ERR_PWD_INVALIDA)
    private String password;

    public RequestUtenteModifica() {}

    private RequestUtenteModifica(RequestUtenteModificaBuilder builder) {
        this.email = builder.email;
        this.password = builder.password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestUtenteModificaBuilder {
        public String email;

        public String password;

        public RequestUtenteModificaBuilder(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public RequestUtenteModifica build() {
            return new RequestUtenteModifica(this);
        }
    }
}

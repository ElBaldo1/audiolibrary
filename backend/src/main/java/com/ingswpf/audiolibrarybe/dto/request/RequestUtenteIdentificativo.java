package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * classe rappresentante il DTO per la request dei servizi inerenti alla condivisione di un audiolibro
 */
public class RequestUtenteIdentificativo {
    @NotNull
    @NotEmpty
    private String username;

    public RequestUtenteIdentificativo () {}

    private RequestUtenteIdentificativo(RequestUtenteIdentificativoBuilder builder) {
        this.username = builder.username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestUtenteIdentificativoBuilder {
        private String username;

        public RequestUtenteIdentificativoBuilder(String username) {
            this.username = username;
        }

        public RequestUtenteIdentificativo build() {
            return new RequestUtenteIdentificativo(this);
        }
    }
}

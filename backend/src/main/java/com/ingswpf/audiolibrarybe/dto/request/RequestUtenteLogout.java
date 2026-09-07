package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * classe rappresentante il DTO per la request di "utente/logout"
 */
public class RequestUtenteLogout {
    @NotEmpty
    @NotNull
    private String jwtToken;

    public RequestUtenteLogout() {}

    private RequestUtenteLogout(RequestUtenteLogoutBuilder builder) {
        this.jwtToken = builder.jwtToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String token) {
        this.jwtToken = token;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestUtenteLogoutBuilder {
        private String jwtToken;

        public RequestUtenteLogoutBuilder(String jwtToken) {
            this.jwtToken = jwtToken;
        }

        public RequestUtenteLogout build() {
            return new RequestUtenteLogout(this);
        }
    }
}

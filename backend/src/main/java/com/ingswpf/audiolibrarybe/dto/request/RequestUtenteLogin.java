package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * classe rappresentante il DTO per la request di "utente/login"
 */
public class RequestUtenteLogin {
    @NotEmpty
    @NotNull
    private String username;

    @NotEmpty
    @NotNull
    private String password;

    public RequestUtenteLogin() {}

    private RequestUtenteLogin(RequestUtenteLoginBuilder builder) {
        this.username = builder.username;
        this.password = builder.password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
    public static class RequestUtenteLoginBuilder {
        private String username;

        private String password;

        public RequestUtenteLoginBuilder(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public RequestUtenteLogin build() {
            return new RequestUtenteLogin(this);
        }
    }
}

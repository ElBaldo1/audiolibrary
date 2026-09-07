package com.ingswpf.audiolibrarybe.dto.response;

/**
 * classe rappresentante il DTO per la response di "utente/login"
 */
public class ResponseUtenteLogin {
    private String jwtToken;

    private ResponseUtente utente;

    private ResponseUtenteLogin(ResponseUtenteLoginBuilder builder) {
        this.jwtToken = builder.jwtToken;
        this.utente = builder.utente;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public ResponseUtente getUtente() {
        return utente;
    }

    public void setUtente(ResponseUtente utente) {
        this.utente = utente;
    }

    /**
     * inner class prevista dal desgin pattern creazionolare builder
     */
    public static class ResponseUtenteLoginBuilder {
        private String jwtToken;

        private ResponseUtente utente;

        public ResponseUtenteLoginBuilder(String jwtToken, ResponseUtente utente) {
            this.jwtToken = jwtToken;
            this.utente = utente;
        }

        public ResponseUtenteLogin build() {
            return new ResponseUtenteLogin(this);
        }
    }
}

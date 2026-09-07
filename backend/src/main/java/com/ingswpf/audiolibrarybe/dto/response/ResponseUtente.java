package com.ingswpf.audiolibrarybe.dto.response;

/**
 * classe rappresentante il DTO per la response dell'entità Utente
 */
public class ResponseUtente {
    private String nome;

    private String cognome;

    private String username;

    private ResponseUtente(ResponseUtenteBuilder builder) {
        this.nome = builder.nome;
        this.cognome = builder.cognome;
        this.username = builder.username;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * inner class prevista dal desgin pattern creazionolare builder
     */
    public static class ResponseUtenteBuilder {
        private String nome;

        private String cognome;

        private String username;

        public ResponseUtenteBuilder(String nome, String cognome, String username) {
            this.nome = nome;
            this.cognome = cognome;
            this.username = username;
        }

        public ResponseUtente build() {
            return new ResponseUtente(this);
        }
    }
}

package com.ingswpf.audiolibrarybe.dto.request;

import com.ingswpf.audiolibrarybe.utils.Utils;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * classe rappresentante il DTO per la request di "utente/registrazione"
 */
public class RequestUtenteRegistrazione {
    @NotEmpty
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 255)
    private String nome;

    @NotEmpty
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 255)
    private String cognome;

    @NotEmpty
    @NotNull
    @Pattern(regexp = Utils.REGEX_EMAIL)
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 255)
    private String email;

    @NotEmpty
    @NotNull
    @Pattern(regexp = "[A-Za-z0-9._-]{1,100}")
    private String username;

    @NotEmpty
    @NotNull
    @Pattern(regexp = Utils.REGEX_PASSWORD, message = Utils.ERR_PWD_INVALIDA)
    private String password;

    public RequestUtenteRegistrazione() {
    }

    private RequestUtenteRegistrazione(RequestUtenteRegistrazioneBuilder builder) {
        this.nome = builder.nome;
        this.cognome = builder.cognome;
        this.email = builder.email;
        this.username = builder.username;
        this.password = builder.password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    public static class RequestUtenteRegistrazioneBuilder {
        private String nome;

        private String cognome;

        private String email;

        private String username;

        private String password;

        public RequestUtenteRegistrazioneBuilder(String nome, String cognome, String email, String username, String password) {
            this.nome = nome;
            this.cognome = cognome;
            this.email = email;
            this.username = username;
            this.password = password;
        }

        public RequestUtenteRegistrazione build() {
            return new RequestUtenteRegistrazione(this);
        }
    }
}

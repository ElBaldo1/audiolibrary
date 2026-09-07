package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * classe rappresentante il DTO per la request di "audiolibro/condividi"
 */
public class RequestAudiolibroCondivisione {
    @NotNull
    @Min(1)
    private Integer idAudiolibro;

    @jakarta.validation.constraints.NotEmpty
    @Valid
    private List<@NotNull RequestUtenteIdentificativo> utenti;

    public RequestAudiolibroCondivisione() {}

    private RequestAudiolibroCondivisione(RequestAudiolibroCondivisioneBuilder builder) {
        this.idAudiolibro = builder.idAudiolibro;
        this.utenti = builder.utenti;
    }

    public Integer getIdAudiolibro() {
        return idAudiolibro;
    }

    public void setIdAudiolibro(Integer idAudiolibro) {
        this.idAudiolibro = idAudiolibro;
    }

    public List<RequestUtenteIdentificativo> getUtenti() {
        return utenti;
    }

    public void setUtenti(List<RequestUtenteIdentificativo> utenti) {
        this.utenti = utenti;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestAudiolibroCondivisioneBuilder {
        private Integer idAudiolibro;

        private List<RequestUtenteIdentificativo> utenti;

        public RequestAudiolibroCondivisioneBuilder(Integer idAudiolibro, List<RequestUtenteIdentificativo> utenti) {
            this.idAudiolibro = idAudiolibro;
            this.utenti = utenti;
        }

        public RequestAudiolibroCondivisione build() {
            return new RequestAudiolibroCondivisione(this);
        }
    }
}

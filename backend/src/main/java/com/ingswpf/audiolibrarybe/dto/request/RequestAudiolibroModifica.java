package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * classe rappresentante il DTO per la request dei servizi che prevedono una modifica di un audiolibro
 */
public class RequestAudiolibroModifica {
    @NotNull
    @Min(1)
    private Integer idAudiolibro;

    public RequestAudiolibroModifica() {}

    private RequestAudiolibroModifica(RequestAudiolibroModificaBuilder builder) {
        this.idAudiolibro = builder.idAudiolibro;
    }

    public Integer getIdAudiolibro() {
        return idAudiolibro;
    }

    public void setIdAudiolibro(Integer idAudiolibro) {
        this.idAudiolibro = idAudiolibro;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestAudiolibroModificaBuilder {
        private Integer idAudiolibro;

        public RequestAudiolibroModificaBuilder(Integer idAudiolibro) {
            this.idAudiolibro = idAudiolibro;
        }

        public RequestAudiolibroModifica build() {
            return new RequestAudiolibroModifica(this);
        }
    }
}

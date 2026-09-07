package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * classe rappresentante il DTO per la request di "audiolibro/ascolta"
 */
public class RequestAudiolibroAscolta {
    @NotNull
    @Min(1)
    private Integer idAudiolibro;

    @NotNull
    @Min(1)
    private Integer secondi;

    public RequestAudiolibroAscolta() {}

    private RequestAudiolibroAscolta(RequestAudiolibroAscoltaBuilder builder) {
        this.idAudiolibro = builder.idAudiolibro;
        this.secondi = builder.secondi;
    }

    public Integer getIdAudiolibro() {
        return idAudiolibro;
    }

    public void setIdAudiolibro(Integer idAudiolibro) {
        this.idAudiolibro = idAudiolibro;
    }

    public Integer getSecondi() {
        return secondi;
    }

    public void setSecondi(Integer secondi) {
        this.secondi = secondi;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestAudiolibroAscoltaBuilder {
        private Integer idAudiolibro;

        private Integer secondi;

        public RequestAudiolibroAscoltaBuilder(Integer idAudiolibro, Integer secondi) {
            this.idAudiolibro = idAudiolibro;
            this.secondi = secondi;
        }

        public RequestAudiolibroAscolta build() {
            return new RequestAudiolibroAscolta(this);
        }
    }
}

package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * classe rappresentante il DTO per la request di "audiolibro/lista"
 */
public class RequestAudiolibroLista {
    @NotNull
    @Min(1)
    @Max(3)
    private Integer tipo;

    public RequestAudiolibroLista() {}

    private RequestAudiolibroLista(RequestAudiolibroListaBuilder builder) {
        this.tipo = builder.tipo;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestAudiolibroListaBuilder {
        private Integer tipo;

        public RequestAudiolibroListaBuilder(Integer tipo) {
            this.tipo = tipo;
        }

        public RequestAudiolibroLista build() {
            return new RequestAudiolibroLista(this);
        }
    }
}

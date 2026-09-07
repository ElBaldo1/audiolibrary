package com.ingswpf.audiolibrarybe.dto.request;

import com.ingswpf.audiolibrarybe.validation.RequestAudiolibroRicercaValidation;

/**
 * classe rappresentante il DTO per la request di "audiolibro/ricerca"
 */
@RequestAudiolibroRicercaValidation
public class RequestAudiolibroRicerca {
    private String titolo;

    private String dataInserimento;

    private Integer tipo;

    public RequestAudiolibroRicerca() {}

    private RequestAudiolibroRicerca(RequestAudiolibroRicercaBuilder builder) {
        this.titolo = builder.titolo;
        this.dataInserimento = builder.dataInserimento;
        this.tipo = builder.tipo;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(String dataInserimento) {
        this.dataInserimento = dataInserimento;
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
    public static class RequestAudiolibroRicercaBuilder {
        private String titolo;

        private String dataInserimento;

        private Integer tipo;

        public RequestAudiolibroRicercaBuilder(String titolo, String dataInserimento, Integer tipo) {
            this.titolo = titolo;
            this.dataInserimento = dataInserimento;
            this.tipo = tipo;
        }

        public RequestAudiolibroRicerca build() {
            return new RequestAudiolibroRicerca(this);
        }
    }
}

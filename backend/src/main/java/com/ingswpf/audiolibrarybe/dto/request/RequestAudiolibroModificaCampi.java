package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * classe rappresentante il DTO per la request dei servizi che prevedono una modifica dei campi di un audiolibro
 */
public class RequestAudiolibroModificaCampi {
    @NotNull
    @Min(1)
    private Integer idAudiolibro;

    @NotNull
    private String titolo;

    @NotNull
    private String descrizione;

    @NotNull
    private String copertina;

    public RequestAudiolibroModificaCampi() {}

    public RequestAudiolibroModificaCampi(RequestAudiolibroModificaCampiBuilder builder) {
        this.idAudiolibro = builder.idAudiolibro;
        this.titolo = builder.titolo;
        this.descrizione = builder.descrizione;
        this.copertina = builder.copertina;
    }

    public Integer getIdAudiolibro() {
        return idAudiolibro;
    }

    public void setIdAudiolibro(Integer idAudiolibro) {
        this.idAudiolibro = idAudiolibro;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getCopertina() {
        return copertina;
    }

    public void setCopertina(String copertina) {
        this.copertina = copertina;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestAudiolibroModificaCampiBuilder {
        private Integer idAudiolibro;

        private String titolo;

        private String descrizione;

        private String copertina;

        public RequestAudiolibroModificaCampiBuilder(Integer idAudiolibro, String titolo, String descrizione, String copertina) {
            this.idAudiolibro = idAudiolibro;
            this.titolo = titolo;
            this.descrizione = descrizione;
            this.copertina = copertina;
        }

        public RequestAudiolibroModificaCampi build() {
            return new RequestAudiolibroModificaCampi(this);
        }
    }
}

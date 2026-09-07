package com.ingswpf.audiolibrarybe.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * classe rappresentante il DTO per la request di "audiolibro/inserisci"
 */
public class RequestAudiolibroInserisci {

    @jakarta.validation.constraints.Size(max = 255)
    private String autore = "";
    public String getAutore() { return autore; }
    public void setAutore(String value) { autore = value; }

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.Min(0)
    private Integer durata = 0;
    public Integer getDurata() { return durata; }
    public void setDurata(Integer value) { durata = value; }

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.Pattern(regexp = "audio/[a-zA-Z0-9.+-]+")
    private String mimeType = "audio/mpeg";
    public String getMimeType() { return mimeType; }
    public void setMimeType(String value) { mimeType = value; }

    @NotEmpty
    @NotNull
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 255)
    private String titolo;

    @jakarta.validation.constraints.Size(max = 5000)
    @NotNull
    private String descrizione;

    @jakarta.validation.constraints.Size(max = 2000000)
    @NotNull
    private String copertina;

    @NotEmpty
    @NotNull
    @jakarta.validation.constraints.Size(max = 27962028)
    private String audio;

    public RequestAudiolibroInserisci(RequestAudiolibroInserisciBuilder builder) {
        this.titolo = builder.titolo;
        this.descrizione = builder.descrizione;
        this.copertina = builder.copertina;
        this.audio = builder.audio;
    }

    private RequestAudiolibroInserisci() {}

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

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    /**
     * inner class prevista dal desgin pattern creazionale builder
     */
    public static class RequestAudiolibroInserisciBuilder {
        private String titolo;

        private String descrizione;

        private String copertina;

        private String audio;

        public RequestAudiolibroInserisciBuilder(String titolo, String descrizione, String copertina, String audio) {
            this.titolo = titolo;
            this.descrizione = descrizione;
            this.copertina = copertina;
            this.audio = audio;
        }

        public RequestAudiolibroInserisci build() {
            return new RequestAudiolibroInserisci(this);
        }
    }
}

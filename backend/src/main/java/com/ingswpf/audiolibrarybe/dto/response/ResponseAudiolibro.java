package com.ingswpf.audiolibrarybe.dto.response;

/**
 * classe rappresentante il DTO per la response dell'entità Audiolibro
 */
public class ResponseAudiolibro {

    private String autore = "";
    public String getAutore() { return autore; }
    public void setAutore(String value) { autore = value; }

    private Integer durata = 0;
    public Integer getDurata() { return durata; }
    public void setDurata(Integer value) { durata = value; }

    private String mimeType = "audio/mpeg";
    public String getMimeType() { return mimeType; }
    public void setMimeType(String value) { mimeType = value; }

    private Integer idAudiolibro;

    private Boolean preferito;

    private Boolean pubblico;

    private String titolo;

    private String descrizione;

    private String copertina;

    private String audio;

    private String dataInserimento;

    private ResponseUtente creatore;

    private ResponseAscolto ultimoAscolto;

    private ResponseAudiolibro(ResponseAudiolibroBuilder builder) {
        this.idAudiolibro = builder.idAudiolibro;
        this.preferito = builder.preferito;
        this.pubblico = builder.pubblico;
        this.titolo = builder.titolo;
        this.descrizione = builder.descrizione;
        this.copertina = builder.copertina;
        this.audio = builder.audio;
        this.dataInserimento = builder.dataInserimento;
        this.creatore = builder.creatore;
        this.ultimoAscolto = builder.ultimoAscolto;
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

    public String getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(String dataInserimento) {
        this.dataInserimento = dataInserimento;
    }

    public Integer getIdAudiolibro() {
        return idAudiolibro;
    }

    public void setIdAudiolibro(Integer idAudiolibro) {
        this.idAudiolibro = idAudiolibro;
    }

    public ResponseUtente getCreatore() {
        return creatore;
    }

    public void setCreatore(ResponseUtente creatore) {
        this.creatore = creatore;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public ResponseAscolto getUltimoAscolto() {
        return ultimoAscolto;
    }

    public void setUltimoAscolto(ResponseAscolto ultimoAscolto) {
        this.ultimoAscolto = ultimoAscolto;
    }

    public Boolean getPreferito() {
        return preferito;
    }

    public void setPreferito(Boolean preferito) {
        this.preferito = preferito;
    }

    public Boolean getPubblico() {
        return pubblico;
    }

    public void setPubblico(Boolean pubblico) {
        this.pubblico = pubblico;
    }

    /**
     * inner class prevista dal desgin pattern creazionolare builder
     */
    public static class ResponseAudiolibroBuilder {
        private Integer idAudiolibro;

        private Boolean preferito;

        private Boolean pubblico;

        private String titolo;

        private String descrizione;

        private String copertina;

        private String audio;

        private String dataInserimento;

        private ResponseUtente creatore;

        private ResponseAscolto ultimoAscolto;

        public ResponseAudiolibroBuilder(Integer idAudiolibro, Boolean preferito, Boolean pubblico, String titolo, String descrizione, String copertina, String audio, String dataInserimento, ResponseUtente creatore, ResponseAscolto ultimoAscolto) {
            this.idAudiolibro = idAudiolibro;
            this.preferito = preferito;
            this.pubblico = pubblico;
            this.titolo = titolo;
            this.descrizione = descrizione;
            this.copertina = copertina;
            this.audio = audio;
            this.dataInserimento = dataInserimento;
            this.creatore = creatore;
            this.ultimoAscolto = ultimoAscolto;
        }

        public ResponseAudiolibro build() {
            return new ResponseAudiolibro(this);
        }
    }
}

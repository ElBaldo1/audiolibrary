package com.ingswpf.audiolibrarybe.dto.response;

/**
 * classe rappresentante il DTO per la response dell'entità Ascolto
 */
public class ResponseAscolto {
    private String updatedAt;
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String value) { updatedAt = value; }

    private String data;

    private Integer secondi;

    private ResponseAscolto(ResponseAscoltoBuilder builder) {
        this.data = builder.data;
        this.secondi = builder.secondi;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getSecondi() {
        return secondi;
    }

    public void setSecondi(Integer secondi) {
        this.secondi = secondi;
    }

    /**
     * inner class prevista dal desgin pattern creazionolare builder
     */
    public static class ResponseAscoltoBuilder {
        private String data;

        private Integer secondi;

        public ResponseAscoltoBuilder(String data, Integer secondi) {
            this.data = data;
            this.secondi = secondi;
        }

        public ResponseAscolto build() {
            return new ResponseAscolto(this);
        }
    }
}

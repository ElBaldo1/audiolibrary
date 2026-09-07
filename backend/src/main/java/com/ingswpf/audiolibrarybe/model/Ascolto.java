package com.ingswpf.audiolibrarybe.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * classe rappresentante l'entity del db Ascolto
 */
@Entity
public class Ascolto {
    private java.time.Instant updatedAt;
    public java.time.Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.Instant value) { updatedAt = value; }

    @EmbeddedId
    AscoltoKey id = new AscoltoKey();

    @ManyToOne
    @MapsId("id_utente")
    @JoinColumn(name = "utente")
    private Utente utente;

    @ManyToOne
    @MapsId("id_audiolibro")
    @JoinColumn(name = "audiolibro")
    private Audiolibro audiolibro;

    private Integer secondi;

    private LocalDate data;

    public Ascolto() {
        super();
    }

    public AscoltoKey getId() {
        return id;
    }

    public void setId(AscoltoKey id) {
        this.id = id;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public Audiolibro getAudiolibro() {
        return audiolibro;
    }

    public void setAudiolibro(Audiolibro audiolibro) {
        this.audiolibro = audiolibro;
    }

    public Integer getSecondi() {
        return secondi;
    }

    public void setSecondi(Integer secondi) {
        this.secondi = secondi;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }
}

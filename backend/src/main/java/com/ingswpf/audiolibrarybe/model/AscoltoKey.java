package com.ingswpf.audiolibrarybe.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * classe embedded java per l'implementazione della chiave combinata per l'entity AScolto
 */
@Embeddable
public class AscoltoKey implements Serializable {
    @Column(name = "id_utente")
    Integer id_utente;

    @Column(name = "id_audiolibro")
    Integer id_audiolibro;

    public AscoltoKey() {
        super();
    }

    public Integer getId_utente() {
        return id_utente;
    }

    public void setId_utente(Integer id_utente) {
        this.id_utente = id_utente;
    }

    public Integer getId_audiolibro() {
        return id_audiolibro;
    }

    public void setId_audiolibro(Integer id_audiolibro) {
        this.id_audiolibro = id_audiolibro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AscoltoKey that = (AscoltoKey) o;
        return Objects.equals(id_utente, that.id_utente) && Objects.equals(id_audiolibro, that.id_audiolibro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_utente, id_audiolibro);
    }
}


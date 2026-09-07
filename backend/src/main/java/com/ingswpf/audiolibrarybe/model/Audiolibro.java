package com.ingswpf.audiolibrarybe.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * classe rappresentante l'entity del db Audiolibro
 */
@Entity
public class Audiolibro {

    private String autore = "";
    public String getAutore() { return autore; }
    public void setAutore(String value) { autore = value; }

    private Integer durata = 0;
    public Integer getDurata() { return durata; }
    public void setDurata(Integer value) { durata = value; }

    private String mimeType = "audio/mpeg";
    public String getMimeType() { return mimeType; }
    public void setMimeType(String value) { mimeType = value; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titolo;

    @Lob
    @Column(name = "descrizione", length = 5000)
    private String descrizione;

    @ManyToOne
    @JoinColumn(name = "creatore")
    private Utente creatore;

    private Boolean flgPubblico;

    private Boolean eliminato;

    @Lob
    private byte[] copertina;

    @Lob
    private byte[] audio;

    private LocalDate dataInserimento;

    @OneToMany(mappedBy = "audiolibro")
    private List<Ascolto> ascolti = new ArrayList<>();

    @ManyToMany(mappedBy = "audiolibroCondivisi")
    private List<Utente> utentiCondivisi = new ArrayList<>();

    @ManyToMany(mappedBy = "audiolibroPreferiti")
    private List<Utente> utentiPreferiti = new ArrayList<>();

    public Audiolibro() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Utente getCreatore() {
        return creatore;
    }

    public void setCreatore(Utente creatore) {
        this.creatore = creatore;
    }

    public byte[] getCopertina() {
        return copertina;
    }

    public void setCopertina(byte[] copertina) {
        this.copertina = copertina;
    }

    public List<Ascolto> getAscolti() {
        return ascolti;
    }

    public void setAscolti(List<Ascolto> ascolti) {
        this.ascolti = ascolti;
    }

    public LocalDate getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(LocalDate dataInserimento) {
        this.dataInserimento = dataInserimento;
    }

    public List<Utente> getUtentiCondivisi() {
        return utentiCondivisi;
    }

    public void setUtentiCondivisi(List<Utente> utentiCondivisi) {
        this.utentiCondivisi = utentiCondivisi;
    }

    public List<Utente> getUtentiPreferiti() {
        return utentiPreferiti;
    }

    public void setUtentiPreferiti(List<Utente> utentiPreferiti) {
        this.utentiPreferiti = utentiPreferiti;
    }

    public Boolean getFlgPubblico() {
        return flgPubblico;
    }

    public void setFlgPubblico(Boolean flgPubblico) {
        this.flgPubblico = flgPubblico;
    }

    public byte[] getAudio() {
        return audio;
    }

    public void setAudio(byte[] audio) {
        this.audio = audio;
    }

    public Boolean getEliminato() {
        return eliminato;
    }

    public void setEliminato(Boolean eliminato) {
        this.eliminato = eliminato;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Audiolibro entity)) return false;
        return getId() != null && getId().equals(entity.getId());
    }

    @Override
    public int hashCode() {
        return Audiolibro.class.hashCode();
    }
}

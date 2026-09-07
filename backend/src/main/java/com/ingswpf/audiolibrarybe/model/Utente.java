package com.ingswpf.audiolibrarybe.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

/**
 * classe rappresentante l'entity del db Utente
 */
@Entity
public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    private String cognome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @OneToMany(mappedBy = "creatore")
    private List<Audiolibro> audiolibroCreati = new ArrayList<>();

    @OneToMany(mappedBy = "utente")
    private List<Ascolto> ascolti = new ArrayList<>();

    @OneToMany(mappedBy = "utente")
    private List<JwtNotExpired> jwtNotExpiredList = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "condivisione", joinColumns =
    @JoinColumn(name = "utente"), inverseJoinColumns =
    @JoinColumn(name = "audiolibro"))
    private List<Audiolibro> audiolibroCondivisi = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "preferenza", joinColumns =
    @JoinColumn(name = "utente"), inverseJoinColumns =
    @JoinColumn(name = "audiolibro"))
    private List<Audiolibro> audiolibroPreferiti = new ArrayList<>();

    public Utente() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Audiolibro> getAudiolibroCreati() {
        return audiolibroCreati;
    }

    public void setAudiolibroCreati(List<Audiolibro> audiolibroCreati) {
        this.audiolibroCreati = audiolibroCreati;
    }

    public List<Ascolto> getAscolti() {
        return ascolti;
    }

    public void setAscolti(List<Ascolto> ascolti) {
        this.ascolti = ascolti;
    }

    public List<JwtNotExpired> getJwtNotExpiredList() {
        return jwtNotExpiredList;
    }

    public void setJwtNotExpiredList(List<JwtNotExpired> jwtNotExpiredList) {
        this.jwtNotExpiredList = jwtNotExpiredList;
    }

    public List<Audiolibro> getAudiolibroCondivisi() {
        return audiolibroCondivisi;
    }

    public void setAudiolibroCondivisi(List<Audiolibro> audiolibroCondivisi) {
        this.audiolibroCondivisi = audiolibroCondivisi;
    }

    public List<Audiolibro> getAudiolibroPreferiti() {
        return audiolibroPreferiti;
    }

    public void setAudiolibroPreferiti(List<Audiolibro> audiolibroPreferiti) {
        this.audiolibroPreferiti = audiolibroPreferiti;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Utente entity)) return false;
        return getId() != null && getId().equals(entity.getId());
    }

    @Override
    public int hashCode() {
        return Utente.class.hashCode();
    }
}

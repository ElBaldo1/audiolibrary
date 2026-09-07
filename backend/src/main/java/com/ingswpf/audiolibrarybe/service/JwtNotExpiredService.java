package com.ingswpf.audiolibrarybe.service;

import com.ingswpf.audiolibrarybe.exception.TokenNonTrovato;
import com.ingswpf.audiolibrarybe.model.JwtNotExpired;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.repository.JwtNotExpiredRepo;
import com.ingswpf.audiolibrarybe.repository.UtenteRepo;
import org.springframework.stereotype.Service;

// classe rappresentante il service per i servizi riguardanti la gestione dei JWT
@Service
public class JwtNotExpiredService {

    public JwtNotExpiredService(JwtNotExpiredRepo jwtNotExpiredRepo, UtenteRepo utenteRepo) {
        this.jwtNotExpiredRepo = jwtNotExpiredRepo;
        this.utenteRepo = utenteRepo;
    }

    private final JwtNotExpiredRepo jwtNotExpiredRepo;

    private final UtenteRepo utenteRepo;

    /**
     * metodo per salvare un JWT token tra quelli validi
     * @param token
     * @param username
     */
    public void salvaTokenValido(String token, String username) {
        JwtNotExpired jwtTest = jwtNotExpiredRepo.findByToken(token);
        if(jwtTest == null) {
            JwtNotExpired jwtNotExpired = new JwtNotExpired();
            Utente utente = utenteRepo.findByUsername(username);
            if(utente == null) utente = utenteRepo.findByEmail(username);
            jwtNotExpired.setToken(token);
            jwtNotExpired.setUtente(utente);
            jwtNotExpiredRepo.save(jwtNotExpired);
        }
    }

    /**
     * metodo per invalidare un JWT token presente nella lista dei JWT token validi
     * @param token
     */
    public void invalidaToken(String token) {
        JwtNotExpired jwtTest = jwtNotExpiredRepo.findByToken(token);
        if(jwtTest == null || jwtTest.getExpired()) {
            throw new TokenNonTrovato();
        }
        jwtNotExpiredRepo.invalidaJwt(jwtTest.getId());
    }
}

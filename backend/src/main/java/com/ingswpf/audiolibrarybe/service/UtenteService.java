package com.ingswpf.audiolibrarybe.service;

import java.util.ArrayList;
import com.ingswpf.audiolibrarybe.exception.CredenzialiNonValide;
import com.ingswpf.audiolibrarybe.exception.UsernameEsistente;
import com.ingswpf.audiolibrarybe.model.JwtNotExpired;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.repository.JwtNotExpiredRepo;
import com.ingswpf.audiolibrarybe.repository.UtenteRepo;
import com.ingswpf.audiolibrarybe.security.JwtTokenUtil;
import com.ingswpf.audiolibrarybe.utils.Utils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

// classe rappresentante il service per i servizi esposti in AccountController
@Service
public class UtenteService implements UserDetailsService {

    public UtenteService(UtenteRepo utenteRepo, JwtNotExpiredRepo jwtNotExpiredRepo, PasswordEncoder bcryptEncoder, JwtTokenUtil jwtTokenUtil) {
        this.utenteRepo = utenteRepo;
        this.jwtNotExpiredRepo = jwtNotExpiredRepo;
        this.bcryptEncoder = bcryptEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    private final UtenteRepo utenteRepo;

    private final JwtNotExpiredRepo jwtNotExpiredRepo;

    private final PasswordEncoder bcryptEncoder;

    private final JwtTokenUtil jwtTokenUtil;

    /**
     * metodo per il login di un utente
     * @param utente
     * @return dettagli dell'entity Utente di Spring Security
     */
    public UserDetails login(Utente utente) throws Exception {
        UserDetails user;
        try {
            user = loadUserByUsername(utente.getUsername());
        } catch (UsernameNotFoundException exception) {
            throw new CredenzialiNonValide();
        }
        if (!bcryptEncoder.matches(utente.getPassword(), user.getPassword())) {
            throw new CredenzialiNonValide();
        }
        return user;
    }

    /**
     * metodo per trovare un utente a partire dal JWT token
     * @param username
     * @return dettagli dell'entity Utente di Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // false -> per username
        // true -> per email
        Boolean situazioneLogin = false;
        Utente utente = utenteRepo.findByUsername(username);
        if (utente == null) {
            situazioneLogin = true;
            utente = utenteRepo.findByEmail(username);
            if(utente == null) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }
        }
        return new org.springframework.security.core.userdetails.User((situazioneLogin) ? utente.getEmail() : utente.getUsername(), utente.getPassword(),
                new ArrayList<>());
    }

    /**
     * metodo per effettuare un check se l'utente sia effettivamente loggato
     * @param username
     * @param token
     * @return valore booleano che conferma o meno il fatto che l'utente sia loggato
     */
    public Boolean checkUtenteLoggato(String username, String token) {
        Utente utente = utenteRepo.findByUsername(username);
        if(utente == null) {
            utente = utenteRepo.findByEmail(username);
        }
        if (utente != null) {
            JwtNotExpired session = jwtNotExpiredRepo.findByToken(token);
            return session != null && !Boolean.TRUE.equals(session.getExpired())
                    && utente.equals(session.getUtente());
        }
        return false;
    }

    /**
     * metodo per effettuare la registrazione di un nuovo utente nella piattaforma
     * @param nome
     * @param cognome
     * @param email
     * @param username
     * @param password
     * @return utente appena registrato nella piattaforma
     */
    public Utente registrazione(String nome, String cognome, String email, String username, String password) {
        Utente testUtente = utenteRepo.findByUsername(username);
        if(testUtente == null && utenteRepo.findByEmail(email) == null) {
            Utente utente = new Utente();
            utente.setNome(nome);
            utente.setCognome(cognome);
            utente.setEmail(email);
            utente.setUsername(username);
            utente.setPassword(bcryptEncoder.encode(password));
            return utenteRepo.save(utente);
        }
        throw new UsernameEsistente();
    }

    /**
     * metodo per effettuare la modifica di un utente
     * @param email
     * @param password
     * @param request
     * @return utente modificato
     */
    public Utente modifica(String email, String password, HttpServletRequest request) {
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        if(email != null && !email.isEmpty()) {
            Utente existing = utenteRepo.findByEmail(email);
            if (existing != null && !existing.equals(utente)) throw new UsernameEsistente();
            utente.setEmail(email);
        }
        if(password != null && !password.isEmpty()) utente.setPassword(bcryptEncoder.encode(password));
        utenteRepo.save(utente);
        return utente;
    }

    /**
     * metodo per ottenere un utente a partire dalla username o dalla email
     * @param username
     * @return utente ottenuto a partire dalla username o dalla email
     */
    public Utente getByUsernameOrEmail(String username) {
        if(utenteRepo.findByUsername(username) != null) return utenteRepo.findByUsername(username);
        return utenteRepo.findByEmail(username);
    }
}
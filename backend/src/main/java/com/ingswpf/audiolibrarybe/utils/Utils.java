package com.ingswpf.audiolibrarybe.utils;

import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.repository.UtenteRepo;
import com.ingswpf.audiolibrarybe.security.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * classe contenente variabili e metodi utilizzabili in qualsiasi momento, da più componenti diversi magari
 */
public class Utils {
    public static final String ERR_JSON_REQUEST = "La richiesta contiene dei campi non validi.";

    public static final String ERR_PWD_INVALIDA = "La password non è valida.";

    public static final String REGEX_EMAIL = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public static final String REGEX_PASSWORD = "(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";

    /**
     * metodo per ottenere l'utente attraverso il token presente nell'header della richiesta
     * @param request
     * @param utenteRepo
     * @param jwtTokenUtil
     * @return
     */
    public static Utente getUtenteFromHeader(HttpServletRequest request, UtenteRepo utenteRepo, JwtTokenUtil jwtTokenUtil) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new org.springframework.security.authentication.BadCredentialsException("Missing session");
        }
        String username = jwtTokenUtil.getUsernameFromToken(header.substring(7));
        Utente utente = utenteRepo.findByUsername(username);
        if (utente == null) utente = utenteRepo.findByEmail(username);
        if (utente == null) throw new org.springframework.security.authentication.BadCredentialsException("Unknown account");
        return utente;
    }
}
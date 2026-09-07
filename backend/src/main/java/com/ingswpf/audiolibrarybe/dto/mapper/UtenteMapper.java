package com.ingswpf.audiolibrarybe.dto.mapper;

import com.ingswpf.audiolibrarybe.dto.request.*;
import com.ingswpf.audiolibrarybe.dto.response.ResponseUtente;
import com.ingswpf.audiolibrarybe.dto.response.ResponseUtenteLogin;
import com.ingswpf.audiolibrarybe.model.Utente;

/**
 * classe che contiene metodi statici utilizzabili per passare dai model ai dto utilizzati nell'UtenteController
 */
public class UtenteMapper {
    /**
     * metodo per ottenere il DTO ResponseUtente
     */
    public static ResponseUtente toResponseUtente(Utente utente) {
        ResponseUtente responseUtente = new ResponseUtente.ResponseUtenteBuilder(
               utente.getNome(),
               utente.getCognome(),
               utente.getUsername()
        ).build();
        return responseUtente;
    }

    /**
     * metodo per ottenere il DTO ReponseUtenteLogin
     */
    public static ResponseUtenteLogin toResponseUtenteLogin(Utente utente, String jwt) {
        ResponseUtente responseUtente = toResponseUtente(utente);
        ResponseUtenteLogin responseUtenteLogin = new ResponseUtenteLogin.ResponseUtenteLoginBuilder(
                jwt,
                responseUtente
        ).build();
        return responseUtenteLogin;
    }

    /**
     * metodo per ottenere il DTO RequestUtenteRegistrazione
     */
    public static RequestUtenteRegistrazione toRequestUtenteRegistrazione(Utente utente) {
        RequestUtenteRegistrazione requestUtenteRegistrazione = new RequestUtenteRegistrazione();
        requestUtenteRegistrazione.setNome(utente.getNome());
        requestUtenteRegistrazione.setCognome(utente.getCognome());
        requestUtenteRegistrazione.setEmail(utente.getEmail());
        requestUtenteRegistrazione.setUsername(utente.getUsername());
        requestUtenteRegistrazione.setPassword(utente.getPassword());
        return requestUtenteRegistrazione;
    }

    /**
     * metodo per ottenere il DTO RequestUtenteLogin
     */
    public static RequestUtenteLogin toRequestUtenteLogin(Utente utente) {
        RequestUtenteLogin requestUtenteLogin = new RequestUtenteLogin();
        requestUtenteLogin.setPassword(utente.getPassword());
        requestUtenteLogin.setUsername(utente.getUsername());
        return requestUtenteLogin;
    }

    /**
     * metodo per ottenere il DTO RequestUtenteModifica
     */
    public static RequestUtenteModifica toRequestUtenteModifica(Utente utente) {
        RequestUtenteModifica requestUtenteModifica = new RequestUtenteModifica();
        requestUtenteModifica.setEmail(utente.getEmail());
        requestUtenteModifica.setPassword(utente.getPassword());
        return requestUtenteModifica;
    }

    /**
     * metodo per ottenere il DTO RequestUtenteIdentificativo
     */
    public static RequestUtenteIdentificativo toRequestUtenteIdentificativo(Utente utente) {
        RequestUtenteIdentificativo requestUtenteIdentificativo = new RequestUtenteIdentificativo();
        requestUtenteIdentificativo.setUsername(utente.getUsername());
        return requestUtenteIdentificativo;
    }

    /**
     * metodo per ottenere il DTO RequestUtenteLogout
     */
    public static RequestUtenteLogout toRequestUtenteLogout(String jwt) {
        RequestUtenteLogout requestUtenteLogout = new RequestUtenteLogout();
        requestUtenteLogout.setJwtToken(jwt);
        return requestUtenteLogout;
    }
}

package com.ingswpf.audiolibrarybe.controller;

import com.ingswpf.audiolibrarybe.dto.mapper.UtenteMapper;
import com.ingswpf.audiolibrarybe.dto.request.*;
import com.ingswpf.audiolibrarybe.dto.response.ResponseUtenteLogin;
import com.ingswpf.audiolibrarybe.dto.response.ResponseUtente;
import com.ingswpf.audiolibrarybe.exception.CredenzialiNonValide;
import com.ingswpf.audiolibrarybe.exception.TokenNonTrovato;
import com.ingswpf.audiolibrarybe.exception.UsernameEsistente;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.security.JwtTokenUtil;
import com.ingswpf.audiolibrarybe.service.JwtNotExpiredService;
import com.ingswpf.audiolibrarybe.service.UtenteService;
import com.ingswpf.audiolibrarybe.utils.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * I servizi esposti in questo controller sono relativi alle operazione consentite su un account
 */
@RestController
@RequestMapping("utente")
public class AccountController {

    public AccountController(JwtTokenUtil jwtTokenUtil, UtenteService utenteService, JwtNotExpiredService jwtNotExpiredService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.utenteService = utenteService;
        this.jwtNotExpiredService = jwtNotExpiredService;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AccountController.class);
    /**
     * dependency injection del componente JwtTokenUtil
     */
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * dependency injection del service UtenteService
     */
    private final UtenteService utenteService;

    /**
     * dependency injection del service JwtNotExpiredService
     */
    private final JwtNotExpiredService jwtNotExpiredService;

    /**
     * Servizio esposto per effettuare il login di un utente
     * @param requestUtenteLogin
     * @param result
     * @return ok status code e json response relativo, in caso di login effettuato correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "login", consumes = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody RequestUtenteLogin requestUtenteLogin, BindingResult result) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Utente utente = new Utente();
            utente.setUsername(requestUtenteLogin.getUsername());
            utente.setPassword(requestUtenteLogin.getPassword());
            UserDetails userDetails = utenteService.login(utente);
            String token = jwtTokenUtil.generateToken(userDetails);
            jwtNotExpiredService.salvaTokenValido(token, requestUtenteLogin.getUsername());
            utente = utenteService.getByUsernameOrEmail(requestUtenteLogin.getUsername());
            ResponseUtenteLogin responseUtenteLogin = UtenteMapper.toResponseUtenteLogin(utente, token);
            return new ResponseEntity<ResponseUtenteLogin>(responseUtenteLogin, HttpStatus.OK);
        } catch(CredenzialiNonValide e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per effettuare la registrazione di un utente
     * @param requestUtenteRegistrazione
     * @param result
     * @return ok status code e json response relativo, in caso di registrazione effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return bad request status code e messaggio relativo, in caso di registrazione con un username esistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "registrazione", consumes = "application/json")
    public ResponseEntity<?> registrazione(@Valid @RequestBody RequestUtenteRegistrazione requestUtenteRegistrazione, BindingResult result) {
        if(result.hasErrors()) {
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error : errors ) {
                if(error.getDefaultMessage().equals(Utils.ERR_PWD_INVALIDA))
                    return new ResponseEntity<String>(Utils.ERR_PWD_INVALIDA, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Utente utenteRegistrato = utenteService.registrazione(requestUtenteRegistrazione.getNome(), requestUtenteRegistrazione.getCognome(), requestUtenteRegistrazione.getEmail(), requestUtenteRegistrazione.getUsername(), requestUtenteRegistrazione.getPassword());
            ResponseUtente responseUtente = UtenteMapper.toResponseUtente(utenteRegistrato);
            return new ResponseEntity<ResponseUtente>(responseUtente, HttpStatus.OK);
        } catch (UsernameEsistente e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per effettuare la mpdifica di un utente
     * @param requestUtenteModifica
     * @param result
     * @param request
     * @return ok status code e json response relativo, in caso di modifica effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PatchMapping(value = "modifica", consumes = "application/json")
    public ResponseEntity<?> modifica(@Valid @RequestBody RequestUtenteModifica requestUtenteModifica, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error : errors ) {
                if(error.getDefaultMessage().equals(Utils.ERR_PWD_INVALIDA))
                    return new ResponseEntity<String>(Utils.ERR_PWD_INVALIDA, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Utente utenteModificato = utenteService.modifica(requestUtenteModifica.getEmail(), requestUtenteModifica.getPassword(), request);
            ResponseUtente responseUtente = UtenteMapper.toResponseUtente(utenteModificato);
            return new ResponseEntity<ResponseUtente>(responseUtente, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per effettuare il logout di un utente
     * @param requestUtenteLogout
     * @param result
     * @return ok status code, in caso di logout effettuato correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return bad request status code e messaggio relativo, in caso di logout con jwt non trovato
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "logout", consumes = "application/json")
    public ResponseEntity<?> logout(@Valid @RequestBody RequestUtenteLogout requestUtenteLogout, BindingResult result) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            jwtNotExpiredService.invalidaToken(requestUtenteLogout.getJwtToken());
            return ResponseEntity.ok().build();
        } catch(TokenNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
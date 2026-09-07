package com.ingswpf.audiolibrarybe.controller;

import com.ingswpf.audiolibrarybe.dto.mapper.AudiolibroMapper;
import com.ingswpf.audiolibrarybe.dto.request.*;
import com.ingswpf.audiolibrarybe.dto.response.ResponseAudiolibro;
import com.ingswpf.audiolibrarybe.exception.*;
import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.repository.UtenteRepo;
import com.ingswpf.audiolibrarybe.security.JwtTokenUtil;
import com.ingswpf.audiolibrarybe.service.AudiolibroService;
import com.ingswpf.audiolibrarybe.utils.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * I servizi esposti in questo controller sono relativi alle operazione consentite su un audiolibro
 */
@RestController
@RequestMapping("audiolibro")
public class AudiolibroController {

    public AudiolibroController(AudiolibroService audiolibroService, UtenteRepo utenteRepo, JwtTokenUtil jwtTokenUtil) {
        this.audiolibroService = audiolibroService;
        this.utenteRepo = utenteRepo;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AudiolibroController.class);
    @GetMapping("/{id}/audio")
    public ResponseEntity<org.springframework.core.io.Resource> audio(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Audiolibro book = audiolibroService.getAccessibleBook(id, request);
            return ResponseEntity.ok()
                    .header("Cache-Control", "private, no-store")
                    .contentType(org.springframework.http.MediaType.parseMediaType(book.getMimeType() == null ? "audio/mpeg" : book.getMimeType()))
                    .body(new org.springframework.core.io.ByteArrayResource(book.getAudio()));
        } catch (AudiolibroNonTrovato exception) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * dependency injection del service AudiolibroService
     */
    private final AudiolibroService audiolibroService;

    /**
     * dependency injection della repository UtenteRepo
     */
    private final UtenteRepo utenteRepo;

    /**
     * dependency injection del componente JwtTokenUtil
     */
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Servizio esposto per effettuare l'inserimento di un audiolibro
     * @param requestAudiolibroInserisci
     * @param result
     * @param request
     * @return ok status code e json response relativo, in caso di audiolibro inserito correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "inserisci", consumes = "application/json")
    public ResponseEntity<?> inserisci(@Valid @RequestBody RequestAudiolibroInserisci requestAudiolibroInserisci, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Audiolibro audiolibro = audiolibroService.inserisci(requestAudiolibroInserisci, request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibro, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.CREATED);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per effettuare la modifica dei campi di un audiolibro
     * @param requestAudiolibroModificaCampi
     * @param result
     * @param request
     * @return ok status code e json response relativo, in caso di audiolibro modificato correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PatchMapping(value = "modifica", consumes = "application/json")
    public ResponseEntity<?> modifica(@Valid @RequestBody RequestAudiolibroModificaCampi requestAudiolibroModificaCampi, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Audiolibro audiolibroModificato = audiolibroService.modifica(requestAudiolibroModificaCampi.getIdAudiolibro(), requestAudiolibroModificaCampi.getTitolo(), requestAudiolibroModificaCampi.getDescrizione(), requestAudiolibroModificaCampi.getCopertina(), request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroModificato, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per effettuare l'eliminazione (logica) di un audiolibro
     * @param requestAudiolibroModifica
     * @param result
     * @param request
     * @return ok status code e json response relativo, in caso di eliminazione effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "rimuovi", consumes = "application/json")
    public ResponseEntity<?> rimuovi(@Valid @RequestBody RequestAudiolibroModifica requestAudiolibroModifica, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Audiolibro audiolibroRimosso = audiolibroService.rimuovi(requestAudiolibroModifica.getIdAudiolibro(), request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroRimosso, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per effettuare una ricerca di audiolibri, per una determianta pagina
     * @param requestAudiolibroRicerca
     * @param result
     * @param request
     * @return ok status code e json response relativo, in caso di ricerca effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "ricerca", consumes = "application/json")
    public ResponseEntity<?> ricerca(@Valid @RequestBody RequestAudiolibroRicerca requestAudiolibroRicerca, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            String titolo = (requestAudiolibroRicerca.getTitolo() != null) ? requestAudiolibroRicerca.getTitolo() : null;
            String dataInserimento = (requestAudiolibroRicerca.getDataInserimento() != null) ? requestAudiolibroRicerca.getDataInserimento() : null;
            List<Audiolibro> audiolibroTrovati = audiolibroService.ricerca(titolo, dataInserimento, requestAudiolibroRicerca.getTipo(), request);
            List<ResponseAudiolibro> responseAudiolibroList = AudiolibroMapper.toResponseAudiolibroList(audiolibroTrovati, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<List<ResponseAudiolibro>>(responseAudiolibroList, HttpStatus.OK);
        } catch(AudiolibroNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per ottenre la lista di audiolibri, per una determinata pagina
     * @param requestAudiolibroLista
     * @param result
     * @param request
     * @return ok status code e json response relativo, in caso di lista ottenuta correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "lista", consumes = "application/json")
    public ResponseEntity<?> lista(@Valid @RequestBody RequestAudiolibroLista requestAudiolibroLista, BindingResult result, HttpServletRequest request, @RequestParam(defaultValue = "false") boolean metadataOnly) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            List<Audiolibro> lista = audiolibroService.lista(requestAudiolibroLista.getTipo(), request);
            var user = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
            List<ResponseAudiolibro> responseAudiolibroList = metadataOnly
                    ? lista.stream().map(book -> AudiolibroMapper.toResponseAudiolibro(book, user, true)).toList()
                    : AudiolibroMapper.toResponseAudiolibroList(lista, user);
            return new ResponseEntity<List<ResponseAudiolibro>>(responseAudiolibroList, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per aggiungere un audiolibro ai preferiti
     * @param requestAudiolibroModifica
     * @param result
     * @param request
     * @return ok status code e json response relativo, in caso di aggiunta ai preferiti effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return bad request status code e messaggio relativo, in caso di audiolibro già tra i preferiti
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PatchMapping(value = "aggiungiAiPreferiti", consumes = "application/json")
    public ResponseEntity<?> aggiungiAiPreferiti(@Valid @RequestBody RequestAudiolibroModifica requestAudiolibroModifica, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Audiolibro audiolibroModificato = audiolibroService.aggiungiAiPreferiti(requestAudiolibroModifica.getIdAudiolibro(), request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroModificato, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroGiaPreferito e) {
            return new ResponseEntity<String >(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch(AudiolibroNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per rimuovere un audiolibro ai preferiti
     * @param requestAudiolibroModifica
     * @param result
     * @param request
     * @return ok status code e json response relativo, in caso di rimozione dai preferiti effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return bad request status code e messaggio relativo, in caso di audiolibro già non preferito
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PatchMapping(value = "rimuoviDaiPreferiti", consumes = "application/json")
    public ResponseEntity<?> rimuoviDaiPreferiti(@Valid @RequestBody RequestAudiolibroModifica requestAudiolibroModifica, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Audiolibro audiolibroModificato = audiolibroService.rimuoviDaiPreferiti(requestAudiolibroModifica.getIdAudiolibro(), request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroModificato, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroGiaPreferito e) {
            return new ResponseEntity<String >(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch(AudiolibroNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per settare la visibilità di un audiolibro a pubblico
     * @param requestAudiolibroModifica
     * @param result
     * @param request
     * @return ok status code e json responde relativo, in caso di settaggio di visibilità a pubblico effettuato correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return bad request status code e messaggio relativo, in caso di audiolibro che è già pubblico
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PatchMapping(value = "rendiPubblico", consumes = "application/json")
    public ResponseEntity<?> rendiPubblico(@Valid @RequestBody RequestAudiolibroModifica requestAudiolibroModifica, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Audiolibro audiolibroModificato = audiolibroService.rendiPubblico(requestAudiolibroModifica.getIdAudiolibro(), request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroModificato, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroGiaPubblico e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch(AudiolibroNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per settare la visibilità di un audiolibro a non pubblico
     * @param requestAudiolibroModifica
     * @param result
     * @param request
     * @return ok status code e json responde relativo, in caso di settaggio di visibilità a non pubblico effettuato correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return bad request status code e messaggio relativo, in caso di audiolibro che è già non pubblico
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PatchMapping(value = "rendiNonPubblico", consumes = "application/json")
    public ResponseEntity<?> rendiNonPubblico(@Valid @RequestBody RequestAudiolibroModifica requestAudiolibroModifica, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Audiolibro audiolibroModificato = audiolibroService.rendiNonPubblico(requestAudiolibroModifica.getIdAudiolibro(), request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroModificato, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroGiaPubblico e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch(AudiolibroNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per condividere un audiolibro con una cerchia specifica di utenti
     * @param requestAudiolibroCondivisione
     * @param result
     * @param request
     * @return ok status code e json responde relativo, in caso di condivisione con una cerchia di utenti effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return bad request status code e messaggio relativo, in caso di audiolibro che è già stato condiviso con uno degli utenti indicati
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return not found status code e messaggio relativo, in caso di utente/utenti non registrati nel sistema
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "condividi", consumes = "application/json")
    public ResponseEntity<?> condividi(@Valid @RequestBody RequestAudiolibroCondivisione requestAudiolibroCondivisione, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            List<String> usernameList = new ArrayList<String>();
            for(RequestUtenteIdentificativo requestUtenteIdentificativo : requestAudiolibroCondivisione.getUtenti()) {
                usernameList.add(requestUtenteIdentificativo.getUsername());
            }
            Audiolibro audiolibroCondiviso = audiolibroService.condividi(requestAudiolibroCondivisione.getIdAudiolibro(), usernameList, request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroCondiviso, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroGiaCondiviso | AudiolibroGiaPubblico e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch(AudiolibroNonTrovato | UtenteNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per rimuovere la condivisione di un audiolibro da una cerchia specifica di utenti
     * @param requestAudiolibroCondivisione
     * @param result
     * @param request
     * @return ok status code e json responde relativo, in caso di rimozione di condivisioni effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return bad request status code e messaggio relativo, in caso di audiolibro che è già in non condivisione con uno degli utenti indicati
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return not found status code e messaggio relativo, in caso di utente/utenti non registrati nel sistema
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "rimuoviCondivisioni", consumes = "application/json")
    public ResponseEntity<?> rimuoviCondivisioni(@Valid @RequestBody RequestAudiolibroCondivisione requestAudiolibroCondivisione, BindingResult result, HttpServletRequest request) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try{
            List<String> usernameList = new ArrayList<String>();
            for(RequestUtenteIdentificativo requestUtenteIdentificativo : requestAudiolibroCondivisione.getUtenti()) {
                usernameList.add(requestUtenteIdentificativo.getUsername());
            }
            Audiolibro audiolibroNonCondiviso = audiolibroService.rimuoviCondivisioni(requestAudiolibroCondivisione.getIdAudiolibro(), usernameList, request);
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroNonCondiviso, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroGiaCondiviso e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch(AudiolibroNonTrovato | UtenteNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servizio esposto per registrare l'ascolto di un audiolibro da parte dell'utente loggato
     * @param requestAudiolibroAscolta
     * @param result
     * @param request
     * @return ok status code e json responde relativo, in caso di registrazione di ascolto di un audiolibro effettuata correttamente
     * @return bad request status code e messaggio relativo, in caso di json request non valido
     * @return unhauthorized status code e messaggio relativo, in caso di login fallito
     * @return not found status code e messaggio relativo, in caso di audiolibro inesistente
     * @return internal server error status code e messaggio relativo, in caso di errore generico
     */
    @PostMapping(value = "ascolta", consumes = "application/json")
    public ResponseEntity<?> ascolta(@Valid @RequestBody RequestAudiolibroAscolta requestAudiolibroAscolta, BindingResult result, HttpServletRequest request, @RequestParam(defaultValue = "false") boolean metadataOnly) {
        if(result.hasErrors()) {
            return new ResponseEntity<String>(Utils.ERR_JSON_REQUEST, HttpStatus.BAD_REQUEST);
        }
        try {
            Audiolibro audiolibroAscoltato = audiolibroService.ascolta(requestAudiolibroAscolta.getIdAudiolibro(), requestAudiolibroAscolta.getSecondi(), request);
            if (metadataOnly) return ResponseEntity.noContent().build();
            ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroAscoltato, Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil));
            return new ResponseEntity<ResponseAudiolibro>(responseAudiolibro, HttpStatus.OK);
        } catch(AudiolibroNonTrovato e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Utils.ERR_JSON_REQUEST);
        } catch(Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

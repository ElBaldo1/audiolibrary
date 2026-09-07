package com.ingswpf.audiolibrarybe.service;

import com.ingswpf.audiolibrarybe.exception.*;
import com.ingswpf.audiolibrarybe.model.Ascolto;
import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.repository.AscoltoRepo;
import com.ingswpf.audiolibrarybe.repository.AudiolibroRepo;
import com.ingswpf.audiolibrarybe.repository.UtenteRepo;
import com.ingswpf.audiolibrarybe.security.JwtTokenUtil;
import com.ingswpf.audiolibrarybe.strategy.listaAudiolibri.ContextListaAudiolibri;
import com.ingswpf.audiolibrarybe.strategy.listaAudiolibri.ListaAudiolibriPreferiti;
import com.ingswpf.audiolibrarybe.strategy.listaAudiolibri.ListaAudiolibriHome;
import com.ingswpf.audiolibrarybe.strategy.listaAudiolibri.ListaAudiolibriNetwork;
import com.ingswpf.audiolibrarybe.strategy.ricercaAudiolibri.ContextRicercaAudiolibri;
import com.ingswpf.audiolibrarybe.strategy.ricercaAudiolibri.RicercaAudiolibriPerDataInserimento;
import com.ingswpf.audiolibrarybe.strategy.ricercaAudiolibri.RicercaAudiolibriPerTitolo;
import com.ingswpf.audiolibrarybe.strategy.ricercaAudiolibri.RicercaAudiolibriPerTitoloEDataInserimento;
import com.ingswpf.audiolibrarybe.utils.Utils;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// classe rappresentante il service per i servizi esposti in AudiolibroController
@Service
@org.springframework.transaction.annotation.Transactional
public class AudiolibroService {

    public AudiolibroService(AudiolibroRepo audiolibroRepo, UtenteRepo utenteRepo, AscoltoRepo ascoltoRepo, JwtTokenUtil jwtTokenUtil) {
        this.audiolibroRepo = audiolibroRepo;
        this.utenteRepo = utenteRepo;
        this.ascoltoRepo = ascoltoRepo;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    private final AudiolibroRepo audiolibroRepo;

    private final UtenteRepo utenteRepo;

    private final AscoltoRepo ascoltoRepo;

    private final JwtTokenUtil jwtTokenUtil;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Audiolibro getAccessibleBook(Integer id, HttpServletRequest request) {
        Audiolibro book = audiolibroRepo.findByIdAndEliminato(id, false).orElseThrow(AudiolibroNonTrovato::new);
        Utente user = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        if (!user.equals(book.getCreatore()) && !Boolean.TRUE.equals(book.getFlgPubblico())
                && !user.getAudiolibroCondivisi().contains(book)) throw new AudiolibroNonTrovato();
        return book;
    }

    public Audiolibro inserisci(com.ingswpf.audiolibrarybe.dto.request.RequestAudiolibroInserisci input,
                               HttpServletRequest request) {
        Audiolibro book = inserisci(input.getTitolo(), input.getDescrizione(), input.getCopertina(), input.getAudio(), request);
        book.setAutore(input.getAutore());
        book.setDurata(input.getDurata());
        book.setMimeType(input.getMimeType());
        return book;
    }

    /**
     * metodo per l'inserimento di un audiolibro
     * @param titolo
     * @param descrizione
     * @param copertina
     * @param audio
     * @param request
     * @return audiolibro inserito
     */
    public Audiolibro inserisci(String titolo, String descrizione, String copertina, String audio, HttpServletRequest request) {
        Audiolibro audiolibro = new Audiolibro();
        audiolibro.setTitolo(titolo);
        audiolibro.setDescrizione(descrizione);
        audiolibro.setFlgPubblico(false);
        audiolibro.setEliminato(false);
        audiolibro.setCopertina(Base64.getDecoder().decode(copertina));
        audiolibro.setAudio(Base64.getDecoder().decode(audio));
        audiolibro.setDataInserimento(LocalDate.now());
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        audiolibro.setCreatore(utente);
        return audiolibroRepo.save(audiolibro);
    }

    /**
     * metodo per la modifica dei campi di un audiolibro
     * @param idAudiolibro
     * @param titolo
     * @param descrizione
     * @param copertina
     * @param request
     * @return audiolibro modificato
     */
    public Audiolibro modifica(Integer idAudiolibro, String titolo, String descrizione, String copertina, HttpServletRequest request){
        Optional<Audiolibro> optionalAudiolibroDaModificare = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Audiolibro audiolibroDaModificare = (!optionalAudiolibroDaModificare.isPresent()) ? new Audiolibro() : optionalAudiolibroDaModificare.get();
        if(!optionalAudiolibroDaModificare.isPresent() || !utente.equals(audiolibroDaModificare.getCreatore())) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te.");
        }
        if(!titolo.isEmpty()) audiolibroDaModificare.setTitolo(titolo);
        if(!descrizione.isEmpty()) audiolibroDaModificare.setDescrizione(descrizione);
        if(!copertina.isEmpty()) audiolibroDaModificare.setCopertina(Base64.getDecoder().decode(copertina));
        audiolibroRepo.save(audiolibroDaModificare);
        return audiolibroDaModificare;
    }

    /**
     * metodo per l'eliminazione di un audiolibro
     * @param idAudiolibro
     * @param request
     * @return audiolibro rimosso
     */
    public Audiolibro rimuovi(Integer idAudiolibro, HttpServletRequest request){
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Optional<Audiolibro> optionalAudiolibroDaRimuovere = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Audiolibro audiolibroDaRimuovere = (!optionalAudiolibroDaRimuovere.isPresent()) ? new Audiolibro() : optionalAudiolibroDaRimuovere.get();
        if(!optionalAudiolibroDaRimuovere.isPresent() || !getListaNotEliminati(utente.getAudiolibroCreati()).contains(audiolibroDaRimuovere)) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te.");
        }
        audiolibroDaRimuovere.setEliminato(true);
        audiolibroRepo.save(audiolibroDaRimuovere);
        return audiolibroDaRimuovere;
    }

    /**
     * metodo per ottenere la lista di audiolibri non eliminati
     * @param lista
     * @return lista di audiolibro non eliminati
     */
    public List<Audiolibro> getListaNotEliminati(List<Audiolibro> lista) {
        return lista.stream()
                .filter(audiolibro -> !Boolean.TRUE.equals(audiolibro.getEliminato()))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * metodo per ordinare una lista di audiolibri in modo decrescente in base alla data di inserimento
     * @param audiolibroList
     */
    public void ordinaListaAudiolibroDecrescente(List<Audiolibro> audiolibroList) {
        Collections.sort(audiolibroList,
                (audiolibro1, audiolibro2) -> Integer.signum(audiolibro2.getDataInserimento().compareTo(audiolibro1.getDataInserimento())));
    }

    /**
     * metodo per ricercare uno o più audiolibri
     * @param titolo
     * @param dataInserimento
     * @param tipo
     * @param request
     * @return lista di audiolibri trovati in base alla ricerca effettuata
     */
    public List<Audiolibro> ricerca(String titolo, String dataInserimento, Integer tipo, HttpServletRequest request) {
        List<Audiolibro> lista = lista(tipo, request);
        ContextRicercaAudiolibri contextRicercaAudiolibri = new ContextRicercaAudiolibri();
        if(titolo != null && dataInserimento != null) {
            contextRicercaAudiolibri = new ContextRicercaAudiolibri(new RicercaAudiolibriPerTitoloEDataInserimento());
        } else if(titolo != null && dataInserimento == null) {
            contextRicercaAudiolibri = new ContextRicercaAudiolibri(new RicercaAudiolibriPerTitolo());
        } else if(titolo == null && dataInserimento != null) {
            //ricerca per data inserimento - ordine decrescente per dataInserimento
            contextRicercaAudiolibri = new ContextRicercaAudiolibri(new RicercaAudiolibriPerDataInserimento());
        }
        lista = contextRicercaAudiolibri.eseguiStrategia(lista, titolo, dataInserimento);
        ordinaListaAudiolibroDecrescente(lista);
        return lista;
    }

    /**
     * metodo per ottenere una lista di audiolibri in base ad una delle tre pagine
     * @param tipo
     * @param request
     * @return lista di audiolibri della pagina specificata
     */
    public List<Audiolibro> lista(Integer tipo, HttpServletRequest request) {
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        ContextListaAudiolibri contextListaAudiolibri = new ContextListaAudiolibri();
        switch(tipo) {
            case 1:
                contextListaAudiolibri = new ContextListaAudiolibri(new ListaAudiolibriHome());
                break;
            case 2:
                contextListaAudiolibri = new ContextListaAudiolibri(new ListaAudiolibriNetwork());
                break;
            case 3:
                contextListaAudiolibri = new ContextListaAudiolibri(new ListaAudiolibriPreferiti());
                break;
            default:
                throw new IllegalArgumentException("Invalid catalog type");
        }
        List<Audiolibro> lista = getListaNotEliminati(contextListaAudiolibri.eseguiStrategia(utente, audiolibroRepo));
        ordinaListaAudiolibroDecrescente(lista);
        return lista;
    }

    /**
     * metodo per aggiungere un audiolibro ai preferiti
     * @param idAudiolibro
     * @param request
     * @return audiolibro aggiunto ai preferiti
     */
    public Audiolibro aggiungiAiPreferiti(Integer idAudiolibro, HttpServletRequest request) {
        Optional<Audiolibro> optionalAudiolibroDaModificare = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Audiolibro audiolibroDaModificare = (!optionalAudiolibroDaModificare.isPresent()) ? new Audiolibro() : optionalAudiolibroDaModificare.get();
        if(!optionalAudiolibroDaModificare.isPresent() || (!getListaNotEliminati(utente.getAudiolibroCreati()).contains(audiolibroDaModificare) && !getListaNotEliminati(utente.getAudiolibroCondivisi()).contains(audiolibroDaModificare) && !audiolibroDaModificare.getFlgPubblico())) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te o condiviso con te.");
        }
        if(getListaNotEliminati(utente.getAudiolibroPreferiti()).contains(audiolibroDaModificare)) {
            throw new AudiolibroGiaPreferito();
        }
        List<Audiolibro> audiolibroPreferitiList = utente.getAudiolibroPreferiti();
        audiolibroPreferitiList.add(audiolibroDaModificare);
        utente.setAudiolibroPreferiti(audiolibroPreferitiList);
        utenteRepo.save(utente);
        return audiolibroDaModificare;
    }

    /**
     * metodo per rimuovere un audiolibro dai preferiti
     * @param idAudiolibro
     * @param request
     * @return audiolibro rimosso dai preferiti
     */
    public Audiolibro rimuoviDaiPreferiti(Integer idAudiolibro, HttpServletRequest request) {
        Optional<Audiolibro> optionalAudiolibroDaModificare = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Audiolibro audiolibroDaModificare = (!optionalAudiolibroDaModificare.isPresent()) ? new Audiolibro() : optionalAudiolibroDaModificare.get();
        if(!optionalAudiolibroDaModificare.isPresent() || (!getListaNotEliminati(utente.getAudiolibroCreati()).contains(audiolibroDaModificare) && !getListaNotEliminati(utente.getAudiolibroCondivisi()).contains(audiolibroDaModificare) && !audiolibroDaModificare.getFlgPubblico())) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te o condiviso con te.");
        }
        if(!getListaNotEliminati(utente.getAudiolibroPreferiti()).contains(audiolibroDaModificare)) {
            throw new AudiolibroGiaPreferito("L'audiolibro non è ancora nella lista dei tuoi audiolibro preferiti.");
        }
        List<Audiolibro> audiolibroPreferitiList = utente.getAudiolibroPreferiti();
        audiolibroPreferitiList.remove(audiolibroDaModificare);
        utente.setAudiolibroPreferiti(audiolibroPreferitiList);
        utenteRepo.save(utente);
        return audiolibroDaModificare;
    }

    /**
     * metodo per settare la visibilità di un audiolibro a "pubblico"
     * @param idAudiolibro
     * @param request
     * @return audiolibro del quale si è cambiata la visibilità a "pubblico"
     */
    public Audiolibro rendiPubblico(Integer idAudiolibro, HttpServletRequest request) {
        Optional<Audiolibro> optionalAudiolibroDaModificare = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Audiolibro audiolibroDaModificare = (!optionalAudiolibroDaModificare.isPresent()) ? new Audiolibro() : optionalAudiolibroDaModificare.get();
        if(!optionalAudiolibroDaModificare.isPresent() || !getListaNotEliminati(utente.getAudiolibroCreati()).contains(audiolibroDaModificare)) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te.");
        }
        if(audiolibroDaModificare.getFlgPubblico()) {
            throw new AudiolibroGiaPubblico();
        }
        audiolibroDaModificare.setFlgPubblico(true);
        List<Utente> utentiCondivisi = audiolibroDaModificare.getUtentiCondivisi();
        for(Utente ut : utentiCondivisi) {
            List<Audiolibro> audiolibroCondivisi = ut.getAudiolibroCondivisi();
            audiolibroCondivisi.remove(audiolibroDaModificare);
            ut.setAudiolibroCondivisi(audiolibroCondivisi);
            utenteRepo.save(ut);
        }
        utentiCondivisi.clear();
        audiolibroDaModificare.setUtentiCondivisi(utentiCondivisi);
        audiolibroRepo.save(audiolibroDaModificare);
        return audiolibroDaModificare;
    }

    /**
     * metodo per settare la visibilità di un audiolibro a "non pubblico"
     * @param idAudiolibro
     * @param request
     * @return audiolibro del quale si è cambiata la visibilità a "non pubblico"
     */
    public Audiolibro rendiNonPubblico(Integer idAudiolibro, HttpServletRequest request) {
        Optional<Audiolibro> optionalAudiolibroDaModificare = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Audiolibro audiolibroDaModificare = (!optionalAudiolibroDaModificare.isPresent()) ? new Audiolibro() : optionalAudiolibroDaModificare.get();
        if(!optionalAudiolibroDaModificare.isPresent() || !getListaNotEliminati(utente.getAudiolibroCreati()).contains(audiolibroDaModificare)) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te.");
        }
        if(!audiolibroDaModificare.getFlgPubblico()) {
            throw new AudiolibroGiaPubblico("L'audiolibro non è pubblico.");
        }
        audiolibroDaModificare.setFlgPubblico(false);
        List<Utente> utentiPreferiti = audiolibroDaModificare.getUtentiPreferiti();
        utentiPreferiti.removeIf(u -> {
            if (u.equals(audiolibroDaModificare.getCreatore())
                    || u.getAudiolibroCondivisi().contains(audiolibroDaModificare)) return false;
            u.getAudiolibroPreferiti().remove(audiolibroDaModificare);
            utenteRepo.save(u);
            return true;
        });
        audiolibroDaModificare.setUtentiPreferiti(utentiPreferiti);
        audiolibroRepo.save(audiolibroDaModificare);
        return audiolibroDaModificare;
    }

    /**
     * metodo per condividere un audiolibro con una cerchia specifica di utenti
     * @param idAudiolibro
     * @param usernameList
     * @param request
     * @return audiolibro condiviso
     */
    public Audiolibro condividi(Integer idAudiolibro, List<String> usernameList, HttpServletRequest request) {
        Optional<Audiolibro> optionalAudiolibroDaCondividere = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Audiolibro audiolibroDaCondividere = (!optionalAudiolibroDaCondividere.isPresent()) ? new Audiolibro() : optionalAudiolibroDaCondividere.get();
        if(!optionalAudiolibroDaCondividere.isPresent() || !getListaNotEliminati(utente.getAudiolibroCreati()).contains(audiolibroDaCondividere)) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te.");
        }
        if(audiolibroDaCondividere.getFlgPubblico()) {
            throw new AudiolibroGiaPubblico();
        }
        for(String username : usernameList) {
            Utente utenteDaCondividere = utenteRepo.findByUsername(username);
            if(utenteDaCondividere == null) {
                throw new UtenteNonTrovato();
            }
            if(getListaNotEliminati(utenteDaCondividere.getAudiolibroCondivisi()).contains(audiolibroDaCondividere) || getListaNotEliminati(utenteDaCondividere.getAudiolibroCreati()).contains(audiolibroDaCondividere)) {
                throw new AudiolibroGiaCondiviso();
            }
            List<Audiolibro> audiolibroCondivisi = utenteDaCondividere.getAudiolibroCondivisi();
            audiolibroCondivisi.add(audiolibroDaCondividere);
            utenteDaCondividere.setAudiolibroCondivisi(audiolibroCondivisi);
            utenteRepo.save(utenteDaCondividere);
        }
        return audiolibroDaCondividere;
    }

    /**
     * metodo per rimuovere la condivisione di un audiolibro a uno o più utenti
     * @param idAudiolibro
     * @param usernameList
     * @param request
     * @return audiolibro da cui si è tolta la condivisione
     */
    public Audiolibro rimuoviCondivisioni(Integer idAudiolibro, List<String> usernameList, HttpServletRequest request) {
        Optional<Audiolibro> optionalAudiolibroDaRimuovereCondivisione = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Audiolibro audiolibroDaRimuovereCondivisione = (!optionalAudiolibroDaRimuovereCondivisione.isPresent()) ? new Audiolibro() : optionalAudiolibroDaRimuovereCondivisione.get();
        if(!optionalAudiolibroDaRimuovereCondivisione.isPresent() || !getListaNotEliminati(utente.getAudiolibroCreati()).contains(audiolibroDaRimuovereCondivisione)) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te.");
        }
        for(String username : usernameList) {
            Utente utenteDaRimuovereCondivisione = utenteRepo.findByUsername(username);
            if(utenteDaRimuovereCondivisione == null) {
                throw new UtenteNonTrovato();
            }
            if(!getListaNotEliminati(utenteDaRimuovereCondivisione.getAudiolibroCondivisi()).contains(audiolibroDaRimuovereCondivisione)) {
                throw new AudiolibroGiaCondiviso("L'audiolibro non è condiviso con uno o più degli utenti da te specificati.");
            }
            List<Audiolibro> audiolibroCondivisioniRimosse = utenteDaRimuovereCondivisione.getAudiolibroCondivisi();
            audiolibroCondivisioniRimosse.remove(audiolibroDaRimuovereCondivisione);
            utenteDaRimuovereCondivisione.setAudiolibroCondivisi(audiolibroCondivisioniRimosse);
            utenteRepo.save(utenteDaRimuovereCondivisione);
        }
        List<Utente> utentiPreferiti = audiolibroDaRimuovereCondivisione.getUtentiPreferiti();
        utentiPreferiti.removeIf(u -> {
            if (!usernameList.contains(u.getUsername())
                    || Boolean.TRUE.equals(audiolibroDaRimuovereCondivisione.getFlgPubblico())
                    || u.equals(audiolibroDaRimuovereCondivisione.getCreatore())) return false;
            u.getAudiolibroPreferiti().remove(audiolibroDaRimuovereCondivisione);
            utenteRepo.save(u);
            return true;
        });
        audiolibroDaRimuovereCondivisione.setUtentiPreferiti(utentiPreferiti);
        return audiolibroDaRimuovereCondivisione;
    }

    /**
     * metodo per registrare l'ascolto di un audiolibro da parte dell'utente loggato
     * @param idAudiolibro
     * @param secondi
     * @param request
     * @return audiolibro che è stato ascoltato dall'utente loggato
     */
    public Audiolibro ascolta(Integer idAudiolibro, Integer secondi, HttpServletRequest request) {
        if (secondi == null || secondi < 0) throw new IllegalArgumentException("Invalid playback position");
        Optional<Audiolibro> optionalAudiolibroDaAscoltare = audiolibroRepo.findByIdAndEliminato(idAudiolibro, false);
        Utente utente = Utils.getUtenteFromHeader(request, utenteRepo, jwtTokenUtil);
        Audiolibro audiolibroDaAscoltare = (!optionalAudiolibroDaAscoltare.isPresent()) ? new Audiolibro() : optionalAudiolibroDaAscoltare.get();
        if(!optionalAudiolibroDaAscoltare.isPresent() || (!getListaNotEliminati(utente.getAudiolibroCreati()).contains(audiolibroDaAscoltare) && !getListaNotEliminati(utente.getAudiolibroCondivisi()).contains(audiolibroDaAscoltare) && !audiolibroDaAscoltare.getFlgPubblico())) {
            throw new AudiolibroNonTrovato("Non è stato trovato alcun audiolibro inserito da te o condiviso con te.");
        }
        List<Ascolto> ascoltoList = utente.getAscolti();
        List<Ascolto> audiolibroAscoltato = ascoltoList.stream()
                .filter(ascolto -> ascolto.getAudiolibro().equals(audiolibroDaAscoltare))
                .toList();
        Ascolto ascolto;
        if(audiolibroAscoltato.size() > 0) {
            ascolto = audiolibroAscoltato.get(0);
            int indexAudiolibroDaAscoltare = ascoltoList.indexOf(ascolto);
            ascolto.setSecondi(secondi);
            ascolto.setData(LocalDate.now());
            ascoltoList.set(indexAudiolibroDaAscoltare, ascolto);
        }
        else {
            ascolto = new Ascolto();
            ascolto.setAudiolibro(audiolibroDaAscoltare);
            ascolto.setSecondi(secondi);
            ascolto.setData(LocalDate.now());
            ascolto.setUtente(utente);
            ascoltoList.add(ascolto);
        }
        ascolto.setUpdatedAt(java.time.Instant.now());
        ascoltoRepo.save(ascolto);
        if (!audiolibroDaAscoltare.getAscolti().contains(ascolto)) {
            audiolibroDaAscoltare.getAscolti().add(ascolto);
        }
        return audiolibroDaAscoltare;
    }
}

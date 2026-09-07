package com.ingswpf.audiolibrarybe.dto.mapper;

import com.ingswpf.audiolibrarybe.dto.request.*;
import com.ingswpf.audiolibrarybe.dto.response.ResponseAscolto;
import com.ingswpf.audiolibrarybe.dto.response.ResponseAudiolibro;
import com.ingswpf.audiolibrarybe.model.Ascolto;
import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.model.Utente;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * classe che contiene metodi statici utilizzabili per passare dai model ai dto utilizzati nell'AudiolibroController
 */
public class AudiolibroMapper {
    /**
     * metodo per ottenere il DTO ResponseAudiolibro
     */
    public static ResponseAudiolibro toResponseAudiolibro(Audiolibro audiolibro, Utente utente) {
        return toResponseAudiolibro(audiolibro, utente, false);
    }

    public static ResponseAudiolibro toResponseAudiolibro(Audiolibro audiolibro, Utente utente, boolean metadataOnly) {
        List<Ascolto> ascoltoList = utente.getAscolti();
        List<Ascolto> ascoltoAudiolibroList = ascoltoList.stream()
                .filter(ascolto -> ascolto.getAudiolibro().equals(audiolibro))
                .toList();
        Ascolto ascoltoAudiolibro = (ascoltoAudiolibroList.size() > 0) ? ascoltoAudiolibroList.get(0) : null;
        ResponseAscolto responseAscolto = new ResponseAscolto.ResponseAscoltoBuilder(
                (ascoltoAudiolibro != null) ? ascoltoAudiolibro.getData().toString() : null,
                (ascoltoAudiolibro != null) ? ascoltoAudiolibro.getSecondi() : 0
        ).build();
        ResponseAudiolibro responseAudiolibro = new ResponseAudiolibro.ResponseAudiolibroBuilder(
                audiolibro.getId(),
                utente.getAudiolibroPreferiti().contains(audiolibro),
                audiolibro.getFlgPubblico(),
                audiolibro.getTitolo(),
                audiolibro.getDescrizione(),
                metadataOnly ? null : Base64.getEncoder().encodeToString(audiolibro.getCopertina()),
                metadataOnly ? null : Base64.getEncoder().encodeToString(audiolibro.getAudio()),
                audiolibro.getDataInserimento().toString(),
                UtenteMapper.toResponseUtente(audiolibro.getCreatore()),
                responseAscolto
        ).build();
        responseAudiolibro.setAutore(audiolibro.getAutore());
        responseAudiolibro.setDurata(audiolibro.getDurata());
        responseAudiolibro.setMimeType(audiolibro.getMimeType());
        responseAscolto.setUpdatedAt(ascoltoAudiolibro == null || ascoltoAudiolibro.getUpdatedAt() == null
                ? null : ascoltoAudiolibro.getUpdatedAt().toString());
        return responseAudiolibro;
    }

    /**
     * metodo per ottenere una lista di DTO ResponseAudiolibro
     */
    public static List<ResponseAudiolibro> toResponseAudiolibroList(List<Audiolibro> audiolibroList, Utente utente) {
        List<ResponseAudiolibro> responseAudiolibroList = new ArrayList<ResponseAudiolibro>();
        for(Audiolibro audiolibro : audiolibroList) {
            ResponseAudiolibro responseAudiolibro = toResponseAudiolibro(audiolibro, utente);
            responseAudiolibroList.add(responseAudiolibro);
        }
        return responseAudiolibroList;
    }

    /**
     * metodo per ottenere il DTO RequestAudiolibroInserisci
     */
    public static RequestAudiolibroInserisci toRequestAudiolibroInserisci(Audiolibro audiolibro) {
        RequestAudiolibroInserisci requestAudiolibroInserisci = new RequestAudiolibroInserisci.RequestAudiolibroInserisciBuilder(
                audiolibro.getTitolo(),
                audiolibro.getDescrizione(),
                new String(Base64.getEncoder().encode(audiolibro.getCopertina()), StandardCharsets.UTF_8),
                new String(Base64.getEncoder().encode(audiolibro.getAudio()), StandardCharsets.UTF_8)
        ).build();
        return requestAudiolibroInserisci;
    }

    /**
     * metodo per ottenere il DTO RequestAudiolibroModifica
     */
    public static RequestAudiolibroModifica toRequestAudiolibroModifica(Audiolibro audiolibro) {
        RequestAudiolibroModifica requestAudiolibroModifica = new RequestAudiolibroModifica();
        requestAudiolibroModifica.setIdAudiolibro(audiolibro.getId());
        return requestAudiolibroModifica;
    }

    public static RequestAudiolibroModificaCampi toRequestAudiolibroModificaCampi(Audiolibro audiolibro) {
        RequestAudiolibroModificaCampi requestAudiolibroModificaCampi = new RequestAudiolibroModificaCampi();
        requestAudiolibroModificaCampi.setIdAudiolibro(audiolibro.getId());
        requestAudiolibroModificaCampi.setCopertina(new String(Base64.getEncoder().encode(audiolibro.getCopertina()), StandardCharsets.UTF_8));
        requestAudiolibroModificaCampi.setTitolo(audiolibro.getTitolo());
        requestAudiolibroModificaCampi.setDescrizione(audiolibro.getDescrizione());
        return requestAudiolibroModificaCampi;
    }

    /**
     * metodo per ottenere il DTO RequestAudiolibroRicerca
     */
    public static RequestAudiolibroRicerca toRequestAudiolibroRicerca(Audiolibro audiolibro, Integer tipo) {
        RequestAudiolibroRicerca requestAudiolibroRicerca = new RequestAudiolibroRicerca();
        requestAudiolibroRicerca.setTipo(tipo);
        requestAudiolibroRicerca.setTitolo(audiolibro.getTitolo());
        requestAudiolibroRicerca.setDataInserimento(audiolibro.getDataInserimento().toString());
        return requestAudiolibroRicerca;
    }

    /**
     * metodo per ottenere il DTO RequestAudiolibroLista
     */
    public static RequestAudiolibroLista toRequestAudiolibroLista(Integer tipo) {
        RequestAudiolibroLista requestAudiolibroLista = new RequestAudiolibroLista();
        requestAudiolibroLista.setTipo(tipo);
        return requestAudiolibroLista;
    }

    /**
     * metodo per ottenere il DTO RequestAudiolibroCondivisione
     */
    public static RequestAudiolibroCondivisione toRequestAudiolibroCondivisione(Audiolibro audiolibro, List<Utente> utenteList) {
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = new RequestAudiolibroCondivisione();
        requestAudiolibroCondivisione.setIdAudiolibro(audiolibro.getId());
        List<RequestUtenteIdentificativo> requestUtenteIdentificativoList = new ArrayList<RequestUtenteIdentificativo>();
        for(Utente utente : utenteList) {
            requestUtenteIdentificativoList.add(UtenteMapper.toRequestUtenteIdentificativo(utente));
        }
        requestAudiolibroCondivisione.setUtenti(requestUtenteIdentificativoList);
        return requestAudiolibroCondivisione;
    }

    /**
     * metodo per ottenere il DTO RequestAudiolibroAscolta
     */
    public static RequestAudiolibroAscolta toRequestAudiolibroAscolta(Audiolibro audiolibro, Integer secondi) {
        RequestAudiolibroAscolta requestAudiolibroAscolta = new RequestAudiolibroAscolta.RequestAudiolibroAscoltaBuilder(
                audiolibro.getId(),
                secondi
        ).build();
        return requestAudiolibroAscolta;
    }
}

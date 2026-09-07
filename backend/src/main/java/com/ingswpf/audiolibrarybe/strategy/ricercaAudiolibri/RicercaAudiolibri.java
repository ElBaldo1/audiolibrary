package com.ingswpf.audiolibrarybe.strategy.ricercaAudiolibri;

import com.ingswpf.audiolibrarybe.model.Audiolibro;
import java.util.List;

/**
 * interfaccia rappresentante la generalizzazione dell'operazione per effettuare la ricerca di audiolibri
 */
public interface RicercaAudiolibri {
    List<Audiolibro> ricercaAudiolibri(List<Audiolibro> listaAudiolibri, String titolo, String dataInserimento);
}

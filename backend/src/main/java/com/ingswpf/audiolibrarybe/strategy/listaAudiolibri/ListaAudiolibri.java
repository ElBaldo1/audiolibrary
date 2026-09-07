package com.ingswpf.audiolibrarybe.strategy.listaAudiolibri;

import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.repository.AudiolibroRepo;
import java.util.List;

/**
 * interfaccia rappresentante la generalizzazione dell'operazione per ottenere la lista di audiolibri
 */
public interface ListaAudiolibri {
    List<Audiolibro> getListaAudiolibri(Utente utente, AudiolibroRepo audiolibroRepo);
}

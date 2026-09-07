package com.ingswpf.audiolibrarybe.strategy.listaAudiolibri;

import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.repository.AudiolibroRepo;
import java.util.List;

/**
 * classe rappresentante il context del pattern strategy per lo use case relativo alla lista degli audiolibri
 */
public class ContextListaAudiolibri {
    private ListaAudiolibri listaAudiolibri;

    public ContextListaAudiolibri() {}

    public ContextListaAudiolibri(ListaAudiolibri listaAudiolibri) {
        this.listaAudiolibri = listaAudiolibri;
    }

    /**
     * metodo che esegue la corretta strategy per ottenere la lista degli audiolibri a seconda del contesto relativo
     */
    public List<Audiolibro> eseguiStrategia(Utente utente, AudiolibroRepo audiolibroRepo) {
        return listaAudiolibri.getListaAudiolibri(utente, audiolibroRepo);
    }
}

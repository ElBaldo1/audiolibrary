package com.ingswpf.audiolibrarybe.strategy.listaAudiolibri;

import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.repository.AudiolibroRepo;
import java.util.List;

/**
 * classe rappresentante la specializzazione per ottenere la lista degli audiolibri nella favourite page
 */
public class ListaAudiolibriPreferiti implements ListaAudiolibri {
    @Override
    public List<Audiolibro> getListaAudiolibri(Utente utente, AudiolibroRepo audiolibroRepo) {
        return utente.getAudiolibroPreferiti();
    }
}

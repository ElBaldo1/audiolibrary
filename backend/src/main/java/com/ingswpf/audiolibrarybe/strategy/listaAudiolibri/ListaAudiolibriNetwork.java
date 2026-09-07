package com.ingswpf.audiolibrarybe.strategy.listaAudiolibri;

import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.repository.AudiolibroRepo;
import java.util.ArrayList;
import java.util.List;

/**
 * classe rappresentante la specializzazione per ottenere la lista degli audiolibri nella network page
 */
public class ListaAudiolibriNetwork implements ListaAudiolibri {
    @Override
    public List<Audiolibro> getListaAudiolibri(Utente utente, AudiolibroRepo audiolibroRepo) {
        List<Audiolibro> listaNetwork = audiolibroRepo.findByFlgPubblico(true);
        listaNetwork = new ArrayList<Audiolibro>(listaNetwork.stream()
                .filter(audiolibro -> !audiolibro.getCreatore().equals(utente))
                .toList());
        if(utente.getAudiolibroCondivisi().size() > 0) {
            listaNetwork.addAll(utente.getAudiolibroCondivisi());
        }
        return listaNetwork;
    }
}

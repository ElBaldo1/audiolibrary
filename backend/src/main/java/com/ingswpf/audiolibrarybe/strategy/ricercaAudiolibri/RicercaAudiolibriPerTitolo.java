package com.ingswpf.audiolibrarybe.strategy.ricercaAudiolibri;

import com.ingswpf.audiolibrarybe.model.Audiolibro;
import java.util.List;
import java.util.stream.Collectors;

/**
 * classe rappresentante la specializzazione per effettuare la ricerca degli audiolibri per titolo
 */
public class RicercaAudiolibriPerTitolo implements RicercaAudiolibri {
    @Override
    public List<Audiolibro> ricercaAudiolibri(List<Audiolibro> listaAudiolibri, String titolo, String dataInserimento) {
        return listaAudiolibri.stream()
                .filter(audiolibro -> audiolibro.getTitolo().equals(titolo))
                .collect(Collectors.toList());
    }
}

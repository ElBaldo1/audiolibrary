package com.ingswpf.audiolibrarybe.strategy.ricercaAudiolibri;

import com.ingswpf.audiolibrarybe.model.Audiolibro;
import java.util.List;

/**
 * classe rappresentante il context del pattern strategy per lo use case relativo alla ricerca degli audiolibri
 */
public class ContextRicercaAudiolibri {
    private RicercaAudiolibri ricercaAudiolibri;

    public ContextRicercaAudiolibri() {}

    public ContextRicercaAudiolibri(RicercaAudiolibri ricercaAudiolibri) {
        this.ricercaAudiolibri = ricercaAudiolibri;
    }

    /**
     * metodo che esegue la corretta strategy per effettuare la ricerca degli audiolibri a seconda del contesto relativo
     */
    public List<Audiolibro> eseguiStrategia(List<Audiolibro> listaAudiolibri, String titolo, String dataInserimento) {
        return ricercaAudiolibri.ricercaAudiolibri(listaAudiolibri, titolo, dataInserimento);
    }
}

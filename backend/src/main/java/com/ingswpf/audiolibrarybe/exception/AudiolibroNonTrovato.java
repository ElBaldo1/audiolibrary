package com.ingswpf.audiolibrarybe.exception;

/**
 * classe caratterizzante l'eccezione che viene lanciata nel caso in cui si tenta di effettuare un'operazione su un audiolibro inesistente
 */
public class AudiolibroNonTrovato extends RuntimeException {
    public AudiolibroNonTrovato() {
        super("Non è stato trovato alcun audiolibro.");
    }

    public AudiolibroNonTrovato(String message) {
        super(message);
    }
}

package com.ingswpf.audiolibrarybe.exception;

/**
 * classe caratterizzante l'eccezione che viene lanciata nel caso in cui si tenta di effettuare un'operazione verso un utente inesistente
 */
public class UtenteNonTrovato extends RuntimeException {
    public UtenteNonTrovato(String message) {
        super(message);
    }

    public UtenteNonTrovato() {
        super("Uno o più degli utenti specificati non sono registrati nel sistema.");
    }
}

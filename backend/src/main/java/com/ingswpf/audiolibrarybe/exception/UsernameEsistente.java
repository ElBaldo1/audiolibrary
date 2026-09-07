package com.ingswpf.audiolibrarybe.exception;

/**
 * classe caratterizzante l'eccezione che viene lanciata nel caso in cui si tenta di utilizzare un username già utilizzato da un altro utente
 */
public class UsernameEsistente extends RuntimeException {
    public UsernameEsistente(String message) {
        super(message);
    }

    public UsernameEsistente() {
        super("Un utente con lo stesso username è già registrato nel sistema.");
    }
}

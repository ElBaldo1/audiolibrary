package com.ingswpf.audiolibrarybe.exception;

/**
 * classe caratterizzante l'eccezione che viene lanciata nel caso in cui non viene trovato un determinato jwt token
 */
public class TokenNonTrovato extends RuntimeException {
    public TokenNonTrovato() {
        super("Il token inserito non è stato trovato nella lista dei token esistenti.");
    }

    public TokenNonTrovato(String message) {
        super(message);
    }
}

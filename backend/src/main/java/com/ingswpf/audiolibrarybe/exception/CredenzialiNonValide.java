package com.ingswpf.audiolibrarybe.exception;

/**
 * classe caratterizzante l'eccezione che viene lanciata nel caso in cui vengnono sbagliate le credenziali di accesso al sistema
 */
public class CredenzialiNonValide extends RuntimeException {
    public CredenzialiNonValide(String message) {
        super(message);
    }

    public CredenzialiNonValide() {
        super("Credenziali non valide: login fallito.");
    }
}

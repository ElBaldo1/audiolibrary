package com.ingswpf.audiolibrarybe.exception;

/**
 * classe caratterizzante l'eccezione che viene lanciata nel caso in cui si tenta di aggiungere ai preferiti un audiolibro già preferito
 */
public class AudiolibroGiaPreferito extends RuntimeException {
    public AudiolibroGiaPreferito() {
        super("L'audiolibro è già stato inserito nella lista dei tuoi audiolibro preferiti.");
    }

    public AudiolibroGiaPreferito(String message) {
        super(message);
    }
}

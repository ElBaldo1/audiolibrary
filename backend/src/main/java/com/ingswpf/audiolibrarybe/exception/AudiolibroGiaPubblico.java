package com.ingswpf.audiolibrarybe.exception;

/**
 * classe caratterizzante l'eccezione che viene lanciata nel caso in cui si tenta di rendere pubblico un audiolibro già pubblico
 */
public class AudiolibroGiaPubblico extends RuntimeException {
    public AudiolibroGiaPubblico() {
        super("La visibilità dell'audiolibro è già stata impostata come pubblica.");
    }

    public AudiolibroGiaPubblico(String message) {
        super(message);
    }
}

package com.ingswpf.audiolibrarybe.exception;

/**
 * classe caratterizzante l'eccezione che viene lanciata nel caso in cui si tenta di condividere un audiolibro già condiviso
 */
public class AudiolibroGiaCondiviso extends RuntimeException {
    public AudiolibroGiaCondiviso() {
        super("L'audiolibro è stato già condiviso con uno o più degli utenti specificati.'");
    }

    public AudiolibroGiaCondiviso(String message) {
        super(message);
    }
}

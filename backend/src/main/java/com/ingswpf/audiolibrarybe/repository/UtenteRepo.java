package com.ingswpf.audiolibrarybe.repository;

import com.ingswpf.audiolibrarybe.model.Utente;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * repository per operazioni sul db inerenti alla entity Utente
 */
@Repository
public interface UtenteRepo extends CrudRepository<Utente, Integer> {
    Utente findByUsername(String username);

    Utente findByEmail(String email);
}

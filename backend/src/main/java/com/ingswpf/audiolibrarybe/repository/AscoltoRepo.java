package com.ingswpf.audiolibrarybe.repository;

import com.ingswpf.audiolibrarybe.model.Ascolto;
import com.ingswpf.audiolibrarybe.model.AscoltoKey;
import org.springframework.data.repository.CrudRepository;

/**
 * repository per operazioni sul db inerenti alla entity Ascolto
 */
public interface AscoltoRepo extends CrudRepository<Ascolto, AscoltoKey> {
}

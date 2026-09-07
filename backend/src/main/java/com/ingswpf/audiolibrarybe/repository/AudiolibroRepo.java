package com.ingswpf.audiolibrarybe.repository;

import com.ingswpf.audiolibrarybe.model.Audiolibro;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * repository per operazioni sul db inerenti alla entity Audiolibro
 */
@Repository
public interface AudiolibroRepo extends CrudRepository<Audiolibro, Integer> {
    List<Audiolibro> findByFlgPubblico(Boolean flg_pubblico);

    Optional<Audiolibro> findByIdAndEliminato(Integer id, Boolean eliminato);
}

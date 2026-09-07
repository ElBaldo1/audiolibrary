package com.ingswpf.audiolibrarybe.repository;

import com.ingswpf.audiolibrarybe.model.JwtNotExpired;
import com.ingswpf.audiolibrarybe.model.Utente;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * repository per operazioni sul db inerenti alla entity JwtNotExpired
 */
@Repository
public interface JwtNotExpiredRepo extends CrudRepository<JwtNotExpired, Integer> {
    JwtNotExpired findByToken(String token);

    @Modifying
    @Transactional
    @Query("update JwtNotExpired j set j.expired = true where j.id =:id")
    void invalidaJwt(@Param("id") int id);

    @Modifying
    @Transactional
    @Query("update JwtNotExpired j set j.expired = false where j.id =:id")
    void validaJwt(@Param("id") int id);

    List<JwtNotExpired> findByUtente(Utente utente);
}

package com.ingswpf.audiolibrarybe.stub;

import com.ingswpf.audiolibrarybe.model.Ascolto;
import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.model.Utente;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;

public class TestFixtures {
    public static Utente getUtenteStub() {
        Utente utente = new Utente();
        utente.setId(1);
        utente.setUsername("test.listener");
        utente.setEmail("listener@example.com");
        utente.setNome("Test");
        utente.setCognome("Listener");
        utente.setPassword("Password?10?");
        utente.setAudiolibroPreferiti(new ArrayList<Audiolibro>());
        utente.setAscolti(new ArrayList<Ascolto>());
        utente.setAudiolibroCondivisi(new ArrayList<Audiolibro>());
        utente.setAudiolibroPreferiti(new ArrayList<Audiolibro>());
        utente.setAudiolibroCreati(new ArrayList<Audiolibro>());
        return utente;
    }

    public static Audiolibro getAudiolibroStub() throws IOException {
        Audiolibro audiolibro = new Audiolibro();
        audiolibro.setId(1);
        audiolibro.setTitolo("Episodio 1 - podcast animali");
        audiolibro.setDescrizione("Questo è il primo episodio del mio podcast preferito sugli animali.");
        audiolibro.setCreatore(getUtenteStub());
        audiolibro.setFlgPubblico(false);
        String base64Copertina = new String(Files.readAllBytes(new ClassPathResource("copertinaStub.txt").getFile().toPath()));
        audiolibro.setCopertina(Base64.getDecoder().decode(base64Copertina));
        audiolibro.setDataInserimento(LocalDate.now());
        String base64Audio = new String(Files.readAllBytes(new ClassPathResource("audioStub.txt").getFile().toPath()));
        audiolibro.setAudio(Base64.getDecoder().decode(base64Audio));
        audiolibro.setEliminato(false);
        audiolibro.setAscolti(new ArrayList<Ascolto>());
        audiolibro.setUtentiCondivisi(new ArrayList<Utente>());
        audiolibro.setUtentiPreferiti(new ArrayList<Utente>());
        return audiolibro;
    }

    public static Ascolto getAscoltoStub() throws IOException {
        Ascolto ascolto = new Ascolto();
        ascolto.setData(LocalDate.now());
        ascolto.setSecondi(12);
        ascolto.setAudiolibro(getAudiolibroStub());
        ascolto.setUtente(getUtenteStub());
        return ascolto;
    }

    public static String getJwtStub() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTIzNDU2Nzg5LCJuYW1lIjoiSm9zZXBoIn0.OpOSSw7e485LOP5PrzScxHb7SR6sAOMRckfFwi4rp7o";
    }
}

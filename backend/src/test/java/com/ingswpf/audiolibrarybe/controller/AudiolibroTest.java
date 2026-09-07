package com.ingswpf.audiolibrarybe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingswpf.audiolibrarybe.dto.mapper.AudiolibroMapper;
import com.ingswpf.audiolibrarybe.dto.mapper.UtenteMapper;
import com.ingswpf.audiolibrarybe.dto.request.*;
import com.ingswpf.audiolibrarybe.dto.response.ResponseAudiolibro;
import com.ingswpf.audiolibrarybe.dto.response.ResponseUtente;
import com.ingswpf.audiolibrarybe.exception.*;
import com.ingswpf.audiolibrarybe.model.Audiolibro;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.service.AudiolibroService;
import com.ingswpf.audiolibrarybe.stub.TestFixtures;
import com.ingswpf.audiolibrarybe.utils.Utils;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AudiolibroTest {
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private AudiolibroService audiolibroService;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /**
     Questo metodo testa l'inserimento di un audiolibro corretto nel sistema. Viene effettuato un mock del servizio di
     inserimento audiolibro, creando uno stub di un audiolibro e impostando che il metodo di inserimento debba restituirlo.
     Successivamente viene effettuata una richiesta POST all'endpoint "/audiolibro/inserisci" contenente i dati dell'audiolibro
     da inserire. Il metodo si aspetta che la risposta abbia uno stato 201 (Created) e che i dati inseriti corrispondano a quelli restituiti dallo stub.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroCorretto_postInserimento_thenInserimentoWith201() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                    .when(audiolibroService)
                    .inserisci(any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroInserisci requestAudiolibroInserisci = AudiolibroMapper.toRequestAudiolibroInserisci(audiolibroStub);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroInserisciAsString = new ObjectMapper().writeValueAsString(requestAudiolibroInserisci);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.post("/audiolibro/inserisci")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestAudiolibroInserisciAsString))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /** Reject a missing cover field; an explicitly empty cover remains optional. */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenCopertinaNull_postInserimento_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .inserisci(any(), any());
        RequestAudiolibroInserisci requestAudiolibroInserisci = AudiolibroMapper.toRequestAudiolibroInserisci(audiolibroStub);
        requestAudiolibroInserisci.setCopertina(null);
        String requestAudiolibroInserisciAsString = new ObjectMapper().writeValueAsString(requestAudiolibroInserisci);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/inserisci")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroInserisciAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Testa il comportamento del microservizio per la modifica di un audiolibro in base all'id fornito.
     Viene utilizzato un audiolibro stub per creare la richiesta di modifica e controllare la risposta.
     Il metodo esegue una chiamata HTTP PATCH all'URL "/audiolibro/modifica" utilizzando un oggetto RequestAudiolibroModificaCampi serializzato come contenuto.
     Si verifica che il risultato sia un codice HTTP 200 (OK) e che i valori nella risposta corrispondano a quelli previsti.
     @throws Exception in caso di errore durante l'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroCorretto_patchModifica_thenModificaWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        Utente utente = TestFixtures.getUtenteStub();
        audiolibroStub.setTitolo("Nuovo titolo");
        RequestAudiolibroModificaCampi requestAudiolibroModificaCampi = AudiolibroMapper.toRequestAudiolibroModificaCampi(audiolibroStub);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, utente);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(utente);
        String requestAudiolibroModificaCampiAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModificaCampi);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(utente);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/modifica")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroModificaCampiAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Test per verificare il comportamento del microservizio quando viene fornito un idAudiolibro errato durante una patch di modifica.
     Si utilizza il mock di AudiolibroService per simularne il comportamento e si invia una richiesta patch all'endpoint "/audiolibro/modifica" con un idAudiolibro impostato a 0.
     Si verifica che la risposta sia un errore 400 Bad Request.
     @throws Exception in caso di errore durante l'invocazione del metodo di test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroInModoSbagliato_patchModifica_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .modifica(any(), any(), any(), any(), any());
        RequestAudiolibroModificaCampi requestAudiolibroModificaCampi = AudiolibroMapper.toRequestAudiolibroModificaCampi(audiolibroStub);
        requestAudiolibroModificaCampi.setIdAudiolibro(0);
        String requestAudiolibroModificaCampiAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModificaCampi);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/modifica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaCampiAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Questo metodo testa il comportamento del microservizio in caso di richiesta di modifica di un audiolibro non esistente.
     Viene utilizzato uno stub per simulare l'errore "AudiolibroNonTrovato" generato dal servizio di modifica.
     La richiesta di modifica viene inviata al microservizio tramite una chiamata HTTP PATCH.
     Si aspetta un errore HTTP 404 (Not Found).
     @throws Exception in caso di problemi nell'invio della richiesta o nell'elaborazione della risposta.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonCreato_patchModifica_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        RequestAudiolibroModificaCampi requestAudiolibroModificaCampi = AudiolibroMapper.toRequestAudiolibroModificaCampi(audiolibroStub);
        String requestAudiolibroModificaCampiAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModificaCampi);
        given(audiolibroService.modifica(any(), any(), any(), any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/modifica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaCampiAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Questo metodo viene utilizzato per testare il comportamento del microservizio di eliminazione di un audiolibro.
     Verifica che se l'ID dell'audiolibro fornito è corretto, l'eliminazione avvenga con successo (HTTP status code 200).
     Il metodo utilizza il mock dell'AudiolibroService per simulare la rimozione dell'audiolibro e verifica che
     i dati dell'audiolibro e del suo creatore vengano restituiti correttamente.
     @throws Exception nel caso in cui ci siano problemi durante l'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroCorretto_postEliminazione_thenEliminazioneWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rimuovi(any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.post("/audiolibro/rimuovi")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroModificaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Questo metodo testa il comportamento del microservizio di eliminazione di un audiolibro.
     Verifica che, quando l'ID dell'audiolibro da eliminare viene fornito in modo errato,
     il sistema restituisca un errore HTTP 400 (Bad Request).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroInModoSbagliato_postEliminazione_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rimuovi(any(), any());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        requestAudiolibroModifica.setIdAudiolibro(0);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/rimuovi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Questo metodo testa il comportamento del microservizio in caso di richiesta di eliminazione di un audiolibro non esistente.
     Viene simulato un errore con l'eccezione "AudiolibroNonTrovato" quando viene richiamato il metodo "rimuovi" della classe "AudiolibroService".
     La richiesta HTTP viene effettuata con un POST sull'endpoint "/audiolibro/rimuovi", passando il corpo della richiesta come un oggetto di tipo "RequestAudiolibroModifica" serializzato in formato JSON.
     Si attende che la risposta HTTP abbia uno stato 404 (NOT FOUND).
     @throws Exception eventuali eccezioni durante l'esecuzione del test
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonCreato_postEliminazione_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        given(audiolibroService.rimuovi(any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/rimuovi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Test di unità per la richiesta di ricerca di un audiolibro con titolo, data di inserimento e tipo 1.
     Verifica che la richiesta sia effettuata con successo con codice HTTP 200 e che i dati restituiti siano corretti.
     Utilizza stubs e mocks per simulare il comportamento delle classi e delle dipendenze.
     @throws Exception eccezione generata dal test di perform
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenRequestAudiolibroRicercaCorrettaConTitoloEDataInserimentoETipo1_postRicerca_thenRicercaWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        List<Audiolibro> audiolibroStubList = new ArrayList<Audiolibro>();
        audiolibroStubList.add(audiolibroStub);
        doReturn(audiolibroStubList)
                .when(audiolibroService)
                .ricerca(any(), any(), any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroRicerca requestAudiolibroRicerca = AudiolibroMapper.toRequestAudiolibroRicerca(audiolibroStub, 1);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        List<ResponseAudiolibro> responseAudiolibroList = new ArrayList<ResponseAudiolibro>();
        responseAudiolibroList.add(responseAudiolibro);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroRicercaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroRicerca);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibroList(any(), any())).thenReturn(responseAudiolibroList);
            mvc.perform(MockMvcRequestBuilders.post("/audiolibro/ricerca")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroRicercaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$[0].preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$[0].titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$[0].descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$[0].copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$[0].audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$[0].dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$[0].creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$[0].creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$[0].creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$[0].ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$[0].ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Test per verificare il comportamento dell'API in caso di richiesta di ricerca di audiolibri con tipo sbagliato.
     Si verifica che venga restituito un errore HTTP 400.
     Il metodo utilizza uno stub per creare una lista di audiolibri fittizi e verifica la risposta del sistema
     alla richiesta di ricerca effettuata tramite il mock del servizio di audiolibri.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenTipoInModoSbagliato_postRicerca_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        List<Audiolibro> audiolibroStubList = new ArrayList<Audiolibro>();
        audiolibroStubList.add(audiolibroStub);
        doReturn(audiolibroStubList)
                .when(audiolibroService)
                .ricerca(any(), any(), any(), any());
        RequestAudiolibroRicerca requestAudiolibroRicerca = AudiolibroMapper.toRequestAudiolibroRicerca(audiolibroStub, 4);
        String requestAudiolibroRicercaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroRicerca);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/ricerca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroRicercaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Test per verificare che una richiesta di ricerca di un audiolibro non esistente restituisca un errore 404 (Not Found).
     Viene utilizzato un AudiolibroStub e viene simulata una chiamata all'AudiolibroService che restituisce un'eccezione di tipo AudiolibroNonTrovato.
     Viene poi effettuato un POST request all'endpoint "/audiolibro/ricerca" e viene verificato che il risultato sia un errore con stato 404.
     @throws Exception
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonEsistente_postRicerca_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        RequestAudiolibroRicerca requestAudiolibroRicerca = AudiolibroMapper.toRequestAudiolibroRicerca(audiolibroStub, 1);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroRicerca);
        given(audiolibroService.ricerca(any(), any(), any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/ricerca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Test per verificare che la richiesta di una lista di audiolibri con un tipo di lista valido restituisca una lista di audiolibri
     con un codice di stato 200. Il test utilizza uno stub per rappresentare l'audiolibro e un mock per rappresentare il servizio di audiolibri.
     Viene testato anche che tutti i campi di un audiolibro nella lista siano presenti e abbiano i valori corretti.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenTipoListaCorretto_postLista_thenListaWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        List<Audiolibro> audiolibroStubList = new ArrayList<Audiolibro>();
        audiolibroStubList.add(audiolibroStub);
        doReturn(audiolibroStubList)
                .when(audiolibroService)
                .lista(any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroLista requestAudiolibroLista = AudiolibroMapper.toRequestAudiolibroLista(1);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        List<ResponseAudiolibro> responseAudiolibroList = new ArrayList<ResponseAudiolibro>();
        responseAudiolibroList.add(responseAudiolibro);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroListaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroLista);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibroList(any(), any())).thenReturn(responseAudiolibroList);
            mvc.perform(MockMvcRequestBuilders.post("/audiolibro/lista")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroListaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$[0].preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$[0].titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$[0].descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$[0].copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$[0].audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$[0].dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$[0].creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$[0].creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$[0].creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$[0].ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$[0].ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Testa il comportamento dell'applicazione quando viene fornito un tipo errato nella richiesta di lista di audiolibri.
     Si verifica che il risultato sia un errore HTTP 400 Bad Request.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenTipoInModoSbagliato_postLista_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        List<Audiolibro> audiolibroStubList = new ArrayList<Audiolibro>();
        audiolibroStubList.add(audiolibroStub);
        doReturn(audiolibroStubList)
                .when(audiolibroService)
                .lista(any(), any());
        RequestAudiolibroLista requestAudiolibroLista = AudiolibroMapper.toRequestAudiolibroLista(-1);
        String requestAudiolibroRicercaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroLista);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/lista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroRicercaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Questo metodo di test verifica la patch di "aggiungiAiPreferiti" con un identificativo di audiolibro valido.
     Si verifica che l'aggiunta dell'audiolibro ai preferiti sia effettuata correttamente con un codice di risposta HTTP 200.
     Viene utilizzato uno stub di audiolibro per verificare il comportamento del metodo.
     L'header viene mockato per fornire un utente creatore valido.
     Viene utilizzata anche la classe di mappatura per convertire l'audiolibro in una richiesta e una risposta.
     La risposta viene poi verificata con il metodo jsonPath per verificare che i campi siano corretti.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroCorretto_patchAggiungiAiPreferiti_thenAggiungiAiPreferitiWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .aggiungiAiPreferiti(any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/aggiungiAiPreferiti")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroModificaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Questo metodo testa il comportamento del sistema quando viene fornito un identificativo di audiolibro non valido nella richiesta di patch per l'aggiunta ai preferiti.
     Si verifica che il sistema restituisca un errore con un codice HTTP 400 Bad Request.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroInModoSbagliato_patchAggiungiAiPreferiti_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .aggiungiAiPreferiti(any(), any());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        requestAudiolibroModifica.setIdAudiolibro(0);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/aggiungiAiPreferiti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Test che verifica il comportamento del sistema quando si prova ad aggiungere un audiolibro già presente tra i preferiti.
     Viene utilizzato un audiolibro stub e viene configurato il comportamento del servizio in modo tale che venga lanciata
     un'eccezione di tipo AudiolibroGiaPreferito.
     Il sistema dovrebbe restituire un codice di errore 400 (Bad Request).
     @throws Exception
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroGiaTraIPreferiti_patchAggiungiAiPreferiti_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.aggiungiAiPreferiti(any(), any()))
                .willThrow(new AudiolibroGiaPreferito());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/aggiungiAiPreferiti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Testa il comportamento dell'API quando viene richiesto di aggiungere un audiolibro ai preferiti ma questo non esiste.
     Deve restituire un errore con codice 404 (Not Found).
     @throws Exception in caso di errore durante l'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonEsistente_patchAggiungiAiPreferiti_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.aggiungiAiPreferiti(any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/aggiungiAiPreferiti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Questo metodo di test verifica che l'eliminazione di un audiolibro dai preferiti sia correttamente gestita.
     Viene utilizzato un audiolibroStub come input e viene simulata la chiamata al metodo rimuoviDaiPreferiti del servizio AudiolibroService.
     Il mock della classe Utils è utilizzato per simularne la chiamata al metodo getUtenteFromHeader.
     Viene quindi effettuato una richiesta PATCH con un oggetto RequestAudiolibroModifica serializzato e si verifica che la risposta
     abbia il codice HTTP 200 OK e che i campi dell'oggetto ResponseAudiolibro e ResponseUtente corrispondano a quelli attesi.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroCorretto_patchRimuoviDaiPreferiti_thenRimuoviDaiPreferitiWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rimuoviDaiPreferiti(any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rimuoviDaiPreferiti")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroModificaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Test del metodo patch "rimuoviDaiPreferiti" per verificare il comportamento in caso di id audiolibro errato.
     Si utilizza uno stub per creare un audiolibro e un mock per verificare la chiamata al servizio "rimuoviDaiPreferiti".
     Si crea una richiesta di modifica dell'audiolibro con un id errato (-100) e si esegue una richiesta HTTP "PATCH" verso il metodo "rimuoviDaiPreferiti".
     Si aspetta che la risposta HTTP sia "400 Bad Request".
     @throws Exception in caso di errore durante la serializzazione o la deserializzazione JSON.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroInModoSbagliato_patchRimuoviDaiPreferiti_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rimuoviDaiPreferiti(any(), any());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        requestAudiolibroModifica.setIdAudiolibro(-100);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rimuoviDaiPreferiti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Test che verifica che se un audiolibro è già stato rimosso dai preferiti, una richiesta di rimozione fallisca con un errore 400.
     Viene creato uno stub dell'audiolibro, e viene simulata una richiesta di rimozione tramite il metodo patch.
     Viene quindi verificato che il risultato sia un errore con codice 400.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroGiaNonPreferito_patchRimuoviPreferiti_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rimuoviDaiPreferiti(any(), any()))
                .willThrow(new AudiolibroGiaPreferito());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rimuoviDaiPreferiti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Testa la rimozione di un audiolibro dai preferiti se l'audiolibro non esiste.
     Si aspetta un errore con codice HTTP 404.
     @throws Exception se ci sono problemi durante l'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonEsistente_patchRimuoviDaiPreferiti_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rimuoviDaiPreferiti(any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rimuoviDaiPreferiti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Testa il metodo "rendiPubblico" della classe AudiolibroController con un identificativo di audiolibro valido.
     Verifica che la richiesta di rendere pubblico un audiolibro venga gestita correttamente e che l'audiolibro risulti pubblico.
     @throws Exception in caso di errore nell'esecuzione del test
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroCorretto_patchRendiPubblico_thenRendiPubblicoWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rendiPubblico(any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rendiPubblico")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroModificaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Questo metodo di test verifica che se viene fornito un id errato per l'audiolibro, durante la richiesta di modifica per rendere pubblico l'audiolibro, il sistema restituisca un errore con codice 400 Bad Request.
     Viene utilizzato uno stub per simulare l'oggetto Audiolibro e viene configurato un mock del servizio Audiolibro per restituire l'oggetto Audiolibro stub.
     Viene poi creata una richiesta di modifica per rendere pubblico l'audiolibro, ma con un id errato, e viene effettuata una richiesta HTTP PATCH per inviare la richiesta al sistema.
     Infine, viene verificato che la risposta sia di tipo Bad Request.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroInModoSbagliato_patchRendiPubblico_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rendiPubblico(any(), any());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        requestAudiolibroModifica.setIdAudiolibro(-1);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rendiPubblico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Questo metodo di test verifica che un errore HTTP 400 Bad Request venga restituito
     se si cerca di rendere pubblico un audiolibro già pubblico.
     Viene creato uno stub di un oggetto Audiolibro e, in seguito,
     viene simulata la lancia di una eccezione "AudiolibroGiaPubblico"
     durante l'invocazione del metodo rendiPubblico del servizio AudiolibroService.
     Infine, viene effettuata una richiesta HTTP PATCH per rendere pubblico l'audiolibro
     utilizzando l'oggetto RequestAudiolibroModifica e si verifica che venga restituito un codice HTTP 400.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroGiaPubblico_patchRendiPubblico_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rendiPubblico(any(), any()))
                .willThrow(new AudiolibroGiaPubblico());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rendiPubblico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Test che verifica il comportamento del sistema quando viene richiesta una modifica di un audiolibro che non esiste.
     Si attende che il sistema restituisca un errore con codice 404 (Not Found).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonEsistente_patchRendiPubblico_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rendiPubblico(any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rendiPubblico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Questo metodo di test verifica il comportamento del sistema quando viene effettuata una chiamata di tipo PATCH all'endpoint "/audiolibro/rendiNonPubblico" con un id di un audiolibro esistente.
     Si attende che la risposta sia 200 (OK) e che sia presente nella risposta il JSON con le informazioni dell'audiolibro rendendolo non pubblico.
     Verranno utilizzati stub e mock di oggetti (Audiolibro, Utente, AudiolibroService, Utils, AudiolibroMapper).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroCorretto_patchRendiNonPubblico_thenRendiNonPubblicoWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rendiNonPubblico(any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rendiNonPubblico")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroModificaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Test per la patch di rendi non pubblico di un audiolibro in cui l'id dell'audiolibro è sbagliato.
     Viene simulato l'invio di una richiesta con un id errato e ci si aspetta un errore 400 Bad Request.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroInModoSbagliato_patchRendiNonPubblico_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rendiNonPubblico(any(), any());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        requestAudiolibroModifica.setIdAudiolibro(-1);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rendiNonPubblico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Testa il comportamento dell'API quando si cerca di rendere non pubblico un audiolibro già non pubblico.
     Si aspetta che la chiamata ritorni un errore con status code 400.
     @throws Exception in caso di problemi durante l'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroGiaNonPubblico_patchRendiNonPubblico_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rendiNonPubblico(any(), any()))
                .willThrow(new AudiolibroGiaPubblico());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rendiNonPubblico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Testa il comportamento del sistema quando si invoca una patch per rendere non pubblico un audiolibro non esistente.
     Si aspetta un errore con codice 404 (Not Found).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonEsistente_patchRendiNonPubblico_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rendiNonPubblico(any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        RequestAudiolibroModifica requestAudiolibroModifica = AudiolibroMapper.toRequestAudiolibroModifica(audiolibroStub);
        String requestAudiolibroModificaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/rendiNonPubblico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroModificaAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Test che verifica la condivisione di un audiolibro con id corretto
     e che verifica che la risposta sia 200 OK.
     Utilizza i mapper per convertire gli oggetti e il mock di Utils e AudiolibroMapper.
     Verifica che i valori della risposta siano corretti.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroCorretto_postCondividi_thenCondividiWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .condividi(any(), any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        List<Utente> utenteList = new ArrayList<Utente>();
        utenteList.add(creatore);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.post("/audiolibro/condividi")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroCondivisioneAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Questo metodo di test verifica che se si invia una richiesta di condivisione di un audiolibro con un nome utente specificato in modo errato, il server risponde con un errore HTTP 400 Bad Request.
     Viene utilizzato un stub per AudiolibroService e Utente. Il nome utente viene impostato vuoto e la lista di utenti viene creata con l'utente stub.
     La richiesta di condivisione viene effettuata tramite una chiamata HTTP POST all'indirizzo "/audiolibro/condividi" con il corpo della richiesta che contiene i dettagli della condivisione in formato JSON.
     Il risultato atteso è che la risposta del server abbia lo stato HTTP 400 Bad Request.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenUsernameUtenteInModoSbagliato_postCondividi_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .condividi(any(), any(), any());
        Utente utenteStub = TestFixtures.getUtenteStub();
        utenteStub.setUsername("");
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/condividi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroCondivisioneAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Questo metodo di test verifica il comportamento dell'applicazione quando viene effettuata una richiesta di condivisione di un audiolibro che è già stato condiviso.
     Si aspetta che l'applicazione risponda con un errore HTTP 400 BAD REQUEST.
     Viene utilizzato il mock di un servizio di audiolibro per simulare la condivisione di un audiolibro già condiviso.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroGiaCondiviso_postCondividi_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.condividi(any(), any(), any()))
                .willThrow(new AudiolibroGiaCondiviso());
        Utente utenteStub = TestFixtures.getUtenteStub();
        utenteStub.setUsername("");
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/condividi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroCondivisioneAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Test per verificare il comportamento dell'API in caso di richiesta di condivisione di un audiolibro inesistente.
     Viene simulata una chiamata a "/audiolibro/condividi" con un audiolibro inesistente.
     Si aspetta un errore con codice 404 (Not Found).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonEsistente_postCondividi_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.condividi(any(), any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        Utente utenteStub = TestFixtures.getUtenteStub();
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/condividi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroCondivisioneAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Test che verifica il comportamento dell'applicazione in presenza di un utente non esistente.
     Viene simulato un errore UtenteNonTrovato attraverso il metodo condividi di AudiolibroService.
     La richiesta HTTP di tipo POST con URL "/audiolibro/condividi" viene inviata al server e si attende un errore HTTP 404 (NOT FOUND).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenUtenteNonEsistente_postCondividi_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.condividi(any(), any(), any()))
                .willThrow(new UtenteNonTrovato());
        Utente utenteStub = TestFixtures.getUtenteStub();
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/condividi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroCondivisioneAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Questo metodo di test verifica che la chiamata POST all'endpoint "/audiolibro/rimuoviCondivisioni" con un ID audiolibro corretto
     rimuova le condivisioni e restituisca una risposta HTTP 200 (OK) contenente i dettagli dell'audiolibro e del creatore.
     Viene utilizzato un stub per il servizio di Audiolibro, e viene mockato l'oggetto Utils e AudiolibroMapper per ottenere il risultato previsto.
     La risposta viene quindi confrontata con i valori previsti, verificando che siano presenti tutti i dettagli corretti.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenIdAudiolibroCorretto_postRimuoviCondivisioni_thenRimuoviCondivisioniWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rimuoviCondivisioni(any(), any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        List<Utente> utenteList = new ArrayList<Utente>();
        utenteList.add(creatore);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.post("/audiolibro/rimuoviCondivisioni")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroCondivisioneAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Test per verificare che un errore con codice 400 venga restituito quando l'utente fornisce il nome utente in modo errato durante la rimozione delle condivisioni dell'audiolibro.
     In questo test, viene creato uno stub per l'oggetto Audiolibro e uno per l'oggetto Utente.
     Viene impostato il nome utente su una stringa vuota e viene creata una lista di utenti con questo stub.
     Viene quindi creato un oggetto RequestAudiolibroCondivisione a partire dallo stub dell'audiolibro e dalla lista di utenti.
     Viene infine effettuato un richiesta POST al endpoint "/audiolibro/rimuoviCondivisioni" con il JSON dell'oggetto RequestAudiolibroCondivisione, e si verifica che il risultato sia un errore con codice 400 Bad Request.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenUsernameUtenteInModoSbagliato_postRimuoviCondivisioni_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .rimuoviCondivisioni(any(), any(), any());
        Utente utenteStub = TestFixtures.getUtenteStub();
        utenteStub.setUsername("");
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/rimuoviCondivisioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroCondivisioneAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Testa il caso in cui l'audiolibro non è già condiviso e si cerca di rimuovere la condivisione.
     Si aspetta che il risultato sia un errore HTTP 400.
     @throws Exception in caso di errore nell'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroGiaNonCondiviso_postRimuoviCondivisioni_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rimuoviCondivisioni(any(), any(), any()))
                .willThrow(new AudiolibroGiaCondiviso());
        Utente utenteStub = TestFixtures.getUtenteStub();
        utenteStub.setUsername("");
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/rimuoviCondivisioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroCondivisioneAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Test che verifica il comportamento dell'API quando si tenta di rimuovere la condivisione di un audiolibro non esistente.
     Deve ritornare un errore con codice 404 (Not Found).
     @throws Exception se si verifica un errore durante il test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonEsistente_postRimuoviCondivisioni_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rimuoviCondivisioni(any(), any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        Utente utenteStub = TestFixtures.getUtenteStub();
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/rimuoviCondivisioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroCondivisioneAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Testa il comportamento del sistema quando un utente non esiste durante la rimozione di una condivisione di un audiolibro.
     Si aspetta un errore HTTP 404 (Not Found).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenUtenteNonEsistente_postRimuoviCondivisioni_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.rimuoviCondivisioni(any(), any(), any()))
                .willThrow(new UtenteNonTrovato());
        Utente utenteStub = TestFixtures.getUtenteStub();
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroCondivisione requestAudiolibroCondivisione = AudiolibroMapper.toRequestAudiolibroCondivisione(audiolibroStub, utenteList);
        String requestAudiolibroCondivisioneAsString = new ObjectMapper().writeValueAsString(requestAudiolibroCondivisione);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/rimuoviCondivisioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroCondivisioneAsString))
                .andExpect(status().isNotFound());
    }

    /**
     Questo metodo di test verifica la corretta esecuzione della richiesta POST per l'ascolto di un audiolibro.
     Si utilizzano stub per l'oggetto Audiolibro e Utente, e vengono mockati alcuni metodi delle classi Utils e AudiolibroMapper.
     Il risultato atteso è una risposta HTTP 200 con i dettagli dell'audiolibro e del creatore, compreso l'ultimo ascolto che dovrebbe essere nullo.
     @throws Exception
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenRequestAudiolibroAscoltaCorretta_postAscolta_thenAscoltaWith200() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .ascolta(any(), any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroAscolta requestAudiolibroAscolta = AudiolibroMapper.toRequestAudiolibroAscolta(audiolibroStub, 20);
        ResponseAudiolibro responseAudiolibro = AudiolibroMapper.toResponseAudiolibro(audiolibroStub, creatore);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(creatore);
        String requestAudiolibroAscoltaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroAscolta);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<AudiolibroMapper> mockAudiolibroMapper = Mockito.mockStatic(AudiolibroMapper.class)) {
            mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockAudiolibroMapper.when(() -> AudiolibroMapper.toResponseAudiolibro(any(), any())).thenReturn(responseAudiolibro);
            mvc.perform(MockMvcRequestBuilders.post("/audiolibro/ascolta")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestAudiolibroAscoltaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAudiolibro", is(responseAudiolibro.getIdAudiolibro())))
                    .andExpect(jsonPath("$.preferito", is(responseAudiolibro.getPreferito())))
                    .andExpect(jsonPath("$.titolo", is(responseAudiolibro.getTitolo())))
                    .andExpect(jsonPath("$.descrizione", is(responseAudiolibro.getDescrizione())))
                    .andExpect(jsonPath("$.copertina", is(responseAudiolibro.getCopertina())))
                    .andExpect(jsonPath("$.audio", is(responseAudiolibro.getAudio())))
                    .andExpect(jsonPath("$.dataInserimento", is(responseAudiolibro.getDataInserimento())))
                    .andExpect(jsonPath("$.creatore.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.creatore.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.creatore.username", is(responseUtente.getUsername())))
                    .andExpect(jsonPath("$.ultimoAscolto.data").value(IsNull.nullValue()))
                    .andExpect(jsonPath("$.ultimoAscolto.secondi", is(0)));
        }
    }

    /**
     Test che verifica il comportamento dell'applicazione nel caso in cui i secondi di ascolto siano specificati in modo non valido (valore negativo).
     Si verifica che venga restituito un errore HTTP 400 (Bad Request).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenSecondiInModoSbagliato_postAscolta_thenErroreWith400() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        doReturn(audiolibroStub)
                .when(audiolibroService)
                .ascolta(any(), any(), any());
        Utente creatore = TestFixtures.getUtenteStub();
        RequestAudiolibroAscolta requestAudiolibroAscolta = AudiolibroMapper.toRequestAudiolibroAscolta(audiolibroStub, -1);
        String requestAudiolibroAscoltaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroAscolta);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/ascolta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroAscoltaAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Questo test verifica il comportamento dell'applicazione quando si cerca di ascoltare un audiolibro che non esiste.
     Si configura il comportamento del servizio AudiolibroService in modo che lancia un'eccezione AudiolibroNonTrovato.
     Viene creato un stub per un utente e una richiesta di ascolto per l'audiolibro.
     Si esegue una richiesta POST a "/audiolibro/ascolta" con il contenuto della richiesta di ascolto.
     Si verifica che la risposta HTTP sia 404 (Not Found).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenAudiolibroNonEsistente_postAscolta_thenErroreWith404() throws Exception {
        Audiolibro audiolibroStub = TestFixtures.getAudiolibroStub();
        given(audiolibroService.ascolta(any(), any(), any()))
                .willThrow(new AudiolibroNonTrovato());
        Utente utenteStub = TestFixtures.getUtenteStub();
        List<Utente> utenteList = new ArrayList<>();
        utenteList.add(utenteStub);
        RequestAudiolibroAscolta requestAudiolibroAscolta = AudiolibroMapper.toRequestAudiolibroAscolta(audiolibroStub, 20);
        String requestAudiolibroAscoltaAsString = new ObjectMapper().writeValueAsString(requestAudiolibroAscolta);
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/ascolta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAudiolibroAscoltaAsString))
                .andExpect(status().isNotFound());
    }
}

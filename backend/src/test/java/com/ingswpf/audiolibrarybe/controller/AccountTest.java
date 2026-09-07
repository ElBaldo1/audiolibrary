package com.ingswpf.audiolibrarybe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingswpf.audiolibrarybe.dto.mapper.AudiolibroMapper;
import com.ingswpf.audiolibrarybe.dto.mapper.UtenteMapper;
import com.ingswpf.audiolibrarybe.dto.request.*;
import com.ingswpf.audiolibrarybe.dto.response.ResponseUtente;
import com.ingswpf.audiolibrarybe.dto.response.ResponseUtenteLogin;
import com.ingswpf.audiolibrarybe.exception.CredenzialiNonValide;
import com.ingswpf.audiolibrarybe.exception.TokenNonTrovato;
import com.ingswpf.audiolibrarybe.exception.UsernameEsistente;
import com.ingswpf.audiolibrarybe.model.Utente;
import com.ingswpf.audiolibrarybe.security.JwtTokenUtil;
import com.ingswpf.audiolibrarybe.service.AudiolibroService;
import com.ingswpf.audiolibrarybe.service.JwtNotExpiredService;
import com.ingswpf.audiolibrarybe.service.UtenteService;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountTest {
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UtenteService utenteService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private JwtNotExpiredService jwtNotExpiredService;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /**
     Questo metodo testa la registrazione di un utente, utilizzando JUnit e Mockito.
     Il test utilizza l'annotazione @WithMockUser per creare un utente di prova con il nome "spring" e il ruolo "ADMIN".
     Viene poi creato un oggetto di richiesta di registrazione dell'utente tramite il metodo UtenteMapper.toRequestUtenteRegistrazione().
     Successivamente, viene configurato il comportamento del metodo utenteService.registrazione() per restituire l'utente creato in precedenza.
     Infine, viene eseguita una richiesta HTTP POST all'indirizzo "/utente/registrazione" contenente l'oggetto di richiesta dell'utente,
     e vengono controllati il codice di stato della risposta (200 OK) e il contenuto della risposta (che dovrebbe contenere i dati dell'utente registrato).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenUtenteCorretto_postRegistrazione_thenRegistratoWith200() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        RequestUtenteRegistrazione requestUtenteRegistrazione = UtenteMapper.toRequestUtenteRegistrazione(utenteStub);
        given(utenteService.registrazione(any(), any(), any(), any(), any()))
                .willReturn(utenteStub);
        String requestUtenteRegistrazioneAsString = new ObjectMapper().writeValueAsString(requestUtenteRegistrazione);
        mvc.perform(MockMvcRequestBuilders.post("/utente/registrazione")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteRegistrazioneAsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is(utenteStub.getNome())))
                .andExpect(jsonPath("$.cognome", is(utenteStub.getCognome())))
                .andExpect(jsonPath("$.username", is(utenteStub.getUsername())));
    }

    /**
     Questo metodo testa la gestione degli errori nella registrazione di un utente, utilizzando JUnit e Mockito.
     Il test utilizza l'annotazione @WithMockUser per creare un utente di prova con il nome "spring" e il ruolo "ADMIN".
     Viene poi creato un oggetto di richiesta di registrazione dell'utente tramite il metodo UtenteMapper.toRequestUtenteRegistrazione().
     Successivamente, viene configurato il comportamento del metodo utenteService.registrazione() per lanciare un'eccezione di tipo "UsernameEsistente".
     Infine, viene eseguita una richiesta HTTP POST all'indirizzo "/utente/registrazione" contenente l'oggetto di richiesta dell'utente,
     e viene controllato il codice di stato della risposta (400 Bad Request), che indica che la richiesta non è stata accettata a causa di un errore nei parametri inviati.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenUsernameEsistente_postRegistrazione_thenErroredWith400() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        RequestUtenteRegistrazione requestUtenteRegistrazione = UtenteMapper.toRequestUtenteRegistrazione(utenteStub);
        String requestUtenteRegistrazioneAsString = new ObjectMapper().writeValueAsString(requestUtenteRegistrazione);
        given(utenteService.registrazione(any(), any(), any(), any(), any()))
                .willThrow(new UsernameEsistente());
        mvc.perform(MockMvcRequestBuilders.post("/utente/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteRegistrazioneAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Testa il comportamento del sistema alla registrazione di un utente con email in formato non corretto.
     Utilizza la classe @WithMockUser per simulare l'autenticazione di un utente con ruolo "ADMIN".
     Effettua una richiesta di tipo POST all'URL "/utente/registrazione" con il corpo della richiesta che contiene l'oggetto RequestUtenteRegistrazione.
     Verifica che la risposta del sistema sia di tipo BAD REQUEST (400).
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenEmailInFormatoSbagliato_postRegistrazione_thenErroreWith400() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        RequestUtenteRegistrazione requestUtenteRegistrazione = UtenteMapper.toRequestUtenteRegistrazione(utenteStub);
        requestUtenteRegistrazione.setEmail("listener@example");
        String requestUtenteRegistrazioneAsString = new ObjectMapper().writeValueAsString(requestUtenteRegistrazione);
        given(utenteService.registrazione(any(), any(), any(), any(), any()))
                .willReturn(utenteStub);
        mvc.perform(MockMvcRequestBuilders.post("/utente/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteRegistrazioneAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Questo metodo esegue un test sulla richiesta di login, utilizzando JUnit e Mockito.
     Viene utilizzato l'annotation WithMockUser per simulare un utente con nome "spring" e ruolo "ADMIN".
     Viene creato uno stub per l'oggetto Utente e per il token JWT.
     Viene creato un oggetto RequestUtenteLogin} a partire dall'oggetto Utente stub
     Viene effettuata la richiesta di login e viene verificato che il risultato sia un HTTP 200 OK, che il contenuto della risposta sia conforme all'oggetto
     ResponseUtenteLogin stub creato, e che il token JWT nella risposta sia uguale allo stub del JWT.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenCredenzialiCorrette_postLogin_thenLoggatoWith200() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        User userStub = new User(utenteStub.getUsername(), utenteStub.getPassword(), new ArrayList<>());
        String jwtStub = TestFixtures.getJwtStub();
        doReturn(userStub)
                .when(mock(UtenteService.class))
                .login(any());
        RequestUtenteLogin requestUtenteLogin = UtenteMapper.toRequestUtenteLogin(utenteStub);
        String requestUtenteLoginAsString = new ObjectMapper().writeValueAsString(requestUtenteLogin);
        ResponseUtenteLogin responseUtenteLogin = UtenteMapper.toResponseUtenteLogin(utenteStub, jwtStub);
        try(MockedStatic<UtenteMapper> mockUtenteMapper = Mockito.mockStatic(UtenteMapper.class)) {
            mockUtenteMapper.when(() -> UtenteMapper.toResponseUtenteLogin(any(), any())).thenReturn(responseUtenteLogin);
            when(jwtTokenUtil.generateToken(any()))
                    .thenReturn(jwtStub);
            mvc.perform(MockMvcRequestBuilders.post("/utente/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestUtenteLoginAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jwtToken", is(responseUtenteLogin.getJwtToken())))
                    .andExpect(jsonPath("$.utente.nome", is(responseUtenteLogin.getUtente().getNome())))
                    .andExpect(jsonPath("$.utente.cognome", is(responseUtenteLogin.getUtente().getCognome())))
                    .andExpect(jsonPath("$.utente.username", is(responseUtenteLogin.getUtente().getUsername())));
        }
    }

    /**
     Test che verifica che l'username inserito in modo scorretto durante la richiesta di login generi un errore di tipo Bad Request (400).
     Il metodo utilizza il framework JUnit e la libreria Mockito per simulare una richiesta HTTP al servizio "Utente" e verificare che
     il sistema risponda con un codice di stato 400 (Bad Request) in caso di username vuoto.
     @throws Exception in caso di errore durante l'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenUsernameInModoSbagliato_postLogin_thenErroreWith400() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        User userStub = new User(utenteStub.getUsername(), utenteStub.getPassword(), new ArrayList<>());
        doReturn(userStub)
                .when(mock(UtenteService.class))
                .login(any());
        RequestUtenteLogin requestUtenteLogin = UtenteMapper.toRequestUtenteLogin(utenteStub);
        requestUtenteLogin.setUsername("");
        String requestUtenteLoginAsString = new ObjectMapper().writeValueAsString(requestUtenteLogin);
        mvc.perform(MockMvcRequestBuilders.post("/utente/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteLoginAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Questo metodo verifica il comportamento dell'applicazione in caso di inserimento di credenziali non valide
     durante l'operazione di login. Viene effettuato un test tramite JUnit e Mockito con l'utilizzo
     del mock user "spring" che ha il ruolo "ADMIN". Il test consiste nell'invio di una richiesta di login
     con credenziali non valide e si aspetta una risposta con status code 401 Unauthorized.
     La richiesta di login viene effettuata tramite la chiamata al metodo "post" dell'oggetto "mvc"
     passando come parametri l'URI "/utente/login", il tipo di contenuto "MediaType.APPLICATION_JSON"
     e il contenuto della richiesta "requestUtenteLoginAsString". Il comportamento del metodo
     "utenteService.login(any())" viene simulato tramite la lanciare dell'eccezione "CredenzialiNonValide".
     @throws Exception eccezione che può essere sollevata dal metodo
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenCredenzialiNonCorrette_postLogin_thenErroreWith401() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        RequestUtenteLogin requestUtenteLogin = UtenteMapper.toRequestUtenteLogin(utenteStub);
        String requestUtenteLoginAsString = new ObjectMapper().writeValueAsString(requestUtenteLogin);
        given(utenteService.login(any()))
                .willThrow(new CredenzialiNonValide());
        mvc.perform(MockMvcRequestBuilders.post("/utente/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteLoginAsString))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenJwtCorretto_postLogout_thenLogoutWith200() throws Exception {
        String jwtStub = TestFixtures.getJwtStub();
        doNothing()
                .when(mock(JwtNotExpiredService.class))
                .invalidaToken(any());
        RequestUtenteLogout requestUtenteLogout = UtenteMapper.toRequestUtenteLogout(jwtStub);
        String requestUtenteLogoutAsString = new ObjectMapper().writeValueAsString(requestUtenteLogout);
        mvc.perform(MockMvcRequestBuilders.post("/utente/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteLogoutAsString))
                .andExpect(status().isOk());
    }

    /**
     Questo metodo effettua un test tramite JUnit e Mockito per verificare il corretto logout con un JWT corretto.
     Viene creato uno stub del JWT e definito il comportamento del mock di JwtNotExpiredService per invaliare il token.
     Viene quindi creata una richiesta di logout e viene effettuata la richiesta POST all'indirizzo "/utente/logout".
     Si verifica che il risultato sia uno stato HTTP 200 OK.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenJwtInModoSbagliato_postLogout_thenErroreWith400() throws Exception {
        String jwtStub = TestFixtures.getJwtStub();
        doNothing()
                .when(mock(JwtNotExpiredService.class))
                .invalidaToken(any());
        RequestUtenteLogout requestUtenteLogout = UtenteMapper.toRequestUtenteLogout(jwtStub);
        requestUtenteLogout.setJwtToken("");
        String requestUtenteLogoutAsString = new ObjectMapper().writeValueAsString(requestUtenteLogout);
        mvc.perform(MockMvcRequestBuilders.post("/utente/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteLogoutAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Test di verifica per l'operazione di logout con un JWT non corretto.
     Verifica che un errore con codice 400 (Bad Request) venga restituito dal sistema.
     Utilizza JUnit e Mockito per simulare l'operazione di logout.
     @throws Exception in caso di errore nell'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenJwtNonCorretto_postLogout_thenErroreWith400() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        RequestUtenteLogin requestUtenteLogin = UtenteMapper.toRequestUtenteLogin(utenteStub);
        String requestUtenteLoginAsString = new ObjectMapper().writeValueAsString(requestUtenteLogin);
        doThrow(new TokenNonTrovato())
                .when(jwtNotExpiredService)
                .invalidaToken(any());
        mvc.perform(MockMvcRequestBuilders.post("/utente/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteLoginAsString))
                .andExpect(status().isBadRequest());
    }

    /**
     Test di unità per il metodo di modifica di un utente, che utilizza JUnit e Mockito.
     Verifica che, dati i campi di modifica corretti, la modifica sia effettuata con successo (status 200).
     @throws Exception in caso di errore nell'esecuzione del test.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenCampiModificaCorretti_patchModifica_thenModificaWith200() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        utenteStub.setEmail("prova@example.com");
        RequestUtenteModifica requestUtenteModifica = UtenteMapper.toRequestUtenteModifica(utenteStub);
        ResponseUtente responseUtente = UtenteMapper.toResponseUtente(utenteStub);
        String requestUtenteModificaAsString = new ObjectMapper().writeValueAsString(requestUtenteModifica);
        try (MockedStatic<Utils> mockUtils = Mockito.mockStatic(Utils.class); MockedStatic<UtenteMapper> mockUtenteMapper = Mockito.mockStatic(UtenteMapper.class)) {
            //mockUtils.when(() -> Utils.getUtenteFromHeader(any(), any(), any())).thenReturn(creatore);
            mockUtenteMapper.when(() -> UtenteMapper.toResponseUtente(any())).thenReturn(responseUtente);
            mvc.perform(MockMvcRequestBuilders.patch("/utente/modifica")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestUtenteModificaAsString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome", is(responseUtente.getNome())))
                    .andExpect(jsonPath("$.cognome", is(responseUtente.getCognome())))
                    .andExpect(jsonPath("$.username", is(responseUtente.getUsername())));
        }
    }

    /**
     Test di JUnit e Mockito che verifica che una richiesta di modifica utente con email non corretta generi un errore con stato 400 (Bad Request).
     Viene creato uno stub dell'utente, che viene poi passato al metodo "modifica" del servizio UtenteService con un mock.
     La richiesta di modifica con email non valida viene effettuata attraverso una chiamata HTTP al percorso "/utente/modifica" utilizzando il metodo "patch".
     Viene quindi verificato che il risultato della chiamata sia un errore con stato 400.
     */
    @WithMockUser(value = "spring", roles = {"ADMIN"})
    @Test
    public void givenEmailNonCorretta_patchModifica_thenErroreWith400() throws Exception {
        Utente utenteStub = TestFixtures.getUtenteStub();
        doReturn(utenteStub)
                .when(mock(UtenteService.class))
                .modifica(any(), any(), any());
        RequestUtenteModifica requestUtenteModifica = UtenteMapper.toRequestUtenteModifica(utenteStub);
        requestUtenteModifica.setEmail("prova");
        String requestUtenteModificaAsString = new ObjectMapper().writeValueAsString(requestUtenteModifica);
        mvc.perform(MockMvcRequestBuilders.patch("/utente/modifica")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUtenteModificaAsString))
                .andExpect(status().isBadRequest());
    }
}

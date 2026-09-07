package com.ingswpf.audiolibrarybe.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    record Account(String username, String token) {}

    private Account account() throws Exception {
        String username = "listener-" + UUID.randomUUID();
        post("/utente/registrazione", null, Map.of("nome", "Test", "cognome", "Listener",
                "username", username, "email", username + "@example.com", "password", "TestPass123!"), 200);
        JsonNode response = post("/utente/login", null,
                Map.of("username", username, "password", "TestPass123!"), 200);
        return new Account(username, response.get("jwtToken").asText());
    }

    private JsonNode post(String path, String token, Object body, int status) throws Exception {
        var request = MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        MvcResult result = mvc.perform(request).andExpect(status().is(status)).andReturn();
        String content = result.getResponse().getContentAsString();
        return content.startsWith("{") || content.startsWith("[") ? json.readTree(content) : json.nullNode();
    }

    private void patch(String path, Account user, int book) throws Exception {
        mvc.perform(MockMvcRequestBuilders.patch(path).header("Authorization", "Bearer " + user.token())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("idAudiolibro", book))))
                .andExpect(status().isOk());
    }

    private int book(Account user) throws Exception {
        JsonNode response = post("/audiolibro/inserisci", user.token(), Map.of(
                "titolo", "A test story", "autore", "Test Author", "descrizione", "",
                "audio", "SUQzBAAAAAAA", "copertina", "", "durata", 120, "mimeType", "audio/mpeg"), 201);
        assertThat(response.get("autore").asText()).isEqualTo("Test Author");
        assertThat(response.get("durata").asInt()).isEqualTo(120);
        return response.get("idAudiolibro").asInt();
    }

    @Test void accountUploadPlaybackAndLogoutWorkWithRealPersistence() throws Exception {
        Account user = account();
        int first = book(user), second = book(user);
        post("/audiolibro/ascolta", user.token(), Map.of("idAudiolibro", first, "secondi", 42), 200);
        post("/audiolibro/ascolta", user.token(), Map.of("idAudiolibro", second, "secondi", 7), 200);
        post("/audiolibro/ascolta", user.token(), Map.of("idAudiolibro", first, "secondi", 65), 200);
        JsonNode library = post("/audiolibro/lista", user.token(), Map.of("tipo", 1), 200);
        for (JsonNode track : library) {
            int expected = track.get("idAudiolibro").asInt() == first ? 65 : 7;
            assertThat(track.get("ultimoAscolto").get("secondi").asInt()).isEqualTo(expected);
            assertThat(track.get("ultimoAscolto").get("updatedAt").isNull()).isFalse();
        }
        post("/utente/logout", user.token(), Map.of("jwtToken", user.token()), 200);
        post("/audiolibro/lista", user.token(), Map.of("tipo", 1), 401);
    }

    @Test void publicBooksCannotBeEditedByOtherUsers() throws Exception {
        Account owner = account(), other = account();
        int book = book(owner);
        patch("/audiolibro/rendiPubblico", owner, book);
        mvc.perform(MockMvcRequestBuilders.patch("/audiolibro/modifica")
                .header("Authorization", "Bearer " + other.token()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("idAudiolibro", book, "titolo", "Hijacked", "descrizione", "", "copertina", ""))))
                .andExpect(status().isNotFound());
    }

    @Test void changingVisibilityPreservesUnrelatedSharesAndFavorites() throws Exception {
        Account owner = account(), listener = account();
        int first = book(owner), second = book(owner);
        for (int id : new int[]{first, second}) {
            post("/audiolibro/condividi", owner.token(), Map.of("idAudiolibro", id,
                    "utenti", new Object[]{Map.of("username", listener.username())}), 200);
            patch("/audiolibro/aggiungiAiPreferiti", listener, id);
        }
        patch("/audiolibro/rendiPubblico", owner, first);
        patch("/audiolibro/rendiNonPubblico", owner, first);
        JsonNode favorites = post("/audiolibro/lista", listener.token(), Map.of("tipo", 3), 200);
        assertThat(favorites.size()).isEqualTo(1);
        assertThat(favorites.get(0).get("idAudiolibro").asInt()).isEqualTo(second);
        JsonNode shared = post("/audiolibro/lista", listener.token(), Map.of("tipo", 2), 200);
        assertThat(shared.toString()).contains("\"idAudiolibro\":" + second);
    }

    @Test void revokingOneShareKeepsOtherListenersFavorites() throws Exception {
        Account owner = account(), first = account(), second = account();
        int book = book(owner);
        post("/audiolibro/condividi", owner.token(), Map.of("idAudiolibro", book,
                "utenti", new Object[]{Map.of("username", first.username()), Map.of("username", second.username())}), 200);
        patch("/audiolibro/aggiungiAiPreferiti", first, book);
        patch("/audiolibro/aggiungiAiPreferiti", second, book);
        post("/audiolibro/rimuoviCondivisioni", owner.token(), Map.of("idAudiolibro", book,
                "utenti", new Object[]{Map.of("username", first.username())}), 200);
        assertThat(post("/audiolibro/lista", first.token(), Map.of("tipo", 3), 200).size()).isZero();
        assertThat(post("/audiolibro/lista", second.token(), Map.of("tipo", 3), 200).size()).isEqualTo(1);
    }

    @Test void sharingIsAtomicWhenOneRecipientDoesNotExist() throws Exception {
        Account owner = account(), listener = account();
        int book = book(owner);
        post("/audiolibro/condividi", owner.token(), Map.of("idAudiolibro", book,
                "utenti", new Object[]{Map.of("username", listener.username()), Map.of("username", "missing-user")}), 404);
        JsonNode shared = post("/audiolibro/lista", listener.token(), Map.of("tipo", 2), 200);
        for (JsonNode entry : shared) assertThat(entry.get("idAudiolibro").asInt()).isNotEqualTo(book);
    }

    @Test void metadataCatalogAndAudioEndpointRespectPermissions() throws Exception {
        Account owner = account(), other = account();
        int id = book(owner);
        JsonNode library = post("/audiolibro/lista?metadataOnly=true", owner.token(), Map.of("tipo", 1), 200);
        assertThat(library.get(0).get("audio").isNull()).isTrue();
        mvc.perform(MockMvcRequestBuilders.get("/audiolibro/" + id + "/audio")
                .header("Authorization", "Bearer " + owner.token())).andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/audiolibro/" + id + "/audio")
                .header("Authorization", "Bearer " + other.token())).andExpect(status().isNotFound());
        post("/audiolibro/ascolta?metadataOnly=true", owner.token(), Map.of("idAudiolibro", id, "secondi", 3), 204);
    }

    @Test void cookieAndBasicCredentialsCannotAuthenticateRequests() throws Exception {
        Account user = account();
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/lista")
                .cookie(new jakarta.servlet.http.Cookie("jwt", user.token()),
                        new jakarta.servlet.http.Cookie("JSESSIONID", "untrusted-session"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":1}"))
                .andExpect(status().isUnauthorized());
        String basic = java.util.Base64.getEncoder().encodeToString(
                (user.username() + ":TestPass123!").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/lista")
                .header("Authorization", "Basic " + basic)
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":1}"))
                .andExpect(status().isUnauthorized());
        post("/audiolibro/lista", user.token(), Map.of("tipo", 1), 200);
    }

    @Test void crossOriginRequestsAndBrowserFormSubmissionsAreRejected() throws Exception {
        mvc.perform(MockMvcRequestBuilders.options("/audiolibro/lista")
                .header("Origin", "https://attacker.example")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.post("/utente/login")
                .header("Origin", "https://attacker.example")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        for (MediaType type : new MediaType[]{MediaType.TEXT_PLAIN, MediaType.APPLICATION_FORM_URLENCODED}) {
            mvc.perform(MockMvcRequestBuilders.post("/utente/login").contentType(type)
                    .content("username=listener&password=test"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Test void allowedOriginsWorkWithoutCookieCredentialsAndApiSendsSecurityHeaders() throws Exception {
        mvc.perform(MockMvcRequestBuilders.options("/audiolibro/lista")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .doesNotExist("Access-Control-Allow-Credentials"));
        Account user = account();
        mvc.perform(MockMvcRequestBuilders.post("/audiolibro/lista")
                .header("Authorization", "Bearer " + user.token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":1}"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Content-Type-Options", "nosniff"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; sandbox"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .doesNotExist("Set-Cookie"));
    }

    @Test void sqlPayloadsCannotBypassLoginOrRevokeAnotherSession() throws Exception {
        Account user = account();
        post("/utente/login", null, Map.of("username", "' OR 1=1 --", "password", "TestPass123!"), 401);
        post("/utente/logout", null, Map.of("jwtToken", "' OR '1'='1"), 400);
        post("/audiolibro/lista", user.token(), Map.of("tipo", 1), 200);
    }

    @Test void markupAndSqlSyntaxAreStoredAsData() throws Exception {
        Account user = account();
        String title = "O'Reilly; DROP TABLE audiolibro; -- <img src=x onerror=alert(1)>";
        JsonNode created = post("/audiolibro/inserisci", user.token(), Map.of(
                "titolo", title, "autore", "<script>alert(1)</script>", "descrizione", "<svg onload=alert(1)>",
                "audio", "SUQzBAAAAAAA", "copertina", "", "durata", 1, "mimeType", "audio/mpeg"), 201);
        assertThat(created.get("titolo").asText()).isEqualTo(title);
        JsonNode library = post("/audiolibro/lista?metadataOnly=true", user.token(), Map.of("tipo", 1), 200);
        assertThat(library.get(0).get("titolo").asText()).isEqualTo(title);
        assertThat(library.get(0).get("autore").asText()).isEqualTo("<script>alert(1)</script>");
        post("/audiolibro/inserisci", user.token(), Map.of("titolo", "Invalid media", "descrizione", "",
                "audio", "PHNjcmlwdD4=", "copertina", "", "mimeType", "text/html"), 400);
    }

    @Test void malformedRequestsHaveClientErrors() throws Exception {
        Account user = account();
        post("/audiolibro/ricerca", user.token(), Map.of("titolo", "Story"), 400);
        post("/audiolibro/condividi", user.token(), Map.of("idAudiolibro", 1), 400);
        post("/audiolibro/ascolta", user.token(), Map.of("idAudiolibro", 1, "secondi", -1), 400);
        post("/audiolibro/lista", "not.a.jwt", Map.of("tipo", 1), 401);
        post("/audiolibro/lista", null, Map.of("tipo", 1), 401);
    }
}

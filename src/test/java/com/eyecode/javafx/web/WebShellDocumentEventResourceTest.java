package com.eyecode.javafx.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebShellDocumentEventResourceTest {

    @Test
    void metadataEventsConfirmSnapshotsWithoutReplacingExistingModelContent() throws IOException {
        String bundle = bundle();

        assertTrue(bundle.contains("this.confirmedVersions.get("));
        assertTrue(bundle.contains("this.changeQueues.get("));
        assertTrue(bundle.contains("request(\"document\",\"change\","));
        assertTrue(bundle.contains("this.onDocumentChange?.("));
    }

    @Test
    void reidentifiedDisposesOldModelAndOpensNewIdentityFromAuthoritativeSnapshot() throws IOException {
        String bundle = bundle();

        assertTrue(bundle.contains("reidentify("));
        assertTrue(bundle.contains("this.models.delete("));
        assertTrue(bundle.contains("this.viewStates.set("));
        assertTrue(bundle.contains("this.open("));
        assertTrue(bundle.contains("this.api.editor.createModel("));
    }

    @Test
    void packagedBundleContainsPracticeVerification() throws IOException {
        String bundle = bundle();

        assertTrue(bundle.contains("session/verify"));
        assertTrue(bundle.contains("Verificar"));
        assertTrue(bundle.contains("Verificando..."));
    }

    private String bundle() throws IOException {
        String index;
        try (InputStream stream = getClass().getResourceAsStream("/webshell/index.html")) {
            index = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        Matcher script = Pattern.compile("src=\"\\./(assets/index-[^\"]+\\.js)\"").matcher(index);
        assertTrue(script.find());
        try (InputStream stream = getClass().getResourceAsStream("/webshell/" + script.group(1))) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

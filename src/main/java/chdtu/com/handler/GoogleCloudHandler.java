package chdtu.com.handler;

import chdtu.com.service.Bot;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
public class GoogleCloudHandler {
    @Value("${google.docs.document.id}")
    private String documentId;

    @Value("${google.cloud.json.configuration}")
    private String jsonConfigurationGoogleCloud;

    private final String jsonRequestFormat = "{ \"requests\": [ { \"insertText\": { \"text\": \"Відгук користувача: %s\\n%s\\n\\n\\n\", \"endOfSegmentLocation\": {} } } ] }";

    public void saveToGoogleDocs(String feedback, String message) {
        String jsonRequest = String.format(jsonRequestFormat, feedback, message);

        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(
                            Objects.requireNonNull(Bot.class.getClassLoader().getResourceAsStream(jsonConfigurationGoogleCloud)))
                    .createScoped("https://www.googleapis.com/auth/documents");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HttpCredentialsAdapter adapter = new HttpCredentialsAdapter(credentials);

        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(adapter);

        GenericUrl url = new GenericUrl("https://docs.googleapis.com/v1/documents/" + documentId + ":batchUpdate");
        HttpContent content = new ByteArrayContent("application/json", jsonRequest.getBytes(StandardCharsets.UTF_8));

        try {
            requestFactory.buildPostRequest(url, content).execute();
        } catch (IOException e) {
            log.info("The document was not saved in Google Cloud: " + e.getMessage());
        }

        log.info("The document was saved in Google Cloud");
    }
}

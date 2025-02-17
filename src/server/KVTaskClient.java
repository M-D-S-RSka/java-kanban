package server;

import exception.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class KVTaskClient {
    private final String apiToken;
    private final String serverURL;

    public KVTaskClient(String serverURL) throws IOException, InterruptedException {
        this.serverURL = serverURL;
        URI uri = URI.create(this.serverURL + "/register");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        apiToken = httpResponse.body();
    }

    public void put(String key, String json) {
        URI uri = URI.create(this.serverURL + "/save/" + key + "?API_TOKEN=" + apiToken);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                System.out.println("Не удалось сохранить данные.");
            }
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Ошибка сохраненнения данных.");
        }
    }

    public String load(String key) {
        URI uri = URI.create(this.serverURL + "/load/" + key + "?API_TOKEN=" + apiToken);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Во время запроса произошла ошибка.");
        }
    }
}
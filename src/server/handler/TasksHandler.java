package server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import utilities.CreateGson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TasksHandler implements HttpHandler {
    TaskManager taskManager;
    private final Gson gson = CreateGson.getGson();
    String response;

    public TasksHandler(TaskManager newTaskManager) {
        this.taskManager = newTaskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            getAllTask(exchange);
        } else {
            writeResponse(exchange, "Данной операции нет.", 404);
        }
    }

    private void getAllTask(HttpExchange exchange) throws IOException {
        if (taskManager.getPrioritizedTasks().isEmpty()) {
            writeResponse(exchange, "Список задач пустой.", 200);
        } else {
            response = gson.toJson(taskManager.getPrioritizedTasks());
            writeResponse(exchange, response, 200);
        }
    }

    public static void writeResponse(HttpExchange exchange, String responseString, int responseCode)
            throws IOException {
        if (responseString.isBlank()) {
            exchange.sendResponseHeaders(responseCode, 0);
        } else {
            byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }
}
package server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import tasks.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class EpicHandler implements HttpHandler {
    TaskManager taskManager;
    private final Gson gson = new Gson();
    String response;

    public EpicHandler(TaskManager newTaskManager) {
        this.taskManager = newTaskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                getEpic(exchange);
                break;
            case "POST":
                addEpic(exchange);
                break;
            case "DELETE":
                deleteEpic(exchange);
                break;
            default:
                writeResponse(exchange, "Данной операции нет.", 404);
        }
    }

    private void getEpic(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getQuery() == null) {
            response = gson.toJson(taskManager.getEpics());
            writeResponse(exchange, response, 200);
            return;
        }
        if (getTaskId(exchange).isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор.", 400);
            return;
        }
        int id = getTaskId(exchange).get();
        for (Epic epics : taskManager.getEpics()) {
            if (epics.getId().equals(id)) {
                response = gson.toJson(taskManager.getEpicById(id));
                writeResponse(exchange, response, 200);
                return;
            }
        }
        writeResponse(exchange, "Задач с данным id нет.", 404);
    }


    private void addEpic(HttpExchange exchange) throws IOException {
        try {
            InputStream json = exchange.getRequestBody();
            String jsonTask = new String(json.readAllBytes(), StandardCharsets.UTF_8);
            Epic epic = gson.fromJson(jsonTask, Epic.class);
            if (epic == null) {
                writeResponse(exchange, "Задача не должна быть пустой.", 400);
                return;
            }
            for (Epic epics : taskManager.getEpics()) {
                if (epic.getId() != null && epics.getId().equals(epic.getId())) {
                    taskManager.updateEpic(epic);
                    writeResponse(exchange, "Эпик обновлен.", 200);
                    return;
                }
            }
            System.out.println(taskManager.createEpic(epic));
            writeResponse(exchange, "Задача добавлена.", 201);
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Получен некорректный JSON.", 400);
        }
    }

    private void deleteEpic(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            taskManager.deleteAllEpic();
            writeResponse(exchange, "Задачи удалены.", 200);
        }
        if (getTaskId(exchange).isEmpty()) {
            return;
        }
        int id = getTaskId(exchange).get();
        for (Epic epics : taskManager.getEpics()) {
            if (epics.getId().equals(id)) {
                taskManager.deleteEpicById(id);
                writeResponse(exchange, "Задача удалена.", 200);
                return;
            }
        }
        writeResponse(exchange, "Эпиков с данным id нет.", 404);
    }

    private Optional<Integer> getTaskId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getQuery().split("=");
        try {
            return Optional.of(Integer.parseInt(pathParts[1]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
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

package server.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TaskHandler implements HttpHandler {
    TaskManager taskManager;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final Gson gson = new Gson();
    String response;

    public TaskHandler(TaskManager newTaskManager) {
        this.taskManager = newTaskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = String.valueOf(exchange.getRequestURI());

        System.out.println("Обрабатывается запрос " + path + " с методом " + method);
        switch (method) {
            case "GET":
                getTask(exchange);
                break;
            case "POST":
                addTask(exchange);
                break;
            case "DELETE":
                deleteTask(exchange);
                break;
            default:
                writeResponse(exchange, "Данной операции нет.", 404);
        }
    }

    private void getTask(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getQuery() == null) {
            response = gson.toJson(taskManager.getTasks());
            writeResponse(exchange, response, 201);
            return;
        }
        if (getTaskId(exchange).isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор.", 400);
            return;
        }
        int id = getTaskId(exchange).get();
        for (Task task : taskManager.getTasks()) {
            if (task.getId().equals(id)) {
                response = gson.toJson(taskManager.getTaskById(id));
                writeResponse(exchange, response, 200);
                return;
            }
        }
        writeResponse(exchange, "Задач с данным id нет.", 404);
    }

    private void addTask(HttpExchange exchange) throws IOException {
        try {
            InputStream json = exchange.getRequestBody();
            String jsonTask = new String(json.readAllBytes(), DEFAULT_CHARSET);
            Task task = gson.fromJson(jsonTask, Task.class);
            if (task == null) {
                writeResponse(exchange, "Задача не должна быть пустой.", 400);
                return;
            }
            for (Task task1 : taskManager.getTasks()) {
                if (task.getId() != null && task1.getId().equals(task.getId())) {
                    taskManager.updateTask(task);
                    writeResponse(exchange, "Данная задача существует и была обновлена.", 200);
                    return;
                }
            }
            taskManager.createTask(task);
//            System.out.println(taskManager.createTask(task)); - выдает в логах айдишку, однако работает в тесте (id - 1)
//            то есть если в логах айди 6, то в тесте указывал 5 и падаем уже в эпике, успешно поскачив таску..
//            в эпике и сабтаске данная история не прокатила
            writeResponse(exchange, "Задача добавлена.", 201);
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Получен некорректный JSON.", 400);
        }
    }

    private void deleteTask(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            taskManager.deleteAllTask();
            writeResponse(exchange, "Задачи удалены.", 200);
            return;
        }
        if (getTaskId(exchange).isEmpty()) {
            writeResponse(exchange, "Задачи удалены.", 400);
            return;
        }
        int id = getTaskId(exchange).get();
        for (Task task : taskManager.getTasks()) {
            if (task.getId().equals(id)) {
                taskManager.deleteTaskById(id);
                writeResponse(exchange, "Задача удалена.", 404);
                return;
            }
        }
        writeResponse(exchange, "Задач с данным id нет.", 404);
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
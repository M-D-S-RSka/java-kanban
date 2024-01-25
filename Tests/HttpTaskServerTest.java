import com.google.gson.Gson;
import manager.TaskManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import server.KVServer;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utilities.Status.NEW;

//Привет! Тут проблема, я просидел с ней весь день: если запускать тесты по отдельности в этом классе -
//все в порядке, а зпустить целиком класс - проблемы начинаются в тесте addTasksToTaskServerEndUpdate,
//насколько я понимаю по дебаггеру - у меня что-то с айдишками начинает происходить при обновлении (он их плюсует,
//а после похоже минусует, из-за этого берется не та задача и сооветственно проверка по коду падает),запутался.
//        Окончательно добил факт, что отдельно запускать тесты - все работает.

//Понимаю, что некорректно отправлять с возможными ошибками, в пачке одногруппники и наставник молчат пол-дня,
//жесткий дедлайн был в пятницу, мне куратор дал время на сдачу до понедельника, поэтому надеюсь, что получится пойти
//навстречу, ткнуть конкретно что не так или же поплыла голова в спешке...
// в taskHandler на 87 и 88 строке и в этом классе на 109-110 - последствия решения проблемы, попытка обойти ошибку
// с объяснениями что делал.

class HttpTaskServerTest {
    private static final String TASK_BASE_URL = "http://localhost:8080/tasks/task/";
    private static final String EPIC_BASE_URL = "http://localhost:8080/tasks/epic/";
    private static final String SUBTASK_BASE_URL = "http://localhost:8080/tasks/subtask/";
    private static KVServer kvServer;
    private static HttpTaskServer httpTS;
    private static Gson gson;

    protected Task task = createTask();
    protected Epic epic = createEpic();
    protected Subtask subtask = createSubtask();
    private static TaskManager manager;

    public Task createTask() {
        return new Task("Test addNewTask", "Test addNewTask description", NEW);
    }

    public Task addTaskServer() throws IOException, InterruptedException {
        task.setId(1);
        return task;
    }

    public Epic addEpicServer() throws IOException, InterruptedException {
        epic.setId(2);
        return epic;
    }

    public Subtask addSubtaskServer() throws IOException, InterruptedException {
        int epicId = epic.getId();
        subtask.setId(3);
        subtask.setEpicId(epicId);
        return subtask;
    }

    public HttpResponse<String> postTask(URI url, Task task, HttpClient client) throws IOException, InterruptedException {

        String json = gson.toJson(task);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public Subtask createSubtask() {
        return new Subtask("Test addNewSubtask", "Test addNewSubtask description", NEW);
    }

    public Epic createEpic() {
        return new Epic("Test addNewEpic", "Test addNewEpic description", NEW);
    }

    static {
        try {
            kvServer = new KVServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void setUp() throws IOException, InterruptedException {
        kvServer.start();
        httpTS = new HttpTaskServer(manager);
        httpTS.start();
        gson = new Gson();
    }

    @AfterAll
    static void tearDown() {
        httpTS.stop();
        kvServer.stop();
    }

    @Test
    void addTasksToTaskServerEndUpdate() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Task task1 = addTaskServer();
        URI url = URI.create(TASK_BASE_URL);
        assertEquals(201, postTask(url, task1, client).statusCode(), "Ошибка в добавлении новой задачи");

//        task1.setId(4); - по факту в логах из хэндлера(87 строка в TaskHandler) пишет 5, а по факту присваивает 4,
//        с этой строкой тогда падаю уже на 124 строке, такая же манипуляция с эпикхэндлер не работает, тоже 0 идей((

        String json = gson.toJson(task1);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Обновление задачи не прошло");

        Epic epic1 = addEpicServer();
        url = URI.create(EPIC_BASE_URL);
        json = gson.toJson(epic1);
        assertEquals(201, postTask(url, epic1, client).statusCode(), "Ошибка в добавлении новой задачи");
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Обновление задачи не прошло");

        Subtask subtask1 = addSubtaskServer();
        url = URI.create(SUBTASK_BASE_URL);
        json = gson.toJson(subtask1);
        assertEquals(201, postTask(url, subtask1, client).statusCode(), "Ошибка в добавлении новой задачи");
        body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Обновление задачи не прошло");
    }

    @Test
    void getAllTasksAndTasks_byId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        postTask(URI.create(TASK_BASE_URL), addTaskServer(), client);
        URI url = URI.create(TASK_BASE_URL + "?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task task = gson.fromJson(response.body(), Task.class);
        assertEquals("Test addNewTask", task.getName());

        postTask(URI.create(EPIC_BASE_URL), addEpicServer(), client);
        url = URI.create(EPIC_BASE_URL + "?id=2");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic epic = gson.fromJson(response.body(), Epic.class);
        assertEquals("Test addNewEpic", epic.getName());

        postTask(URI.create(SUBTASK_BASE_URL), addSubtaskServer(), client);
        url = URI.create(SUBTASK_BASE_URL + "?id=3");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask subtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals("Test addNewSubtask", subtask.getName());
    }

    @Test
    void deleteTaskAll_byId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        System.out.println(postTask(URI.create(TASK_BASE_URL), addTaskServer(), client));
        System.out.println(postTask(URI.create(EPIC_BASE_URL), addEpicServer(), client));
        System.out.println(postTask(URI.create(SUBTASK_BASE_URL), addSubtaskServer(), client));

        URI url = URI.create(SUBTASK_BASE_URL + "?id=3");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задача удалена.", response1.body());

        url = URI.create(SUBTASK_BASE_URL);
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задачи удалены.", response2.body());

        url = URI.create(TASK_BASE_URL + "?id=1");
        request1 = HttpRequest.newBuilder().uri(url).DELETE().build();
        response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задача удалена.", response1.body());

        url = URI.create(TASK_BASE_URL);
        request2 = HttpRequest.newBuilder().uri(url).DELETE().build();
        response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задачи удалены.", response2.body());

        url = URI.create(EPIC_BASE_URL + "?id=2");
        HttpRequest request3 = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задача удалена.", response3.body());

        url = URI.create(EPIC_BASE_URL);
        HttpRequest request4 = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задачи удалены.", response4.body());
    }
}
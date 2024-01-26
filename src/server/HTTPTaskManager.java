package server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import manager.FileBackedTasksManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import utilities.CreateGson;
import utilities.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utilities.Type.*;
//Написал в пачку, сообщение висит прочитанным, но без ответа, мб ошибся с адресом))
// при инициализации Gson getGson в хэндлерах эпика, таски и сабтаски - выкидыввет ошибку в тесте, поэтому в этих
// трех случаях оставил как было...
public class HTTPTaskManager extends FileBackedTasksManager {

    final static String KEY_TASKS = "tasks";
    final static String KEY_SUBTASKS = "subtasks";
    final static String KEY_EPICS = "epics";
    final static String KEY_HISTORY = "history";
    final KVTaskClient client;
    private static final Gson gson = CreateGson.getGson();

    public HTTPTaskManager(String urlKVServer) throws IOException, InterruptedException {
        client = new KVTaskClient(urlKVServer);
    }

    private void load() {
        ArrayList<Task> tasks = gson.fromJson(client.load(KEY_TASKS), new TypeToken<ArrayList<Task>>() {}.getType());
        addTasks(tasks);
        ArrayList<Epic> epics = gson.fromJson(client.load(KEY_EPICS), new TypeToken<ArrayList<Epic>>(){}.getType());
        addTasks(epics);
        ArrayList<Subtask> subtasks = gson.fromJson(client.load(KEY_SUBTASKS),
                new TypeToken<ArrayList<Subtask>>(){}.getType());
        addTasks(subtasks);

        List<Integer> history = gson.fromJson(client.load(KEY_HISTORY), new TypeToken<ArrayList<Integer>>() {
        }.getType());
        for (Integer taskId : history) {
            historyManager.addTask(getTaskById(taskId));
        }
    }
    protected void addTasks(List<? extends Task> tasks) {
        for (Task task : tasks) {
            final int id = task.getId();
            if (id > generatedTaskId) {
                generatedTaskId = id;
            }
            Type type = task.getType();
            if (type == TASK) {
                this.tasks.put(id, task);
                prioritizedTasks.add(task);
            } else if (type == SUBTASK) {
                subtasks.put(id, (Subtask) task);
                prioritizedTasks.add(task);
            } else if (type == EPIC) {
                epics.put(id, (Epic) task);
            }
        }
    }

    public HTTPTaskManager(String path, KVTaskClient client) {
        super(path);
        this.client = client;
    }

    @Override
    public void save() {
        client.put(KEY_TASKS, gson.toJson(tasks.values()));
        client.put(KEY_SUBTASKS, gson.toJson(subtasks.values()));
        client.put(KEY_EPICS, gson.toJson(epics.values()));
        client.put(KEY_HISTORY, gson.toJson(historyManager.getHistory()));
    }
}
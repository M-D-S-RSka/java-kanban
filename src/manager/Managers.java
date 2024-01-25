package manager;

import server.HTTPTaskManager;

import java.io.IOException;

public class Managers {
    public static TaskManager getInMemoryTaskManger() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    static public TaskManager getDefault(String url) throws IOException, InterruptedException {
        return new HTTPTaskManager(url);
    }
}
package manager;

public class Managers {
    public static TaskManager getInMemoryTaskManger() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
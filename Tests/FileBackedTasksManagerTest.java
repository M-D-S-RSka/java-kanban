import exception.ManagerSaveException;
import manager.FileBackedTasksManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    private final String path = "src/file221.csv";
    private final File file = new File(path);

    @Override
    public FileBackedTasksManager createTaskManager() {
        return new FileBackedTasksManager(path);
    }

    @AfterEach
    public void afterEach() {
        try {
            Files.delete(Path.of(path));
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Test
    public void correctlyLoadfromfileEndSave() {
        taskManager.createTask(task);
        taskManager.createEpic(epic);
        taskManager.getHistory();
        FileBackedTasksManager fileManager = FileBackedTasksManager.loadFromFile(file);

        assertEquals(fileManager.getTaskById(task.getId()), task);
        assertEquals(fileManager.getEpicById(epic.getId()), epic);
        assertEquals(0, fileManager.getEpicById(epic.getId()).getSubtaskIds().size());
    }

    @Test
    public void shouldSaveOfEmptyThrowException() {
        final ManagerSaveException exception = assertThrows(
                ManagerSaveException.class,
                () -> FileBackedTasksManager.loadFromFile(file));

        assertEquals("src/file221.csv (No such file or directory)", exception.getMessage());
    }

    @Test
    public void shouldSaveEmptyHistoryThrowException() {
        final ManagerSaveException exception = assertThrows(
                ManagerSaveException.class,
                () -> FileBackedTasksManager.loadFromFile(file).getHistory());

        assertEquals("src/file221.csv (No such file or directory)", exception.getMessage());
    }
}

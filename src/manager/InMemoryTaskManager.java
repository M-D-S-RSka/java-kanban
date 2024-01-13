package manager;

import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static tasks.Status.IN_PROGRESS;
import static tasks.Status.NEW;


public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    protected Integer generatedTaskId = 0;

    @Override
    public List<Task> getPrioritizedTasks() {
//        //prioritizedTasks.clear();
//        prioritizedTasks.addAll(tasks.values());
//        prioritizedTasks.addAll(subtasks.values());
        return new ArrayList<>(prioritizedTasks);
    }


    @Override
    public Integer createTask(Task task) {
        if (task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.MAX); task.setDuration(Duration.ofSeconds(0)); 
            task.setId(++generatedTaskId);
            prioritizedTasks.add(task);
            tasks.put(task.getId(), task);
            return task.getId();
        }
        else  {
            addTaskToPrioritizedList(task);
            final int id = ++generatedTaskId;
            task.setId(id);
            tasks.put(id, task);
            return id;
        }
    }


    @Override
    public Integer createSubtask(Subtask task) {
        if (task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.MAX); task.setDuration(Duration.ofSeconds(0)); 
            task.setId(++generatedTaskId);
            int epicId = task.getEpicId();
            Epic epic = epics.get(epicId);
            epic.addSubtaskIds(task.getId());
            prioritizedTasks.add(task);
            subtasks.put(task.getId(), task);
            return task.getId();
        }
        else  {
            addTaskToPrioritizedList(task);
            final int id = ++generatedTaskId;
            task.setId(id);
            int epicId = task.getEpicId();
            Epic epic = epics.get(epicId);
            epic.addSubtaskIds(task.getId());
            subtasks.put(id, task);
            updateTimeEpic(epic);
            return id;
        }
    }

    @Override
    public Integer createEpic(Epic task) {
        final int id = ++generatedTaskId;
        task.setId(id);
        epics.put(id, task);
        return id;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void updateTask(Task task) {
        if (task == null)
            return;
        if (tasks.containsKey(task.getId())) {
            if (task.getStartTime() == null) {
                Task oldTask = tasks.put(task.getId(), task);
                task.setStartTime(LocalDateTime.MAX); task.setDuration(Duration.ofSeconds(0)); 
                prioritizedTasks.remove(oldTask);
                prioritizedTasks.add(task);
            } else if (getBooleanPrioritizedTasks(task)) {
                Task oldTask = tasks.put(task.getId(), task);
                prioritizedTasks.remove(oldTask);
                addTaskToPrioritizedList(task);
            } else {
                throw new RuntimeException("Время начала задачи уже занято.");
            }
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask task) {
        if (task == null)
            return;
        if (subtasks.containsKey(task.getId())) {
            if (task.getStartTime() == null) {
                Subtask oldTask = subtasks.put(task.getId(), task);
                task.setStartTime(LocalDateTime.MAX); task.setDuration(Duration.ofSeconds(0)); 
                prioritizedTasks.remove(oldTask);
                prioritizedTasks.add(task);
                updateEpicStatus(task.getEpicId());
            } else if (!getBooleanPrioritizedTasks(task)) {
                throw new RuntimeException("Время начала задачи уже занято.");
            } else {
                Subtask oldTask = subtasks.put(task.getId(), task);
                prioritizedTasks.remove(oldTask);
                addTaskToPrioritizedList(task);
                updateEpicStatus(task.getEpicId());
                updateTimeEpic(epics.get(task.getEpicId()));
            }
        }
    }

    @Override
    public List<Subtask> getAllEpicSubtasks(int epicId) {
        ArrayList<Subtask> subtaskArrayList = new ArrayList<>();
        Epic epic = epics.get(epicId);

        for (Integer idSubtask : epic.getSubtaskIds()) {
            subtaskArrayList.add(subtasks.get(idSubtask));
        }
        return subtaskArrayList;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        List<Integer> subs = epic.getSubtaskIds();
        Status status = null;
        if (subs.isEmpty()) {
            epic.setStatus(NEW);
            return;
        }
        for (Integer task : subs) {
            final Subtask subtask = subtasks.get(task);
            if (status == null) {
                status = subtask.getStatus();
                continue;
            }
            if (status == subtask.getStatus()
                    && status != IN_PROGRESS) {
                continue;
            }
            epic.setStatus(IN_PROGRESS);
            return;
        }
        epic.setStatus(status);
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
//        tasks.remove(id);
        historyManager.remove(id);
        prioritizedTasks.remove(task);
    }

    @Override
    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
            historyManager.remove(id);
            prioritizedTasks.remove(epic);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Integer epicId = subtasks.get(id).getEpicId();
            Epic epic = epics.get(epicId);
            Subtask subtask = subtasks.remove(id);
            epics.get(epicId).removeSubtask(id);
            historyManager.remove(id);
            prioritizedTasks.remove(subtask);
            updateEpicStatus(epicId);
            updateTimeEpic(epic);
        }
    }

    @Override
    public Task getTaskById(int id) {
        final Task task = tasks.get(id);
        historyManager.addTask(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        final Epic epic = epics.get(id);
        historyManager.addTask(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        final Subtask subtask = subtasks.get(id);
        historyManager.addTask(subtask);
        return subtask;
    }

    @Override
    public void deleteAllTask() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpic() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            prioritizedTasks.remove(epic);
        }
        epics.clear();
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtask() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtask();
            updateEpicStatus(epic.getId());
            updateTimeEpic(epic);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public Boolean getBooleanPrioritizedTasks(Task task) {
//        boolean isTask = true;
////        if (prioritizedTasks.isEmpty()) {
////            prioritizedTasks.add(task);
////        }
        final LocalDateTime startTime = task.getStartTime();
        final LocalDateTime endTime = task.getEndTime();
        for (Task t : prioritizedTasks) {
            final LocalDateTime existStart = t.getStartTime();
            final LocalDateTime existEnd = t.getEndTime();
            if (!endTime.isAfter(existStart)) {// newTimeEnd <= existTimeStart
                continue;
            }
            if (!existEnd.isAfter(startTime)) {// existTimeEnd <= newTimeStart
                continue;
            }
            throw new TaskConflictException("Задача пересекаются с id=" + t.getId() + " c " + existStart + " по " + existEnd);
        }
        return true;
//
//
//        for (Task taskPrioritized : getPrioritizedTasks()) {
//            if (task.getStartTime().equals(taskPrioritized.getStartTime())
//                    || task.getStartTime().equals(taskPrioritized.getEndTime())
//                    || task.getEndTime().equals(taskPrioritized.getEndTime())) {
//                isTask = false;
//            }
//        }
//        return isTask;
    }

//    private void updateTimeEpic(Epic epic) {
//        if (epic.getStartTime() != null) {
//            for (Integer idSubtask : epic.getSubtaskIds()) {
//                if (epic.getStartTime().isAfter(subtasks.get(idSubtask).getStartTime())) {
//                    epic.setDuration(subtasks.get(idSubtask).getDuration());
//                    epic.setStartTime(subtasks.get(idSubtask).getStartTime());
//                }
//                if (epic.getEndTime().isBefore(subtasks.get(idSubtask).getEndTime())) {
//                    epic.setEndTime(subtasks.get(idSubtask).getEndTime());
//                }
//            }
//        }
//    }

    private void updateTimeEpic(Epic epic) {
        List<Integer> subs = epic.getSubtaskIds();
        if (subs.isEmpty()) {
            epic.setDuration(Duration.of(0L, ChronoUnit.MINUTES));
            return;
        }
        LocalDateTime start = LocalDateTime.MAX;
        LocalDateTime end = LocalDateTime.MIN;
        long duration = 0L;
        for (int id : subs) {
            final Subtask subtask = subtasks.get(id);
            final LocalDateTime startTime = subtask.getStartTime();
            final LocalDateTime endTime = subtask.getEndTime();
            if (startTime.isBefore(start)) {
                start = startTime;
            }
            if (endTime.isAfter(end)) {
                end = endTime;
            }
            duration += subtask.getDuration().get(ChronoUnit.MINUTES);
        }
        epic.setDuration(Duration.of(duration, ChronoUnit.MINUTES));
        epic.setStartTime(start);
        epic.setEndTime(end);
    }

    public void addTaskToPrioritizedList(Task task) {
        boolean isValidated = getBooleanPrioritizedTasks(task);
        if (!isValidated) {
            prioritizedTasks.add(task);
        } else {
            throw new TaskConflictException("There is a problem caused by similar tasks time");
        }
    }

    public static class TaskConflictException extends RuntimeException {
        public TaskConflictException(final String message) {
            super(message);
        }
    }
//
//    private boolean validation(Task task) {
//        boolean isOverlapping = false;
//        Instant startOfTask = task.getStartTime();
//        Instant endOfTask = task.getEndTime();
//        for (Task taskValue : prioritizedTasks) {
//            if (taskValue.getStartTime() == null) {
//                continue;
//            }
//            Instant startTime = taskValue.getStartTime();
//            Instant endTime = taskValue.getEndTime();
//            boolean isCovering = startTime.isBefore(startOfTask) && endTime.isAfter(endOfTask);
//            boolean isOverlappingByEnd = startTime.isBefore(startOfTask) && endTime.isAfter(startOfTask);
//            boolean isOverlappingByStart = startTime.isBefore(endOfTask) && endTime.isAfter(endOfTask);
//            boolean isWithin = startTime.isAfter(startOfTask) && endTime.isBefore(endOfTask);
//            isOverlapping = isCovering || isOverlappingByEnd || isOverlappingByStart || isWithin;
//        }
//        return isOverlapping;
//    }
}


package tasks;

public enum StatusOfTask {
    NEW,
    IN_PROGRESS,
    DONE;

    public static StatusOfTask getStatus(String value) {
        switch (value) {
            case "NEW":
                return StatusOfTask.NEW;
            case "IN_PROGRESS":
                return StatusOfTask.IN_PROGRESS;
            case "DONE":
                return StatusOfTask.DONE;
            default:
                return null;
        }
    }
}
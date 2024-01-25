package utilities;

public enum Status {
    NEW("NEW"), IN_PROGRESS("IN_PROGRESS"), DONE("DONE");

    private final String status;

    Status(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
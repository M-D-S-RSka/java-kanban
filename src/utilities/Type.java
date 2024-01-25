package utilities;

public enum Type {
    TASK("TASK"), SUBTASK("SUBTASK"), EPIC("EPIC");

    private final String Type;

    Type(String Type) {
        this.Type = Type;
    }

    public String getType() {
        return Type;
    }
}
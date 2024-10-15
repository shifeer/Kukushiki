package ru.troyanov.Redis;

import java.util.Arrays;

public enum Status {
    PROCESSING("processing"),
    ERROR("error"),
    ERROR_FORMAT("error_format"),
    DONE("done");

    private String value;

    Status(String s) {
        this.value = s;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static Status fromString(String value) {
        return Arrays.stream(Status.values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}

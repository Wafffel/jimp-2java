package pl.graph;

public enum ExitCodes {
    SUCCESS(0),
    ARGUMENTS_ERROR(1),
    FILE_ERROR(2),
    INPUT_FORMAT_ERROR(3),
    ALGORITHM_ERROR(4),
    INVALID_ITERATION_COUNT(5),
    OUTPUT_WRITE_ERROR(6),
    UNKNOWN_ARGUMENT(7),
    MEMORY_ERROR(8),
    EMPTY_OR_INVALID_GRAPH(9),
    TUTTE_ASSUMPTIONS_ERROR(10),
    NUMERICAL_ERROR(11),
    INPUT_TOO_LARGE(12);

    private final int code;

    ExitCodes(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}

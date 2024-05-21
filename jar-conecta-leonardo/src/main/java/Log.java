public class Log {
    private String date;
    private String logLevel;
    private Integer statusCode;
    private Integer idMaquina;
    private String hostnameMaquina;
    private String message;
    private String stackTrace;

    public Log(String date, String logLevel, Integer statusCode, String message, String stackTrace) {
        this.date = date;
        this.logLevel = logLevel;
        this.statusCode = statusCode;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public Log(String date, String logLevel, Integer statusCode, Integer idMaquina, String hostnameMaquina, String message, String stackTrace) {
        this.date = date;
        this.logLevel = logLevel;
        this.statusCode = statusCode;
        this.idMaquina = idMaquina;
        this.hostnameMaquina = hostnameMaquina;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public String toStringMessage() {
        return "[%s] %s {'statusCode': %s, 'detail': '{\"message\": \"%s\"}', 'stackTrace': '%s'}".formatted(date, logLevel, statusCode, message, stackTrace);
    }

    @Override
    public String toString() {
        return "[%s] %s {'statusCode': %s, 'detail': '{\"message\": \"%s\", \"idMaquina\": %d, \"hostnameMaquina\": \"%s\"}', 'stackTrace': '%s'}".formatted(date, logLevel, statusCode, message, idMaquina, hostnameMaquina, stackTrace);
    }
}
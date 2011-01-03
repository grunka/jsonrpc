package se.grunka.jsonrpc;

class ServiceError {


    private final Class<? extends Throwable> type;
    private final String message;


    public Class<? extends Throwable> getType() {
        return type;
    }


    public String getMessage() {
        return message;
    }


    @SuppressWarnings("unused")
    public ServiceError() {
        this(null, null);
    }


    public ServiceError(Class<? extends Throwable> type, String message) {
        this.type = type;
        this.message = message;
    }
}

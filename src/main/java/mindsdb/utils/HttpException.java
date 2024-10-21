package mindsdb.utils;

public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public HttpException(int statusCode, String responseBody) {
        super(String.format("HTTP Error %d: %s", statusCode, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
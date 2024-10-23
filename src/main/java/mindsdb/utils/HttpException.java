package mindsdb.utils;

/**
 * Custom exception to handle HTTP errors.
 */
public class HttpException extends RuntimeException {
    /**
     * The HTTP status code.
     */
    private final int statusCode;

    /**
     * The response body.
     */
    private final String responseBody;

    /**
     * Constructs a new HttpException with the specified status code and response body.
     *
     * @param statusCode the HTTP status code
     * @param responseBody the response body
     */
    public HttpException(int statusCode, String responseBody) {
        super(String.format("HTTP Error %d: %s", statusCode, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the response body.
     *
     * @return the response body
     */
    public String getResponseBody() {
        return responseBody;
    }
}
import java.util.List;

public class Request {

    private final String method;
    private final String path;
    private final List<String> headers;
    private final String body;


    public Request(String method,String path, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Request{" +
                "Метод ='" + method + '\'' +
                ", путь ='" + path + '\'' +
                ", заголовки ='" + headers + '\'' +
                ", тело запроса ='" + body + '\'' +
                '}';
    }
}

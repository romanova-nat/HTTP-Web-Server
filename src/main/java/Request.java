import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Request {

    private final String method;
    private String URL;
    private final String path;
    private final String query;
    private final List<String> headers;
    private final String body;
    private List<NameValuePair> queryParam;

    public Request(String method, String URL, String path, String query, List<String> headers, String body) {
        this.method = method;
        this.URL = URL;
        this.path = path;
        this.query = query;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getURL() {
        return URL;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Request{" + "Метод = '" + getMethod() + '\'' + " , параметры: "+ getQuery()+ '\'' + '\'' + ", путь ='" + getPath() + '\'' + ", заголовки ='" + getHeaders() + '\'' + ", тело запроса ='" + getBody() + '\'' + '}';
    }

    public List<NameValuePair> getQueryParams() {
        queryParam = URLEncodedUtils.parse(URI.create(URL), String.valueOf(StandardCharsets.UTF_8));
        if (!queryParam.isEmpty()) {
            for (NameValuePair nvp : queryParam) {
                System.out.println("Параметры: " + nvp.getName() + " = " + nvp.getValue());
            }
        }
        return queryParam;
    }

    public List<NameValuePair> getQueryParam(String name) {
        try {
            queryParam = URLEncodedUtils.parse(new URI(URL), String.valueOf(StandardCharsets.UTF_8));
        } catch (URISyntaxException e) {
            System.out.println("Не получилось");;
        }
        if (!queryParam.isEmpty()) {
            for (NameValuePair nvp : queryParam) {
                if (nvp.getName().equals(name)) {
                    System.out.println("Параметры: " + nvp.getName() + " = " + nvp.getValue());
                }
            }
        }
        return queryParam;
    }

    public Map<String, String> getMethodPostParams() {
        Map<String, String> methodPostParams = new HashMap<>();
        final var bodyLine = body.split("&");
        for (String bl : bodyLine) {
            var values = bl.split("=");
            if (values.length == 2) {
                methodPostParams.put(bodyLine[0], bodyLine[1]);
                System.out.println(bodyLine[0] + " = " + bodyLine[1]);
            }
        }
        return methodPostParams;
    }

    public Map<String, String> getMethodPostParam(String name) {
        Map<String, String> methodPostParams = new HashMap<>();
        final var bodyLine = body.split("&");
        for (String bl : bodyLine) {
            var values = bl.split("=");
            if (values.length == 2) {
                methodPostParams.put(bodyLine[0], bodyLine[1]);
                if (bodyLine[0].equals(name)) {
                    System.out.println(bodyLine[0] + " = " + bodyLine[1]);
                }
            }
        }
        return methodPostParams;
    }

}

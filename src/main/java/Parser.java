import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Parser {

    public static final String GET = "GET";
    public static final String POST = "POST";
    protected final List<String> allowedMethods = List.of(GET, POST);

    public Request getRequest(String startURL, BufferedInputStream in, BufferedOutputStream out) throws IOException, URISyntaxException {
        // лимит на request line + заголовки
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            badRequest(out);
            return null;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            badRequest(out);
            return null;
        }

        // определяем метод
        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            badRequest(out);
            return null;
        }

        // определяем путь
        final var pathWhithQuery = requestLine[1];
        if (!pathWhithQuery.startsWith("/") && pathWhithQuery.startsWith("/favicon")) {
            badRequest(out);
            return null;
        }
               System.out.println("Параметры: " + pathWhithQuery);


        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            badRequest(out);
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));


        // для GET тела нет
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                final var body = new String(bodyBytes);
                System.out.println(body);
            }
        }

        if (method.equals(GET)) {
            var pathAndQuery = pathWhithQuery.split("\\?");
            if (pathAndQuery.length == 2) {
                var path = pathAndQuery[0];
               var query = "?" + pathAndQuery[1];
                var URL = startURL + path + query;
//                System.out.println(new Request(method, URL, path, query, headers, null));
                return new Request(method, URL, path, query, headers, null);
            } else if (pathAndQuery.length == 1) {
                var path = pathAndQuery[0];
                var URL = startURL + path;
//                System.out.println(new Request(method, URL, path, null, headers, null));
                return new Request(method, URL, path, null, headers, null);
            }
        }
        var URL = startURL + pathWhithQuery;
        return new Request(method, URL, pathWhithQuery, null, headers, null);
    }

    private Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }


    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}


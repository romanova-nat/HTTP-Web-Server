import org.apache.http.NameValuePair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private int port;
    private int countOfThreads; // количество потоков
    private String startURL = "http://localhost:";
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, Hendlers>> allHandlers = new ConcurrentHashMap();


    public void addHandler(String method, String path, Hendlers hendlers) {

        // сохраняем хендлеры в мапу
        var methodMap = allHandlers.get(method);
        if (methodMap == null) {
            methodMap = new ConcurrentHashMap<>();
            allHandlers.put(method, methodMap);
        }
        methodMap.put(path, hendlers);
    }


    public void listen(int port, int countOfThreads) throws IllegalAccessException {
        if (port <= 0)
            throw new IllegalAccessException("Server port must be greater than 0");

        ExecutorService executorService = Executors.newFixedThreadPool(countOfThreads);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                var socket = serverSocket.accept();
                executorService.execute(() -> handelConnection(socket));
            }
        } catch (IOException e) {
            System.out.println("Не подключился");
        }
    }

    private void handelConnection(Socket socket) {
        try (
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {

            Parser parser = new Parser();
            Request request = parser.getRequest(startURL + port, in, out);
            if (request == null) {
                return;
            }

            if (!request.getPath().startsWith("/favicon")) {
                final var path = request.getPath();
                final var filePath = Path.of(".", "static", path);
                final var mimeType = Files.probeContentType(filePath);

                if (path.equals("/default-get.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                } else {
                    parser.badRequest(out);
                }

                if (!request.getMethod().equals("GET")) {
                    Map<String, String> postParams = request.getMethodPostParams();
                    Map<String, String> postParam = request.getMethodPostParam("value");
                }

                if (!request.getMethod().equals("POST")) {
                    List<NameValuePair> queryParams = request.getQueryParams();
                    List<NameValuePair> queryParam = request.getQueryParam("value");

                }
            }

            var methodMap = allHandlers.get(request.getMethod());
            if (methodMap == null) {  // если мапа пустая
                notFound(out);
                return;
            }

            var hendlers = methodMap.get(request.getPath());
            if (hendlers == null) {
                notFound(out);
                return;
            }
            hendlers.handle(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}

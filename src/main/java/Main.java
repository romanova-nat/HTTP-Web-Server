public class Main {
    public static void main(String[] args) throws IllegalAccessException {

        int countOfThreads = 64; // количество потоков
        int port = 9999;

        final var server = new Server();

        server.addHandler("GET", "/messages", (request, out) -> {
            out.write((
                    "HTTP/1.1 201 Created\r\n" +
                            "Content-Type: text/plain" + "\r\n" +
                            "Content-Length: 14" + "\r\n" +
                            "Connection: close" + "\r\n" +
                            "\r\n" +
                            "Hello"
            ).getBytes());
            out.flush();

        });

        server.addHandler("POST", "/messages", (request, out) -> {
            out.write((
                    "HTTP/1.1 202 Accepted" + "\r\n" +
                            "Content-Type: text/plain" + "\r\n" +
                            "Content-Length: 15" + "\r\n" +
                            "Connection: close" + "\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        });

        server.listen(port, countOfThreads);
    }
}


















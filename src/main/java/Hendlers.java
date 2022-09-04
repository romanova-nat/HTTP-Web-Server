import java.io.BufferedOutputStream;
import java.io.IOException;

public interface Hendlers {

    void handle(Request request, BufferedOutputStream stream) throws IOException;
}

package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;

public class ProjectPostController implements HttpController{
    @Override
    public void handle(HttpMessage request, Socket clientSocket) throws IOException {
        QueryString requestParameter = new QueryString(request.getBody());

        String body = "You have added a new worker!";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;

        // Write the response back to the client
        clientSocket.getOutputStream().write(response.getBytes());

    }
}

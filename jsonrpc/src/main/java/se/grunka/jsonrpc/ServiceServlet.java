package se.grunka.jsonrpc;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServiceServlet extends HttpServlet {
    private final Gson gson = new Gson();


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServiceRequest serviceRequest = gson.fromJson(req.getReader(), ServiceRequest.class);
        serviceRequest.method();
        serviceRequest.arguments();
    }
}

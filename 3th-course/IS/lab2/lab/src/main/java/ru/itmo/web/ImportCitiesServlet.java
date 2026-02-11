package ru.itmo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.itmo.service.ImportService;
import ru.itmo.web.dto.ImportCitiesRequest;

import java.io.IOException;

@WebServlet(name = "ImportCitiesServlet", urlPatterns = "/api/import/cities")
public class ImportCitiesServlet extends HttpServlet {

    private final ImportService importService = new ImportService();
    private final ObjectMapper mapper;

    public ImportCitiesServlet() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String username = req.getHeader("X-User");
        String role = req.getHeader("X-Role");
        if (username == null || username.isBlank()) username = "anonymous";
        if (role == null || role.isBlank()) role = "USER";

        try {
            ImportCitiesRequest importReq =
                    mapper.readValue(req.getInputStream(), ImportCitiesRequest.class);

            long opId = importService.importCities(username, role, importReq);

            resp.setStatus(202);
            resp.getWriter().write("{\"opId\":" + opId + "}");

        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"" + safe(e.getMessage()) + "\"}");
        }
    }

    private String safe(String s) {
        if (s == null) return "error";
        return s.replace("\"", "'");
    }
}
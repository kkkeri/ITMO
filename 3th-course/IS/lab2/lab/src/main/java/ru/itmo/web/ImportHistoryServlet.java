package ru.itmo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.itmo.model.ImportOperation;
import ru.itmo.service.ImportOperationService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ImportHistoryServlet", urlPatterns = "/api/import/history")
public class ImportHistoryServlet extends HttpServlet {

    private final ImportOperationService service = new ImportOperationService();
    private final ObjectMapper mapper;

    public ImportHistoryServlet() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        // читаем пользователя и роль из заголовков
        String username = req.getHeader("X-User");
        String role = req.getHeader("X-Role");

        if (username == null || username.isBlank()) {
            sendError(resp, 400, "Заголовок X-User обязателен");
            return;
        }
        if (role == null || role.isBlank()) {
            role = "USER";
        }

        try {
            List<ImportOperation> history = service.getHistory(username, role);
            mapper.writeValue(resp.getWriter(), history);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, 500, "Ошибка при получении истории импорта");
        }
    }

    private void sendError(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        resp.getWriter().write("{\"error\":\"" + msg.replace("\"", "'") + "\"}");
    }
}
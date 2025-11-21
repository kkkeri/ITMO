package ru.itmo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.itmo.model.BookCreature;
import ru.itmo.service.BookCreatureService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebServlet(name = "BookCreatureServlet", urlPatterns = "/api/creatures")
public class BookCreatureServlet extends HttpServlet {

    private final BookCreatureService service = new BookCreatureService();
    private final ObjectMapper mapper;

    public BookCreatureServlet() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // get

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String idParam = req.getParameter("id");

        // если id не передан тл вернуть все существа
        if (idParam == null) {
            List<BookCreature> all = service.getAll();
            mapper.writeValue(resp.getWriter(), all);
            return;
        }

        // если id есть то ищем одного
        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            sendError(resp, 400, "Некорректный формат id");
            return;
        }

        Optional<BookCreature> found = service.getById(id);
        if (found.isPresent()) {
            mapper.writeValue(resp.getWriter(), found.get());
        } else {
            sendError(resp, 404, "Существо с id=" + id + " не найдено");
        }
    }

    // post

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        try {
            BookCreature creature = mapper.readValue(req.getInputStream(), BookCreature.class);

            Long id = service.createCreature(creature);

            resp.setStatus(201);
            resp.getWriter().write("{\"id\":" + id + "}");

        } catch (IllegalArgumentException e) {
            // проверки из модели/сервиса
            e.printStackTrace();
            sendError(resp, 400, e.getMessage());
        } catch (Exception e) {
            // выводим полную причину, нужна для исправления ошибок(позде надо исправить)
            e.printStackTrace();
            String msg = "Некорректные данные существа: "
                    + e.getClass().getSimpleName()
                    + " - " + (e.getMessage() != null ? e.getMessage() : "(no message)");
            sendError(resp, 400, msg);
        }
    }

    // put

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String idParam = req.getParameter("id");
        if (idParam == null) {
            sendError(resp, 400, "Параметр id обязателен");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            sendError(resp, 400, "Некорректный id");
            return;
        }

        try {
            BookCreature creature = mapper.readValue(req.getInputStream(), BookCreature.class);
            creature.setId(id);

            service.updateCreature(creature);

            resp.setStatus(200);
        } catch (IllegalArgumentException e) {
            sendError(resp, 400, e.getMessage());
        } catch (Exception e) {
            sendError(resp, 400, "Некорректные данные при обновлении");
        }
    }

    // delete

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String idParam = req.getParameter("id");
        if (idParam == null) {
            sendError(resp, 400, "Параметр id обязателен");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            sendError(resp, 400, "Некорректный id");
            return;
        }

        service.deleteCreature(id);
        resp.setStatus(204);
    }

    // помощник

    private void sendError(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        resp.getWriter().write("{\"error\":\"" + msg.replace("\"", "'") + "\"}");
    }
}
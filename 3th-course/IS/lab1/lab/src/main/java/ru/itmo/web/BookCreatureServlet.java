package ru.itmo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.itmo.model.BookCreature;
import ru.itmo.service.BookCreatureService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "BookCreatureServlet", urlPatterns = "/api/creatures")
public class BookCreatureServlet extends HttpServlet {

    private final BookCreatureService service = new BookCreatureService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String idParam = req.getParameter("id");

        if (idParam == null) {
            List<BookCreature> all = service.getAll();
            mapper.writeValue(resp.getWriter(), all);
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Некорректный id\"}");
            return;
        }

        Optional<BookCreature> found = service.getById(id);
        if (found.isPresent()) {
            mapper.writeValue(resp.getWriter(), found.get());
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Существо не найдено\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            BookCreature creature = mapper.readValue(req.getInputStream(), BookCreature.class);
            Long id = service.createCreature(creature);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"id\":" + id + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Ошибка создания\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"id обязателен\"}");
            return;
        }

        long id = Long.parseLong(idParam);

        BookCreature creature = mapper.readValue(req.getInputStream(), BookCreature.class);
        creature.setId(id);
        service.updateCreature(creature);

        resp.setStatus(200);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"id обязателен\"}");
            return;
        }

        long id = Long.parseLong(idParam);
        service.deleteCreature(id);

        resp.setStatus(204);
    }
}
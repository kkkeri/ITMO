package ru.itmo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.itmo.model.MagicCity;
import ru.itmo.service.MagicCityService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "MagicCityServlet", urlPatterns = "/api/cities")
public class MagicCityServlet extends HttpServlet {

    private final MagicCityService service = new MagicCityService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String idParam = req.getParameter("id");

        // без id -> вернуть все города
        if (idParam == null) {
            List<MagicCity> all = service.getAll();
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

        Optional<MagicCity> found = service.getById(id);
        if (found.isPresent()) {
            mapper.writeValue(resp.getWriter(), found.get());
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Город не найден\"}");
        }
    }

    // создание нового города
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            MagicCity city = mapper.readValue(req.getInputStream(), MagicCity.class);
            Long id = service.createCity(city);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"id\":" + id + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"error\":\"Ошибка при создании города\"}");
        }
    }

    // обновление города: PUT /api/cities?id=...
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр id обязателен\"}");
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

        try {
            MagicCity city = mapper.readValue(req.getInputStream(), MagicCity.class);
            city.setId(id);
            service.updateCity(city);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Ошибка при обновлении города\"}");
        }
    }

    /**
     * Удаление города с перепривязкой существ.
     * deleteCityId = id удаляемого города (id)
     * reassignmentCityId = id города, к которому перепривязать существ (может быть null)
     *
     * DELETE /api/cities?deleteId=1&reassignId=2
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String deleteIdParam = req.getParameter("deleteId");
        String reassignIdParam = req.getParameter("reassignId"); // может быть null

        if (deleteIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр deleteId обязателен\"}");
            return;
        }

        Long deleteId;
        try {
            deleteId = Long.parseLong(deleteIdParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Некорректный deleteId\"}");
            return;
        }

        Long reassignId = null;
        if (reassignIdParam != null && !reassignIdParam.isBlank()) {
            try {
                reassignId = Long.parseLong(reassignIdParam);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Некорректный reassignId\"}");
                return;
            }
        }

        try {
            service.deleteCityWithReassignment(deleteId, reassignId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" +
                    ex.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
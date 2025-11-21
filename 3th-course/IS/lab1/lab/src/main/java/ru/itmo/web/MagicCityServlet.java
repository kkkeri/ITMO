package ru.itmo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.itmo.model.MagicCity;
import ru.itmo.service.MagicCityService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "MagicCityServlet", urlPatterns = "/api/cities")
public class MagicCityServlet extends HttpServlet {

    private final MagicCityService service = new MagicCityService();
    private final ObjectMapper mapper;

    public MagicCityServlet() {
        mapper = new ObjectMapper();
        // поддержка java.time.*
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // get

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String idParam = req.getParameter("id");

        if (idParam == null) {
            List<MagicCity> all = service.getAll();
            mapper.writeValue(resp.getWriter(), all);
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            sendError(resp, 400, "Некорректный формат id");
            return;
        }

        Optional<MagicCity> found = service.getById(id);
        if (found.isPresent()) {
            mapper.writeValue(resp.getWriter(), found.get());
        } else {
            sendError(resp, 404, "Город с id=" + id + " не найден");
        }
    }

    // post

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        try {
            MagicCity city = mapper.readValue(req.getInputStream(), MagicCity.class);
            Long id = service.createCity(city);

            resp.setStatus(201);
            resp.getWriter().write("{\"id\":" + id + "}");

        } catch (IllegalArgumentException e) {
            sendError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // для исправления
            sendError(resp, 400, "Некорректные данные города");
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
            MagicCity city = mapper.readValue(req.getInputStream(), MagicCity.class);
            city.setId(id);

            service.updateCity(city);

            resp.setStatus(200);

        } catch (IllegalArgumentException e) {
            sendError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, 400, "Некорректные данные при обновлении");
        }
    }

    // удаление с учетом перевязки

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String deleteIdParam = req.getParameter("deleteId");
        if (deleteIdParam == null) {
            sendError(resp, 400, "Параметр deleteId обязателен");
            return;
        }

        long deleteId;
        try {
            deleteId = Long.parseLong(deleteIdParam);
        } catch (NumberFormatException e) {
            sendError(resp, 400, "Некорректный deleteId");
            return;
        }

        Long reassignId = null;
        String reassignParam = req.getParameter("reassignId");
        if (reassignParam != null && !reassignParam.isBlank()) {
            try {
                reassignId = Long.parseLong(reassignParam);
            } catch (NumberFormatException e) {
                sendError(resp, 400, "Некорректный reassignId");
                return;
            }
        }

        try {
            service.deleteCityWithReassignment(deleteId, reassignId);
            resp.setStatus(204);
        } catch (IllegalArgumentException e) {
            sendError(resp, 400, e.getMessage());
        }
    }

    // помощник

    private void sendError(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        resp.getWriter().write("{\"error\":\"" + msg.replace("\"", "'") + "\"}");
    }
}

package ru.itmo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.itmo.model.BookCreature;
import ru.itmo.service.specialop.SpecialOperationsService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SpecialOperationsServlet", urlPatterns = "/api/special")
public class SpecialOperationsServlet extends HttpServlet {

    private final SpecialOperationsService service = new SpecialOperationsService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String op = req.getParameter("op");
        if (op == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр op обязателен\"}");
            return;
        }

        switch (op) {
            case "maxName" -> handleMaxName(resp);
            case "countRingLess" -> handleCountRingLess(req, resp);
            case "attackGreater" -> handleAttackGreater(req, resp);
            case "strongestRing" -> handleStrongestRing(resp);
            default -> {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неизвестная операция op=" + op + "\"}");
            }
        }
    }

    /**
     * Уничтожить города эльфов – это изменяющая операция,
     * сделаем её через POST /api/special?op=destroyElfCities
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String op = req.getParameter("op");
        if (!"destroyElfCities".equals(op)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Поддерживается только op=destroyElfCities для POST\"}");
            return;
        }

        int count = service.destroyElfCities();
        resp.setContentType("application/json; charset=UTF-8");
        mapper.writeValue(resp.getWriter(), Map.of("destroyedCities", count));
    }

    private void handleMaxName(HttpServletResponse resp) throws IOException {
        var opt = service.getCreatureWithMaxName();
        if (opt.isPresent()) {
            mapper.writeValue(resp.getWriter(), opt.get());
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Существ нет\"}");
        }
    }

    private void handleCountRingLess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String maxParam = req.getParameter("maxPower");
        if (maxParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр maxPower обязателен\"}");
            return;
        }

        Integer max;
        try {
            max = Integer.parseInt(maxParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Некорректный maxPower\"}");
            return;
        }

        long count = service.countCreaturesWithRingPowerLessThan(max);
        mapper.writeValue(resp.getWriter(), Map.of("count", count));
    }

    private void handleAttackGreater(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String minParam = req.getParameter("minAttack");
        if (minParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр minAttack обязателен\"}");
            return;
        }

        Float min;
        try {
            min = Float.parseFloat(minParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Некорректный minAttack\"}");
            return;
        }

        List<BookCreature> list = service.getCreaturesWithAttackLevelGreaterThan(min);
        mapper.writeValue(resp.getWriter(), list);
    }

    private void handleStrongestRing(HttpServletResponse resp) throws IOException {
        var opt = service.getCreatureWithStrongestRing();
        if (opt.isPresent()) {
            mapper.writeValue(resp.getWriter(), opt.get());
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Нет существ с кольцами\"}");
        }
    }
}
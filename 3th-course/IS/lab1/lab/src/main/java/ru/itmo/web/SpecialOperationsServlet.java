package ru.itmo.web;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.itmo.model.BookCreature;
import ru.itmo.service.BookCreatureService;
import ru.itmo.service.MagicCityService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SpecialOperationsServlet", urlPatterns = "/api/special")
public class SpecialOperationsServlet extends HttpServlet {

    private final BookCreatureService creatureService = new BookCreatureService();
    private final MagicCityService cityService = new MagicCityService();

    private final ObjectMapper mapper;

    public SpecialOperationsServlet() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ======================= GET =======================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String op = req.getParameter("op");
        if (op == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Параметр op обязателен\"}");
            return;
        }

        switch (op) {
            case "maxName" -> handleMaxName(resp);
            case "countRingLess" -> handleCountRingLess(req, resp);
            case "attackGreater" -> handleAttackGreater(req, resp);
            case "strongestRing" -> handleStrongestRing(resp);

            default -> {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"Неизвестная операция op=" + op + "\"}");
            }
        }
    }

    // ======================= POST =======================

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String op = req.getParameter("op");

        if (!"destroyElfCities".equals(op)) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Поддерживается только op=destroyElfCities\"}");
            return;
        }

        int count = cityService.destroyElfCities();

        resp.setContentType("application/json; charset=UTF-8");
        mapper.writeValue(resp.getWriter(), Map.of("destroyedCities", count));
    }

    // ======================= HANDLERS =======================

    private void handleMaxName(HttpServletResponse resp) throws IOException {
        var opt = creatureService.getCreatureWithMaxName();
        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.getWriter().write("{\"error\":\"Существ нет\"}");
            return;
        }
        mapper.writeValue(resp.getWriter(), opt.get());
    }

    private void handleCountRingLess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String maxParam = req.getParameter("maxPower");
        if (maxParam == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Параметр maxPower обязателен\"}");
            return;
        }

        Integer max;
        try {
            max = Integer.parseInt(maxParam);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Некорректный maxPower\"}");
            return;
        }

        long count = creatureService.countCreaturesWithRingPowerLessThan(max);
        mapper.writeValue(resp.getWriter(), Map.of("count", count));
    }

    private void handleAttackGreater(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String minParam = req.getParameter("minAttack");

        if (minParam == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Параметр minAttack обязателен\"}");
            return;
        }

        Float min;
        try {
            min = Float.parseFloat(minParam);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Некорректный minAttack\"}");
            return;
        }

        List<BookCreature> list = creatureService.getCreaturesWithAttackLevelGreaterThan(min);
        mapper.writeValue(resp.getWriter(), list);
    }

    private void handleStrongestRing(HttpServletResponse resp) throws IOException {
        var opt = creatureService.getCreatureWithStrongestRing();
        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.getWriter().write("{\"error\":\"Нет существ с кольцами\"}");
            return;
        }

        mapper.writeValue(resp.getWriter(), opt.get());
    }
}
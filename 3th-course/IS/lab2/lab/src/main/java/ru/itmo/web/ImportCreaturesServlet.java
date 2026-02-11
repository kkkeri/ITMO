package ru.itmo.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.itmo.service.ImportService;
import ru.itmo.web.dto.ImportCreaturesRequest;

import java.io.IOException;

@WebServlet(name = "ImportCreaturesServlet", urlPatterns = "/api/import/creatures")
public class ImportCreaturesServlet extends HttpServlet {

    private final ImportService importService = new ImportService();
    private final ObjectMapper mapper;

    public ImportCreaturesServlet() {
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
            ImportCreaturesRequest importReq =
                    mapper.readValue(req.getInputStream(), ImportCreaturesRequest.class);

            long opId = importService.importCreatures(username, role, importReq);

            resp.setStatus(202); // операция зарегистрирована, статус смотри в истории
            resp.getWriter().write("{\"opId\":" + opId + "}");

        } catch (InvalidFormatException e) {
            // например: неверный enum (DRAGON), неверный тип числа и т.д.
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"" + safe(humanInvalidFormat(e)) + "\"}");

        } catch (JsonParseException e) {
            // битый JSON (скобки/запятые)
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Некорректный JSON: проверьте скобки, запятые и кавычки\"}");

        } catch (JsonMappingException e) {
            // структура не совпала (не то поле, не тот тип объекта и т.д.)
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Некорректная структура JSON файла\"}");

        } catch (Exception e) {
            // прочие ошибки ввода
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Не удалось прочитать файл импорта\"}");
        }
    }

    private String humanInvalidFormat(InvalidFormatException e) {
        // Пытаемся понять, какое поле сломалось
        String field = (e.getPath() != null && !e.getPath().isEmpty())
                ? e.getPath().get(e.getPath().size() - 1).getFieldName()
                : null;

        Class<?> target = e.getTargetType();
        String badValue = String.valueOf(e.getValue());

        // ====== Самое важное: enum BookCreatureType ======
        // DRAGON -> "Неверный тип существа..."
        if (target != null && target.getName().endsWith("BookCreatureType")) {
            return "Неверный тип существа: \"" + badValue + "\". Допустимые значения: HOBBIT, ELF, HUMAN, GOLLUM";
        }

        // Если хочешь — можно сделать более точные сообщения по полям
        if ("age".equals(field)) {
            return "Поле \"возраст\" должно быть целым числом больше 0";
        }
        if ("attackLevel".equals(field)) {
            return "Поле \"уровень атаки\" должно быть числом от 0 до 100";
        }
        if ("x".equals(field) || "y".equals(field)) {
            return "Координаты должны быть числами (y обязательно)";
        }

        // Фолбэк
        return "Некорректное значение в поле" + (field != null ? " \"" + field + "\"" : "") + ": \"" + badValue + "\"";
    }

    private String safe(String s) {
        if (s == null) return "Ошибка";
        return s.replace("\"", "'").replace("\n", " ").replace("\r", " ");
    }
}
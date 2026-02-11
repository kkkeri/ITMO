package ru.itmo.service;

import ru.itmo.db.ImportOperationRepository;
import ru.itmo.model.ImportOperation;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ImportOperationService {

    private final ImportOperationRepository repo = new ImportOperationRepository();

    // роль читаем из заголовка X-Role: "ADMIN" или "USER"
    public List<ImportOperation> getHistory(String username, String role) {
        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            return repo.findAll();
        }
        return repo.findAllByUser(username);
    }
}
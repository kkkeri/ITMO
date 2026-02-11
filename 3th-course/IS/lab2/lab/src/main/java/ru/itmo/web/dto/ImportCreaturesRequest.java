package ru.itmo.web.dto;

import java.util.List;

public class ImportCreaturesRequest {
    private List<CreatureImportItem> items;

    public ImportCreaturesRequest() {}

    public List<CreatureImportItem> getItems() {
        return items;
    }

    public void setItems(List<CreatureImportItem> items) {
        this.items = items;
    }
}
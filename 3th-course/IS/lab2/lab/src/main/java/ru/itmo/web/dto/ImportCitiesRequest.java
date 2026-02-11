package ru.itmo.web.dto;

import java.util.List;

public class ImportCitiesRequest {
    private List<CityImportItem> items;

    public ImportCitiesRequest() {}

    public List<CityImportItem> getItems() {
        return items;
    }

    public void setItems(List<CityImportItem> items) {
        this.items = items;
    }
}
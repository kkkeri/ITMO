package ru.itmo.controller;

import ru.itmo.model.BookCreature;
import ru.itmo.model.MagicCity;
import ru.itmo.model.util.BookCreatureType;
import ru.itmo.service.BookCreatureService;
import ru.itmo.service.MagicCityService;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ManagedBean(name = "bookCreatureController")
@SessionScoped
public class BookCreatureController implements Serializable {

    @ManagedProperty("#{bookCreatureService}")
    private BookCreatureService creatureService;

    @ManagedProperty("#{magicCityService}")
    private MagicCityService cityService;

    // ОБЯЗАТЕЛЬНЫ сеттеры для @ManagedProperty
    public void setCreatureService(BookCreatureService creatureService) {
        this.creatureService = creatureService;
    }

    public void setCityService(MagicCityService cityService) {
        this.cityService = cityService;
    }

    // Список существ для таблицы
    private List<BookCreature> creatures;

    // Список городов для выпадающего списка в форме
    private List<MagicCity> cities;

    // Строка фильтра по имени
    private String nameFilter;

    // Сущность, которую сейчас создаём или редактируем
    private BookCreature editingCreature;

    // id выбранного города в форме (может быть null)
    private Long selectedCityId;

    // Для выпадающего списка типов существ
    public List<BookCreatureType> getCreatureTypes() {
        return Arrays.asList(BookCreatureType.values());
    }

    @PostConstruct
    public void init() {
        reloadData();
    }

    private void reloadData() {
        creatures = creatureService.getAll();
        cities = cityService.getAll();
    }

    // ====== Таблица / список ======

    public List<BookCreature> getCreatures() {
        return creatures;
    }

    public String getNameFilter() {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
    }

    // Применить фильтр по имени
    public void applyNameFilter() {
        if (nameFilter == null || nameFilter.isBlank()) {
            creatures = creatureService.getAll();
        } else {
            creatures = creatureService.searchByName(nameFilter);
        }
    }

    // ====== Работа с городами в форме ======

    public List<MagicCity> getCities() {
        return cities;
    }

    public Long getSelectedCityId() {
        return selectedCityId;
    }

    public void setSelectedCityId(Long selectedCityId) {
        this.selectedCityId = selectedCityId;
    }

    // Применить выбранный город к editingCreature
    private void syncCreatureCityFromSelected() {
        if (editingCreature == null) return;

        if (selectedCityId == null) {
            editingCreature.setCreatureLocation(null);
        } else {
            Optional<MagicCity> cityOpt = cityService.getById(selectedCityId);
            editingCreature.setCreatureLocation(cityOpt.orElse(null));
        }
    }

    // Подставить selectedCityId из уже установленного creatureLocation
    private void syncSelectedFromCreatureCity() {
        if (editingCreature == null || editingCreature.getCreatureLocation() == null) {
            selectedCityId = null;
        } else {
            selectedCityId = editingCreature.getCreatureLocation().getId();
        }
    }

    // ====== Создание / редактирование ======

    public BookCreature getEditingCreature() {
        return editingCreature;
    }

    // Начать создание нового существа
    public void startCreate() {
        editingCreature = new BookCreature();
        selectedCityId = null;
    }

    // Начать редактирование по id
    public void startEdit(Long creatureId) {
        creatureService.getById(creatureId).ifPresent(c -> {
            editingCreature = c;
            syncSelectedFromCreatureCity();
        });
    }

    // Сохранить (создание или обновление)
    public void saveEditingCreature() {
        syncCreatureCityFromSelected();

        if (editingCreature.getId() == 0) {
            // Новый объект: id ещё не сгенерирован БД
            creatureService.createCreature(editingCreature);
        } else {
            // Уже существующий объект
            creatureService.updateCreature(editingCreature);
        }

        // Обновляем таблицу
        reloadData();

        // Чистим форму
        editingCreature = null;
        selectedCityId = null;
        nameFilter = null;
    }

    // Отмена редактирования
    public void cancelEditing() {
        editingCreature = null;
        selectedCityId = null;
    }

    // ====== Удаление ======

    public void deleteCreature(Long creatureId) {
        creatureService.deleteCreature(creatureId);
        reloadData();
    }
}
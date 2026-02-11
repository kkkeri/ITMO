const CITIES_API_BASE = 'api/cities';

// ====== Состояние ======
let allCities = [];
let filteredCities = [];

let editingCityId = null;
let deleteCityId = null;

let cityCurrentPage = 1;
let cityPageSize = 10;

// сортировка
let citySortField = null; // 'id','name','governor','area','population','capital','density','establishmentDate'
let citySortDir = 'asc';

// ====== Контекст пользователя (заголовки) ======
let X_USER = 'student';
let X_ROLE = 'USER';

function applyUserContext() {
    const u = document.getElementById('userNameInput')?.value.trim();
    const r = document.getElementById('userRoleSelect')?.value;

    X_USER = u || 'guest';
    X_ROLE = r || 'USER';

    alert(`Контекст установлен: ${X_USER} / ${X_ROLE}`);
}

function authHeaders() {
    return {
        'X-User': X_USER,
        'X-Role': X_ROLE
    };
}

// форматирование даты
function formatCityDate(raw) {
    if (!raw) return '';
    const noZone = raw.split('[')[0];
    const replaced = noZone.replace('T', ' ');
    return replaced.substring(0, 16);
}

// загружаем города
async function loadCities() {
    try {
        const resp = await fetch(CITIES_API_BASE, {
            headers: {
                ...authHeaders()
            }
        });

        if (!resp.ok) {
            throw new Error('Не удалось загрузить города (код ' + resp.status + ')');
        }

        allCities = await resp.json();
        filteredCities = allCities.slice();
        cityCurrentPage = 1;
        renderCitiesTable();
        hideCityGlobalError();
    } catch (e) {
        showCityGlobalError(e.message);
    }
}

// сортировка
function sortCitiesBy(field) {
    if (citySortField === field) {
        citySortDir = citySortDir === 'asc' ? 'desc' : 'asc';
    } else {
        citySortField = field;
        citySortDir = 'asc';
    }
    renderCitiesTable();
}

function compareCities(a, b) {
    if (!citySortField) return 0;

    let av, bv;

    switch (citySortField) {
        case 'id':
            av = a.id; bv = b.id; break;
        case 'name':
            av = (a.name || '').toString().toLowerCase();
            bv = (b.name || '').toString().toLowerCase();
            break;
        case 'governor':
            av = (a.governor || '').toString().toLowerCase();
            bv = (b.governor || '').toString().toLowerCase();
            break;
        case 'area':
            av = Number(a.area) || 0;
            bv = Number(b.area) || 0;
            break;
        case 'population':
            av = Number(a.population) || 0;
            bv = Number(b.population) || 0;
            break;
        case 'capital':
            av = !!a.capital;
            bv = !!b.capital;
            break;
        case 'density':
            av = Number(a.populationDensity) || 0;
            bv = Number(b.populationDensity) || 0;
            break;
        case 'establishmentDate':
            av = a.establishmentDate || '';
            bv = b.establishmentDate || '';
            break;
        default:
            return 0;
    }

    let cmp;
    if (typeof av === 'boolean' && typeof bv === 'boolean') {
        cmp = (av === bv) ? 0 : (av ? 1 : -1);
    } else if (typeof av === 'number' && typeof bv === 'number') {
        cmp = av - bv;
    } else {
        if (av < bv) cmp = -1;
        else if (av > bv) cmp = 1;
        else cmp = 0;
    }

    return citySortDir === 'asc' ? cmp : -cmp;
}

// табличка с учетом пагинации
function renderCitiesTable() {
    const tbody = document.getElementById('citiesTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    let data = filteredCities.slice();
    if (citySortField) {
        data.sort(compareCities);
    }

    const total = data.length;
    const totalPages = Math.max(1, Math.ceil(total / cityPageSize));

    if (cityCurrentPage > totalPages) cityCurrentPage = totalPages;
    if (cityCurrentPage < 1) cityCurrentPage = 1;

    const start = (cityCurrentPage - 1) * cityPageSize;
    const end = start + cityPageSize;
    const pageItems = data.slice(start, end);

    pageItems.forEach(city => {
        const tr = document.createElement('tr');

        const capitalStr = city.capital ? 'Да' : 'Нет';
        const governorStr = city.governor ? city.governor : '';
        const densityStr = city.populationDensity != null ? city.populationDensity : '';
        const estDateStr = formatCityDate(city.establishmentDate);

        tr.innerHTML = `
            <td>${city.id}</td>
            <td>${city.name}</td>
            <td>${city.area}</td>
            <td>${city.population}</td>
            <td>${estDateStr}</td>
            <td>${capitalStr}</td>
            <td>${governorStr}</td>
            <td>${densityStr}</td>
            <td>
                <button onclick="startEditCity(${city.id})">Редактировать</button>
                <button class="danger" onclick="prepareDeleteCity(${city.id})">Удалить</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    updateCityPaginationInfo(totalPages);
}

function updateCityPaginationInfo(totalPages) {
    const info = document.getElementById('cityPageInfo');
    if (info) {
        info.textContent = `Страница ${cityCurrentPage} из ${totalPages}`;
    }

    const prev = document.getElementById('cityPrevPage');
    const next = document.getElementById('cityNextPage');

    if (prev) prev.disabled = cityCurrentPage <= 1;
    if (next) next.disabled = cityCurrentPage >= totalPages;
}

// фильтры
function applyCityFilters() {
    const nameInput = document.getElementById('cityNameFilter');
    const governorInput = document.getElementById('cityGovernorFilter');

    const nameVal = nameInput ? nameInput.value.trim().toLowerCase() : '';
    const governorVal = governorInput ? governorInput.value.trim().toLowerCase() : '';

    filteredCities = allCities.filter(c => {
        let ok = true;

        if (nameVal) {
            const cityName = (c.name || '').toString().toLowerCase();
            ok = ok && cityName.includes(nameVal);
        }

        if (governorVal) {
            const gov = (c.governor || '').toString().toLowerCase();
            ok = ok && gov.includes(governorVal);
        }

        return ok;
    });

    cityCurrentPage = 1;
    renderCitiesTable();
}

function resetCityFilters() {
    const nameInput = document.getElementById('cityNameFilter');
    const governorInput = document.getElementById('cityGovernorFilter');

    if (nameInput) nameInput.value = '';
    if (governorInput) governorInput.value = '';

    filteredCities = allCities.slice();
    cityCurrentPage = 1;
    renderCitiesTable();
}

// совместимость с твоими кнопками
function resetCityFilter() { resetCityFilters(); }

// форма создания и редакт
function startCreateCity() {
    editingCityId = null;
    document.getElementById('cityEditTitle').textContent = 'Новый город';
    clearCityForm();
    showCityEditSection();
}

function startEditCity(id) {
    const city = allCities.find(c => c.id === id);
    if (!city) return;

    editingCityId = id;
    document.getElementById('cityEditTitle').textContent = 'Редактирование города #' + id;

    document.getElementById('cityName').value = city.name;
    document.getElementById('cityArea').value = city.area;
    document.getElementById('cityPopulation').value = city.population;

    if (city.establishmentDate) {
        const noZone = city.establishmentDate.split('[')[0];
        document.getElementById('cityEstablishmentDate').value = noZone.substring(0, 16);
    } else {
        document.getElementById('cityEstablishmentDate').value = '';
    }

    document.getElementById('cityCapital').checked = !!city.capital;
    document.getElementById('cityDensity').value =
        city.populationDensity != null ? city.populationDensity : '';
    document.getElementById('cityGovernor').value = city.governor ? city.governor : '';

    hideCityFormError();
    showCityEditSection();
}

function clearCityForm() {
    document.getElementById('cityName').value = '';
    document.getElementById('cityArea').value = '';
    document.getElementById('cityPopulation').value = '';
    document.getElementById('cityEstablishmentDate').value = '';
    document.getElementById('cityCapital').checked = false;
    document.getElementById('cityDensity').value = '';
    document.getElementById('cityGovernor').value = '';
    hideCityFormError();
}

function showCityEditSection() {
    document.getElementById('cityEditSection').style.display = 'block';
}

function hideCityEditSection() {
    document.getElementById('cityEditSection').style.display = 'none';
}

function cancelCityEdit() {
    hideCityEditSection();
    editingCityId = null;
    clearCityForm();
}

// сохраняем город
async function saveCity() {
    const nameRaw = document.getElementById('cityName').value.trim();
    const areaRaw = document.getElementById('cityArea').value.trim();
    const populationRaw = document.getElementById('cityPopulation').value.trim();
    const capital = document.getElementById('cityCapital').checked;
    const densityRaw = document.getElementById('cityDensity').value.trim();
    let governorVal = document.getElementById('cityGovernor').value.trim();

    // --- Название ---
    if (!nameRaw) {
        showCityFormError('Название города не может быть пустым');
        return;
    }
    if (nameRaw.length > 80) {
        showCityFormError('Название не должно быть длиннее 80 символов');
        return;
    }

    // --- Площадь ---
    if (!areaRaw) {
        showCityFormError('Площадь обязательна');
        return;
    }
    const area = parseInt(areaRaw, 10);
    if (!Number.isFinite(area) || area <= 0 || !Number.isInteger(area)) {
        showCityFormError('Площадь должна быть положительным целым числом');
        return;
    }

    // --- Население ---
    if (!populationRaw) {
        showCityFormError('Население обязательно');
        return;
    }
    const population = parseInt(populationRaw, 10);
    if (!Number.isFinite(population) || population <= 0 || !Number.isInteger(population)) {
        showCityFormError('Население должно быть положительным целым числом');
        return;
    }

    // --- Плотность населения ---
    if (!densityRaw) {
        showCityFormError('Плотность населения обязательна');
        return;
    }
    const density = parseFloat(densityRaw);
    if (!Number.isFinite(density) || density <= 0) {
        showCityFormError('Плотность населения должна быть положительным числом');
        return;
    }

    // --- Губернатор (ENUM) ---
    governorVal = governorVal ? governorVal.toUpperCase() : null;
    const allowedTypes = ['HOBBIT', 'ELF', 'HUMAN', 'GOLLUM'];
    if (governorVal && !allowedTypes.includes(governorVal)) {
        showCityFormError('Губернатор должен быть одним из: HOBBIT, ELF, HUMAN, GOLLUM');
        return;
    }

    const payload = {
        name: nameRaw,
        area: area,
        population: population,
        capital: capital,
        populationDensity: density,
        governor: governorVal
        // establishmentDate специально НЕ отправляем — её хранит и защищает сервер
    };

    try {
        let resp;
        if (editingCityId == null) {
            resp = await fetch(CITIES_API_BASE, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...authHeaders()
                },
                body: JSON.stringify(payload)
            });
        } else {
            resp = await fetch(CITIES_API_BASE + '?id=' + editingCityId, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    ...authHeaders()
                },
                body: JSON.stringify(payload)
            });
        }

        if (!resp.ok) {
            let msg = 'Ошибка сохранения города (код ' + resp.status + ')';
            try {
                const data = await resp.json();
                if (data && data.error) msg = data.error;
            } catch (_) {}
            showCityFormError(msg);
            return;
        }

        await loadCities();
        cancelCityEdit();
    } catch (e) {
        showCityFormError(e.message);
    }
}

// удаление с перепривязкой
function prepareDeleteCity(id) {
    deleteCityId = id;
    const label = document.getElementById('deleteCityIdLabel');
    if (label) {
        label.textContent = String(id);
    }

    const select = document.getElementById('reassignCityId');
    if (select) {
        select.innerHTML = '<option value="">(оставить без города)</option>';

        allCities
            .filter(c => c.id !== id)
            .forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = c.name;
                select.appendChild(opt);
            });
    }

    hideCityDeleteError();
    document.getElementById('cityDeleteSection').style.display = 'block';
}

function cancelDeleteCity() {
    deleteCityId = null;
    document.getElementById('cityDeleteSection').style.display = 'none';
}

async function confirmDeleteCity() {
    if (deleteCityId == null) return;

    const select = document.getElementById('reassignCityId');
    const reassignIdStr = select ? select.value : '';
    let url = CITIES_API_BASE + '?deleteId=' + deleteCityId;
    if (reassignIdStr) {
        url += '&reassignId=' + reassignIdStr;
    }

    try {
        const resp = await fetch(url, {
            method: 'DELETE',
            headers: {
                ...authHeaders()
            }
        });

        if (resp.status !== 204 && !resp.ok) {
            let msg = 'Ошибка удаления города (код ' + resp.status + ')';
            try {
                const data = await resp.json();
                if (data && data.error) msg = data.error;
            } catch (_) {}
            showCityDeleteError(msg);
            return;
        }

        await loadCities();
        cancelDeleteCity();
    } catch (e) {
        showCityDeleteError(e.message);
    }
}

// ошибки
function showCityFormError(msg) {
    const el = document.getElementById('cityFormError');
    if (!el) return;
    el.textContent = msg;
    el.style.display = 'block';
}

function hideCityFormError() {
    const el = document.getElementById('cityFormError');
    if (!el) return;
    el.textContent = '';
    el.style.display = 'none';
}

function showCityDeleteError(msg) {
    const el = document.getElementById('cityDeleteError');
    if (!el) return;
    el.textContent = msg;
    el.style.display = 'block';
}

function hideCityDeleteError() {
    const el = document.getElementById('cityDeleteError');
    if (!el) return;
    el.textContent = '';
    el.style.display = 'none';
}

function showCityGlobalError(msg) {
    const el = document.getElementById('cityGlobalError');
    if (!el) return;
    el.textContent = msg;
    el.style.display = 'block';
}

function hideCityGlobalError() {
    const el = document.getElementById('cityGlobalError');
    if (!el) return;
    el.textContent = '';
    el.style.display = 'none';
}

// ========== импорт (JSON) — Города ==========
async function importCities() {
    const fileInput = document.getElementById('cityImportFile');
    const msg = document.getElementById('cityImportMsg');

    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        showCityImportMsg(msg, 'Выбери JSON-файл для импорта', true);
        return;
    }

    const file = fileInput.files[0];
    let text;
    try {
        text = await file.text();
    } catch (e) {
        showCityImportMsg(msg, 'Не удалось прочитать файл: ' + e.message, true);
        return;
    }

    let json;
    try {
        json = JSON.parse(text);
    } catch (e) {
        showCityImportMsg(msg, 'Файл не является валидным JSON', true);
        return;
    }

    // ✅ FIX #1: поддерживаем и массив, и {items:[...]}
    const payload = Array.isArray(json) ? { items: json } : json;

    if (!payload || !Array.isArray(payload.items) || payload.items.length === 0) {
        showCityImportMsg(msg, 'Файл пустой или нет поля "items" (ожидается { "items": [...] })', true);
        return;
    }

    try {
        const resp = await fetch('api/import/cities', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders()
            },
            body: JSON.stringify(payload)
        });

        const data = await safeJson(resp);

        if (!resp.ok) {
            const err = data?.error || ('Ошибка импорта (код ' + resp.status + ')');
            showCityImportMsg(msg, err, true);
            return;
        }

        const opId = data?.opId;
        showCityImportMsg(msg, 'Импорт завершён. Operation id = ' + opId, false);

        await loadCities();
    } catch (e) {
        showCityImportMsg(msg, 'Ошибка запроса: ' + e.message, true);
    }
}

function showCityImportMsg(el, text, isError) {
    if (!el) return;
    el.style.display = 'block';
    el.textContent = text;
    el.classList.toggle('error', !!isError);
}

// ✅ FIX #2: safeJson оставляем ОДНУ (не дублируем)
async function safeJson(resp) {
    try { return await resp.json(); } catch (_) { return null; }
}


// ================= История импорта =================

async function loadImportHistory() {
    const tbody = document.getElementById('importHistoryBody');
    const errEl = document.getElementById('importHistoryError');
    if (!tbody) return;

    try {
        if (errEl) { errEl.style.display = 'none'; errEl.textContent = ''; }

        const resp = await fetch('api/import/history', {
            headers: { ...authHeaders() }   // X-User / X-Role
        });

        const data = await safeJson(resp);

        if (!resp.ok) {
            const msg = data?.error || ('Не удалось загрузить историю (код ' + resp.status + ')');
            if (errEl) { errEl.textContent = msg; errEl.style.display = 'block'; }
            return;
        }

        tbody.innerHTML = '';

        (data || []).forEach(op => {
            const added = (op.status === 'SUCCESS') ? (op.insertedCount ?? '') : '';


            const errMsg = (op.status === 'FAILED') ? (op.errorMessage ?? '') : '';
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td>${op.id ?? ''}</td>
        <td>${op.status ?? ''}</td>
        <td>${op.username ?? ''}</td>
        <td>${added}</td>
        <td>${errMsg}</td>
      `;
            tbody.appendChild(tr);
        });

    } catch (e) {
        if (errEl) {
            errEl.textContent = 'Ошибка: ' + e.message;
            errEl.style.display = 'block';
        }
    }
}


// инициализация
window.addEventListener('load', () => {
    const prev = document.getElementById('cityPrevPage');
    const next = document.getElementById('cityNextPage');
    const select = document.getElementById('cityPageSizeSelect');

    if (prev) {
        prev.addEventListener('click', () => {
            if (cityCurrentPage > 1) {
                cityCurrentPage--;
                renderCitiesTable();
            }
        });
    }

    if (next) {
        next.addEventListener('click', () => {
            const total = filteredCities.length;
            const totalPages = Math.max(1, Math.ceil(total / cityPageSize));
            if (cityCurrentPage < totalPages) {
                cityCurrentPage++;
                renderCitiesTable();
            }
        });
    }

    if (select) {
        select.addEventListener('change', e => {
            const val = parseInt(e.target.value, 10);
            cityPageSize = Number.isFinite(val) && val > 0 ? val : 10;
            cityCurrentPage = 1;
            renderCitiesTable();
        });
    }

    loadCities();

    // автообновление
    setInterval(() => {
        const editSection = document.getElementById('cityEditSection');
        const deleteSection = document.getElementById('cityDeleteSection');

        const editingVisible = editSection && editSection.style.display !== 'none';
        const deletingVisible = deleteSection && deleteSection.style.display !== 'none';

        if (editingVisible || deletingVisible) return;

        loadCities();
    }, 5000);
});
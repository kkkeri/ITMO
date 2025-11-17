const CITIES_API = '/api/cities';

let cities = [];
let editingCityId = null;
let deleteCityId = null;

/* ====== вспомогательные ====== */

function showCityGlobalError(msg) {
    const el = document.getElementById('cityGlobalError');
    el.textContent = msg;
    el.style.display = 'block';
}

function hideCityGlobalError() {
    const el = document.getElementById('cityGlobalError');
    el.textContent = '';
    el.style.display = 'none';
}

function showCityFormError(msg) {
    const el = document.getElementById('cityFormError');
    el.textContent = msg;
    el.style.display = 'block';
}

function hideCityFormError() {
    const el = document.getElementById('cityFormError');
    el.textContent = '';
    el.style.display = 'none';
}

/* ====== загрузка и рендер ====== */

async function loadCities() {
    try {
        const resp = await fetch(CITIES_API);
        if (!resp.ok) {
            throw new Error('Не удалось загрузить города (код ' + resp.status + ')');
        }
        cities = await resp.json();
        renderCities();
        hideCityGlobalError();
    } catch (e) {
        showCityGlobalError(e.message);
    }
}

function renderCities() {
    const tbody = document.getElementById('citiesTableBody');
    tbody.innerHTML = '';

    cities.forEach(city => {
        const tr = document.createElement('tr');

        const gov = city.governor ? city.governor : '';
        const capitalStr = city.capital ? 'Да' : 'Нет';

        tr.innerHTML = `
            <td>${city.id}</td>
            <td>${city.name}</td>
            <td>${city.area}</td>
            <td>${city.population}</td>
            <td>${capitalStr}</td>
            <td>${gov}</td>
            <td>${city.populationDensity}</td>
            <td>
                <button onclick="startEditCity(${city.id})">Редактировать</button>
                <button class="danger" onclick="openDeleteCityDialog(${city.id})">Удалить</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

/* ====== форма создания / редактирования ====== */

function startCreateCity() {
    editingCityId = null;
    document.getElementById('cityEditTitle').textContent = 'Новый город';
    clearCityForm();
    hideCityFormError();
    document.getElementById('cityEditSection').style.display = 'block';
}

function startEditCity(id) {
    const city = cities.find(c => c.id === id);
    if (!city) return;

    editingCityId = id;
    document.getElementById('cityEditTitle').textContent = 'Редактирование города #' + id;

    document.getElementById('cityName').value = city.name;
    document.getElementById('cityArea').value = city.area;
    document.getElementById('cityPopulation').value = city.population;
    document.getElementById('cityCapital').checked = !!city.capital;
    document.getElementById('cityDensity').value = city.populationDensity;
    document.getElementById('cityGovernor').value = city.governor || '';

    hideCityFormError();
    document.getElementById('cityEditSection').style.display = 'block';
}

function clearCityForm() {
    document.getElementById('cityName').value = '';
    document.getElementById('cityArea').value = '';
    document.getElementById('cityPopulation').value = '';
    document.getElementById('cityCapital').checked = false;
    document.getElementById('cityDensity').value = '';
    document.getElementById('cityGovernor').value = '';
}

function cancelCityEdit() {
    editingCityId = null;
    document.getElementById('cityEditSection').style.display = 'none';
    hideCityFormError();
}

/* ====== сохранение города ====== */

async function saveCity() {
    const name = document.getElementById('cityName').value.trim();
    const areaVal = document.getElementById('cityArea').value;
    const populationVal = document.getElementById('cityPopulation').value;
    const capital = document.getElementById('cityCapital').checked;
    const densityVal = document.getElementById('cityDensity').value;
    const governorVal = document.getElementById('cityGovernor').value;

    if (!name) {
        showCityFormError('Название не может быть пустым');
        return;
    }

    const area = parseInt(areaVal, 10);
    if (!Number.isFinite(area) || area <= 0) {
        showCityFormError('Площадь должна быть положительным числом');
        return;
    }

    const population = parseInt(populationVal, 10);
    if (!Number.isFinite(population) || population <= 0) {
        showCityFormError('Население должно быть положительным числом');
        return;
    }

    const density = parseFloat(densityVal);
    if (!Number.isFinite(density) || density <= 0) {
        showCityFormError('Плотность населения должна быть положительным числом');
        return;
    }

    const payload = {
        name: name,
        area: area,
        population: population,
        capital: capital,
        populationDensity: density,
        governor: governorVal || null
    };

    try {
        let resp;
        if (editingCityId == null) {
            resp = await fetch(CITIES_API, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
        } else {
            const params = new URLSearchParams();
            params.append('id', editingCityId);
            resp = await fetch(CITIES_API + '?' + params.toString(), {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
        }

        if (!resp.ok) {
            let msg = 'Ошибка сохранения (код ' + resp.status + ')';
            try {
                const data = await resp.json();
                if (data.error) msg = data.error;
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

/* ====== удаление с перепривязкой ====== */

function openDeleteCityDialog(id) {
    const city = cities.find(c => c.id === id);
    if (!city) return;

    deleteCityId = id;

    document.getElementById('deleteCityName').textContent = city.name;
    document.getElementById('deleteCityIdDisplay').textContent = id;

    const select = document.getElementById('reassignCitySelect');
    select.innerHTML = '';

    // опция "оставить без города"
    const noneOpt = document.createElement('option');
    noneOpt.value = '';
    noneOpt.textContent = '(оставить существ без города)';
    select.appendChild(noneOpt);

    // все остальные города
    cities
        .filter(c => c.id !== id)
        .forEach(c => {
            const opt = document.createElement('option');
            opt.value = c.id;
            opt.textContent = `${c.name} (ID: ${c.id})`;
            select.appendChild(opt);
        });

    document.getElementById('cityDeleteSection').style.display = 'block';
}

function cancelDeleteCity() {
    deleteCityId = null;
    document.getElementById('cityDeleteSection').style.display = 'none';
}

async function confirmDeleteCity() {
    if (deleteCityId == null) return;

    const select = document.getElementById('reassignCitySelect');
    const value = select.value;

    const params = new URLSearchParams();
    params.append('deleteId', deleteCityId);      // <--- ИМЯ ПАРАМЕТРА КАК В СЕРВЛЕТЕ

    if (value) {
        params.append('reassignId', value);       // <--- ИМЯ ПАРАМЕТРА КАК В СЕРВЛЕТЕ
    }

    try {
        const resp = await fetch(CITIES_API + '?' + params.toString(), {
            method: 'DELETE'
        });

        if (!resp.ok && resp.status !== 204) {
            let msg = 'Ошибка удаления (код ' + resp.status + ')';
            try {
                const data = await resp.json();
                if (data.error) msg = data.error;
            } catch (_) {}
            showCityGlobalError(msg);
            return;
        }

        await loadCities();
        cancelDeleteCity();
    } catch (e) {
        showCityGlobalError(e.message);
    }
}

/* ====== старт ====== */

window.addEventListener('load', loadCities);
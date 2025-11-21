const API_BASE = 'api/creatures';

// полный список с сервера
let creatures = [];
// после фильтрации
let filteredCreatures = [];
let editingId = null; // null = создаём, число = редактируем

// пагинация
let creatureCurrentPage = 1;
let creaturePageSize = 10;

// текущее состояние сортировки
let creatureSortField = null; // 'name', 'age', 'creatureType', 'city', 'ring', ...
let creatureSortDir = 'asc';  // 'asc' | 'desc'

// загрузка существ

async function loadCreatures() {
    try {
        const resp = await fetch(API_BASE);
        if (!resp.ok) {
            throw new Error('Не удалось загрузить существ (код ' + resp.status + ')');
        }
        creatures = await resp.json();
        hideGlobalError();

        // при загрузке сразу применяем текущие фильтры и сортировку
        applyCreatureFilters();
    } catch (e) {
        showGlobalError(e.message);
    }
}

// сортировка

function sortCreaturesBy(field) {
    if (creatureSortField === field) {
        // меняем направление
        creatureSortDir = (creatureSortDir === 'asc') ? 'desc' : 'asc';
    } else {
        // выбираем новое поле сортировки
        creatureSortField = field;
        creatureSortDir = 'asc';
    }
    // просто перерисовываем с учётом новых настроек сортировки
    renderTable(filteredCreatures);
}

function compareValues(a, b) {
    if (a == null && b == null) return 0;
    if (a == null) return -1;
    if (b == null) return 1;

    if (typeof a === 'string' && typeof b === 'string') {
        return a.localeCompare(b, 'ru', { sensitivity: 'base' });
    }
    if (a < b) return -1;
    if (a > b) return 1;
    return 0;
}

// применяем сортировку к списку
function sortCreatureList(list) {
    if (!creatureSortField) return list;

    const sorted = [...list];

    sorted.sort((a, b) => {
        let va, vb;
        switch (creatureSortField) {
            case 'id':
                va = a.id; vb = b.id; break;
            case 'name':
                va = a.name; vb = b.name; break;
            case 'age':
                va = a.age; vb = b.age; break;
            case 'creatureType':
                va = a.creatureType; vb = b.creatureType; break;
            case 'attackLevel':
                va = a.attackLevel; vb = b.attackLevel; break;
            case 'city':
                va = a.creatureLocation ? a.creatureLocation.name : null;
                vb = b.creatureLocation ? b.creatureLocation.name : null;
                break;
            case 'ring':
                va = a.ring ? a.ring.name : null;
                vb = b.ring ? b.ring.name : null;
                break;
            case 'creationDate':
                va = a.creationDate;
                vb = b.creationDate;
                break;
            default:
                return 0;
        }
        return compareValues(va, vb);
    });

    if (creatureSortDir === 'desc') {
        sorted.reverse();
    }
    return sorted;
}

// фильтр по name + type + ring.name

function applyCreatureFilters() {
    const nameVal = document.getElementById('nameFilter')?.value.trim().toLowerCase() || '';
    const typeVal = document.getElementById('typeFilter')?.value || ''; // "" или HOBBIT/ELF/...
    const ringVal = document.getElementById('ringFilter')?.value.trim().toLowerCase() || '';

    filteredCreatures = creatures.filter(c => {
        // имя
        const matchesName = !nameVal ||
            (c.name && c.name.toLowerCase().includes(nameVal));

        // тип (enum, точное сравнение строки)
        const matchesType = !typeVal ||
            c.creatureType === typeVal;

        // имя кольца
        const ringName = (c.ring && c.ring.name) ? c.ring.name.toLowerCase() : '';
        const matchesRing = !ringVal || ringName.includes(ringVal);

        return matchesName && matchesType && matchesRing;
    });

    creatureCurrentPage = 1; // при смене фильтра всегда на первую страницу
    renderTable(filteredCreatures);
}

function resetCreatureFilters() {
    const nameInput = document.getElementById('nameFilter');
    const typeSelect = document.getElementById('typeFilter');
    const ringInput = document.getElementById('ringFilter');

    if (nameInput) nameInput.value = '';
    if (typeSelect) typeSelect.value = '';
    if (ringInput) ringInput.value = '';

    filteredCreatures = creatures.slice();
    creatureCurrentPage = 1;
    renderTable(filteredCreatures);
}

function reapplyFilterAndRender() {
    applyCreatureFilters();
}

//отрисовка с пагинацией

function renderTable(list) {
    const tbody = document.getElementById('creaturesTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    // применяем сортировку перед отрисовкой
    const sorted = sortCreatureList(list);

    // Пагинация
    const total = sorted.length;
    const totalPages = Math.max(1, Math.ceil(total / creaturePageSize));

    if (creatureCurrentPage > totalPages) creatureCurrentPage = totalPages;
    if (creatureCurrentPage < 1) creatureCurrentPage = 1;

    const start = (creatureCurrentPage - 1) * creaturePageSize;
    const end = start + creaturePageSize;
    const pageItems = sorted.slice(start, end);

    // Рисуем только элементы выбранной страницы
    pageItems.forEach(c => {
        const tr = document.createElement('tr');

        const cityName = c.creatureLocation ? c.creatureLocation.name : '';
        const ringStr = c.ring
            ? (c.ring.name + (c.ring.power ? ' (' + c.ring.power + ')' : ''))
            : '';
        const creationStr = formatCreationDate(c.creationDate);

        const xStr = c.coordinates ? c.coordinates.x : '';
        const yStr = c.coordinates ? c.coordinates.y : '';

        tr.innerHTML = `
            <td>${c.id}</td>
            <td>${c.name}</td>
            <td>${c.age}</td>
            <td>${xStr}</td>
            <td>${yStr}</td>
            <td>${c.creatureType}</td>
            <td>${c.attackLevel}</td>
            <td>${cityName}</td>
            <td>${ringStr}</td>
            <td>${creationStr}</td>
            <td>
                <button onclick="startEdit(${c.id})">Редактировать</button>
                <button class="danger" onclick="deleteCreature(${c.id})">Удалить</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    updateCreaturePaginationInfo(totalPages);
}

function updateCreaturePaginationInfo(totalPages) {
    const info = document.getElementById('pageInfo');
    if (info) {
        info.textContent = `Страница ${creatureCurrentPage} из ${totalPages}`;
    }

    const prev = document.getElementById('prevPage');
    const next = document.getElementById('nextPage');

    if (prev) prev.disabled = creatureCurrentPage <= 1;
    if (next) next.disabled = creatureCurrentPage >= totalPages;
}

// форма создания и редактирования

function startCreate() {
    editingId = null;
    document.getElementById('editTitle').textContent = 'Новое существо';
    clearForm();
    showEditSection();
}

function startEdit(id) {
    const creature = creatures.find(c => c.id === id);
    if (!creature) return;

    editingId = id;
    document.getElementById('editTitle').textContent = 'Редактирование существа #' + id;

    document.getElementById('creatureName').value = creature.name;
    document.getElementById('creatureAge').value = creature.age;
    document.getElementById('creatureType').value = creature.creatureType;
    document.getElementById('attackLevel').value = creature.attackLevel;
    document.getElementById('coordX').value = creature.coordinates ? creature.coordinates.x : '';
    document.getElementById('coordY').value = creature.coordinates ? creature.coordinates.y : '';
    document.getElementById('cityId').value = creature.creatureLocation ? creature.creatureLocation.id : '';
    document.getElementById('ringName').value = creature.ring ? creature.ring.name : '';
    document.getElementById('ringPower').value =
        creature.ring && creature.ring.power != null ? creature.ring.power : '';

    hideFormError();
    showEditSection();
}

function clearForm() {
    document.getElementById('creatureName').value = '';
    document.getElementById('creatureAge').value = '';
    document.getElementById('creatureType').value = 'HOBBIT';
    document.getElementById('attackLevel').value = '';
    document.getElementById('coordX').value = '';
    document.getElementById('coordY').value = '';
    document.getElementById('cityId').value = '';
    document.getElementById('ringName').value = '';
    document.getElementById('ringPower').value = '';
    hideFormError();
}

function showEditSection() {
    document.getElementById('editSection').style.display = 'block';
}

function hideEditSection() {
    document.getElementById('editSection').style.display = 'none';
}

function cancelEdit() {
    hideEditSection();
    editingId = null;
    clearForm();
}

function formatCreationDate(raw) {
    if (!raw) return '';
    const noZone = raw.split('[')[0];
    const replaced = noZone.replace('T', ' ');
    return replaced.substring(0, 16);
}

// сохранение

async function saveCreature() {
    const name = document.getElementById('creatureName').value.trim();
    const age = parseInt(document.getElementById('creatureAge').value, 10);
    const type = document.getElementById('creatureType').value;
    const attack = parseFloat(document.getElementById('attackLevel').value);
    const x = parseFloat(document.getElementById('coordX').value);
    const yStr = document.getElementById('coordY').value;
    const cityIdStr = document.getElementById('cityId').value;
    const ringName = document.getElementById('ringName').value.trim();
    const ringPowerStr = document.getElementById('ringPower').value;

    if (!name) {
        showFormError('Имя не может быть пустым');
        return;
    }
    if (!Number.isFinite(age) || age <= 0) {
        showFormError('Возраст должен быть положительным числом');
        return;
    }
    if (!Number.isFinite(attack) || attack <= 0) {
        showFormError('Уровень атаки должен быть положительным числом');
        return;
    }
    if (!yStr) {
        showFormError('Координата Y обязательна');
        return;
    }
    const y = parseFloat(yStr);

    const coordinates = {
        x: Number.isFinite(x) ? x : 0,
        y: y
    };

    let creatureLocation = null;
    if (cityIdStr) {
        const cityId = parseInt(cityIdStr, 10);
        if (!Number.isFinite(cityId) || cityId <= 0) {
            showFormError('ID города должен быть положительным числом');
            return;
        }
        creatureLocation = { id: cityId };
    }

    let ring = null;
    if (ringName) {
        ring = { name: ringName };
        if (ringPowerStr) {
            const power = parseInt(ringPowerStr, 10);
            if (!Number.isFinite(power) || power <= 0) {
                showFormError('Сила кольца должна быть положительным числом');
                return;
            }
            ring.power = power;
        }
    }

    const payload = {
        name: name,
        age: age,
        creatureType: type,
        attackLevel: attack,
        coordinates: coordinates,
        creatureLocation: creatureLocation,
        ring: ring
    };

    try {
        let resp;
        if (editingId == null) {
            resp = await fetch(API_BASE, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
        } else {
            resp = await fetch(API_BASE + '?id=' + editingId, {
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
            showFormError(msg);
            return;
        }

        await loadCreatures();
        cancelEdit();
    } catch (e) {
        showFormError(e.message);
    }
}

// удаление

async function deleteCreature(id) {
    if (!confirm('Удалить существо #' + id + '?')) return;

    try {
        const resp = await fetch(API_BASE + '?id=' + id, { method: 'DELETE' });
        if (!resp.ok && resp.status !== 204) {
            let msg = 'Ошибка удаления (код ' + resp.status + ')';
            try {
                const data = await resp.json();
                if (data.error) msg = data.error;
            } catch (_) {}
            showGlobalError(msg);
            return;
        }
        await loadCreatures();
    } catch (e) {
        showGlobalError(e.message);
    }
}

// ошибки

function showFormError(msg) {
    const el = document.getElementById('formError');
    el.textContent = msg;
    el.style.display = 'block';
}

function hideFormError() {
    const el = document.getElementById('formError');
    el.style.display = 'none';
    el.textContent = '';
}

function showGlobalError(msg) {
    const el = document.getElementById('globalError');
    el.textContent = msg;
    el.style.display = 'block';
}

function hideGlobalError() {
    const el = document.getElementById('globalError');
    el.style.display = 'none';
    el.textContent = '';
}

//автозагрузка + автообновление + пагинация

window.addEventListener('load', () => {
    // Пагинация
    const prev = document.getElementById('prevPage');
    const next = document.getElementById('nextPage');
    const select = document.getElementById('pageSizeSelect');

    if (prev) {
        prev.addEventListener('click', () => {
            if (creatureCurrentPage > 1) {
                creatureCurrentPage--;
                renderTable(filteredCreatures);
            }
        });
    }

    if (next) {
        next.addEventListener('click', () => {
            const total = filteredCreatures.length;
            const totalPages = Math.max(1, Math.ceil(total / creaturePageSize));
            if (creatureCurrentPage < totalPages) {
                creatureCurrentPage++;
                renderTable(filteredCreatures);
            }
        });
    }

    if (select) {
        select.addEventListener('change', e => {
            const val = parseInt(e.target.value, 10);
            creaturePageSize = Number.isFinite(val) && val > 0 ? val : 10;
            creatureCurrentPage = 1;
            renderTable(filteredCreatures);
        });
    }

    loadCreatures();

    // автообновление
    setInterval(() => {
        const editSection = document.getElementById('editSection');

        // если модалка/форма редактирования открыта - не трогаем
        if (editSection && editSection.style.display !== 'none') {
            return;
        }

        // иначе просто подгружаем актуальные данные с сервера
        loadCreatures();
    }, 5000);
});

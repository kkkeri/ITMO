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
let creatureSortField = null;
let creatureSortDir = 'asc';

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

async function safeJson(resp) {
    try { return await resp.json(); } catch (_) { return null; }
}

// ================= Загрузка существ =================

async function loadCreatures() {
    try {
        const resp = await fetch(API_BASE, {
            headers: { ...authHeaders() }
        });

        if (!resp.ok) {
            throw new Error('Не удалось загрузить существ (код ' + resp.status + ')');
        }

        creatures = await resp.json();
        hideGlobalError();
        applyCreatureFilters();
    } catch (e) {
        showGlobalError(e.message);
    }
}

// ================= Сортировка =================

function sortCreaturesBy(field) {
    if (creatureSortField === field) {
        creatureSortDir = (creatureSortDir === 'asc') ? 'desc' : 'asc';
    } else {
        creatureSortField = field;
        creatureSortDir = 'asc';
    }
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

    if (creatureSortDir === 'desc') sorted.reverse();
    return sorted;
}

// ================= Фильтры =================

function applyCreatureFilters() {
    const nameVal = document.getElementById('nameFilter')?.value.trim().toLowerCase() || '';
    const typeVal = document.getElementById('typeFilter')?.value || '';
    const ringVal = document.getElementById('ringFilter')?.value.trim().toLowerCase() || '';

    filteredCreatures = creatures.filter(c => {
        const matchesName = !nameVal || (c.name && c.name.toLowerCase().includes(nameVal));
        const matchesType = !typeVal || c.creatureType === typeVal;

        const ringName = (c.ring && c.ring.name) ? c.ring.name.toLowerCase() : '';
        const matchesRing = !ringVal || ringName.includes(ringVal);

        return matchesName && matchesType && matchesRing;
    });

    creatureCurrentPage = 1;
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

// ================= Отрисовка с пагинацией =================

function renderTable(list) {
    const tbody = document.getElementById('creaturesTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    const sorted = sortCreatureList(list);

    const total = sorted.length;
    const totalPages = Math.max(1, Math.ceil(total / creaturePageSize));

    if (creatureCurrentPage > totalPages) creatureCurrentPage = totalPages;
    if (creatureCurrentPage < 1) creatureCurrentPage = 1;

    const start = (creatureCurrentPage - 1) * creaturePageSize;
    const end = start + creaturePageSize;
    const pageItems = sorted.slice(start, end);

    pageItems.forEach(c => {
        const tr = document.createElement('tr');

        const cityName = c.creatureLocation ? c.creatureLocation.name : '';
        const ringStr = c.ring ? (c.ring.name + (c.ring.power ? ' (' + c.ring.power + ')' : '')) : '';
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
    if (info) info.textContent = `Страница ${creatureCurrentPage} из ${totalPages}`;

    const prev = document.getElementById('prevPage');
    const next = document.getElementById('nextPage');

    if (prev) prev.disabled = creatureCurrentPage <= 1;
    if (next) next.disabled = creatureCurrentPage >= totalPages;
}

function formatCreationDate(raw) {
    if (!raw) return '';
    const noZone = raw.split('[')[0];
    const replaced = noZone.replace('T', ' ');
    return replaced.substring(0, 16);
}

// ================= Форма создания / редактирования =================

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
    document.getElementById('ringPower').value = (creature.ring && creature.ring.power != null) ? creature.ring.power : '';

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

// ================= Сохранение =================

async function saveCreature() {
    const name = document.getElementById('creatureName').value.trim();
    const ageRaw = document.getElementById('creatureAge').value.trim();
    const type = document.getElementById('creatureType').value;
    const attackRaw = document.getElementById('attackLevel').value.trim();
    const xRaw = document.getElementById('coordX').value.trim();
    const yRaw = document.getElementById('coordY').value.trim();
    const cityIdStr = document.getElementById('cityId').value.trim();
    const ringName = document.getElementById('ringName').value.trim();
    const ringPowerStr = document.getElementById('ringPower').value.trim();

    if (!name) { showFormError('Имя не может быть пустым'); return; }
    if (name.length > 80) { showFormError('Имя не должно быть длиннее 80 символов'); return; }

    if (!ageRaw) { showFormError('Возраст обязателен'); return; }
    const age = parseInt(ageRaw, 10);
    if (!Number.isFinite(age) || age <= 0 || !Number.isInteger(age)) {
        showFormError('Возраст должен быть положительным целым числом');
        return;
    }

    if (!attackRaw) { showFormError('Уровень атаки обязателен'); return; }
    const attack = parseFloat(attackRaw);
    if (!Number.isFinite(attack) || attack <= 0 || attack > 100) {
        showFormError('Уровень атаки должен быть числом от 0 до 100');
        return;
    }

    if (!yRaw) { showFormError('Координата Y обязательна'); return; }
    const y = parseFloat(yRaw);
    if (!Number.isFinite(y)) { showFormError('Координата Y должна быть числом'); return; }

    let x = null;
    if (xRaw) {
        x = parseFloat(xRaw);
        if (!Number.isFinite(x)) { showFormError('Координата X должна быть числом'); return; }
    }

    let creatureLocation = null;
    if (cityIdStr) {
        const cityId = parseInt(cityIdStr, 10);
        if (!Number.isFinite(cityId) || cityId <= 0 || !Number.isInteger(cityId)) {
            showFormError('ID города должен быть положительным целым числом');
            return;
        }
        creatureLocation = { id: cityId };
    }

    let ring = null;
    if (ringName) {
        if (ringName.length > 80) { showFormError('Имя кольца не должно быть длиннее 80 символов'); return; }
        ring = { name: ringName };

        if (ringPowerStr) {
            const power = parseInt(ringPowerStr, 10);
            if (!Number.isFinite(power) || !Number.isInteger(power) || power <= 0 || power > 100) {
                showFormError('Сила кольца должна быть целым числом от 1 до 100');
                return;
            }
            ring.power = power;
        }
    } else if (ringPowerStr) {
        showFormError('Сначала введите имя кольца, затем его силу');
        return;
    }

    const payload = {
        name,
        age,
        creatureType: type,
        attackLevel: attack,
        coordinates: { x, y },
        creatureLocation,
        ring
    };

    try {
        let resp;
        if (editingId == null) {
            resp = await fetch(API_BASE, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...authHeaders()
                },
                body: JSON.stringify(payload)
            });
        } else {
            resp = await fetch(API_BASE + '?id=' + editingId, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    ...authHeaders()
                },
                body: JSON.stringify(payload)
            });
        }

        if (!resp.ok) {
            const data = await safeJson(resp);
            const msg = data?.error || ('Ошибка сохранения (код ' + resp.status + ')');
            showFormError(msg);
            return;
        }

        await loadCreatures();
        cancelEdit();
    } catch (e) {
        showFormError(e.message);
    }
}

// ================= Удаление =================

async function deleteCreature(id) {
    if (!confirm('Удалить существо #' + id + '?')) return;

    try {
        const resp = await fetch(API_BASE + '?id=' + id, {
            method: 'DELETE',
            headers: { ...authHeaders() }
        });

        if (!resp.ok && resp.status !== 204) {
            const data = await safeJson(resp);
            const msg = data?.error || ('Ошибка удаления (код ' + resp.status + ')');
            showGlobalError(msg);
            return;
        }

        await loadCreatures();
    } catch (e) {
        showGlobalError(e.message);
    }
}

// ================= Ошибки =================

function showFormError(msg) {
    const el = document.getElementById('formError');
    if (!el) return;
    el.textContent = msg;
    el.style.display = 'block';
}

function hideFormError() {
    const el = document.getElementById('formError');
    if (!el) return;
    el.style.display = 'none';
    el.textContent = '';
}

function showGlobalError(msg) {
    const el = document.getElementById('globalError');
    if (!el) return;
    el.textContent = msg;
    el.style.display = 'block';
}

function hideGlobalError() {
    const el = document.getElementById('globalError');
    if (!el) return;
    el.style.display = 'none';
    el.textContent = '';
}

// ================= ИМПОРТ (JSON) — Существа =================

async function importCreatures() {
    const fileInput = document.getElementById('creaturesImportFile');
    const msg = document.getElementById('creaturesImportResult');

    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        showCreatureImportMsg(msg, 'Выбери JSON-файл для импорта', true);
        return;
    }

    const file = fileInput.files[0];

    let text;
    try {
        text = await file.text();
    } catch (e) {
        showCreatureImportMsg(msg, 'Не удалось прочитать файл: ' + e.message, true);
        return;
    }

    let json;
    try {
        json = JSON.parse(text);
    } catch (e) {
        showCreatureImportMsg(msg, 'Файл не является валидным JSON', true);
        return;
    }

    const payload = Array.isArray(json) ? { items: json } : json;

    if (!payload || !Array.isArray(payload.items) || payload.items.length === 0) {
        showCreatureImportMsg(msg, 'Файл пустой или нет поля "items" (ожидается { "items": [...] })', true);
        return;
    }

    try {
        const resp = await fetch('api/import/creatures', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders()
            },
            body: JSON.stringify(payload)
        });

        const data = await safeJson(resp);

        if (!resp.ok) {
            const errMsg = data?.error || ('Ошибка импорта (код ' + resp.status + ')');
            showCreatureImportMsg(msg, errMsg, true);
            return;
        }

        const opId = data?.opId || data?.operationId || data?.id;
        showCreatureImportMsg(
            msg,
            opId ? ('Импорт запущен. Operation id = ' + opId + '. Смотри статус в истории импорта ниже.') : 'Импорт запущен.',
            false
        );

        await loadCreatures();
    } catch (e) {
        showCreatureImportMsg(msg, 'Ошибка запроса: ' + e.message, true);
    }
}

function showCreatureImportMsg(el, text, isError) {
    if (!el) return;
    el.style.display = 'block';
    el.textContent = text;
    el.classList.toggle('error', !!isError);
}

// ================= История импорта =================

async function loadImportHistory() {
    const tbody = document.getElementById('importHistoryBody');
    const errEl = document.getElementById('importHistoryError');
    if (!tbody) return;

    try {
        if (errEl) { errEl.style.display = 'none'; errEl.textContent = ''; }

        const resp = await fetch('api/import/history', {
            headers: { ...authHeaders() }
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

// ================= Автозагрузка + пагинация =================

window.addEventListener('load', () => {
    const prev = document.getElementById('prevPage');
    const next = document.getElementById('nextPage');
    const select = document.getElementById('pageSizeSelect');
    const applyBtn = document.getElementById('applyUserContextBtn');
    if (applyBtn) {
        applyBtn.addEventListener('click', (e) => {
            e.preventDefault();
            applyUserContext();
        });
    }


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

    setInterval(() => {
        const editSection = document.getElementById('editSection');
        if (editSection && editSection.style.display !== 'none') return;
        loadCreatures();
    }, 5000);
});
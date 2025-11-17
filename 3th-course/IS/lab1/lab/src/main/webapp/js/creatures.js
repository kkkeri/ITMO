const API_BASE = '/api/creatures'; // если контекст не корень, можно будет поменять

let creatures = [];
let editingId = null; // null = создаём, число = редактируем

async function loadCreatures() {
    try {
        const resp = await fetch(API_BASE);
        if (!resp.ok) {
            throw new Error('Не удалось загрузить существ (код ' + resp.status + ')');
        }
        creatures = await resp.json();
        renderTable(creatures);
        hideGlobalError();
    } catch (e) {
        showGlobalError(e.message);
    }
}

function renderTable(list) {
    const tbody = document.getElementById('creaturesTableBody');
    tbody.innerHTML = '';

    list.forEach(c => {
        const tr = document.createElement('tr');

        const cityName = c.creatureLocation ? c.creatureLocation.name : '';
        const ringStr = c.ring ? (c.ring.name + (c.ring.power ? ' (' + c.ring.power + ')' : '')) : '';

        tr.innerHTML = `
            <td>${c.id}</td>
            <td>${c.name}</td>
            <td>${c.age}</td>
            <td>${c.creatureType}</td>
            <td>${c.attackLevel}</td>
            <td>${cityName}</td>
            <td>${ringStr}</td>
            <td>
                <button onclick="startEdit(${c.id})">Редактировать</button>
                <button class="danger" onclick="deleteCreature(${c.id})">Удалить</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

/* ====== Фильтр по имени ====== */

function applyNameFilter() {
    const value = document.getElementById('nameFilter').value.trim().toLowerCase();
    if (!value) {
        renderTable(creatures);
        return;
    }
    const filtered = creatures.filter(c => c.name.toLowerCase().includes(value));
    renderTable(filtered);
}

function resetFilter() {
    document.getElementById('nameFilter').value = '';
    renderTable(creatures);
}

/* ====== Форма создания / редактирования ====== */

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
    document.getElementById('ringPower').value = creature.ring && creature.ring.power != null ? creature.ring.power : '';

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

/* ====== Сохранение ====== */

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
            // создание
            resp = await fetch(API_BASE, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
        } else {
            // обновление
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

        // успех
        await loadCreatures();
        cancelEdit();
    } catch (e) {
        showFormError(e.message);
    }
}

/* ====== Удаление ====== */

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

/* ====== Сообщения об ошибках ====== */

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

/* ====== Автозагрузка при открытии страницы ====== */

window.addEventListener('load', loadCreatures);
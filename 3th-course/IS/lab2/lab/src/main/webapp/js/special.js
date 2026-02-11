const SPECIAL_API = 'api/special';




let X_USER = 'student';
let X_ROLE = 'USER';

function applyUserContext() {
    const u = document.getElementById('userNameInput')?.value.trim();
    const r = document.getElementById('userRoleSelect')?.value;

    X_USER = u || 'guest';
    X_ROLE = r || 'USER';

    alert(`Контекст установлен: ${X_USER} / ${X_ROLE}`);
}

// общие хелперы

function showSpecialError(msg) {
    const el = document.getElementById('specialGlobalError');
    el.textContent = msg;
    el.style.display = 'block';

    const info = document.getElementById('specialGlobalInfo');
    info.style.display = 'none';
}

function showSpecialInfo(msg) {
    const el = document.getElementById('specialGlobalInfo');
    el.textContent = msg;
    el.style.display = 'block';

    const err = document.getElementById('specialGlobalError');
    err.style.display = 'none';
}

function clearSpecialMessages() {
    document.getElementById('specialGlobalError').style.display = 'none';
    document.getElementById('specialGlobalInfo').style.display = 'none';
}

// форматирование даты как на странице существ
function formatCreationDate(raw) {
    if (!raw) return '';
    const noZone = raw.split('[')[0];        // отрезаем зону, если есть
    const replaced = noZone.replace('T', ' ');
    return replaced.substring(0, 16);        // до минут
}

// красивый вывод

function creatureToText(c) {
    if (!c) return 'Нет данных';

    const coords = c.coordinates
        ? `x=${c.coordinates.x}, y=${c.coordinates.y}`
        : 'нет';

    const city = (c.creatureLocation && c.creatureLocation.name)
        ? c.creatureLocation.name
        : 'нет';

    let ring = 'нет';
    if (c.ring) {
        const hasName = !!c.ring.name;
        const hasPower = c.ring.power != null;
        if (hasName && hasPower) {
            ring = `${c.ring.name}, сила: ${c.ring.power}`;
        } else if (hasName) {
            ring = c.ring.name;
        } else if (hasPower) {
            ring = `сила: ${c.ring.power}`;
        }
    }

    const creation = formatCreationDate(c.creationDate);

    return [
        `ID: ${c.id}`,
        `Имя: ${c.name}`,
        `Координаты: ${coords}`,
        `Дата создания: ${creation}`,
        `Возраст: ${c.age}`,
        `Тип существа: ${c.creatureType}`,
        `Город: ${city}`,
        `Уровень атаки: ${c.attackLevel}`,
        `Кольцо: ${ring}`
    ].join('\n');
}

function creaturesListToText(list) {
    if (!Array.isArray(list) || list.length === 0) {
        return 'Существ не найдено';
    }
    // разделяем существ пустой строкой
    return list.map(creatureToText).join('\n\n');
}

// существо с максимальным name

async function fetchMaxName() {
    clearSpecialMessages();
    const out = document.getElementById('resultMaxName');
    out.textContent = '';

    try {
        const resp = await fetch(`${SPECIAL_API}?op=maxName`);
        if (!resp.ok) {
            let msg = `Ошибка: ${resp.status}`;
            try {
                const data = await resp.json();
                if (data.error) msg = data.error;
            } catch (_) {}
            showSpecialError(msg);
            return;
        }
        const creature = await resp.json();
        out.textContent = creatureToText(creature);
    } catch (e) {
        showSpecialError(e.message);
    }
}

//кол-во существ с ring.power < maxPower

async function fetchCountRing() {
    clearSpecialMessages();
    const out = document.getElementById('resultCountRing');
    out.textContent = '';

    const input = document.getElementById('inputRingMaxPower').value.trim();
    const val = parseInt(input, 10);
    if (!Number.isFinite(val) || val <= 0) {
        showSpecialError('Введите положительное целое число для силы кольца');
        return;
    }

    try {
        const resp = await fetch(`${SPECIAL_API}?op=countRingLess&maxPower=${encodeURIComponent(val)}`);
        if (!resp.ok) {
            let msg = `Ошибка: ${resp.status}`;
            try {
                const data = await resp.json();
                if (data.error) msg = data.error;
            } catch (_) {}
            showSpecialError(msg);
            return;
        }
        const data = await resp.json(); // { count: N }
        out.textContent = `Количество существ: ${data.count}`;
    } catch (e) {
        showSpecialError(e.message);
    }
}

// существа с attackLevel > minAttack

async function fetchAttackGreater() {
    clearSpecialMessages();
    const out = document.getElementById('resultAttackMore');
    out.textContent = '';

    const input = document.getElementById('inputMinAttack').value.trim();
    const val = parseFloat(input);
    if (!Number.isFinite(val)) {
        showSpecialError('Введите число для минимального уровня атаки');
        return;
    }

    try {
        const resp = await fetch(`${SPECIAL_API}?op=attackGreater&minAttack=${encodeURIComponent(val)}`);
        if (!resp.ok) {
            let msg = `Ошибка: ${resp.status}`;
            try {
                const data = await resp.json();
                if (data.error) msg = data.error;
            } catch (_) {}
            showSpecialError(msg);
            return;
        }
        const list = await resp.json(); // массив BookCreature
        out.textContent = creaturesListToText(list);
    } catch (e) {
        showSpecialError(e.message);
    }
}

// существо с самым сильным кольцом

async function fetchStrongestRing() {
    clearSpecialMessages();
    const out = document.getElementById('resultStrongestRing');
    out.textContent = '';

    try {
        const resp = await fetch(`${SPECIAL_API}?op=strongestRing`);
        if (!resp.ok) {
            let msg = `Ошибка: ${resp.status}`;
            try {
                const data = await resp.json();
                if (data.error) msg = data.error;
            } catch (_) {}
            showSpecialError(msg);
            return;
        }
        const creature = await resp.json();
        out.textContent = creatureToText(creature);
    } catch (e) {
        showSpecialError(e.message);
    }
}

// уничтожить города эльфов

async function destroyElfCities() {
    clearSpecialMessages();
    const out = document.getElementById('resultDestroyElfCities');
    out.textContent = '';

    if (!confirm('Точно уничтожить все города эльфов? Это действие нельзя отменить.')) {
        return;
    }

    try {
        const resp = await fetch(`${SPECIAL_API}?op=destroyElfCities`, {
            method: 'POST'
        });
        if (!resp.ok) {
            let msg = `Ошибка: ${resp.status}`;
            try {
                const data = await resp.json();
                if (data.error) msg = data.error;
            } catch (_) {}
            showSpecialError(msg);
            return;
        }

        const data = await resp.json(); // { destroyedCities: N }
        out.textContent = `Уничтожено городов: ${data.destroyedCities}`;
        showSpecialInfo('Операция успешно выполнена');
    } catch (e) {
        showSpecialError(e.message);
    }
}

// обработчики

window.addEventListener('load', () => {
    document.getElementById('btnMaxName')
        .addEventListener('click', fetchMaxName);

    document.getElementById('btnCountRing')
        .addEventListener('click', fetchCountRing);

    document.getElementById('btnAttackMore')
        .addEventListener('click', fetchAttackGreater);

    document.getElementById('btnStrongestRing')
        .addEventListener('click', fetchStrongestRing);

    document.getElementById('btnDestroyElfCities')
        .addEventListener('click', destroyElfCities);
});

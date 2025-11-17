const SPECIAL_API = '/api/special';

/* ===== общие хелперы ===== */

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

/* небольшая функция для красивого вывода JSON */
function pretty(obj) {
    return JSON.stringify(obj, null, 2);
}

/* ===== 1. Существо с максимальным name ===== */

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
        out.textContent = pretty(creature);
    } catch (e) {
        showSpecialError(e.message);
    }
}

/* ===== 2. Кол-во существ с ring.power < maxPower ===== */

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

/* ===== 3. Существа с attackLevel > minAttack ===== */

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
        if (!Array.isArray(list) || list.length === 0) {
            out.textContent = 'Подходящих существ не найдено';
        } else {
            out.textContent = pretty(list);
        }
    } catch (e) {
        showSpecialError(e.message);
    }
}

/* ===== 4. Существо с самым сильным кольцом ===== */

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
        out.textContent = pretty(creature);
    } catch (e) {
        showSpecialError(e.message);
    }
}

/* ===== 5. Уничтожить города эльфов ===== */

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

/* ===== навешиваем обработчики ===== */

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
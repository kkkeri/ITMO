
from __future__ import annotations
from typing import Dict, Set, Tuple, List
import sys
import re

# === БАЗА ЗНАНИЙ (факты) ===
male: Set[str] = {
    "юрий_кудияш","михаил_симонов","кирилл_панко","антон_кудияш","борис_кудияш",
    "митя_кудияш","алексей_бутанин","виктор_бутанин","михаил_кудияш","я",
    "александр_кудияш","алексей_васько","андрей_васько","дмитрий_ляхов","сергей_фоменко",
}

female: Set[str] = {
    "александра_мелько","алла_симонова","лидия_норрис","наталья_симонова","светлана_кудияш",
    "алена_кудияш","кира_ершова","светлана_бутанина","елена_кудияш","мария_кудияш",
    "надежда_алексеева","надежда_васько","любовь_громова","галина_васько","наталья_васько",
}

born: Dict[str, int] = {
    "юрий_кудияш":1890, "александра_мелько":1892, "михаил_симонов":1889, "алла_симонова":1889,
    "кирилл_панко":1917, "лидия_норрис":1918,
    "антон_кудияш":1911, "наталья_симонова":1912,
    "борис_кудияш":1941, "светлана_кудияш":1949,
    "митя_кудияш":1939, "алена_кудияш":1948,
    "алексей_бутанин":1920, "кира_ершова":1924, "виктор_бутанин":1946, "светлана_бутанина":1945,
    "михаил_кудияш":1979, "елена_кудияш":1979, "я":2005, "мария_кудияш":2012, "александр_кудияш":1976,
    "алексей_васько":1888, "надежда_алексеева":1888, "андрей_васько":1915,
    "дмитрий_ляхов":1885, "любовь_громова":1885, "надежда_васько":1916,
    "галина_васько":1951, "наталья_васько":1948, "сергей_фоменко":1944,
}

died: Dict[str, int] = {
    "юрий_кудияш":1967, "александра_мелько":1969, "михаил_симонов":1966, "алла_симонова":1966,
    "лидия_норрис":1985, "антон_кудияш":1988, "наталья_симонова":1998,
    "борис_кудияш":2016, "митя_кудияш":2013, "алексей_бутанин":1989,
    "кира_ершова":2009, "александр_кудияш":2024, "алексей_васько":1965,
    "надежда_алексеева":1964, "андрей_васько":1999, "дмитрий_ляхов":1966, "любовь_громова":1963,
    "надежда_васько":2002,
}

married: Set[Tuple[str,str,int]] = {
    ("юрий_кудияш","александра_мелько",1911),
    ("михаил_симонов","алла_симонова",1910),
    ("кирилл_панко","лидия_норрис",1938),
    ("антон_кудияш","наталья_симонова",1937),
    ("борис_кудияш","светлана_кудияш",1970),
    ("алексей_бутанин","кира_ершова",1946),
    ("виктор_бутанин","светлана_бутанина",1966),
    ("михаил_кудияш","елена_кудияш",2010),
    ("алексей_васько","надежда_алексеева",1910),
    ("андрей_васько","надежда_васько",1935),
    ("дмитрий_ляхов","любовь_громова",1917),
    ("сергей_фоменко","наталья_васько",1966),
}

divorced: Set[Tuple[str,str,int]] = {
    ("сергей_фоменко","наталья_васько",2013),
}

parent: Set[Tuple[str,str]] = {
    ("юрий_кудияш","антон_кудияш"),("александра_мелько","антон_кудияш"),
    ("михаил_симонов","наталья_симонова"),("алла_симонова","наталья_симонова"),
    ("кирилл_панко","светлана_кудияш"),("лидия_норрис","светлана_кудияш"),
    ("антон_кудияш","борис_кудияш"),("наталья_симонова","борис_кудияш"),
    ("антон_кудияш","алена_кудияш"),("наталья_симонова","алена_кудияш"),
    ("антон_кудияш","митя_кудияш"),("наталья_симонова","митя_кудияш"),
    ("борис_кудияш","михаил_кудияш"),("светлана_кудияш","михаил_кудияш"),
    ("борис_кудияш","александр_кудияш"),("светлана_кудияш","александр_кудияш"),
    ("алексей_бутанин","виктор_бутанин"),("кира_ершова","виктор_бутанин"),
    ("андрей_васько","светлана_бутанина"),("надежда_васько","светлана_бутанина"),
    ("андрей_васько","галина_васько"),("надежда_васько","галина_васько"),
    ("андрей_васько","наталья_васько"),("надежда_васько","наталья_васько"),
    ("алексей_васько","андрей_васько"),("надежда_алексеева","андрей_васько"),
    ("дмитрий_ляхов","надежда_васько"),("любовь_громова","надежда_васько"),
    ("михаил_кудияш","я"),("елена_кудияш","я"),
    ("михаил_кудияш","мария_кудияш"),("елена_кудияш","мария_кудияш"),
}

# === ВСПОМОГАТЕЛЬНЫЕ ПРЕДИКАТЫ ===

def died_before_or_in(p: str, y: int) -> bool:
    return p in died and died[p] <= y

# === КЛЮЧЕВЫЕ ПРЕДИКАТЫ (часть из 30 правил) ===

def alive_in(p: str, y: int) -> bool:
    return (p in born and born[p] <= y) and (not died_before_or_in(p, y))


def age_in(p: str, y: int) -> int | None:
    if p not in born or born[p] > y:
        return None
    end = y
    if p in died:
        end = min(end, died[p])
    age = end - born[p]
    return age if age >= 0 else None


def father(f: str, c: str) -> bool:
    return (f in male) and ((f, c) in parent)


def mother(m: str, c: str) -> bool:
    return (m in female) and ((m, c) in parent)


def parents_of(c: str) -> Set[str]:
    return {p for (p, ch) in parent if ch == c}


def children_of(pers: str) -> Set[str]:
    return {c for (p, c) in parent if p == pers}


def sibling(a: str, b: str) -> bool:
    if a == b:
        return False
    pa = parents_of(a)
    pb = parents_of(b)
    return len(pa.intersection(pb)) >= 1


def full_sibling(a: str, b: str) -> bool:
    if a == b:
        return False
    fa = {p for p in parents_of(a) if p in male}
    fb = {p for p in parents_of(b) if p in male}
    ma = {p for p in parents_of(a) if p in female}
    mb = {p for p in parents_of(b) if p in female}
    return len(fa.intersection(fb)) == 1 and len(ma.intersection(mb)) == 1


def half_sibling(a: str, b: str) -> bool:
    return sibling(a, b) and (not full_sibling(a, b))


def ancestor(a: str, d: str) -> bool:
    # транзитивно
    to_visit = list(children_of(a))
    seen = set()
    while to_visit:
        x = to_visit.pop()
        if x == d:
            return True
        if x in seen:
            continue
        seen.add(x)
        to_visit.extend(children_of(x))
    return False


def cousin(a: str, b: str) -> bool:
    # двоюродные: родители — братья/сестры
    pas = parents_of(a)
    pbs = parents_of(b)
    for pa in pas:
        for pb in pbs:
            if sibling(pa, pb) and a != b:
                return True
    return False


def uncle_aunt(u: str, n: str) -> bool:
    # дядя/тётя: родной брат/сестра родителя
    for p in parents_of(n):
        if sibling(u, p):
            return True
    return False


def married_sym(p1: str, p2: str) -> List[Tuple[str,str,int]]:
    res = []
    for (a,b,y) in married:
        if (a == p1 and b == p2) or (a == p2 and b == p1):
            res.append((a,b,y))
    return res


def marriage_end(p1: str, p2: str) -> int | None:
    # min(развод, смерть1, смерть2) или None (бесконечность)
    cand: List[int] = []
    for (a,b,y) in divorced:
        if (a==p1 and b==p2) or (a==p2 and b==p1):
            cand.append(y)
    if p1 in died:
        cand.append(died[p1])
    if p2 in died:
        cand.append(died[p2])
    return min(cand) if cand else None


def married_in_year(p1: str, p2: str, y: int) -> bool:
    for (a,b,start) in married:
        if (a==p1 and b==p2) or (a==p2 and b==p1):
            if start <= y:
                end = marriage_end(p1,p2)
                if end is None or y <= end:
                    # не учитывать развод/смерть до y
                    if not died_before_or_in(p1, y) and not died_before_or_in(p2, y):
                        # оба живы к Y для статуса "состоят в браке"
                        return True
    return False


def all_people() -> Set[str]:
    people = set(male) | set(female) | set(born.keys())
    for a,b in parent:
        people.add(a); people.add(b)
    for a,b,_ in married:
        people.add(a); people.add(b)
    for a,b,_ in divorced:
        people.add(a); people.add(b)
    return people

# === РЕКОМЕНДАТОРЫ ===

def degree_of_kinship(a: str, b: str) -> int:
    """Приблизительная степень родства (для сортировки):
    0 — тот же человек; 1 — родители/дети/полные сиблинги; 2 — бабушки/дедушки/внуки/дяди/тёти/племянники/двоюродные.
    Если далеко — вернуть 99.
    """
    if a == b:
        return 0
    # родители/дети
    if (a,b) in parent or (b,a) in parent:
        return 1
    # полные/неполные сиблинги
    if full_sibling(a,b) or half_sibling(a,b):
        return 1
    # бабушки/дедушки
    for g in parents_of(a):
        if (g,b) in parent:  # b — ребёнок моего родителя => мой брат/сестра
            if sibling(b, a):
                return 1
    # предки на 2 колена и двоюродные
    if any(ancestor(x, b) for x in parents_of(a)) or any(ancestor(x, a) for x in parents_of(b)):
        # дяди/тёти/племянники/внуки
        return 2
    if cousin(a,b):
        return 2
    return 99


def recommend_alive_relatives(me: str, year: int, limit: int = 10) -> List[Tuple[str,int,int]]:
    res = []
    for p in all_people():
        if p == me:
            continue
        if alive_in(p, year):
            deg = degree_of_kinship(me, p)
            ag = age_in(p, year)
            if ag is None:
                continue
            if deg <= 2:  # близкий круг
                res.append((p, deg, ag))
    res.sort(key=lambda x: (x[1], abs((age_in(me, year) or 0) - x[2]), -x[2]))
    return res[:limit]


def recommend_married_close(me: str, year: int, limit: int = 10) -> List[Tuple[str,str,int]]:
    pairs = []
    # взять только пары, в которых хотя бы один — близкий родственник
    for a in all_people():
        for b in all_people():
            if a >= b:
                continue
            if married_in_year(a,b,year):
                if degree_of_kinship(me, a) <= 2 or degree_of_kinship(me, b) <= 2:
                    pairs.append((a,b,year))
    # сортировки тут особо не требуется; выведем как есть
    return pairs[:limit]


def recommend_guardians(me: str, year: int, limit: int = 10) -> List[Tuple[str,int,str]]:
    """Рекомендовать совершеннолетних близких (>=18) — родители, сиблинги, тёти/дяди, бабушки/дедушки."""
    res = []
    for p in all_people():
        if p == me:
            continue
        ag = age_in(p, year)
        if ag is None or ag < 18:
            continue
        if degree_of_kinship(me, p) <= 2:
            rel = relation_name(me, p)
            res.append((p, ag, rel))
    res.sort(key=lambda x: (relation_priority(x[2]), -x[1]))
    return res[:limit]


def recommend_mentors(me: str, year: int, min_gap: int = 8, limit: int = 10) -> List[Tuple[str,int,str]]:
    """Наставники — старшие на ≥min_gap из близкого круга."""
    my_age = age_in(me, year) or 0
    res = []
    for p in all_people():
        if p == me:
            continue
        ag = age_in(p, year)
        if ag is None or ag < my_age + min_gap:
            continue
        if degree_of_kinship(me, p) <= 2:
            res.append((p, ag, relation_name(me,p)))
    res.sort(key=lambda x: (relation_priority(x[2]), x[1]))
    return res[:limit]

# === ЧЕЛОВЕКО-ПОНЯТНЫЕ НАИМЕНОВАНИЯ СВЯЗЕЙ ===

def relation_name(a: str, b: str) -> str:
    # b → a (b родитель a)
    if (b, a) in parent:
        return "родитель"
    # a → b (a родитель b)
    if (a, b) in parent:
        return "ребёнок"
    if full_sibling(a,b):
        return "родной брат/сестра"
    if half_sibling(a,b):
        return "сводный брат/сестра"
    if uncle_aunt(b,a):
        return "дядя/тётя"
    if uncle_aunt(a,b):
        return "племянник/племянница"
    if cousin(a,b):
        return "двоюродный(ая) брат/сестра"
    return "родственник"


def relation_priority(name: str) -> int:
    order = {
        "родитель": 0,
        "родной брат/сестра": 1,
        "сводный брат/сестра": 2,
        "дядя/тётя": 3,
        "племянник/племянница": 4,
        "бабушка/дедушка": 5,
        "двоюродный(ая) брат/сестра": 6,
        "родственник": 9,
    }
    return order.get(name, 9)

# === ПАРСЕР ФИКСИРОВАННОГО ФОРМАТА ===
KV_RE = re.compile(r"\s*([^=;]+?)\s*=\s*([^;]+?)\s*(?:;|$)")


def parse_query(line: str) -> Dict[str, str]:
    data: Dict[str,str] = {}
    for m in KV_RE.finditer(line.strip()):
        key = m.group(1).strip().lower()
        val = m.group(2).strip().lower()
        data[key] = val
    return data

# === ДИАЛОГ ===

def prompt_missing(d: Dict[str,str], key: str, question: str) -> None:
    if key not in d or not d[key]:
        print(question, end=" ")
        d[key] = input().strip().lower()


def run_dialog() -> None:
    print("Введите запрос в формате: имя=...; год=YYYY; хочу=[живые|в_браке|опекуны|наставник]")
    line = input("> ")
    d = parse_query(line)

    prompt_missing(d, "имя", "Кого считать \"я\" (имя из БЗ)?")
    prompt_missing(d, "год", "За какой год смотреть? (YYYY)")
    prompt_missing(d, "хочу", "Что вы хотите узнать? [живые|в_браке|опекуны|наставник]")

    name = d["имя"].replace(" ", "_")
    try:
        year = int(d["год"])
    except Exception:
        print("Год должен быть целым числом.")
        return
    goal = d["хочу"]

    if name not in all_people():
        print(f"Имя '{name}' не найдено в базе знаний. Проверьте написание (нижнее подчеркивание вместо пробелов).")
        return

    if goal == "живые":
        recs = recommend_alive_relatives(name, year)
        if not recs:
            print("Подходящих живых близких родственников не найдено.")
            return
        print(f"Рекомендации: с кем из близких стоит связаться в {year} году:")
        for p,deg,ag in recs:
            print(f"  - {p} ({relation_name(name,p)}), возраст {ag}")

    elif goal == "в_браке":
        pairs = recommend_married_close(name, year)
        if not pairs:
            print("В близком круге нет пар, состоящих в браке на этот год.")
            return
        print(f"В {year} году в браке (из вашего близкого круга):")
        for a,b,_ in pairs:
            print(f"  - {a} ❤ {b}")

    elif goal == "опекуны":
        recs = recommend_guardians(name, year)
        if not recs:
            print("Совершеннолетних близких кандидатов в опекуны не найдено.")
            return
        print(f"Кандидаты в опекуны (совершеннолетние, ближний круг) на {year} год:")
        for p,ag,rel in recs:
            print(f"  - {p} — {rel}, {ag} лет")

    elif goal == "наставник":
        recs = recommend_mentors(name, year)
        if not recs:
            print("Старших наставников (на 8+ лет старше) в близком круге не найдено.")
            return
        print(f"Потенциальные наставники (старше на 8+ лет) на {year} год:")
        for p,ag,rel in recs:
            print(f"  - {p} — {rel}, {ag} лет")

    else:
        print("Неизвестная цель. Поддерживаемые: живые, в_браке, опекуны, наставник")

# === ТЕСТЫ ===

def _test_basic():
    assert alive_in("я", 2024)
    assert not alive_in("александр_кудияш", 2025)  # умер в 2024
    assert age_in("я", 2024) == 19
    assert sibling("михаил_кудияш","александр_кудияш")
    assert cousin("я", "виктор_бутанин") is False  # не двоюродные
    assert ("михаил_кудияш","я") in parent
    print("[OK] Базовые тесты прошли")


if __name__ == "__main__":
    if "--test" in sys.argv:
        _test_basic()
        sys.exit(0)
    run_dialog()

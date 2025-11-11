#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Лабораторная 3. Линейная регрессия (вариант: Student_Performance.csv)

Требования из задания:
1) Загрузить датасет и вывести + визуализировать статистику (count, mean, std, min, max, квартили).
2) Провести предварительную обработку: пропуски, кодирование категорий, нормировка.
3) Разделить на train/test без sklearn (только NumPy/Pandas; Matplotlib — для графиков).
4) Реализовать линейную регрессию МНК без «готовых регрессоров»: по нормальному уравнению.
5) Построить 3 модели с разными наборами признаков.
6) Оценка: R^2 на тесте. Сравнить модели.
Бонус: добавить синтетический признак.

Как запускать:
    python lab3_linear_regression.py Student_Performance.csv

Примечание: matplotlib используется только для визуализаций (не запрещено в ТЗ).
"""

from __future__ import annotations
import sys
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from dataclasses import dataclass
from typing import List, Tuple, Dict

# ------------------------------
# Утилиты визуализации и отчёта
# ------------------------------

def describe_with_quantiles(df: pd.DataFrame, q: List[float] = [0.25, 0.5, 0.75]) -> pd.DataFrame:
    desc = df.describe().T  # count, mean, std, min, 25%, 50%, 75%, max
    # Убедимся, что есть необходимые квантили
    for quant in q:
        if f"{int(quant*100)}%" not in desc.columns:
            desc[f"{int(quant*100)}%"] = df.quantile(quant)
    return desc


def plot_hist(df: pd.DataFrame, cols: List[str], bins: int = 10, title: str = "Гистограммы"):
    for col in cols:
        plt.figure()
        df[col].hist(bins=bins)
        plt.title(f"{title}: {col}")
        plt.xlabel(col)
        plt.ylabel("count")
        plt.tight_layout()
        plt.show()


def plot_box(df: pd.DataFrame, cols: List[str], title: str = "Boxplot"):
    for col in cols:
        plt.figure()
        df.boxplot(column=col)
        plt.title(f"{title}: {col}")
        plt.tight_layout()
        plt.show()


def plot_bar(values: Dict[str, float], title: str):
    labels = list(values.keys())
    vals = list(values.values())
    plt.figure()
    plt.bar(labels, vals)
    plt.title(title)
    plt.xticks(rotation=20)
    plt.tight_layout()
    plt.show()

# ------------------------------
# Предобработка
# ------------------------------

def load_student_performance(csv_path: str) -> pd.DataFrame:
    df = pd.read_csv(csv_path)
    rename_map = {
        'Hours Studied': 'hours',
        'Previous Scores': 'prev_score',
        'Extracurricular Activities': 'extracurr',
        'Sleep Hours': 'sleep',
        'Sample Question Papers Practiced': 'samples',
        'Performance Index': 'target',
    }
    df = df.rename(columns=rename_map)
    return df


def preprocess(df: pd.DataFrame) -> Tuple[pd.DataFrame, Dict[str, float], Dict[str, float]]:
    df = df.copy()

    # 1) Обработка пропусков
    # Числовые: заполним медианой, категориальные: самым частым значением
    num_cols = ['hours', 'prev_score', 'sleep', 'samples']
    cat_cols = ['extracurr']

    for c in num_cols:
        if c in df.columns:
            med = df[c].median()
            df[c] = df[c].fillna(med)
    for c in cat_cols:
        if c in df.columns:
            mode = df[c].mode(dropna=True)
            if len(mode) > 0:
                df[c] = df[c].fillna(mode.iloc[0])
            else:
                df[c] = df[c].fillna('No')

    # 2) Кодирование категориального признака Extracurricular: Yes/No -> 1/0
    if 'extracurr' in df.columns:
        df['extracurr'] = df['extracurr'].astype(str).str.strip().str.lower().map({'yes': 1, 'no': 0})
        # если встречались другие значения — заменим их на 0
        df['extracurr'] = df['extracurr'].fillna(0).astype(int)

    # 3) Синтетический признак (бонус): эффективность учёбы = hours * sleep
    if set(['hours', 'sleep']).issubset(df.columns):
        df['study_eff'] = df['hours'] * df['sleep']

    # 4) Нормализация (z-score) ТОЛЬКО признаков, не целевой переменной
    feature_cols = ['hours', 'prev_score', 'extracurr', 'sleep', 'samples', 'study_eff']
    feature_cols = [c for c in feature_cols if c in df.columns]

    mu: Dict[str, float] = {}
    sigma: Dict[str, float] = {}
    for c in feature_cols:
        mu[c] = float(df[c].mean())
        sigma[c] = float(df[c].std(ddof=0)) or 1.0
        df[c] = (df[c] - mu[c]) / sigma[c]

    return df, mu, sigma

# ------------------------------
# Разделение train/test без sklearn
# ------------------------------

def train_test_split_np(X: np.ndarray, y: np.ndarray, test_size: float = 0.2, seed: int = 42) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
    assert 0.0 < test_size < 1.0
    n = X.shape[0]
    rng = np.random.default_rng(seed)
    idx = np.arange(n)
    rng.shuffle(idx)
    test_n = int(round(n * test_size))
    test_idx = idx[:test_n]
    train_idx = idx[test_n:]
    return X[train_idx], X[test_idx], y[train_idx], y[test_idx]

# ------------------------------
# Линейная регрессия (МНК)
# ------------------------------

def add_bias(X: np.ndarray) -> np.ndarray:
    ones = np.ones((X.shape[0], 1))
    return np.hstack([ones, X])


def fit_normal_equation(X: np.ndarray, y: np.ndarray) -> np.ndarray:
    """Находит вектор коэффициентов b из нормального уравнения: b = (X^T X)^{-1} X^T y
    ОЖИДАЕТ, что X уже содержит столбец единиц (bias).
    """
    XtX = X.T @ X
    Xty = X.T @ y
    b = np.linalg.pinv(XtX) @ Xty  # pinv устойчивее, чем inv
    return b


def predict(X: np.ndarray, b: np.ndarray) -> np.ndarray:
    return X @ b


def r2_score(y_true: np.ndarray, y_pred: np.ndarray) -> float:
    y_true = y_true.ravel()
    y_pred = y_pred.ravel()
    ss_res = float(np.sum((y_true - y_pred) ** 2))
    ss_tot = float(np.sum((y_true - np.mean(y_true)) ** 2))
    return 1.0 - ss_res / ss_tot if ss_tot != 0 else 0.0

# ------------------------------
# Построение моделей (три набора признаков)
# ------------------------------

@dataclass
class ModelSpec:
    name: str
    features: List[str]


def build_X(df: pd.DataFrame, features: List[str]) -> np.ndarray:
    X = df[features].to_numpy(dtype=float)
    return add_bias(X)


def run_experiment(df_raw: pd.DataFrame):
    # Статистика
    numeric_for_stats = [c for c in ['hours','prev_score','sleep','samples','target'] if c in df_raw.columns]
    print("\n== БАЗОВАЯ СТАТИСТИКА ==")
    print(describe_with_quantiles(df_raw[numeric_for_stats]))

    plot_hist(df_raw, numeric_for_stats, bins=10, title="Гистограмма (сырые данные)")
    plot_box(df_raw, numeric_for_stats, title="Boxplot (сырые данные)")

    # Предобработка
    df, mu, sigma = preprocess(df_raw)

    # Показать статистику после нормализации по признакам
    print("\n== СТАТИСТИКА ПОСЛЕ НОРМАЛИЗАЦИИ (только признаки) ==")
    feats = [c for c in df.columns if c != 'target']
    print(describe_with_quantiles(df[feats]))

    plot_hist(df, feats, bins=10, title="Гистограмма (признаки, нормированные)")

    # Формируем целевую переменную
    y = df['target'].to_numpy(dtype=float).reshape(-1, 1)

    # Спецификации моделей
    specs = [
        ModelSpec(name="Модель A (hours)", features=[f for f in ['hours'] if f in df.columns]),
        ModelSpec(name="Модель B (hours + prev_score + extr + sleep)", features=[f for f in ['hours','prev_score','extracurr','sleep'] if f in df.columns]),
        ModelSpec(name="Модель C (все признаки + study_eff)", features=[c for c in ['hours','prev_score','extracurr','sleep','samples','study_eff'] if c in df.columns]),
    ]

    results: Dict[str, float] = {}

    for spec in specs:
        if not spec.features:
            print(f"Пропускаю {spec.name}: нет доступных признаков")
            continue
        X = build_X(df, spec.features)
        X_train, X_test, y_train, y_test = train_test_split_np(X, y, test_size=0.2, seed=42)
        b = fit_normal_equation(X_train, y_train)
        y_pred = predict(X_test, b)
        r2 = r2_score(y_test, y_pred)
        results[spec.name] = r2
        print(f"\n{spec.name}\n  Признаки: {spec.features}\n  Коэффициенты b: {b.ravel()}\n  R^2 (test): {r2:.4f}")

    # Визуализация сравнения R^2
    if results:
        plot_bar(results, title="Сравнение моделей по R^2 (тест)")


# ------------------------------
# Точка входа
# ------------------------------

if __name__ == "__main__":
    csv_path = "Student_Performance.csv"
    df0 = load_student_performance(csv_path)
    run_experiment(df0)

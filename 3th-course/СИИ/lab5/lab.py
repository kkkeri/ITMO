from __future__ import annotations
import sys
import math
import random
from dataclasses import dataclass, field
from typing import Dict, List, Tuple, Optional

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

def load_dataset(csv_path: str) -> pd.DataFrame:
    df = pd.read_csv(csv_path)
    df.columns = [str(c).strip() for c in df.columns]
    return df


def make_binary_target(df: pd.DataFrame, threshold: int = 3) -> pd.Series:
    # Успешный/неуспешный по грейду
    y = (df['GRADE'].astype(int) >= threshold).astype(int)
    return y


def select_features(df: pd.DataFrame) -> List[str]:
    feature_cols = [c for c in df.columns if c.isdigit()]  # '1'..'30'
    n = len(feature_cols)
    k = max(1, int(round(math.sqrt(n))))
    chosen = sorted(random.sample(feature_cols, k), key=lambda x: int(x))  # без seed
    return chosen

def train_test_split_np(X: np.ndarray, y: np.ndarray, test_size: float = 0.2, seed: int = 42) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
    n = X.shape[0]
    rng = np.random.default_rng(seed)
    idx = np.arange(n)
    rng.shuffle(idx)
    test_n = int(round(n * test_size))
    test_idx = idx[:test_n]
    train_idx = idx[test_n:]
    return X[train_idx], X[test_idx], y[train_idx], y[test_idx]

@dataclass
class Attribute:
    name: str
    unique_values: List[int]

@dataclass
class Node:
    is_leaf: bool
    pred_class: Optional[int] = None       # класс, если лист
    prob_pos: float = 0.0                  # p(y=1) в листе (для ROC/PR)
    split_attr: Optional[Attribute] = None # атрибут для разбиения
    children: Dict[int, 'Node'] = field(default_factory=dict)


def entropy(labels: np.ndarray) -> float:
    if labels.size == 0:
        return 0.0
    p1 = np.mean(labels == 1)
    p0 = 1.0 - p1
    ent = 0.0
    if p0 > 0:
        ent -= p0 * math.log2(p0)
    if p1 > 0:
        ent -= p1 * math.log2(p1)
    return ent


def majority_class(labels: np.ndarray) -> int:
    # при равенстве — 1 побеждает 0?
    n1 = int(np.sum(labels == 1))
    n0 = labels.size - n1
    return 1 if n1 >= n0 else 0


def information_gain_ratio(X: np.ndarray, y: np.ndarray, col_idx: int) -> Tuple[float, Dict[int, np.ndarray]]:
    n = y.size
    base_ent = entropy(y)

    # Группировка по уникальным значениям признака
    values = np.unique(X[:, col_idx])
    splits: Dict[int, np.ndarray] = {}

    info_x = 0.0
    split_info = 0.0
    for v in values:
        idx = np.where(X[:, col_idx] == v)[0]
        splits[int(v)] = idx
        if idx.size == 0:
            continue
        p = idx.size / n
        info_x += p * entropy(y[idx])
        split_info -= p * math.log2(p)

    gain = base_ent - info_x
    if split_info == 0.0:
        return 0.0, splits
    return gain / split_info, splits


def build_tree(X: np.ndarray, y: np.ndarray, attributes: List[Attribute], min_samples_leaf: int = 1, max_depth: int = 100, depth: int = 0) -> Node:
    # Условия остановки: чистый узел или ограничения
    if y.size == 0:
        return Node(is_leaf=True, pred_class=0, prob_pos=0.0)
    if np.all(y == y[0]) or depth >= max_depth or X.shape[1] == 0:
        pred = int(y[0]) if y.size > 0 and np.all(y == y[0]) else majority_class(y)
        return Node(is_leaf=True, pred_class=pred, prob_pos=float(np.mean(y)))

    # Выбор наилучшего атрибута по gain ratio
    best_attr: Optional[Attribute] = None
    best_splits: Dict[int, np.ndarray] = {}
    best_gr: float = -1.0
    for j, attr in enumerate(attributes):
        gr, splits = information_gain_ratio(X, y, j)
        if gr > best_gr:
            best_gr = gr
            best_attr = attr
            best_splits = splits

    if best_attr is None or len(best_splits) == 0:
        return Node(is_leaf=True, pred_class=majority_class(y), prob_pos=float(np.mean(y)))

    # Построить детей (многоветвистый узел: по каждому значению признака)
    node = Node(is_leaf=False, split_attr=best_attr)
    # Подготовим новую матрицу признаков для потомков: удалим выбранный столбец
    best_col = attributes.index(best_attr)

    for v, idx in best_splits.items():
        X_child = X[idx, :]
        y_child = y[idx]
        # удаляем столбец best_col
        X_child_reduced = np.delete(X_child, best_col, axis=1)
        attrs_child = attributes[:best_col] + attributes[best_col+1:]

        if y_child.size < min_samples_leaf:
            child = Node(is_leaf=True, pred_class=majority_class(y), prob_pos=float(np.mean(y)))
        else:
            child = build_tree(X_child_reduced, y_child, attrs_child, min_samples_leaf, max_depth, depth+1)
        node.children[v] = child

    # Если по факту разбиение не дало детей
    if len(node.children) == 0:
        return Node(is_leaf=True, pred_class=majority_class(y), prob_pos=float(np.mean(y)))

    return node


def predict_one(node: Node, x: np.ndarray, attrs: List[Attribute]) -> Tuple[int, float]:
    cur = node
    cur_attrs = attrs
    cur_x = x
    while not cur.is_leaf:
        # находим индекс текущего атрибута
        try:
            j = cur_attrs.index(cur.split_attr)
        except ValueError:
            # если структура не совпала — возвращаем текущую вероятность
            return (cur.pred_class if cur.pred_class is not None else 0, cur.prob_pos)
        v = int(cur_x[j])
        if v in cur.children:
            # спускаемся, одновременно удаляя столбец
            child = cur.children[v]
            cur_x = np.delete(cur_x, j)
            cur_attrs = cur_attrs[:j] + cur_attrs[j+1:]
            cur = child
        else:
            # невиденное значение — вернём текущую оценку узла
            return (cur.pred_class if cur.pred_class is not None else majority_class(np.array([0,1])), cur.prob_pos)
    return (cur.pred_class if cur.pred_class is not None else 0, cur.prob_pos)

# ---------------------------
# Метрики и кривые без sklearn
# ---------------------------

def accuracy_score(y_true: np.ndarray, y_pred: np.ndarray) -> float:
    return float(np.mean(y_true == y_pred))


def precision_recall(y_true: np.ndarray, y_pred: np.ndarray) -> Tuple[float, float]:
    tp = int(np.sum((y_true == 1) & (y_pred == 1)))
    fp = int(np.sum((y_true == 0) & (y_pred == 1)))
    fn = int(np.sum((y_true == 1) & (y_pred == 0)))
    precision = tp / (tp + fp) if (tp + fp) > 0 else 0.0
    recall = tp / (tp + fn) if (tp + fn) > 0 else 0.0
    return precision, recall


def roc_curve_manual(y_true: np.ndarray, scores: np.ndarray) -> Tuple[np.ndarray, np.ndarray]:
    # Сортируем по убыванию score
    order = np.argsort(-scores)
    y = y_true[order]
    P = np.sum(y == 1)
    N = np.sum(y == 0)
    tpr = [0.0]
    fpr = [0.0]
    tp = 0
    fp = 0
    for i in range(len(y)):
        if y[i] == 1:
            tp += 1
        else:
            fp += 1
        tpr.append(tp / P if P > 0 else 0.0)
        fpr.append(fp / N if N > 0 else 0.0)
    return np.array(fpr), np.array(tpr)


def pr_curve_manual(y_true: np.ndarray, scores: np.ndarray) -> Tuple[np.ndarray, np.ndarray]:
    order = np.argsort(-scores)
    y = y_true[order]
    precisions = []
    recalls = []
    tp = 0
    fp = 0
    P = np.sum(y_true == 1)
    for i in range(len(y)):
        if y[i] == 1:
            tp += 1
        else:
            fp += 1
        prec = tp / (tp + fp) if (tp + fp) > 0 else 0.0
        rec = tp / P if P > 0 else 0.0
        precisions.append(prec)
        recalls.append(rec)
    return np.array(recalls), np.array(precisions)


def auc_trapezoid(x: np.ndarray, y: np.ndarray) -> float:
    # x должен быть по возрастанию
    order = np.argsort(x)
    x = x[order]
    y = y[order]
    area = 0.0
    for i in range(1, len(x)):
        area += (x[i] - x[i-1]) * (y[i] + y[i-1]) / 2.0
    return float(area)

def main(path: str, seed: int = 42):
    df = load_dataset(path)
    y = make_binary_target(df, threshold=3).to_numpy(dtype=int)

    # Выбор признаков sqrt(n)
    chosen_cols = select_features(df)
    print(f"Выбраны признаки (sqrt(n)): {chosen_cols}")

    X = df[chosen_cols].to_numpy(dtype=int)

    # Train/Test split
    X_train, X_test, y_train, y_test = train_test_split_np(X, y, test_size=0.2, seed=seed)

    # Атрибуты для дерева: уникальные значения по train
    attributes = []
    for j, col in enumerate(chosen_cols):
        uniques = sorted(np.unique(X_train[:, j]).tolist())
        attributes.append(Attribute(name=col, unique_values=uniques))

    # Обучение дерева (многоветвистое)
    tree = build_tree(X_train, y_train, attributes, min_samples_leaf=1, max_depth=100, depth=0)

    # Предсказания и скоры (вероятность положительного класса в листе)
    y_pred = []
    scores = []
    for i in range(X_test.shape[0]):
        pred, score = predict_one(tree, X_test[i, :].astype(int), attributes.copy())
        y_pred.append(pred)
        scores.append(score)
    y_pred = np.array(y_pred, dtype=int)
    scores = np.array(scores, dtype=float)

    # Метрики
    acc = accuracy_score(y_test, y_pred)
    prec, rec = precision_recall(y_test, y_pred)
    print(f"Accuracy: {acc:.4f}\nPrecision: {prec:.4f}\nRecall: {rec:.4f}")

    # ROC и PR кривые + AUC (вручную)
    fpr, tpr = roc_curve_manual(y_test, scores)
    auc_roc = auc_trapezoid(fpr, tpr)

    recs, precs = pr_curve_manual(y_test, scores)
    auc_pr = auc_trapezoid(recs, precs)

    print(f"AUC-ROC: {auc_roc:.4f}\nAUC-PR: {auc_pr:.4f}")

    # Визуализация
    plt.figure()
    plt.plot(fpr, tpr)
    plt.xlabel('FPR')
    plt.ylabel('TPR')
    plt.title('ROC curve (manual)')
    plt.tight_layout()
    plt.show()

    plt.figure()
    plt.plot(recs, precs)
    plt.xlabel('Recall')
    plt.ylabel('Precision')
    plt.title('PR curve (manual)')
    plt.tight_layout()
    plt.show()

if __name__ == '__main__':
    main("data.csv")

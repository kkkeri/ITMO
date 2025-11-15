import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from scipy import stats

def estimate_hyperexponential_params(data, cv_target=None):
    """
    Оценка параметров гиперэкспоненциального распределения по выборке данных.
    Если задан cv_target, подбираются параметры смеси для заданного коэффициента вариации.
    """
    data = np.asarray(data)
    data = data[np.isfinite(data)]   # убираем NaN, inf

    mean = np.mean(data)
    var = np.var(data, ddof=1)
    cv = np.sqrt(var) / mean

    print("=" * 60)
    print("ОЦЕНКА ПАРАМЕТРОВ ГИПЕРЭКСПОНЕНЕНЦИАЛЬНОГО РАСПРЕДЕЛЕНИЯ")
    print(len(data))
    print("=" * 60)
    print(f"Среднее значение: {mean:.15f}")
    print(f"Дисперсия: {var:.15f}")
    print(f"Коэффициент вариации: {cv:.15f}")
    
    # Для CV > 1 можно аппроксимировать смесью двух экспонент
    if cv_target is None:
        cv_target = cv

    if cv_target <= 1:
        print("CV <= 1, гиперэкспоненциальное распределение неприменимо, используйте экспоненциальное или Эрланга.")
        return None

    # Параметры смеси двух экспонент
    # p - вероятность выбора λ1, (1-p) - λ2
    p = 0.5 * (1 + np.sqrt((cv_target**2 - 1)/(cv_target**2 + 1)))
    lambda1 = 2 * p / mean
    lambda2 = 2 * (1 - p) / mean

    print(f"Вероятность p: {p:.15f}")
    print(f"λ1: {lambda1:.15f}, λ2: {lambda2:.15f}")
    print("=" * 60)

    # Теоретическая плотность гиперэкспоненциального распределения
    x = np.linspace(0, np.max(data), 1000)
    pdf_hyper = p * stats.expon.pdf(x, scale=1/lambda1) + (1 - p) * stats.expon.pdf(x, scale=1/lambda2)

    # График
    plt.figure(figsize=(10,6))
    plt.hist(data, bins=30, density=True, alpha=0.6, color='skyblue', edgecolor='black', label='Данные')
    plt.plot(x, pdf_hyper, 'r-', lw=2, label=f'Hyperexponential PDF (CV_target={cv_target:.2f})')
    plt.title("Аппроксимация гиперэкспоненциальным распределением")
    plt.xlabel("Значение")
    plt.ylabel("Плотность вероятности")
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.show()

    return {"mean": mean, "var": var, "cv": cv, "p": p, "lambda1": lambda1, "lambda2": lambda2}


# Пример использования
if __name__ == "__main__":
    # Загрузка данных из Excel без заголовка
    data = pd.read_excel("/Users/egorkudias/Desktop/ITMO/моделирование/lab1/Книга1.xlsx", header=None).iloc[:, 0]
    data = data.dropna().astype(str).str.replace(',', '.', regex=False).astype(float)
    result = estimate_hyperexponential_params(data, cv_target=2.0)


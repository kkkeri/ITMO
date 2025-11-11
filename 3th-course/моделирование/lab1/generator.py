import numpy as np
import pandas as pd

def generate_custom_hyperexponential(n, t, v, q):
    """
    Генератор случайных величин по аппроксимирующему закону с t1, t2 и q.
    
    Параметры:
    - n: количество значений
    - t: математическое ожидание
    - v: коэффициент вариации
    - q: порог вероятности (по умолчанию 0.2)
    - random_seed: для воспроизводимости
    """    
    # Вычисляем t1 и t2 по формулам
    t1 = t * (1 + np.sqrt((1 - q) / (2 * q) * (v**2 - 1)))
    t2 = t * (1 - np.sqrt(q / (2 * (1 - q)) * (v**2 - 1)))

    # Генерация случайных чисел
    r1 = np.random.rand(n)
    r2 = np.random.rand(n)
    
    # Генерация величин
    x = np.where(r1 < q, t1 * (-np.log(1 - r2)), t2 * (-np.log(1 - r2)))
    
    return x

if __name__ == "__main__":
    # Параметры
    t = 15.234
    v = 1.81
    q = 0.18
    n = 300

    data = generate_custom_hyperexponential(n, t, v, q)

    # Сохраняем в Excel
    df = pd.DataFrame(data, columns=["Values"])
    df.to_excel("/Users/egorkudias/Desktop/ITMO/моделирование/lab1/custom_generator_data.xlsx", index=False, float_format="%.15f")

    print("Генерация завершена. Данные сохранены в 'custom_generator_data.xlsx'.")

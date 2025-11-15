import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from scipy.stats import kstest

# Параметры
t = 15.234
v = 1.81
q = 0.18

# Вычисление t1 и t2
t1 = t * (1 + np.sqrt((1 - q) / (2 * q) * (v**2 - 1)))
t2 = t * (1 - np.sqrt(q / (2 * (1 - q)) * (v**2 - 1)))
lambda1 = 1 / t1
lambda2 = 1 / t2

# Загрузка или генерация данных
# Если данные в файле:
df = pd.read_excel("/Users/egorkudias/Desktop/ITMO/моделирование/lab1/data.xlsx")
data = df["Values"].values

# Построение гистограммы и плотности
plt.figure(figsize=(10, 6))

# Нормализованная гистограмма (density=True)
plt.hist(data, bins=30, density=True, alpha=0.6, color='blue', label='Гистограмма (оценка плотности)')

# Кривые плотности
x = np.linspace(min(data), max(data), 1000)
pdf = q * lambda1 * np.exp(-lambda1 * x) + (1 - q) * lambda2 * np.exp(-lambda2 * x)
plt.plot(x, pdf, 'r-', label='Плотность аппроксимирующего закона')

plt.title('Сравнение гистограммы и плотности распределения')
plt.xlabel('Значения')
plt.ylabel('Плотность')
plt.legend()
plt.grid(True)
plt.show()


import numpy as np
from itertools import permutations


def safe_float_input(prompt):
    while True:
        try:
            value = float(input(prompt))
            if 0 < value < 1:
                return value
            print("Ошибка: введите число в диапазоне (0, 1).")
        except ValueError:
            print("Ошибка: введите число.")


def safe_int_input(prompt, min_val, max_val):
    while True:
        try:
            value = int(input(prompt))
            if min_val <= value <= max_val:
                return value
            print(f"Ошибка: введите целое число от {min_val} до {max_val}.")
        except ValueError:
            print("Ошибка: введите целое число.")


def read_input():
    choice = input("Вы хотите ввести данные вручную (1) или из файла (2)? ")
    matrix = []

    if choice == "1":
        epsilon = safe_float_input("Введите точность epsilon (0 < epsilon < 1): ")
        n = safe_int_input("Введите размерность матрицы (n <= 20): ", 1, 20)

        print("Введите коэффициенты матрицы (каждая строка n+1 чисел через пробел):")
        for _ in range(n):
            while True:
                row = input().split()
                if len(row) != n + 1:
                    print(f"Ошибка: введите {n + 1} чисел.")
                    continue
                try:
                    matrix.append(list(map(float, row)))
                    break
                except ValueError:
                    print("Ошибка: введите только числа.")

    elif choice == "2":
        filename = input("Введите имя файла: ")
        try:
            with open(filename, 'r') as f:
                lines = f.readlines()

            try:
                epsilon = float(lines[0].strip())
                if not (0 < epsilon < 1):
                    print("Ошибка: точность должна быть в пределах (0, 1).")
                    return read_input()
            except ValueError:
                print("Ошибка: точность должна быть числом.")
                return read_input()

            try:
                n = int(lines[1].strip())
                if not (1 <= n <= 20):
                    print("Ошибка: размерность должна быть в пределах 1-20.")
                    return read_input()
            except ValueError:
                print("Ошибка: размерность должна быть целым числом.")
                return read_input()

            for line in lines[2:n + 2]:
                row = line.split()
                if len(row) != n + 1:
                    print("Ошибка в файле: некорректное количество элементов в строках.")
                    return read_input()
                try:
                    matrix.append(list(map(float, row)))
                except ValueError:
                    print("Ошибка в файле: найдены некорректные данные.")
                    return read_input()
        except (IndexError, FileNotFoundError) as e:
            print(f"Ошибка чтения файла: {e}")
            return read_input()
    else:
        print("Некорректный ввод")
        return read_input()

    return epsilon, n, np.array(matrix)


def check_diagonal_dominance(A):
    n = A.shape[0]
    for i in range(n):
        if abs(A[i, i]) < sum(abs(A[i, j]) for j in range(n) if i != j):
            return False
    return True


def make_diagonally_dominant(A, b):
    n = A.shape[0]
    for perm in permutations(range(n)):
        A_perm = A[list(perm), :]
        if check_diagonal_dominance(A_perm):
            return A_perm, b[list(perm)]
    return None, None


def simple_iteration_method(C, d, x0, epsilon=0.01, max_iterations=100):
    x_prev = np.array(x0, dtype=float)
    x_next = np.zeros_like(x_prev)
    iterations = 0
    errors = []

    while iterations < max_iterations:
        x_next = C @ x_prev + d
        error = np.max(np.abs(x_next - x_prev))
        errors.append(error)

        if error < epsilon:
            break

        x_prev = x_next.copy()
        iterations += 1

    return x_next, iterations, errors


epsilon, n, matrix = read_input()
A = matrix[:, :-1]
b = matrix[:, -1]

if not check_diagonal_dominance(A):
    A, b = make_diagonally_dominant(A, b)
    if A is None:
        print("Невозможно привести матрицу к диагональному преобладанию.")
    else:
        print("Матрица была переставлена для достижения диагонального преобладания.")

if A is not None:
    if np.any(np.diag(A) == 0):
        print("Ошибка: Матрица имеет нули на диагонали и не может быть инвертирована.")
    else:
        C = np.eye(n) - np.linalg.inv(np.diag(np.diag(A))) @ A
        d = np.linalg.inv(np.diag(np.diag(A))) @ b

        norm_C = np.linalg.norm(C, ord=1)
        print(f"Норма матрицы C (по максимуму столбцов): {norm_C}")
        if norm_C >= 1:
            print("Условие сходимости не выполняется. Метод может не сойтись.")

        x0 = np.zeros(n)

        solution, num_iterations, errors = simple_iteration_method(C, d, x0, epsilon)

        print(f"Решение: {solution}")
        print(f"Число итераций: {num_iterations}")
        print("Вектор погрешностей на каждой итерации:")
        for i, err in enumerate(errors):
            if i == 0:
                print(f"Итерация {i + 1}: -")
            else:
                print(f"Итерация {i + 1}: {err}")

#define _POSIX_C_SOURCE 200809L
#include <getopt.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

static void usage(void) {
  fprintf(stderr, "Usage: ema_join_hash --size=N [--iters=M]\n");
}

static long long now_ns(void) {
  struct timespec ts;
  clock_gettime(CLOCK_MONOTONIC, &ts);
  return (long long)ts.tv_sec * 1000000000LL + ts.tv_nsec;
}

typedef struct Entry {
  uint64_t key;
  double value;
} Entry;

static inline uint64_t hash64(uint64_t x) {
  x ^= x >> 33;
  x *= 0xff51afd7ed558ccdULL;
  x ^= x >> 33;
  x *= 0xc4ceb9fe1a85ec53ULL;
  x ^= x >> 33;
  return x;
}

int main(int argc, char** argv) {
  long size = 1000000;
  long iters = 5;  // сколько раз прогнать EMA + hash join

  static struct option opts[] = {
      { "size", required_argument, 0, 's'},
      {"iters", required_argument, 0, 'i'},
      {      0,                 0, 0,   0}
  };

  int opt;
  while ((opt = getopt_long(argc, argv, "", opts, NULL)) != -1) {
    switch (opt) {
      case 's':
        size = atol(optarg);
        break;
      case 'i':
        iters = atol(optarg);
        break;
      default:
        usage();
        return 1;
    }
  }

  if (size <= 0 || iters <= 0) {
    usage();
    return 1;
  }

  Entry* table = malloc(size * sizeof(Entry));
  if (!table) {
    perror("malloc");
    return 1;
  }

  // инициализация случайных данных
  srand(42);
  for (long i = 0; i < size; i++) {
    table[i].key = hash64(i);
    table[i].value = (double)rand() / RAND_MAX;
  }

  long long t0 = now_ns();

  double alpha = 0.3;
  double ema = 0.0;
  double acc = 0.0;

  for (long iter = 0; iter < iters; iter++) {
    // обновление EMA (нагрузка на CPU)
    for (long i = 0; i < size; i++) {
      ema = alpha * table[i].value + (1.0 - alpha) * ema;
    }

    // хэш-доступы (нагрузка на память)
    for (long j = 0; j < size; j++) {
      uint64_t key = hash64((uint64_t)(rand() % size));
      long idx = key % size;
      acc += table[idx].value * ema;
    }
  }

  long long t1 = now_ns();
  double sec = (t1 - t0) / 1e9;
  printf(
      "ema-join-hash: size=%ld iters=%ld time=%.3f s result=%.6f\n",
      size,
      iters,
      sec,
      acc
  );

  free(table);
  return 0;
}
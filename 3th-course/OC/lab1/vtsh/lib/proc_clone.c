#define _POSIX_C_SOURCE 200809L
#include <errno.h>
#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>

static void usage(void) {
  fprintf(stderr, "Usage: proc_clone --count=N [--child-us=MICROS]\n");
}

static long now_ns(void) {
  struct timespec ts;
  clock_gettime(CLOCK_MONOTONIC, &ts);
  return (long)ts.tv_sec * 1000000000L + ts.tv_nsec;
}

static void child_busy_wait(long micros) {
  if (micros <= 0)
    return;
  long start = now_ns();
  long target = start + micros * 1000L;
  while (now_ns() < target) { /* busy */
  }
}

int main(int argc, char** argv) {
  long count = 1000;
  long child_us = 0;

  static struct option opts[] = {
      {   "count", required_argument, 0, 'c'},
      {"child-us", required_argument, 0, 'u'},
      {         0,                 0, 0,   0}
  };
  int opt;
  while ((opt = getopt_long(argc, argv, "", opts, NULL)) != -1) {
    switch (opt) {
      case 'c':
        count = atol(optarg);
        break;
      case 'u':
        child_us = atol(optarg);
        break;
      default:
        usage();
        return 1;
    }
  }
  if (count <= 0) {
    usage();
    return 1;
  }

  long t0 = now_ns();

  // Создаём count детей, каждый сразу завершается (_exit(0))
  long started = 0;
  for (long i = 0; i < count; i++) {
    pid_t pid = fork();
    if (pid < 0) {
      perror("fork");
      break;
    }
    if (pid == 0) {
      child_busy_wait(child_us);
      _exit(0);
    }
    started++;
  }

  long reaped = 0;
  int st;
  while (reaped < started) {
    if (wait(&st) > 0)
      reaped++;
    else if (errno == EINTR)
      continue;
    else
      break;
  }

  long t1 = now_ns();
  double sec = (t1 - t0) / 1e9;

  printf(
      "proc-clone: created=%ld reaped=%ld time=%.6f s rate=%.2f procs/s "
      "(child-us=%ld)\n",
      started,
      reaped,
      sec,
      (started > 0 && sec > 0) ? started / sec : 0.0,
      child_us
  );
  return 0;
}
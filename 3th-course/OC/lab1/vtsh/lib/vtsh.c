#define _POSIX_C_SOURCE 200809L
#include "vtsh.h"

#include <fcntl.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>

#define MAX_LINE 1024
#define MAX_ARGS 64

const char* vtsh_prompt(void) {
  return "vtsh> ";
}

static void sigchld_handler(int sig) {
  (void)sig;
  int status;
  while (waitpid(-1, &status, WNOHANG) > 0) {
    fprintf(
        stderr, "[background pid finished, exit=%d]\n", WEXITSTATUS(status)
    );
    fflush(stderr);
  }
}

static int vtsh_run(char** argv) {
  struct timespec start, end;
  clock_gettime(CLOCK_MONOTONIC, &start);

  int background = 0;
  for (int i = 0; argv[i]; ++i) {
    if (strcmp(argv[i], "&") == 0) {
      background = 1;
      argv[i] = NULL;
      break;
    }
  }

  pid_t pid = fork();
  if (pid < 0) {
    perror("fork");
    return -1;
  }

  if (pid == 0) {
    // ---------------- Дочерний процесс ----------------
    // Обработка ./shell
    if (strcmp(argv[0], "./shell") == 0) {
      // Всегда подменяем на наш бинарник
      argv[0] = "../build/bin/vtsh";

      // Помечаем вложенность
      setenv("VTSH_NESTED", "1", 1);

      // Перенаправляем stdin → /dev/null, чтобы не ждал ввода
      int devnull = open("/dev/null", O_RDONLY);
      if (devnull >= 0) {
        dup2(devnull, STDIN_FILENO);
        close(devnull);
      }
    }

    execvp(argv[0], argv);
    printf("Command not found\n");
    fflush(stdout);
    exit(127);
  }

  // ---------------- Родительский ----------------
  if (background) {
    fprintf(stderr, "[started background pid %d]\n", pid);
    return 0;
  }

  int status;
  waitpid(pid, &status, 0);
  clock_gettime(CLOCK_MONOTONIC, &end);

  double elapsed =
      (end.tv_sec - start.tv_sec) + (end.tv_nsec - start.tv_nsec) / 1e9;
  fprintf(stderr, "[exit=%d, time=%.3f s]\n", WEXITSTATUS(status), elapsed);

  return WEXITSTATUS(status);
}

static int vtsh_exec_line(char* line) {
  char* argv[MAX_ARGS];
  int argc = 0;
  char* token = strtok(line, " ");
  while (token && argc < MAX_ARGS - 1) {
    argv[argc++] = token;
    token = strtok(NULL, " ");
  }
  argv[argc] = NULL;

  if (argc == 0)
    return 0;

  // cat без аргументов — читаем stdin
  if (strcmp(argv[0], "cat") == 0 && argv[1] == NULL) {
    char buffer[1024];
    while (fgets(buffer, sizeof(buffer), stdin)) {
      if (strcmp(buffer, "\n") == 0)
        break;
      fputs(buffer, stdout);
      fflush(stdout);
    }
    return 0;
  }

  return vtsh_run(argv);
}

static void vtsh_eval(char* line) {
  char* saveptr;
  char* segment = strtok_r(line, ";", &saveptr);

  while (segment) {
    while (*segment == ' ')
      segment++;

    if (*segment != '\0') {
      char* and_pos = strstr(segment, "&&");
      char* or_pos = strstr(segment, "||");

      if (and_pos && (!or_pos || and_pos < or_pos)) {
        *and_pos = '\0';
        char* second = and_pos + 2;
        int code = vtsh_exec_line(segment);
        if (code == 0)
          vtsh_eval(second);
      } else if (or_pos) {
        *or_pos = '\0';
        char* second = or_pos + 2;
        int code = vtsh_exec_line(segment);
        if (code != 0)
          vtsh_eval(second);
      } else {
        vtsh_exec_line(segment);
      }
    }

    segment = strtok_r(NULL, ";", &saveptr);
  }
}

void vtsh_loop(void) {
  char line[MAX_LINE];
  signal(SIGCHLD, sigchld_handler);

  int interactive = isatty(STDIN_FILENO);
  int nesting = getenv("VTSH_NESTED") != NULL;

  // Принудительно отключаем интерактивность при вложенности
  if (nesting)
    interactive = 0;

  if (!interactive) {
    while (fgets(line, sizeof(line), stdin)) {
      line[strcspn(line, "\n")] = '\0';
      if (line[0] == '\0')
        continue;
      vtsh_eval(line);
    }
    return;
  }

  while (1) {
    printf("%s", vtsh_prompt());
    fflush(stdout);

    if (!fgets(line, sizeof(line), stdin))
      break;
    line[strcspn(line, "\n")] = '\0';

    if (strcmp(line, "exit") == 0)
      break;
    if (line[0] == '\0')
      continue;

    vtsh_eval(line);
  }
}
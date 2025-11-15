add_library(
    libvtsh
    STATIC
    vtsh.c
)

target_include_directories(
    libvtsh
    PUBLIC
    .
)

add_executable(proc_clone proc_clone.c)
target_compile_options(proc_clone PRIVATE -std=c11 -Wall -Wextra -O0)

add_executable(shell_seq shell_seq.c)
target_compile_options(shell_seq PRIVATE -std=c11 -Wall -Wextra -O0)

add_executable(cpu_short_path cpu_short_path.c)
target_compile_options(cpu_short_path PRIVATE -std=c11 -Wall -Wextra -O0 -lm)
target_link_libraries(cpu_short_path PRIVATE m)

add_executable(ema_join_hash ema_join_hash.c)
target_compile_options(ema_join_hash PRIVATE -std=c11 -Wall -Wextra -O0)
target_link_libraries(ema_join_hash PRIVATE m)
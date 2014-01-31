#ifndef _BROKER_INTERNALS_H
#define _BROKER_INTERNALS_H

#include "config.h"

#include <stdio.h>

#pragma GCC visibility push(hidden)
int
_log_msgf(sapo_broker_t *sb, const char *, const char *, int, ...);
int
_log_err(sapo_broker_t *sb, const char *, const char *, int, ...);

#ifndef TEST
    #define log_msgf(sb, msg, ...) _log_msgf(sb, msg, __FILE__, __LINE__, ##__VA_ARGS__)

    #if defined(DEBUG)
     #define log_debug(sb, msg, ...) _log_msgf(sb, msg, __FILE__, __LINE__, ##__VA_ARGS__)
    #else
     #define log_debug(sb, msg, ...) do { } while(0);
    #endif

    #define log_info(sb, msg, ...) _log_msgf(sb, msg, __FILE__, __LINE__, ##__VA_ARGS__)

    #define log_err(sb, msg, ...) _log_err(sb, msg, __FILE__, __LINE__, ##__VA_ARGS__)

#else
    #define log_msgf(sb, msg, ...) do { \
        printf( msg, ##__VA_ARGS__); \
        printf("\n"); \
        fflush(stdout); \
    } while(0);
    #define log_debug(sb, msg, ...) do { \
        printf( msg, ##__VA_ARGS__); \
        printf("\n"); \
        fflush(stdout); \
    } while(0);
    #define log_info(sb, msg, ...) do { \
        printf( msg, ##__VA_ARGS__); \
        printf("\n"); \
        fflush(stdout); \
    } while(0);
    #define log_err(sb, msg, ...) do { \
        fprintf(stderr, msg, ##__VA_ARGS__); \
        fprintf(stderr,"\n"); \
        fflush(stderr); \
    } while(0);
#endif

broker_destination_t
broker_get_destination( sapo_broker_t *sb, const char *dest_name, uint8_t type);

int
broker_resubscribe_destinations( sapo_broker_t *sb, _broker_server_t *srv );

#pragma GCC visibility pop

#endif // _BROKER_INTERNALS_H

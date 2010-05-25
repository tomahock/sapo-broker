#include <stdio.h>
#include <string.h>
#include <stdarg.h>

#include "sapo-broker2.h"
#include "broker_internals.h"


/* strip path from filename,
 * return the filename without the path.
 */
static char *strip_path(const char *fullname)
{
    uint_t i;
    char *dash = (char *) fullname;
    for(i = 0; fullname[i] != '\0'; i++) {
        if(fullname[i] == '/') {
            dash = (char *) &fullname[i+1];
        }
    }
    return dash;
}


/* Send a message to syslog after choosing the logging level.

   The log level must be choosed /manually/ to keep compatibility
   with the old log facility
 */
void log_it(sapo_broker_t *sb, char *msg)
{
    strncpy( sb->last_error_msg, msg, SB_BUFSIZ );
    sb->last_error_msg[SB_BUFSIZ-1] = '\0';
}

/* Wrapper function to old log facility */
int _log_msgf(sapo_broker_t *sb, const char *msg, const char *file, int lineno, ...)
{
    char log_msg[SB_BUFSIZ];
    char log_msg2[SB_BUFSIZ];
    char *fname = strip_path(file);
    va_list ap;
    va_start(ap, lineno);
    vsnprintf(log_msg, SB_BUFSIZ, msg, ap);
    va_end(ap);
    // add file:number information to the log
    snprintf(log_msg2, SB_BUFSIZ, "[%s:%d] %s", fname, lineno, log_msg);
    log_it(sb, log_msg2 );
    return 0;
}

/* Wrapper function to old log facility */
int _log_err(sapo_broker_t *sb, const char *msg, const char *file, int lineno, ...)
{
    char log_msg[SB_BUFSIZ];
    char log_msg2[SB_BUFSIZ];
    char *fname = strip_path(file);
    va_list ap;
    va_start(ap, lineno);
    vsnprintf(log_msg, SB_BUFSIZ, msg, ap);
    va_end(ap);
    // add file:number ERROR() information to the log
    snprintf(log_msg2,
            SB_BUFSIZ,
            "[%s:%d] %s",
            fname,
            lineno,
            log_msg);
    log_it( sb, log_msg2);
    /* allways log errors to stderr */
    fprintf(stderr, "%s\n", log_msg2);
    return 0;
}


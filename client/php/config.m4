PHP_ARG_ENABLE(sapobroker, whether to enable sapobroker support,
[  --enable-sapobroker           Enable sapobroker support])


AC_CHECK_HEADERS([sapo-broker2.h], [], [
  AC_MSG_ERROR([unable to find sapo-broker2.h])
])




PHP_SUBST(SAPOBROKER_SHARED_LIBADD)
PHP_ADD_LIBRARY(sapo-broker2, 1, SAPOBROKER_SHARED_LIBADD)
PHP_NEW_EXTENSION(sapobroker, sapobroker.c, $ext_shared)

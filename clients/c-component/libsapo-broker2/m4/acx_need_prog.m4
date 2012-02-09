AC_DEFUN([ACX_NEED_PROG],
[
AC_PATH_PROG($1, $2)
if test -z "$$1"; then
    AC_MSG_ERROR([$2 is required but wasn't found in PATH "$PATH"])
fi
])

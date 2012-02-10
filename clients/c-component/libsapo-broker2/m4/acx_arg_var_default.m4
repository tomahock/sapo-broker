AC_DEFUN([ACX_ARG_VAR_DEFAULT],
[
AC_ARG_VAR($1, $2)

if test -z "$[$1]"; then
	AC_MSG_NOTICE([Using default value "$3" for $1])
	[$1]="$3";
fi
])

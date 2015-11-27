parm1 = %1%

MsgBox, % parm1

StringReplace, x, parm1, $LF, `n, All

MsgBox, % x


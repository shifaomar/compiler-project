* C- compilation to TM code
* standard prelude
  0:    LD 6, 0(0)	load gp with maxaddress
  1:   LDA 5, 0(6)	copy gp to fp
  2:    ST 0, 0(0)	clear location 0
* code for input routine
  4:    ST 0, -1(5)	store return
  5:    IN 0, 0, 0	input
  6:    LD 7, -1(5)	return to caller
* code for output routine
  7:    ST 0, -1(5)	store return
  8:    LD 0, -2(5)	load output value
  9:   OUT 0, 0, 0	output
 10:    LD 7, -1(5)	return to caller
  3:   LDA 7, 7(7)	jump around i/o routines
* runtime error: array index below range
 12:   LDC 0, -1000000(0)	out of range below
 13:   OUT 0, 0, 0	print runtime error
 14:  HALT 0, 0, 0	halt
* runtime error: array index above range
 15:   LDC 0, -2000000(0)	out of range above
 16:   OUT 0, 0, 0	print runtime error
 17:  HALT 0, 0, 0	halt
 11:   LDA 7, 6(7)	jump around runtime handlers
* function main
 19:    ST 0, -1(5)	store return
 20:    ST 5, -4(5)	push ofp
 21:   LDA 5, -4(5)	push frame
 22:   LDA 0, 1(7)	load ac with ret ptr
 23:   LDA 7, -20(7)	jump to input
 24:    LD 5, 0(5)	pop frame
 25:    ST 0, -3(5)	store x
 26:   LDC 0, 0(0)	load const
 27:    ST 0, -4(5)	store sum
 28:    LD 0, -3(5)	load x
 29:    ST 0, -5(5)	save left operand
 30:   LDC 0, 0(0)	load const
 31:    LD 1, -5(5)	load left operand
 32:   SUB 0, 1, 0	left - right
 34:   LDC 0, 0(0)	cmp false
 36:   LDC 0, 1(0)	cmp true
 35:   LDA 7, 1(7)	cmp merge
 33:   JGT 0, 2(7)	gt
 38:    LD 0, -4(5)	load sum
 39:    ST 0, -5(5)	save left operand
 40:    LD 0, -3(5)	load x
 41:    LD 1, -5(5)	load left operand
 42:   ADD 0, 1, 0	op +
 43:    ST 0, -4(5)	store sum
 44:    LD 0, -3(5)	load x
 45:    ST 0, -5(5)	save left operand
 46:   LDC 0, 1(0)	load const
 47:    LD 1, -5(5)	load left operand
 48:   SUB 0, 1, 0	op -
 49:    ST 0, -3(5)	store x
 50:   LDA 7, -23(7)	while: loop
 37:   JEQ 0, 13(7)	while: exit if false
 51:    LD 0, -4(5)	load sum
 52:    ST 0, -2(5)	output arg
 53:    ST 5, -4(5)	push ofp
 54:   LDA 5, -4(5)	push frame
 55:   LDA 0, 1(7)	load ac with ret ptr
 56:   LDA 7, -50(7)	jump to output
 57:    LD 5, 0(5)	pop frame
 58:    LD 7, -1(5)	return to caller
 18:   LDA 7, 40(7)	jump over function bodies to finale
* finale
 59:    ST 5, -1(5)	push ofp
 60:   LDA 5, -1(5)	push frame
 61:   LDA 0, 1(7)	load ac with return pointer
 62:   LDA 7, -44(7)	jump to main
 63:    LD 5, 0(5)	pop frame
 64:  HALT 0, 0, 0	done

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
* function main
 11:    ST 0, -1(5)	store return
 12:    LD 0, -1(6)	load variable x
 13:    ST 0, -3(5)	save left operand
 14:   LDC 0, 3(0)	load const
 15:    LD 1, -3(5)	load left operand
 16:   ADD 0, 1, 0	op +
 17:    ST 0, -1(6)	assign to x
 18:    LD 7, -1(5)	return to caller
* finale
 19:    ST 5, -1(5)	push ofp
 20:   LDA 5, -1(5)	push frame
 21:   LDA 0, 1(7)	load ac with return pointer
 22:   LDA 7, -12(7)	jump to main
 23:    LD 5, 0(5)	pop frame
 24:  HALT 0, 0, 0	done

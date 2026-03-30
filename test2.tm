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
 12:    ST 0, -1(5)	store return
 13:   LDC 0, 3(0)	load const
 14:    ST 0, -1(6)	store global x
 15:    LD 7, -1(5)	return to caller
 11:   LDA 7, 4(7)	jump over function bodies to finale
* finale
 16:    ST 5, -1(5)	push ofp
 17:   LDA 5, -1(5)	push frame
 18:   LDA 0, 1(7)	load ac with return pointer
 19:   LDA 7, -8(7)	jump to main
 20:    LD 5, 0(5)	pop frame
 21:  HALT 0, 0, 0	done

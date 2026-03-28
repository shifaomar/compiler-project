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
 12:   LDC 0, 3(0)	load const
 13:    LD 7, -1(5)	return to caller
* finale
 14:    ST 5, 0(5)	push ofp
 15:   LDA 5, 0(5)	push frame
 16:   LDA 0, 1(7)	load ac with return pointer
 17:   LDA 7, -7(7)	jump to main
 18:    LD 5, 0(5)	pop frame
 19:  HALT 0, 0, 0	done

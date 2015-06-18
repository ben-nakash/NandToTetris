(START_INFINITE_LOOP)
// Initializing the SCREEN pointer
@SCREEN
D=A
@P
M=D


@KBD
D=M
@START_REVERSE
D;JNE
@rows
M=0
@colums
M=0

//**********************
//		LINES 0 - 128
//**********************
	(START_WHILE0)
	@127
	D=A
	@rows
	D=M-D
	@END_WHILE0
	D;JGT
		
		@colums
		M=0
		
		(START_WHILE01)
		@15
		D=A
		@colums
		D=M-D
		@END_WHILE01
		D;JEQ
		
		A=M
		@P
		A=M
		M=-1
		@P
		M=M+1

		@colums
		M=M+1
		@START_WHILE01
		0;JMP
		(END_WHILE01)
		//----------------------
		(START_WHILE02)
		@31
		D=A
		@colums
		D=M-D
		@END_WHILE02
		D;JGT
		
		A=M
		@P
		A=M
		M=0
		@P
		M=M+1

		@colums
		M=M+1
		@START_WHILE02
		0;JMP
		(END_WHILE02)
		
	@rows
	M=M+1
	@START_WHILE0
	0;JMP
	(END_WHILE0)

//**********************
//		LINES 128-255
//**********************

	(START_WHILE1)
	@255
	D=A
	@rows
	D=M-D
	@END_WHILE1
	D;JGT
		
		@colums
		M=0
		
		(START_WHILE11)
		@15
		D=A
		@colums
		D=M-D
		@END_WHILE11
		D;JEQ
		
		A=M
		@P
		A=M
		M=0
		@P
		M=M+1

		@colums
		M=M+1
		@START_WHILE11
		0;JMP
		(END_WHILE11)
		//----------------------
		(START_WHILE12)
		@31
		D=A
		@colums
		D=M-D
		@END_WHILE12
		D;JGT
		
		A=M
		@P
		A=M
		M=-1
		@P
		M=M+1

		@colums
		M=M+1
		@START_WHILE12
		0;JMP
		(END_WHILE12)
		
	@rows
	M=M+1
	@START_WHILE1
	0;JMP
	(END_WHILE1)
	
@START_INFINITE_LOOP
0;JMP

//--------------------------------
//    REVERSE PRINTING
//--------------------------------

(START_REVERSE)
// Initializing the SCREEN pointer
@SCREEN
D=A
@P
M=D


@KBD
D=M
@START_INFINITE_LOOP
D;JEQ
@rows
M=0
@colums
M=0

//**********************
//		LINES 0 - 128
//**********************
	(START_WHILE_R0)
	@127
	D=A
	@rows
	D=M-D
	@END_WHILE_R0
	D;JGT
		
		@colums
		M=0
		
		(START_WHILE_R01)
		@15
		D=A
		@colums
		D=M-D
		@END_WHILE_R01
		D;JEQ
		
		A=M
		@P
		A=M
		M=0
		@P
		M=M+1

		@colums
		M=M+1
		@START_WHILE_R01
		0;JMP
		(END_WHILE_R01)
		//----------------------
		(START_WHILE_R02)
		@31
		D=A
		@colums
		D=M-D
		@END_WHILE_R02
		D;JGT
		
		A=M
		@P
		A=M
		M=-1
		@P
		M=M+1

		@colums
		M=M+1
		@START_WHILE_R02
		0;JMP
		(END_WHILE_R02)
		
	@rows
	M=M+1
	@START_WHILE_R0
	0;JMP
	(END_WHILE_R0)

//**********************
//		LINES 128-255
//**********************

	(START_WHILE_R1)
	@255
	D=A
	@rows
	D=M-D
	@END_WHILE_R1
	D;JGT
		
		@colums
		M=0
		
		(START_WHILE_R11)
		@15
		D=A
		@colums
		D=M-D
		@END_WHILE_R11
		D;JEQ
		
		A=M
		@P
		A=M
		M=-1
		@P
		M=M+1

		@colums
		M=M+1
		@START_WHILE_R11
		0;JMP
		(END_WHILE_R11)
		//----------------------
		(START_WHILE_R12)
		@31
		D=A
		@colums
		D=M-D
		@END_WHILE_R12
		D;JGT
		
		A=M
		@P
		A=M
		M=0
		@P
		M=M+1

		@colums
		M=M+1
		@START_WHILE_R12
		0;JMP
		(END_WHILE_R12)
		
	@rows
	M=M+1
	@START_WHILE_R1
	0;JMP
	(END_WHILE_R1)
	
@START_REVERSE
0;JMP
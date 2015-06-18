@i
M=0

//while(i-14<0)
(START_LOOP0)
@14
D=A
@i
D=M-D
@END_LOOP0
D;JGT

@j
M=0;
//while(j-14<0)
(START_LOOP1)
@14
D=A
@j
D=M-D
@END_LOOP1
D;JGT

@j 		// J->0
A=M 	//A<-ram[j] = ram[0]
D=M		
@temp 	// temp=a[j]
M=D
@j
M=M+1
A=M
D=M			
//D=a[j+1]

			// if(a[j]-a[j+1]<0)
@temp
D = M-D
@IF_END
D;JGT

(IF_START)
@j
A=M
D=M
@j
M=M-1
A=M
M=D 

@temp
D=M
@j
M=M+1
A=M
M=D
(IF_END)
@START_LOOP1
0;JMP
(END_LOOP1)
@i
M=M+1
@START_LOOP0
0;JMP
(END_LOOP0)
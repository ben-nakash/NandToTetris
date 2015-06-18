import java.io.BufferedWriter;
import java.io.IOException;


public class CodeWriter 
{
	private VMcode vmc;
	private BufferedWriter bw;
	private String line[];
	private int arithmeticsCounter=1;
	private String fileName, funcName="";
	
	public CodeWriter(VMcode vmCode, BufferedWriter buffW, String fName)
	{
		vmc = vmCode;
		bw = buffW;
		fileName = fName;
	}
	
	public void convertToAssembly() throws IOException
	{
		while (vmc.hasMoreCommands()) 
		{
			line = vmc.nextCommand().split(" ");
			switch(line[0]) {
			case "push":		pushHandler();			break;
			case "pop":			popHandler();			break;
			case "label":		labelHandler();			break;
			case "goto":		gotoHandler();			break;
			case "if-goto": 	ifGoToHandler();		break;
			case "call":		callHandler();			break;
			case "function":	functionHandler();		break;
			case "return":		returnHandler();		break;
			default:			arithmeticHandler();	break;
			}
			vmc.advance();
		}
	}


	private void pushHandler() throws IOException 
	{
		int num = Integer.parseInt(line[2]);
		switch(line[1]) {
		case "constant":
			bw.write("@"+num+"\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "local":
			bw.write("@"+num+"\nD=A\n@LCL\nA=M+D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "argument":
			bw.write("@"+num+"\nD=A\n@ARG\nA=M+D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "this":
			bw.write("@"+num+"\nD=A\n@THIS\nA=M+D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "that":
			bw.write("@"+num+"\nD=A\n@THAT\nA=M+D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "temp":
			if (num >= 8)
				System.out.println("Error: illegal value in 'push temp' (Bigger then 8).");
			bw.write("@"+(num+5)+"\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "pointer":
			bw.write("@"+(num+3)+"\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "static":
			bw.write("@"+fileName+"."+num+"\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "nothing":
			bw.write("@"+num+"\nD=A\n@SP\nM=M+D\n");
			break;
		}
	}
	
	private void popHandler() throws IOException
	{
		int num = Integer.parseInt(line[2]);
		
		switch(line[1]) {
		case "local":
			bw.write("@"+num+"\nD=A\n@LCL\nA=M\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "argument":
			bw.write("@"+num+"\nD=A\n@ARG\nA=M\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "this":
			bw.write("@"+num+"\nD=A\n@THIS\nA=M\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "that":
			bw.write("@"+num+"\nD=A\n@THAT\nA=M\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "temp":
			if (num >= 8)
				System.out.println("Error: illegal value in 'pop temp'.");
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@"+(num+5)+"\nM=D\n");
			break;
		case "pointer":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@"+(num+3)+"\nM=D\n");
			break;
		case "static":
			bw.write("@"+fileName+"."+num+"\nD=A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "nothing":
			bw.write("@"+num+"\nD=A\n@SP\nM=M-D\n");
			break;
		}
	}
	
	private void arithmeticHandler() throws IOException
	{
		switch(line[0]) {
		case "add":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=M+D\n@SP\nM=M+1\n");
			break;
		case "sub":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=M-D\n@SP\nM=M+1\n");
			break;
		case "eq":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=D-M\n@isTrue"+arithmeticsCounter+"\nD;JEQ\n@isFalse"+arithmeticsCounter+"\n0;JMP\n(isFalse"+arithmeticsCounter+")\n@SP\nA=M\nM=0\n@continue"+arithmeticsCounter+"\n0;JMP\n(isTrue"+arithmeticsCounter+")\n@SP\nA=M\nM=-1\n(continue"+arithmeticsCounter+")\n@SP\nM=M+1\n");
			arithmeticsCounter++;
			break;
		case "not":
			bw.write("@SP\nM=M-1\nA=M\nM=!M\n@SP\nM=M+1\n");
			break;
		case "neg":
			bw.write("@SP\nM=M-1\nA=M\nM=-M\n@SP\nM=M+1\n");
			break; 
		case "lt":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@isTrue"+arithmeticsCounter+"\nD;JLT\n@isFalse"+arithmeticsCounter+"\n0;JMP\n(isFalse"+arithmeticsCounter+")\n@SP\nA=M\nM=0\n@continue"+arithmeticsCounter+"\n0;JMP\n(isTrue"+arithmeticsCounter+")\n@SP\nA=M\nM=-1\n(continue"+arithmeticsCounter+")\n@SP\nM=M+1\n");
			arithmeticsCounter++;
			break;
		case "gt":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@isTrue"+arithmeticsCounter+"\nD;JGT\n@isFalse"+arithmeticsCounter+"\n0;JMP\n(isFalse"+arithmeticsCounter+")\n@SP\nA=M\nM=0\n@continue"+arithmeticsCounter+"\n0;JMP\n(isTrue"+arithmeticsCounter+")\n@SP\nA=M\nM=-1\n(continue"+arithmeticsCounter+")\n@SP\nM=M+1\n");
			arithmeticsCounter++;
			break;
		case "and":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M&D\nM=D\n@SP\nM=M+1\n");
			break;
		case "or":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M|D\nM=D\n@SP\nM=M+1\n");
			break;
		case "mult2":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@temporary\nM=D\nD=D+M\n@SP\nA=M\nM=D\n");
			break;
		}
	}

	
	private void labelHandler() throws IOException
	{
		if (funcName.equals(""))
			bw.write("("+line[1]+")\n");
		else
			bw.write("("+funcName+"$"+line[1]+")\n");
	}

	private void gotoHandler() throws IOException
	{
		if (funcName.equals(""))
			bw.write("@"+line[1]+"\n");
		else if (!funcName.equals("Sys.init") || line[1].equals("WHILE"))
			bw.write("@"+funcName+"$"+line[1]+"\n");
		else
			bw.write("@"+line[1]+"$"+line[1]+"\n");
		bw.write("0;JMP\n");
	}

	private void ifGoToHandler() throws IOException
	{
		bw.write("@SP\nM=M-1\nA=M\nD=M\n@"+funcName+"$"+line[1]+"\nD;JNE\n");
	}

	
	private void callHandler() throws IOException
	{
		int num = Integer.parseInt(line[2]);
		// PUSH return-address
		bw.write("@return_address_"+line[1]+"$"+VMtranslator.funcCounter+"\n");
		bw.write("D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
		
		// Push LCL
		bw.write("@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

		// Push ARG
		bw.write("@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

		// Push THIS
		bw.write("@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

		// Push THAT
		bw.write("@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

		// ARG = SP-m-5
		bw.write("@"+(num+5)+"\nD=A\n@SP\nD=M-D\n@ARG\nM=D\n");
		
		// LCL = SP
		bw.write("@SP\nD=M\n@LCL\nM=D\n");

		// goto f
		gotoHandler();
		
		// (return-address)
		bw.write("(return_address_"+line[1]+"$"+VMtranslator.funcCounter+")\n");

		VMtranslator.funcCounter++;
	}

	private void functionHandler() throws IOException
	{
		funcName = line[1];
		int num = Integer.parseInt(line[2]);
		bw.write("("+line[1]+"$"+line[1]+")\n");
		for (int i=0 ; i<num ; i++)
			bw.write("@0\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
	}

	private void returnHandler() throws IOException
	{
		// FRAME = LCL	
		bw.write("@LCL\nD=M\n@FRAME\nM=D\n");
		
		// RET = *(FRAME-5)	
		bw.write("@5\nD=A\n@FRAME\nA=M-D\nD=M\n@RET\nM=D\n");
		
		// *ARG = pop()	
		bw.write("@SP\nM=M-1\nA=M\nD=M\n@ARG\nA=M\nM=D\n");

		// SP = ARG+1	
		bw.write("@ARG\nD=M+1\n@SP\nM=D\n");

		// THAT = *(FRAME-1)
		bw.write("@FRAME\nM=M-1\nA=M\nD=M\n@THAT\nM=D\n");

		// THIS = *(FRAME-2)
		bw.write("@FRAME\nM=M-1\nA=M\nD=M\n@THIS\nM=D\n");

		// ARG = *(FRAME-3)	
		bw.write("@FRAME\nM=M-1\nA=M\nD=M\n@ARG\nM=D\n");

		// LCL = *(FRAME-4)
		bw.write("@FRAME\nM=M-1\nA=M\nD=M\n@LCL\nM=D\n");

		// goto RET	
		bw.write("@RET\nA=M\n0;JMP\n");
	}
}

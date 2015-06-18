import java.io.BufferedWriter;
import java.io.IOException;


public class CodeWriter 
{
	private VMcode vmc;
	private BufferedWriter bw;
	private String line[];
	private int counter=0;
	private String fileName;
	
	public CodeWriter(VMcode vmCode, BufferedWriter buffW, String fName)
	{
		vmc = vmCode;
		bw = buffW;
		fileName = fName;
	}
	
	public void converToAssembly() throws IOException
	{
		while (vmc.hasMoreCommands()) {
			line = vmc.nextCommand().split(" ");
			if(line[0].equals("push"))
				pushHandler();
			else if (line[0].equals("pop"))
				popHandler();
			else
				arithmeticHandler();
			vmc.advance();
		}
	}

	private void pushHandler() throws IOException 
	{
		switch(line[1]) {
		case "constant":
			bw.write("@"+line[2]+"\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\nA=M\n");
			break;
		case "local":
			bw.write("@"+line[2]+"\nD=A\n@LCL\nA=D+M\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "argument":
			bw.write("@"+line[2]+"\nD=A\n@ARG\nA=D+M\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "this":
			bw.write("@"+line[2]+"\nD=A\n@THIS\nA=D+M\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "that":
			bw.write("@"+line[2]+"\nD=A\n@THAT\nA=D+M\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "temp":
			if (Integer.parseInt(line[2]) >= 8)
				System.out.println("Error: illegal value in 'push temp'.");
			bw.write("@"+line[2]+"\nD=A\n@5\nD=D+A\nA=D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "pointer":
			bw.write("@"+line[2]+"\nD=A\n@3\nD=D+A\nA=D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "static":
			bw.write("@"+fileName+"."+line[2]+"\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
			break;
		case "nothing":
			bw.write("@"+line[2]+"\nD=A\n@SP\nM=M+D\n");
			break;
		}
	}
	
	private void popHandler() throws IOException
	{
		switch(line[1]) {
		case "local":
			bw.write("@"+line[2]+"\nD=A\n@LCL\nA=M\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "argument":
			bw.write("@"+line[2]+"\nD=A\n@ARG\nA=M\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "this":
			bw.write("@"+line[2]+"\nD=A\n@THIS\nA=M\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "that":
			bw.write("@"+line[2]+"\nD=A\n@THAT\nA=M\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "temp":
			if (Integer.parseInt(line[2]) >= 8)
				System.out.println("Error: illegal value in 'pop temp'.");
			bw.write("@"+line[2]+"\nD=A\n@5\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "pointer":
			bw.write("@"+line[2]+"\nD=A\n@3\nD=D+A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "static":
			bw.write("@"+fileName+"."+line[2]+"\nD=A\n@temporary\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@temporary\nA=M\nM=D\n");
			break;
		case "nothing":
			bw.write("@"+line[2]+"\nD=A\n@SP\nM=M-D\n");
			break;
		}
	}
	
	private void arithmeticHandler() throws IOException
	{
		switch(line[0]) {
		case "add":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M+D\nM=D\n@SP\nM=M+1\nA=M\nM=0\n");
			break;
		case "sub":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\nM=D\n@SP\nM=M+1\nA=M\nM=0\n");
			break;
		case "eq":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=D-M\n@isTrue"+counter+"\nD;JEQ\n@isFalse"+counter+"\n0;JMP\n(isFalse"+counter+")\n@SP\nA=M\nM=0\n@continue"+counter+"\n0;JMP\n(isTrue"+counter+")\n@SP\nA=M\nM=-1\n(continue"+counter+")\n@SP\nM=M+1\n");
			counter++;
			break;
		case "not":
			bw.write("@SP\nM=M-1\nA=M\nM=!M\n@SP\nM=M+1\n");
			break;
		case "neg":
			bw.write("@SP\nM=M-1\nA=M\nM=-M\n@SP\nM=M+1\n");
			break; 
		case "lt":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@isTrue"+counter+"\nD;JLT\n@isFalse"+counter+"\n0;JMP\n(isFalse"+counter+")\n@SP\nA=M\nM=0\n@continue"+counter+"\n0;JMP\n(isTrue"+counter+")\n@SP\nA=M\nM=-1\n(continue"+counter+")\n@SP\nM=M+1\n");
			counter++;
			break;
		case "gt":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@isTrue"+counter+"\nD;JGT\n@isFalse"+counter+"\n0;JMP\n(isFalse"+counter+")\n@SP\nA=M\nM=0\n@continue"+counter+"\n0;JMP\n(isTrue"+counter+")\n@SP\nA=M\nM=-1\n(continue"+counter+")\n@SP\nM=M+1\n");
			counter++;
			break;
		case "and":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M&D\nM=D\n@SP\nM=M+1\nA=M\nM=0\n");
			break;
		case "or":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M|D\nM=D\n@SP\nM=M+1\nA=M\nM=0\n");
			break;
		case "mult2":
			bw.write("@SP\nM=M-1\nA=M\nD=M\n@temporary\nM=D\nD=D+M\n@SP\nA=M\nM=D\n");
			break;
		}
	}
}

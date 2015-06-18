import java.io.BufferedWriter;
import java.io.IOException;


public class Code 
{
	private BufferedWriter bw;
	private ASMcode ac;
	private SymbolTable sTable;
	private int labelCounter=0, ramCounter=16;
	private String line;
	
	public Code(ASMcode asmCode, BufferedWriter bWriter)
	{
		ac = asmCode;
		bw = bWriter;
		sTable = new SymbolTable();
	}
	
	public void transformToBinary() throws IOException
	{
		initializeTable();

		while(ac.hasMoreCommands()) 
		{
			line = ac.nextCommand();
			line = line.trim();
			if (line.contains("@")) {
				A_Instruction_Handler();
				labelCounter++;
			}
			else if (line.contains("=") || line.contains(";")) {
				C_Instruction_Handler();
			}
			ac.advance();
		}
	}


	private void initializeTable() 
	{
		while (ac.hasMoreCommands())
		{
			line = ac.nextCommand();
			line = line.trim();
			
			if (line.contains("(") && !sTable.containValue(line)) 
			{
				
				line = line.substring(1, line.length()-1);
				line = line.trim();
				sTable.addEntry(line, labelCounter);
				line = line.trim();

				ac.advance();
				continue;
			}
			labelCounter++;
			ac.advance();
		}
		ac.resetIndex();
		
		while (ac.hasMoreCommands())
		{
			line=ac.nextCommand();
			line = line.trim();
			if (line.contains("@")) 
			{
				line = line.substring(1, line.length());
				if (!isInteger(line) && !sTable.containValue(line)) 
				{
					sTable.addEntry(line, ramCounter);
					ramCounter++;
				}
			}
			ac.advance();
		}
		ac.resetIndex();
	}

	private void C_Instruction_Handler() throws IOException
	{
		String[] instruction;
		if (line.contains("="))
			instruction = line.split("=");
		else
			instruction = line.split(";");
		
		compHandler(instruction);
		destHandler(instruction);
		jumpHandler(instruction);
		bw.write("\n");
	}
	
	
	private void compHandler(String[] instruction) throws IOException 
	{
		int index;
		if (line.contains("="))
			index = 1;
		else
			index = 0;
		
		if (instruction[index].startsWith("$")) {
			bw.write("101");
			instruction[index] = instruction[index].substring(1, instruction[index].length());
		}
		else
			bw.write("111");
		
		switch (instruction[index])
		{
		case "0":		bw.write("0101010");		break;
		case "1":		bw.write("0111111");		break;
		case "-1":		bw.write("0111010");		break;
		case "D":		bw.write("0001100");		break;
		case "A":		bw.write("0110000");		break;
		case "!D":		bw.write("0001101");		break;
		case "!A":		bw.write("0110001");		break;
		case "-D":		bw.write("0001111");		break;
		case "-A":		bw.write("0110011");		break;
		case "D+1":		bw.write("0011111");		break;
		case "1+D":		bw.write("0011111");		break;
		case "A+1":		bw.write("0110111");		break;
		case "1+A":		bw.write("0110111");		break;
		case "D-1":		bw.write("0001110");		break;
		case "A-1":		bw.write("0110010");		break;
		case "D+A":		bw.write("0000010");		break;
		case "A+D":		bw.write("0000010");		break;
		case "D-A":		bw.write("0010011");		break;
		case "A-D":		bw.write("0000111");		break;
		case "D&A":		bw.write("0000000");		break;
		case "A&D":		bw.write("0000000");		break;
		case "D|A":		bw.write("0010101");		break;
		case "A|D":		bw.write("0010101");		break;
		case "M":		bw.write("1110000");		break;
		case "!M":		bw.write("1110001");		break;
		case "-M":		bw.write("1110011");		break;
		case "M+1":		bw.write("1110111");		break;
		case "1+M":		bw.write("1110111");		break;
		case "M-1":		bw.write("1110010");		break;
		case "D+M":		bw.write("1000010");		break;
		case "M+D":		bw.write("1000010");		break;
		case "D-M":		bw.write("1010011");		break;
		case "M-D":		bw.write("1000111");		break;
		case "D&M":		bw.write("1000000");		break;
		case "M&D":		bw.write("1000000");		break;
		case "D|M":		bw.write("1010101");		break;
		case "M|D":		bw.write("1010101");		break;
		default:		break;
		}
	}
	
	private void destHandler(String[] instruction) throws IOException 
	{
		if (line.contains("=")) 
		{
			switch(instruction[0]) 
			{
			case "M":		bw.write("001");	break;
			case "D":		bw.write("010");	break;
			case "MD":		bw.write("011");	break;
			case "A":		bw.write("100");	break;
			case "AM":		bw.write("101");	break;
			case "AD":		bw.write("110");	break;
			case "AMD":		bw.write("111");	break;
			default:break;
			}
		}
		else
			bw.write("000");
	}
	
	private void jumpHandler(String[] instruction) throws IOException 
	{
		if (line.contains(";"))
		{
			switch(instruction[1])
			{
			case "JGT":		bw.write("001");	break;
			case "JEQ":		bw.write("010");	break;
			case "JGE":		bw.write("011");	break;
			case "JLT":		bw.write("100");	break;
			case "JNE":		bw.write("101");	break;
			case "JLE":		bw.write("110");	break;
			case "JMP":		bw.write("111");	break;
			default:		break;
			}
		}
		else
			bw.write("000");
	}

	private void A_Instruction_Handler() throws IOException 
	{
		String substring = line.substring(1);
		
		if (isInteger(substring)) 
		{
			// is Integer
			int num = Integer.parseInt(substring);
			String binaryCode = Integer.toBinaryString(num);
			int length = binaryCode.length(); 
			while (length < 16) {
				bw.write("0");
				length++;
			}
			bw.write(binaryCode+"\n");
		}
		else {
			// not an Integer
			if (sTable.containValue(substring)) 
			{
				// Value exists within table
				int num = sTable.getAddress(substring);
				String binaryCode = Integer.toBinaryString(num);
				int length = binaryCode.length(); 
				while (length < 16) {
					bw.write("0");
					length++;
				}
				bw.write(binaryCode+"\n");
			}
			else {
				// Value doesn't exist within table
				sTable.addEntry(substring, ramCounter);
				ramCounter++;
			}
		}
		
	}
	
	public boolean isInteger(String str) 
	{
	    try { 
	    	Integer.parseInt(str); 
	    } 
	    catch(Exception e) { 
	        return false; 
	    } 
	    return true;
	}
}

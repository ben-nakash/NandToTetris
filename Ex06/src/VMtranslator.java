import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;



public class VMtranslator {

	private static String[] arg;
	private static BufferedWriter bw;
	private static String fName;
	public  static int funcCounter=0;

	/**
	 * usage: VMtranslator <file> or VMtranslator <dir>
	 * @throws IOException 
	 * @throws MyException 
	 */
	public static void main(String[] args) throws IOException, Exception 
	{
		File myfile = new File(args[0]);
		FileWriter fw;
		arg=args;
		if(myfile.isDirectory())
		{
			fw=new FileWriter(new File(args[0]+".asm"));
			bw = new BufferedWriter(fw);
			if (!(arg.length>1) || !arg[1].contains("-noinit")) {
			   bw.write("@256\nD=A\n@SP\nM=D\n");
			   callHandler();
			}
			TranslateDir(myfile);
		}
		else
		{
			if(args[0].substring(args[0].length()-3).matches("\\.vm"))
			{
				fw=new FileWriter(new File(args[0].substring(0, args[0].length()-3)+".asm"));
				bw = new BufferedWriter(fw);
				if (!(arg.length>1) || !arg[1].contains("-noinit")) {
					   bw.write("@256\nD=A\n@SP\nM=D\n");
					   callHandler();
				}
				TranslateFile(myfile);
			}
			else{
				throw new Exception("not a vm file: " + args[0]);
			}
		}
		bw.close();
		fw.close();		
	}
		


/**
* handles a single vm file
*/
    private static void TranslateFile(File filename) throws IOException, Exception
    {
		   VMcode vmc = new VMcode(filename.getPath());	
		   fName = filename.getName().substring(0, filename.getName().length()-3);
		   CodeWriter cw = new CodeWriter(vmc, bw, fName);
		   cw.convertToAssembly();
	}
	
/**
* handles a directory of vm files
*/
	private static void TranslateDir(File folder) throws IOException, Exception
	{
		File[] listOfFiles = folder.listFiles(); 
		String filename,ext;
		for(int i=0;i<listOfFiles.length;i++)
		{
			filename=listOfFiles[i].getName();
			ext = filename.substring(filename.length()-3);			
			if (ext.matches(".vm$")){	
				TranslateFile(listOfFiles[i]);			
			}
		}
	}
	
	private static void callHandler() throws IOException
	{
		// PUSH return-address
		bw.write("@return-address0\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
		// Push LCL
		bw.write("@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
		// Push ARG
		bw.write("@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
		// Push THIS
		bw.write("@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
		// Push THAT
		bw.write("@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
		// ARG = SP-m-5
		bw.write("@5\nD=A\n@SP\nD=M-D\n@ARG\nM=D\n");
		// LCL = SP
		bw.write("@SP\nD=M\n@LCL\nM=D\n");
		// goto f
		bw.write("@Sys.init$Sys.init\n");
		bw.write("0;JMP\n");
		// (return-address)
		bw.write("(return-address0)\n");
	}
}

class VMcode
{
		private String [] arr;
		private int  curr_index;

		public VMcode(String filename) throws IOException, Exception
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line,str="";
			while ((line = br.readLine()) != null) 
			{
				curr_index=line.indexOf("//");
				if(curr_index!=-1){
					line=line.substring(0, curr_index);
				}			
				if(!line.matches("^\\s*$")){			
					str=str.concat(line + "\n");
				}
			}			
			arr = str.split("\n");	
			br.close();
			curr_index=0;		
		}
		
		public boolean hasMoreCommands(){
			return (arr.length>(curr_index));
		}
		
		public void advance(){
			curr_index++;
		}
		
		public String nextCommand(){
			return arr[curr_index];
		}
	}


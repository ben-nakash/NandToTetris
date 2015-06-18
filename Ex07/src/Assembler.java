import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;



public class Assembler 
{
	/**
	 * usage: Assembler <file> 
	 * @throws IOException 
	 * @throws MyException 
	 */
	public static void main(String[] args) throws IOException, Exception {
		File myfile = new File(args[0]);
		FileWriter fw;
		BufferedWriter bw;
		if(args[0].substring(args[0].length()-4).matches("\\.asm")){
			fw=new FileWriter(new File(args[0].substring(0, args[0].length()-4)+".hack"));
			bw = new BufferedWriter(fw);
			TranslateFile(myfile,bw);
		}
		else{
			throw new Exception("not an asm file: " + args[0]);
		}
		bw.close();
		fw.close();		
	}

	
	// handles a single vm file
    private static void TranslateFile(File filename,BufferedWriter bw) throws IOException, Exception
    {
		   ASMcode ac = new ASMcode(filename.getPath());
		   Code code = new Code(ac, bw);
		   code.transformToBinary();
			//You should start working on the ex from here, and remove the following line of course:
//			bw.write("This should be replaced with the required output\n");// remove this line!!!
			//The following lines from here to the end of the file shouldn't be removed:			
	}
}
	

class ASMcode
{
	private String [] arr;
	private int  curr_index;
	
	public ASMcode(String filename) throws IOException, Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line,str="";
		while ((line = br.readLine()) != null) {
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
	
	public void resetIndex() {
		curr_index=0;
	}
}


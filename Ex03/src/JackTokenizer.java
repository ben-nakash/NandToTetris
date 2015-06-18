import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author arikgi
 */
public class JackTokenizer 
{
	private final static int ERROR=-1;
	private final static int INTEGERCONSTANT=0;
	private final static int KEYWORD=1;
	private final static int IDENTIFIER=2;	
	private final static int STRINGCONSTANT=3;
	private final static int SYMBOL=4;
	String tokens[];
	String[] keywordsarray= {"null","let","this","do","if","else","while","return","var","int","char","boolean","void","true","false","class","constructor","function","method","field","static"};
	String[] symbolarray= {"\\{","\\}","\\.","\\,","\\;","\\+","\\-","\\*","\\/","\\&","\\|","\\<","\\>","\\~","\\=","\\[","\\]","\\(","\\)","\\#","\\^"};
	int tokens_type[];
	int currentToken=0;


	public JackTokenizer(String filename) throws IOException, Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line,str="";
		while ((line = br.readLine()) != null) {			
			str=str.concat(line + "\n");				
		}
		br.close();		
		initTokensArray(str);
	}
	
	
	public boolean hasMoreTokens(){
		return (currentToken+1)<tokens.length;
	}
	
	public void advance() throws Exception
	{
		if(hasMoreTokens()) {
			currentToken++;
		}
		else{
			throw new Exception("Tokenizer Cannot advance - end of file");
		}
	}
	
	public String keyWord() throws Exception
	{
		if(tokens_type[currentToken]!=KEYWORD){
			throw new Exception("Current token is not a keyword: "+tokens[currentToken]);			
		}
		return tokens[currentToken];		
	}
	
	public char symbol() throws Exception
	{
		if(tokens_type[currentToken]!=SYMBOL){
			throw new Exception("Current token is not a symbol: "+tokens[currentToken]);			
		}
		return tokens[currentToken].charAt(0);		
	}
	
	public String identifier() throws Exception
	{
		if(tokens_type[currentToken]!=IDENTIFIER){
			throw new Exception("Current token is not an identifier: "+tokens[currentToken]);			
		}
		return tokens[currentToken];		
	}
	
	public int intVal() throws Exception
	{
		if(tokens_type[currentToken]!=INTEGERCONSTANT){
			throw new Exception("Current token is not an integer: "+tokens[currentToken]);			
		}
		return Integer.valueOf(tokens[currentToken]);		
	}
	
	public String stringVal() throws Exception
	{
		if(tokens_type[currentToken]!=STRINGCONSTANT){
			throw new Exception("Current token is not a string constant: "+tokens[currentToken]);			
		}
		return (tokens[currentToken]);		
	}

	/**
	 * This is the init function - handles the file content (from a single string), and builds the data structure 
	 */
	private void initTokensArray(String str) throws Exception 
	{
		int lines_num,i;
		str= removeComplexComments(str);
		str= removeSimpleComments(str);		
		str = str.replaceAll("\"(.+)\"", "\n\"$1\n");
		str=handleSpaces(str);
		str=handleSymbols(str);
		
		str=joinLines(str.split("\n"),"\n");
		tokens= str.split("\n");
		lines_num = tokens.length;
		tokens_type = new int[lines_num];
		for (i=0;i<lines_num;i++)
		{
			tokens_type[i]=returnTokenType(tokens[i]);
			if(tokens_type[i]==ERROR){
				throw new Exception("token:"+tokens[i]+" was not identified");
			}
			if(tokens_type[i]==SYMBOL || tokens_type[i]==STRINGCONSTANT)
			{
				tokens[i]=tokens[i].substring(1); 
				if(tokens[i].matches("\\>")){
					tokens[i]="&gt;";
				}
				else if(tokens[i].matches("\\<")){
					tokens[i]="&lt;";
				}
				else if(tokens[i].matches("\\&")){
					tokens[i]="&amp;";
				}
			}
		}

	}
	/**
	 * returns tokens type
	 */
	private int returnTokenType(String token)
	{
		int i,kw_len= keywordsarray.length;
		if(token.charAt(0)=='{'){
			return SYMBOL;
		}
		if(token.charAt(0)=='"'){
			return STRINGCONSTANT;
		}
		if(token.matches("\\d+")){
			return INTEGERCONSTANT;
		}
		for(i=0;i<kw_len;i++){			
			if(token.compareTo(keywordsarray[i])==0){				
				return KEYWORD;
			}
		}		
		if(token.matches("[a-zA-Z_]\\w*")){
			return IDENTIFIER;
		}	
		return ERROR;				
	}
	/**
	 * replace all spaces with new lines
	 */
	private String handleSpaces(String str)
	{
		String[] codeLines=str.split("\n");
		int i,lines_num;
		lines_num= codeLines.length;			
		for (i=0;i<lines_num;i++){
			if(!codeLines[i].isEmpty() && codeLines[i].charAt(0)!='"') {
				codeLines[i] = codeLines[i].replaceAll(" ","\n");
				codeLines[i] = codeLines[i].replaceAll("\\s","\n");			
			}
		}
		return joinLines(codeLines,"\n");	
	}

	/**
	 * handles symbols - adds { to each symbol - in order to identify it as a symbol
	 * This will be removed later by the line tokens[i]=tokens[i].substring(1); 
	 */
	private String handleSymbols(String str)
	{
		String[] codeLines=str.split("\n");
		int i,j,lines_num,symbols_num;
		lines_num= codeLines.length;
		symbols_num=symbolarray.length;		
		for (i=0;i<lines_num;i++)
		{
			if(!codeLines[i].isEmpty() && codeLines[i].charAt(0)!='"')
			{
				for(j=0;j<symbols_num;j++){								
					codeLines[i] = codeLines[i].replaceAll(symbolarray[j], "\n"+symbolarray[0]+symbolarray[j]+"\n");				
				}
			}
		}		
		return joinLines(codeLines,"\n");	
	}
	/**
	 * converts string array to a single string with the s delimiter
	 */
	private String joinLines(String[] codeLines, String s)
	{
		int i,len = codeLines.length;
		String linesJoined="";
		for (i=0;i<len;i++)
		{
			if (!codeLines[i].matches("^(\\s*)") && !codeLines[i].isEmpty()){				
				linesJoined =  linesJoined.concat(codeLines[i]+s);			
			}
		}
		return linesJoined;		
	}
	/**
	 * removes simple (single-line) comments
	 */
	private String removeSimpleComments(String str)
	{		
		String[] codeLines=str.split("\n");
		int indexOfComment,i=0,len = codeLines.length;
		while(i<len)
		{
			indexOfComment=codeLines[i].indexOf("//");
			if(indexOfComment!=-1 && !indexWithinQuotation(codeLines[i],indexOfComment)){
				codeLines[i]= codeLines[i].substring(0,indexOfComment);
			}
			i++;
		}	
		return joinLines(codeLines,"\n");		
	}
	/**
	 * removes complex (multi-line) comments
	 */

	private String removeComplexComments(String str)
	{
		int indexOfCommentEnd,indexOfComment=str.indexOf("/*");
		while(indexOfComment!=-1)
		{
			if(!indexWithinQuotation(str,indexOfComment))
			{
				indexOfCommentEnd = str.indexOf("*/",indexOfComment+3);
				str=str.substring(0, indexOfComment)+str.substring(indexOfCommentEnd+2);				
				indexOfComment--;
			}
			indexOfComment=str.indexOf("/*",indexOfComment+1);
		}
		return str;
	}


	/**
	 * finds whether index is within quotation marks
	 */

	private boolean indexWithinQuotation(String str,int index)
	{
		int curr=0,countOfQuotationMarks=0;
		curr=str.indexOf('"',curr);
		while(curr<index && curr!=-1) {
			countOfQuotationMarks++;
			curr=str.indexOf('"',curr+1);
		}
		return countOfQuotationMarks%2==1;
	}
}
import java.util.Hashtable;

public class SymbolTable 
{
	private JackTokenizer tok;
	private Hashtable<String,varDetails> globalsTable;
	private Hashtable<String, varDetails> localsTable;
	private int staticIndex=0, fieldIndex=0, varIndex=0, argumentIndex=0;
	private String vKind, vType, vName;
	private varDetails newVar;
	
	public SymbolTable(JackTokenizer tokenizer)
	{
		tok = tokenizer;
		localsTable = new Hashtable<String, varDetails>();
		globalsTable = new Hashtable<String, varDetails>();
	}
	
	public void initilizeGlobalsTable() 
	{
		while (tok.tokens[tok.currentToken].matches("field") || tok.tokens[tok.currentToken].matches("static"))
		{
			vKind = tok.tokens[tok.currentToken];
			advance();
			vType = tok.tokens[tok.currentToken];
			advance();
			
			do {
				if (tok.tokens[tok.currentToken].matches("\\,"))
					advance();
				vName = tok.tokens[tok.currentToken];
				
				if (existInGlobalsTable(vName)) {
					System.out.println("Error: Duplicate global variable " + vName);
					System.exit(-1);
				}
	
				if (vKind.equals("field")) {
					newVar = new varDetails(vKind, vType, fieldIndex);
					fieldIndex++;
				}
				else {
					newVar = new varDetails(vKind, vType, staticIndex);
					staticIndex++;
				}
					
				globalsTable.put(vName, newVar);
				advance();
			} while(tok.tokens[tok.currentToken].matches("\\,"));
			advance();
		}
		return;
	}
	
	public void clearLocalTable() {
		localsTable.clear();
	}
	
	public void resetLocalIndices() {
		varIndex=0;
		argumentIndex=0;
	}
	
	private boolean advance()
	{
    	try {
    		tok.advance();
    		return true;
    	}
    	catch(Exception e) {
    		return false;
    	}
	}

	public void addFunctionArgumentsToLocalsTable(String subroutineType)
	{
		vKind="argument";
		if (subroutineType.matches("method"))
			argumentIndex++;
		do {
			if (tok.tokens[tok.currentToken].matches("\\,"))
				advance();
			vType = tok.tokens[tok.currentToken];
			advance();
			vName = tok.tokens[tok.currentToken];
			if (existInLocalsTable(vName)) {
				System.out.println("Error: Duplicate local variable " + vName);
				System.exit(-1);
			}
			
			newVar = new varDetails(vKind, vType, argumentIndex);
			localsTable.put(vName, newVar);
			argumentIndex++;
			advance();
		} while (tok.tokens[tok.currentToken].matches("\\,"));
	}
	
	public void addFunctionVarsToLocalsTable()
	{
		vKind="var";
		advance();
		vType = tok.tokens[tok.currentToken];
		advance();
		do {
			if (tok.tokens[tok.currentToken].matches("\\,"))
				advance();
			vName = tok.tokens[tok.currentToken];
			newVar = new varDetails(vKind, vType, varIndex);
			varIndex++;
			if (existInLocalsTable(vName)) {
				System.out.println("Error: Duplicate local variable " + vName);
				System.exit(-1);
			}
			localsTable.put(vName, newVar);
			advance();
		}while (tok.tokens[tok.currentToken].matches("\\,"));
		advance();
	}
	
	public int getNumOfLocalVars() {
		return varIndex;
	}
	
	public int getNumOfGlobalVars() {
		return fieldIndex;
	}
	
	public boolean existInLocalsTable(String varName)
	{
		if(localsTable.containsKey(varName))
			return true;
		return false;
	}

	public boolean existInGlobalsTable(String varName)
	{
		if(globalsTable.containsKey(varName))
			return true;
		return false;
	}
	
	public varDetails getVarDetails(String varName) {
		if (existInLocalsTable(varName))
			return localsTable.get(varName);
		else if (existInGlobalsTable(varName))
			return globalsTable.get(varName);
		else
			return null;
	}
		
	public boolean isArgument(String varName) {
		varDetails temp = localsTable.get(varName);
		if ((temp!=null) && (temp.getKind().equals("argument")))
			return true;
		return false;
	}
	
	public boolean isLocal(String varName) {
		varDetails temp = localsTable.get(varName); 
		if ((temp!=null) && (temp.getKind().equals("var")))
			return true;
		return false;
	}
	
	public int getIndex(String varScope, String varName)
	{
		varDetails var;
		int index;
		if (varScope.matches("LOCAL") || varScope.matches("argument") || varScope.matches("var"))
			var = localsTable.get(varName);
		else
			var = globalsTable.get(varName);
		index = var.getIndex();
		return index;
	}
}

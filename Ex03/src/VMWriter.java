import java.io.BufferedWriter;
import java.io.IOException;


public class VMWriter 
{
	private BufferedWriter bw;
	private JackTokenizer tok;
	private SymbolTable sTable;
	private static String className, subroutineName, subroutineType;
	private static int ifCounter=-1, whileCounter=-1;
	
	private final static int INTEGERCONSTANT=0;
	private final static int IDENTIFIER=2;	
	private final static int STRINGCONSTANT=3;
	
	public VMWriter(BufferedWriter bufferedWriter, JackTokenizer tokenizer) throws IOException
	{
		bw = bufferedWriter;
		tok = tokenizer;
		sTable = new SymbolTable(tok);
	}
	
	public void compile() throws IOException
	{
		classHandler();
	}
	
	
	private void classHandler() throws IOException
	{
		advance();
		className = tok.tokens[tok.currentToken];
		advance();
		advance();

		while (tok.tokens[tok.currentToken].matches("static") || tok.tokens[tok.currentToken].matches("field"))
			classVarDecHandler();

		while (tok.tokens[tok.currentToken].matches("constructor") || tok.tokens[tok.currentToken].matches("function") || tok.tokens[tok.currentToken].matches("method"))
		{
			subroutineDecHandler();
			sTable.clearLocalTable();
			sTable.resetLocalIndices();	
		}
	}

	
	private void classVarDecHandler() throws IOException 
	{
		sTable.initilizeGlobalsTable();
	}

	private void subroutineDecHandler() throws IOException
	{
		subroutineType = tok.tokens[tok.currentToken];
		advance();
		advance();
		subroutineName = tok.tokens[tok.currentToken];
		advance();
		advance();

		parameterListHandler();
		advance();
		subroutineBodyHandler();
		ifCounter=-1;
		whileCounter=-1;
	}

	
	private void parameterListHandler() throws IOException
	{
		if (!tok.tokens[tok.currentToken].matches("\\)"))
			sTable.addFunctionArgumentsToLocalsTable(subroutineType);
	}
	
	
	private void subroutineBodyHandler() throws IOException
	{
		advance();
		while (tok.tokens[tok.currentToken].matches("var")) {
			varDecHandler();
		}
		
		// this is the printing of the localVariables number in the first line.
		writeFunction(subroutineName, sTable.getNumOfLocalVars());
		switch (subroutineType) {
		case "constructor": writePush("CONST", sTable.getNumOfGlobalVars());
							writeCall("Memory.alloc", 1);
							writePop("POINTER", 0);
							break;
		case "method":		writePush("argument", 0);
							writePop("POINTER", 0);
							break;
		}
		statementsHandler();
		advance();
	}

	
	private void varDecHandler() throws IOException
	{
		sTable.addFunctionVarsToLocalsTable();
	}

	private void statementsHandler() throws IOException
	{		
		while(isStatement(tok.currentToken))
		{
			switch(tok.tokens[tok.currentToken]) {
			case "let":
				letStatementHandler();	  
				break;
			case "if":
				ifCounter++;	
				ifStatementHandler();
				break;
			case "while":
				whileCounter++;	
				whileStatementHandler();	
				break;
			case "do":
				doStatementHandler();	 
				break;
			default:
				returnStatementHandler();
				break;				
			}
		}
	}


	private boolean isStatement(int index) 
	{
		if (tok.tokens[index].matches("let")   || tok.tokens[index].matches("if") ||
			tok.tokens[index].matches("while") || tok.tokens[index].matches("do") || 
			tok.tokens[index].matches("return"))
			return true;
		return false;
	}


	private void letStatementHandler() throws IOException
	{
		String varScope="", varName;
		varDetails var;
		advance();
		
		varName=tok.tokens[tok.currentToken];
		var = sTable.getVarDetails(varName);
		if (var != null)
			varScope = var.getKind();
		else
			variableNotExistError();
		advance();
		if (tok.tokens[tok.currentToken].matches("\\["))
		{
			advance();
			expressionHandler();
			advance();
			writePush(var.getKind(), var.getIndex());
			bw.write("add\n");
			advance();
			expressionHandler();
			writePop("TEMP", 0);
			writePop("POINTER", 1);
			writePush("TEMP", 0);
			writePop("THAT", 0);
			advance();
			return;
		}
		advance();
		expressionHandler();
		writePop(varScope, sTable.getIndex(varScope, varName));
		advance();
	}


	private void ifStatementHandler() throws IOException
	{
		advance();	
		advance();	
		int localIfCounter = ifCounter;
		expressionHandler();
		
		writeIf("IF_TRUE", localIfCounter);
		writeGoTo("IF_FALSE", localIfCounter);
		writeLabel("IF_TRUE", localIfCounter);
		advance();
		advance();	
		statementsHandler();
		advance();
		if (tok.tokens[tok.currentToken].matches("else")) {
			writeGoTo("IF_END", localIfCounter);
			writeLabel("IF_FALSE", localIfCounter);
			advance();
			advance();
			statementsHandler();
			advance();
			writeLabel("IF_END", localIfCounter);
		}
		else {
			writeLabel("IF_FALSE", localIfCounter);
			return;
		}
	}


	private void whileStatementHandler() throws IOException
	{
		int localWhileCounter=whileCounter;
		advance();	//		printKeyword();
		advance();	//		printSymbol();
		writeLabel("WHILE_EXP", localWhileCounter);
		expressionHandler();
		bw.write("not\n");
		writeIf("WHILE_END", localWhileCounter);;
		advance();	//		printSymbol();
		advance();	//		printSymbol();
		statementsHandler();
		writeGoTo("WHILE_EXP", localWhileCounter);
		writeLabel("WHILE_END", localWhileCounter);
		advance();	//		printSymbol();
	}


	private void doStatementHandler() throws IOException
	{
		advance();
		subroutineCallHandler();
		writePop("TEMP", 0);
		advance();
	}


	private void returnStatementHandler() throws IOException
	{
		advance();
		if(!tok.tokens[tok.currentToken].matches("\\;")) {
			expressionHandler();
			writeReturn();
			advance();
			return;
		}

		writePush("CONST", 0);
		writeReturn();
		advance();
	}


	private void expressionHandler() throws IOException
	{
		String op="";
		termHandler();
		while(currentTokIsOp())
		{	
			op = findWhichOpItIs();
			advance();
			termHandler();
			writeArithmetic(op);
		}
	}


	private void termHandler() throws IOException
	{
		if (tok.tokens_type[tok.currentToken] == INTEGERCONSTANT) {
			writePush("CONST", Integer.parseInt(tok.tokens[tok.currentToken]));
			advance();
		}
		else if (tok.tokens_type[tok.currentToken] == STRINGCONSTANT) 
		{
			String str = tok.tokens[tok.currentToken];
			writePush("CONST", str.length());
			writeCall("String.new", 1);
			for(int i=0 ; i<str.length() ; i++) {
				writePush("CONST", (int)str.charAt(i));
				writeCall("String.appendChar", 2);
			}
			advance();
		}
		else if (tok.tokens[tok.currentToken].matches("true") || tok.tokens[tok.currentToken].matches("false") ||
				 tok.tokens[tok.currentToken].matches("null") || tok.tokens[tok.currentToken].matches("this"))
		{
			keywordConstantHandler();
			advance();
		}
		else if (tok.tokens[tok.currentToken].matches("\\("))
		{
			advance();
			expressionHandler();
			advance();
		}
		else if (tok.tokens[tok.currentToken].matches("\\~") || tok.tokens[tok.currentToken].matches("\\-"))
		{
			String op;
			if (tok.tokens[tok.currentToken].matches("\\~")) 
				op="NOT";
			else
				op="NEG";
			advance();
			termHandler();
			writeArithmetic(op);
		}
		else if (tok.tokens_type[tok.currentToken] == IDENTIFIER)
		{
			if (tok.tokens[tok.currentToken+1].matches("\\.") || tok.tokens[tok.currentToken+1].matches("\\(")) {
				subroutineCallHandler();
			}
			else if (tok.tokens[tok.currentToken+1].matches("\\["))
			{
				String varName = tok.tokens[tok.currentToken];
				varDetails var = sTable.getVarDetails(varName);
				advance();
				advance();
				expressionHandler();
				writePush(var.getKind(), var.getIndex());
				bw.write("add\n");
				writePop("POINTER", 1);
				writePush("THAT", 0);
				advance();
			}
			else 
			{
				varDetails var = sTable.getVarDetails(tok.tokens[tok.currentToken]);
				if (var != null)
					writePush(var.getKind(), var.getIndex());
				advance();
			}
		}
	}
	

	private void keywordConstantHandler() throws IOException 
	{
		switch (tok.tokens[tok.currentToken]) {
		case "false":	writePush("CONST", 0);		break;
		case "null":	writePush("CONST", 0);		break;
		case "this": 	writePush("POINTER", 0);	break;
		case "true":	writePush("CONST", 0);
						bw.write("not\n");
						break;
		}
		
	}

	private void subroutineCallHandler() throws IOException
	{
		int numOfArgs;
		if (tok.tokens[tok.currentToken+1].matches("\\(")) 
		{
			String subroutineName = className + "." + tok.tokens[tok.currentToken];
			advance();
			advance();
			writePush("POINTER", 0);
			numOfArgs = expressionListHandler() + 1;
			writeCall(subroutineName, numOfArgs);
			advance();
		}
		else if (tok.tokens[tok.currentToken+1].matches("\\.")) 
		{
			String varName = tok.tokens[tok.currentToken];
			varDetails var = sTable.getVarDetails(varName);
			if (var != null) {
				advance();
				advance();
				String output = var.getType() + "." + tok.tokens[tok.currentToken];
				advance();
				advance();
				if (var != null)
					writePush(var.getKind(), var.getIndex());
				numOfArgs = expressionListHandler() + 1;

				writeCall(output, numOfArgs);
				advance();
			}
			else {
				String output = "";
				for (int i=0 ; i<3 ; i++) {
					output += tok.tokens[tok.currentToken];
					advance();
				}
				advance();
				numOfArgs = expressionListHandler();
				writeCall(output, numOfArgs);
				advance();
			}
				
		}
	}
	
	private int expressionListHandler() throws IOException
	{
		int numOfArgs=0;
		if(!tok.tokens[tok.currentToken].matches("\\)"))
		{
			numOfArgs++;
			expressionHandler();
			while (tok.tokens[tok.currentToken].matches("\\,"))
			{
				numOfArgs++;
				advance();
				expressionHandler();
			}
		}
		return numOfArgs;
	}
	
	
	private boolean currentTokIsOp() 
	{
		if (tok.tokens[tok.currentToken].matches("\\+") || tok.tokens[tok.currentToken].matches("\\-") ||
			tok.tokens[tok.currentToken].matches("\\*") || tok.tokens[tok.currentToken].matches("\\/") ||
			tok.tokens[tok.currentToken].matches("\\&amp;")|| tok.tokens[tok.currentToken].matches("\\&lt;") ||
			tok.tokens[tok.currentToken].matches("\\&gt;") || tok.tokens[tok.currentToken].matches("\\=") ||
			tok.tokens[tok.currentToken].matches("\\^")	   || tok.tokens[tok.currentToken].matches("\\|"))
			return true;
		
		return false;
	}
	
	
	private String findWhichOpItIs() 
	{
		String temp = tok.tokens[tok.currentToken];
		switch(temp) {
		case "+":		return "ADD";
		case "-":		return "SUB";
		case "*":		return "MULT";
		case "/":		return "DEV";
		case "&amp;":	return "AND";
		case "|":		return "OR";
		case "&lt;":	return "LT";
		case "&gt;":	return "GT";
		case "=":		return "EQ";
		case "~":		return "NOT";
		case "^":		return "POW";
		default: 
			return null;
		}
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
	
	
	private void variableNotExistError() 
	{
		System.out.println("In subroutine "+subroutineName+": "+tok.tokens[tok.currentToken]+" is not defined as a field, parameter, "
				+ "or local or static variable");
		System.exit(-1);
	}
	
	private void writePush(String segment, int index) throws IOException 
	{
		switch (segment) {
		case "CONST":	bw.write("push constant "+index+"\n");	break;
		case "argument":bw.write("push argument "+index+"\n");	break;
		case "LOCAL":	bw.write("push local "	 +index+"\n");	break;
		case "POINTER":	bw.write("push pointer " +index+"\n");	break;
		case "var":		bw.write("push local " 	 +index+"\n");	break;
		case "field":	bw.write("push this "    +index+"\n");	break;
		case "THAT":	bw.write("push that "    +index+"\n");	break;
		case "TEMP":	bw.write("push temp "    +index+"\n");	break;
		case "static":	bw.write("push static "  +index+"\n");	break;
		}
	}
	
	private void writePop(String segment, int index) throws IOException
	{
		switch(segment) {
		case "LOCAL":		bw.write("pop local "   +index+"\n");		break;
		case "field":		bw.write("pop this "    +index+"\n");		break;
		case "POINTER":		bw.write("pop pointer " +index+"\n");		break;
		case "TEMP":		bw.write("pop temp "    +index+"\n");		break;
		case "var":			bw.write("pop local "   +index+"\n");		break;
		case "argument":	bw.write("pop argument "+index+"\n");		break;
		case "THAT":		bw.write("pop that "    +index+"\n");		break;	
		case "static":		bw.write("pop static "  +index+"\n");		break;	
		}
	}
	
	private void writeArithmetic(String op) throws IOException
	{
		if (op == null) {
			System.out.println("Error - unrecognized operation: " + op + "\n");
			System.exit(-1);
		}
		switch(op) {
		case "ADD":		bw.write("add\n");				break;
		case "SUB":		bw.write("sub\n");				break;
		case "NEG":		bw.write("neg\n");				break;
		case "EQ":		bw.write("eq\n");				break;
		case "GT":		bw.write("gt\n");				break;
		case "LT":		bw.write("lt\n");				break;
		case "AND":		bw.write("and\n");				break;
		case "OR":		bw.write("or\n");				break;
		case "NOT":		bw.write("not\n");				break;
		case "MULT":	writeCall("Math.multiply", 2);	break;
		case "POW":		writeCall("Math.power", 2);		break;
		case "DEV":		writeCall("Math.divide", 2);	break;
		}
	}
	
	private void writeLabel(String label, int index) throws IOException
	{
		if (label.contains("IF"))
			bw.write("label " + label + index + "\n");
		else if (label.contains("WHILE"))
			bw.write("label " + label + index + "\n");
	}
	
	private void writeGoTo(String label, int index) throws IOException
	{
		if (label.contains("IF"))
			bw.write("goto " + label + index + "\n");
		else if (label.contains("WHILE"))
			bw.write("goto " + label + index + "\n");
	}
	
	private void writeIf(String label, int index) throws IOException
	{
		if (label.contains("IF"))
			bw.write("if-goto " + label + index + "\n");
		else if (label.contains("WHILE"))
			bw.write("if-goto " + label + index + "\n");
	}
	
	private void writeCall(String name, int nArgs) throws IOException
	{
		bw.write("call "+name+" "+nArgs+"\n");
	}
	
	private void writeFunction(String name, int nLocals) throws IOException
	{
		bw.write("function " + className + "." + subroutineName + " " + nLocals + "\n");
	}
	
	private void writeReturn() throws IOException
	{
		bw.write("return\n");
	}
	

	

	
}











import java.io.BufferedWriter;
import java.io.IOException;

public class xmlConverter 
{
	private final static int INTEGERCONSTANT=0;
	private final static int IDENTIFIER=2;	
	private final static int STRINGCONSTANT=3;
	private BufferedWriter bw;
	private JackTokenizer tok;
	
	public xmlConverter(BufferedWriter bufferedWriter, JackTokenizer tokenizer) throws IOException 
	{
		bw = bufferedWriter;
		tok = tokenizer;
	    classHandler();
	}

	
	private void classHandler() throws IOException
	{
		bw.write("<class>\n");
		printKeyword();
		printIdentifier();
		printSymbol();
		while (tok.tokens[tok.currentToken].matches("static") || tok.tokens[tok.currentToken].matches("field"))
			classVarDecHandler();

		while (tok.tokens[tok.currentToken].matches("constructor") || tok.tokens[tok.currentToken].matches("function") || tok.tokens[tok.currentToken].matches("method"))
			subroutineDecHandler();

		printSymbol();
		bw.write("</class>");
	}

	private void classVarDecHandler() throws IOException 
	{
		bw.write("<classVarDec>\n");
		printKeyword();
		typeHandler();
		printIdentifier();
		while (tok.tokens[tok.currentToken].matches("\\,")) {
			printSymbol();
			printIdentifier();
		}
		printSymbol();
		bw.write("</classVarDec>\n");
	}

	
	private void typeHandler() throws IOException
	{
		bw.write("<type>\n");
		if (tok.tokens[tok.currentToken].matches("int") || tok.tokens[tok.currentToken].matches("char") || tok.tokens[tok.currentToken].matches("boolean"))
			printKeyword();
		else 
			printIdentifier();
		bw.write("</type>\n");
	}


	private void subroutineDecHandler() throws IOException
	{
		bw.write("<subroutineDec>\n");
		printKeyword();

		if (tok.tokens[tok.currentToken].matches("void"))
			printKeyword();
		else
			typeHandler();
		printSubroutineName();
		printSymbol();
		parameterListHandler();
		printSymbol();
		subroutineBodyHandler();
		bw.write("</subroutineDec>\n");
	}

	
	private void parameterListHandler() throws IOException
	{
		bw.write("<parameterList>\n");
		if (!tok.tokens[tok.currentToken].matches("\\)"))
		{
			typeHandler();
			printIdentifier();
			while(tok.tokens[tok.currentToken].matches("\\,"))
			{
				printSymbol();
				typeHandler();
				printIdentifier();
			}
		}
		bw.write("</parameterList>\n");
		return;
	}
	
	
	private void subroutineBodyHandler() throws IOException
	{
		bw.write("<subroutineBody>\n");
		printSymbol();
		while (tok.tokens[tok.currentToken].matches("var")) {
			varDecHandler();
		}
		statementsHandler();
		printSymbol();
		bw.write("</subroutineBody>\n");
	}

	
	private void varDecHandler() throws IOException
	{
		bw.write("<varDec>\n");
		printKeyword();
		typeHandler();
		printIdentifier();
		while(tok.tokens[tok.currentToken].matches("\\,")) 
		{
			printSymbol();
			printIdentifier();
		}
		printSymbol();
		bw.write("</varDec>\n");
	}

	private void statementsHandler() throws IOException
	{		
		bw.write("<statements>\n");
		while(isStatement(tok.currentToken))
		{
			bw.write("<statement>\n");
			if (tok.tokens[tok.currentToken].matches("let"))
				letStatementHandler();
			else if (tok.tokens[tok.currentToken].matches("if"))
				ifStatementHandler();
			else if (tok.tokens[tok.currentToken].matches("while"))
				whileStatementHandler();
			else if (tok.tokens[tok.currentToken].matches("do"))
				doStatementHandler();
			else
				returnStatementHandler();
			bw.write("</statement>\n");
		}
		bw.write("</statements>\n");
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
		bw.write("<letStatement>\n");
		printKeyword();
		printIdentifier();
		if (tok.tokens[tok.currentToken].matches("\\["))
		{
			printSymbol();
			expressionHandler();
			printSymbol();
		}
		printSymbol();
		expressionHandler();
		printSymbol();
		bw.write("</letStatement>\n");
	}


	private void ifStatementHandler() throws IOException
	{
		bw.write("<ifStatement>\n");
		printKeyword();	
		printSymbol();	
		expressionHandler();
		printSymbol();	
		printSymbol();	
		statementsHandler();
		printSymbol();
		if (tok.tokens[tok.currentToken].matches("else")) 
		{
			printKeyword();
			printSymbol();
			statementsHandler();
			printSymbol();
		}
		bw.write("</ifStatement>\n");
	}


	private void whileStatementHandler() throws IOException
	{
		bw.write("<whileStatement>\n");
		printKeyword();
		printSymbol();
		expressionHandler();
		printSymbol();
		printSymbol();
		statementsHandler();
		printSymbol();
		bw.write("</whileStatement>\n");
	}


	private void doStatementHandler() throws IOException
	{
		bw.write("<doStatement>\n");
		printKeyword();
		subroutineCallHandler();
		printSymbol();
		bw.write("</doStatement>\n");
	}


	private void returnStatementHandler() throws IOException
	{
		bw.write("<returnStatement>\n");
		printKeyword();
		if(!tok.tokens[tok.currentToken].matches("\\;"))
			expressionHandler();
		printSymbol();
		bw.write("</returnStatement>\n");
	}

	
	private void expressionHandler() throws IOException
	{
		bw.write("<expression>\n");
		termHandler();
		
		while(nextTokIsOp())
		{	
			printOp();
			termHandler();	
		}
		
		bw.write("</expression>\n");
	}


	private void termHandler() throws IOException
	{
		bw.write("<term>\n");
		if (tok.tokens_type[tok.currentToken] == INTEGERCONSTANT)
			printIntegerConstant();
		else if (tok.tokens_type[tok.currentToken] == STRINGCONSTANT)
			printStringConstant();
		else if (tok.tokens[tok.currentToken].matches("true") || tok.tokens[tok.currentToken].matches("false") ||
				 tok.tokens[tok.currentToken].matches("null") || tok.tokens[tok.currentToken].matches("this"))
			printKeywordConstant();
		else if (tok.tokens[tok.currentToken].matches("\\("))
		{
			printSymbol();
			expressionHandler();
			printSymbol();
		}
		else if (tok.tokens[tok.currentToken].matches("\\~") || tok.tokens[tok.currentToken].matches("\\-"))
		{
			printUnaryOp();
			termHandler();
		}
		else if (tok.tokens_type[tok.currentToken] == IDENTIFIER)
		{
			if (tok.tokens[tok.currentToken+1].matches("\\.") || tok.tokens[tok.currentToken+1].matches("\\("))
				subroutineCallHandler();
			else if (tok.tokens[tok.currentToken+1].matches("\\["))
			{
				printIdentifier();
				printSymbol();
				expressionHandler();
				printSymbol();
			}
			else
				printIdentifier();
		}
		bw.write("</term>\n");
	}


	private void subroutineCallHandler() throws IOException
	{
		bw.write("<subroutineCall>\n");
		if (tok.tokens[tok.currentToken+1].matches("\\(")) 
		{
			printSubroutineName();
			printSymbol();
			expressionListHandler();
			printSymbol();
		}
		else if (tok.tokens[tok.currentToken+1].matches("\\.")) 
		{
			printIdentifier();
			printSymbol(); 
			printSubroutineName();
			printSymbol();
			expressionListHandler();
			printSymbol();
		}
		bw.write("</subroutineCall>\n");
	}


	private void expressionListHandler() throws IOException
	{
		bw.write("<expressionList>\n");
		if(!tok.tokens[tok.currentToken].matches("\\)"))
		{
			expressionHandler();
			while (tok.tokens[tok.currentToken].matches("\\,"))
			{
				printSymbol();
				expressionHandler();
			}
		}
		bw.write("</expressionList>\n");
		return;
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
	
	
	private boolean nextTokIsOp() 
	{
		if (tok.tokens[tok.currentToken].matches("\\+") || tok.tokens[tok.currentToken].matches("\\-") ||
			tok.tokens[tok.currentToken].matches("\\*") || tok.tokens[tok.currentToken].matches("\\/") ||
			tok.tokens[tok.currentToken].matches("\\&amp;")|| tok.tokens[tok.currentToken].matches("\\&lt;") ||
			tok.tokens[tok.currentToken].matches("\\&gt;") || tok.tokens[tok.currentToken].matches("\\=") ||
			tok.tokens[tok.currentToken].matches("\\^")	   || tok.tokens[tok.currentToken].matches("\\|"))
			return true;
		
		return false;
	}
	
	
	private void printUnaryOp() throws IOException
	{
		bw.write("<unaryOp>\n");
		printSymbol();
		bw.write("</unaryOp>\n");
	}
	
	
	private void printSymbol() throws IOException 
	{
		bw.write("<symbol> " + tok.tokens[tok.currentToken] + " </symbol>\n");
		advance();
	}

	
	private void printKeyword() throws IOException
	{
		bw.write("<keyword> "+tok.tokens[tok.currentToken]+" </keyword>\n");
		advance();
	}
	
	
	private void printIdentifier() throws IOException
	{
		bw.write("<identifier> " + tok.tokens[tok.currentToken] + " </identifier>\n");
		advance();
	}
	
	private void printKeywordConstant() throws IOException
	{
		bw.write("<keywordConstant>\n");
		printKeyword();
		bw.write("</keywordConstant>\n");
	}


	private void printStringConstant()throws IOException
	{
		bw.write("<stringConstant> " + tok.tokens[tok.currentToken] + " </stringConstant>\n");
		advance();
	}


	private void printIntegerConstant() throws IOException
	{
		bw.write("<integerConstant> " + tok.tokens[tok.currentToken] + " </integerConstant>\n");
		advance();
	}
	
	private void printOp() throws IOException
	{
		bw.write("<op>\n");
		printSymbol();
		bw.write("</op>\n");
	}
	
	private void printSubroutineName() throws IOException
	{
		bw.write("<subroutineName>\n");
		printIdentifier();
		bw.write("</subroutineName>\n");
	}
}
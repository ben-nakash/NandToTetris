import java.util.Hashtable;


public class SymbolTable 
{
	private Hashtable<String, Integer> table;
	
	public SymbolTable()
	{
		table = new Hashtable<String, Integer>();
		initTable();
	}
	
	private void initTable()
	{
		int i;
		String symbol;
		
		addEntry("SP", 0);
		addEntry("LCL", 1);
		addEntry("ARG", 2);
		addEntry("THIS", 3);
		addEntry("THAT", 4);
		for (i=0 ; i<16 ; i++) {
			symbol = "R"+i;
			addEntry(symbol, i);
		}
		addEntry("SCREEN", 16384);
		addEntry("KBD", 24576);
	}
	
	public void addEntry(String symbol, int address)
	{
		table.put(symbol, address);
	}
	
	public boolean containValue(String symbol) 
	{
		if (table.containsKey(symbol))
			return true;
		return false;
	}
	
	public int getAddress(String symbol)
	{
		return table.get(symbol);
	}
}

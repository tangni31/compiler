package cop5556fa18;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import cop5556fa18.PLPAST.Declaration;


public class SymbolTable {
	
	class Attribute{
		int scopeNum;
		Declaration dec;
		
		public Attribute(int n, Declaration dec){
			this.scopeNum = n;
			this.dec = dec;
		}
	}
	
	
	/*
	 * the symbol table has a scope stack that indicates,
	 * in order, the scopes that compose the current referencing environment
	 */
	Stack<Integer> scope;
	int curScope, nextScope;
	/*
	 * All names, regardless of scope, are entered into a single large hash table, 
	 * keyed by name. Each entry in the table then contains the symbol name, 
	 * its category, scope number, type, and additional, category-specific fields.
	 */ 
	HashMap<String, ArrayList<Attribute>> table;
	
	
	public SymbolTable(){
		this.table = new HashMap<String, ArrayList<Attribute>>();//hash table
		this.scope = new Stack<Integer>(); //scope stack
		this.curScope = 0;	//current scope
		this.scope.push(0); //outermost scope
		this.nextScope = 1; //next scope
	}
	
	/*
	 * As the semantic analyzer scans the program, it pushes and pops this stack 
	 * whenever it enters or leaves a scope
	*/
	public void enterScope(){
		curScope = nextScope;
		nextScope ++;
		scope.push(curScope);
	}
	
	public void leaveScope(){
		scope.pop();
		curScope = scope.peek();
	}
	
	public Declaration lookup(String name){
		if(table.containsKey(name)){
			//apply hash function to name to find appropriate chain
			ArrayList<Attribute> chain = table.get(name);
			Declaration dec = null;
			for(int i=0; i<chain.size(); i++){
				Attribute entry = chain.get(i);
				if(entry.scopeNum <= curScope){
					if(scope.contains(entry.scopeNum)){
						dec = entry.dec; //find best instance
					}
				}
				else{ //cannot see farther
					break;
				}
			}
			return dec;
		}
		return null;
	}
	
	public boolean insert(String name, Declaration dec){
		if(table.containsKey(name)){
			ArrayList<Attribute> chain = table.get(name);
			for(int i=0; i<chain.size(); i++){
				Attribute entry = chain.get(i);
				if(entry.scopeNum == curScope){
					return false; //already existed in cur scope
				}
			}
			chain.add(new Attribute(curScope, dec));
			return true;
		}
		ArrayList<Attribute> newChain = new ArrayList<Attribute>();
		Attribute newAttr = new Attribute(curScope, dec);
		newChain.add(newAttr);
		table.put(name, newChain);
		return true;
	}
	
}

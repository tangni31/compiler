package cop5556fa18.PLPAST;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes.Type;

public abstract class Expression extends PLPASTNode {

	public Expression(Token firstToken) {
		super(firstToken);
	}
	
	private Type t;
	
	public Type getType(){
		return t;
	}
	
	public void setType(Type t1){
		t=t1;
	}

}

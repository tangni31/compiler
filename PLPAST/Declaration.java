package cop5556fa18.PLPAST;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes.Type;

public abstract class Declaration extends PLPASTNode {

	public Declaration(Token firstToken) {
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

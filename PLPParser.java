package cop5556fa18;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPScanner.Kind;

public class PLPParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	
	PLPScanner scanner;
	Token t;

	PLPParser(PLPScanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	public void parse() throws SyntaxException {
		program();
		match(Kind.EOF);
	}
	
	/*
	 * Program -> Identifier Block
	 */
	public void program() throws SyntaxException {
		match(Kind.IDENTIFIER);
		block();
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string};
	Kind[] firstStatement = {Kind.IDENTIFIER, Kind.KW_if, Kind.KW_while, Kind.KW_sleep, Kind.KW_print};
	
	public void block() throws SyntaxException {
		match(Kind.LBRACE);
		while (checkKind(firstDec) | checkKind(firstStatement)) {
	     if (checkKind(firstDec)) {
			declaration();
		} else if (checkKind(firstStatement)) {
			statement();
		}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);
	}
	
	/*  Declaration -> Type Identifier ( = Expression | E ) | Type IDENTIFIERLIST
		IDENTIFIERLIST -> Identifier (, Identifier)*
		Type -> int | float | boolean | char | string
	 */
	public void declaration() throws SyntaxException {
		match(t.kind);
		match(Kind.IDENTIFIER);
		if(checkKind(Kind.OP_ASSIGN)){ //Type Identifier = Expression
			match(Kind.OP_ASSIGN);
			expression();
		}
		else if(checkKind(Kind.COMMA)){ //Type Identifier (, Identifier)*
			while (checkKind(Kind.COMMA)){
				match(Kind.COMMA);
				match(Kind.IDENTIFIER);
			}	
		}
	}
	
	/*	
	 * Statement -> IfStatement | AssignmentStatement | SleepStatement 
		| PrintStatement | WhileStatment
	*/
	public void statement() throws SyntaxException {
		if (checkKind(Kind.KW_print)){ //PrintStatement -> print Expression
			match(Kind.KW_print);
			expression();
		}
		else if (checkKind(Kind.KW_sleep)){ //SleepStatement -> sleep Expression
			match(Kind.KW_sleep);
			expression();
		}
		else if (checkKind(Kind.KW_if)){  //IfStatement -> if ( Expression ) Block
			match(Kind.KW_if);
			match(Kind.LPAREN);
			expression();
			match(Kind.RPAREN);
			block();
		}
		else if (checkKind(Kind.KW_while)){ //WhileStatement -> while ( Expression ) Block
			match(Kind.KW_while);
			match(Kind.LPAREN);
			expression();
			match(Kind.RPAREN);
			block();
		}
		else if (checkKind(Kind.IDENTIFIER)){ //AssignmentStatement -> Identifier = Expression
			match(Kind.IDENTIFIER);
			match(Kind.OP_ASSIGN);
			expression();
		}
		else throw new UnsupportedOperationException();
	}
	
	//RelExpression
	Kind[] rel = {Kind.OP_GE, Kind.OP_LE, Kind.OP_GT, Kind.OP_LT};
	
	//MultExpression 
	Kind[] mult = {Kind.OP_MOD, Kind.OP_DIV, Kind.OP_TIMES};
	
	//Primary 
	Kind[] primary = {Kind.BOOLEAN_LITERAL, Kind.INTEGER_LITERAL, Kind.FLOAT_LITERAL,
			Kind.CHAR_LITERAL, Kind.STRING_LITERAL, Kind.IDENTIFIER};
	
	//UnaryExpression
	Kind[] UnaryExpFront = {Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_EXCLAMATION};
	
	//FunctionName -> sin | cos | atan | abs | log | int | float
	Kind[] functionNames = {Kind.KW_abs, Kind.KW_atan, Kind.KW_cos, Kind.KW_float, 
			Kind.KW_int, Kind.KW_sin, Kind.KW_log};
	
	//Expression -> OrExpression ? Expression : Expression | OrExpression
	public void expression() throws SyntaxException{
		OrExpression();
		if(checkKind(Kind.OP_QUESTION)){
			match(Kind.OP_QUESTION);
			expression();
			match(Kind.OP_COLON);
			expression();
		}
	}
	
	//OrExpression -> AndExpression ( | AndExpression )*
	public void OrExpression() throws SyntaxException{
		AndExpression();
		while (checkKind(Kind.OP_OR)){
			match(Kind.OP_OR);
			AndExpression();
		}
	}
	
	//AndExpression -> EqExpression ( & EqExpression )*
	public void AndExpression() throws SyntaxException{
		EqExpression();
		while (checkKind(Kind.OP_AND)){
			match(Kind.OP_AND);
			EqExpression();
		}
	}
	
	//EqExpression -> RelExpression ( ( == | != ) RelExpression )*
	public void EqExpression() throws SyntaxException{
		RelExpression();
		while (checkKind(Kind.OP_NEQ) || checkKind(Kind.OP_EQ)){
			match(t.kind);
			RelExpression();
		}
	}
	
	//RelExpression -> AddExpression ( ( < | > | <= | >= ) AddExpression )*
	public void RelExpression() throws SyntaxException{
		AddExpression();
		while (checkKind(rel)){
			match(t.kind);
			AddExpression();
		}
	}
	
	//AddExpression -> MultExpression ( ( + | - ) MultExpression )*
	public void AddExpression() throws SyntaxException{
		MultExpression();
		while (checkKind(Kind.OP_MINUS) || checkKind(Kind.OP_PLUS)){
			match(t.kind);
			MultExpression();
		}
	}
	
	//MultExpression -> PowerExpression ( ( * | / | % ) PowerExpression )*
	public void MultExpression() throws SyntaxException{
		PowerExpression();
		while (checkKind(mult)){
			match(t.kind);
			PowerExpression();
		}
	}
	
	//PowerExpression -> UnaryExpression ( ** PowerExpression | E )
	public void PowerExpression() throws SyntaxException{
		UnaryExpression();
		while (checkKind(Kind.OP_POWER)){
			match(Kind.OP_POWER);
			UnaryExpression();
		}
	}
	
	/*
	 * UnaryExpression -> + UnaryExpression | - UnaryExpression |
	 *	! UnaryExpression | Primary
	 */
	public void UnaryExpression() throws SyntaxException{
		if (checkKind(UnaryExpFront)){ // + - !
			match(t.kind);
			UnaryExpression();
		}
		else if (checkKind(Kind.LPAREN)){ //Primary -> ( Expression )
			match(Kind.LPAREN);
			expression();
			match(Kind.RPAREN);
		}
		else if (checkKind(functionNames)){//Primary ->  Function
			function();
		}
		/*
		 * Primary -> INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | CHAR_LITERAL
 			| STRING_LITERAL | IDENTIFIER 
		*/
		else if (checkKind(primary)){
			match(t.kind);
		}
		else throw new SyntaxException
		(t,"Syntax Error, invalid expression at line " + t.line() + ", position " + t.pos);
	}
	/*
	 * Function -> FunctionName ( Expression ) 
	 */
	public void function() throws SyntaxException{
		match(t.kind);
		match(Kind.LPAREN);
		expression();
		match(Kind.RPAREN);
	}
	
	
	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	/**
	 * @param kind
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (kind == Kind.EOF) {
			System.out.println("End of file"); //return t;
		}
		else if (checkKind(kind)) {
			t = scanner.nextToken();
		}
		else {
			throw new SyntaxException(t,"Syntax Error at line " + t.line() + ", position " 
			+ t.pos + ". Expect a " + kind + " token, but " + t.kind + " found");
		}
	}

}

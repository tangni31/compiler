package cop5556fa18;


import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.Statement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;

import java.util.ArrayList;
import java.util.List;

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
	
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
		//match(Kind.EOF);
	}
	
	/*
	 * Program -> Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token firstToken = t;
		match(Kind.IDENTIFIER);
		Block b = block();
		Program p = new Program(firstToken,firstToken.getName(), b);
		return p;	
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string};
	Kind[] firstStatement = {Kind.IDENTIFIER, Kind.KW_if, Kind.KW_while, Kind.KW_sleep, Kind.KW_print};
	
	public Block block() throws SyntaxException {
		match(Kind.LBRACE);
		List<PLPASTNode> declarationsAndStatements = new ArrayList<PLPASTNode>();
		Token firstToken = null;
		if (checkKind(firstDec) | checkKind(firstStatement)){
			firstToken = t;
		}
		while (checkKind(firstDec) | checkKind(firstStatement)) {
	     if (checkKind(firstDec)) {
	    	declarationsAndStatements.add(declaration());
		} else if (checkKind(firstStatement)) {
			declarationsAndStatements.add(statement());
		}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);
		Block b = new Block(firstToken, declarationsAndStatements);
		return b;
	}
	
	/*  Declaration -> Type Identifier ( = Expression | E ) | Type IDENTIFIERLIST
		IDENTIFIERLIST -> Identifier (, Identifier)*
		Type -> int | float | boolean | char | string
	 */
	public Declaration declaration() throws SyntaxException {
		Declaration decl = null;
		Token firstToken = t;
		String name = null;
		match(t.kind);
		if(checkKind(Kind.IDENTIFIER)){
			name = t.getName();
		}
		match(Kind.IDENTIFIER);
		if(checkKind(Kind.OP_ASSIGN)){ //Type Identifier = Expression
			match(Kind.OP_ASSIGN);
			Expression e = expression();
			decl = new VariableDeclaration(firstToken, firstToken.kind, name, e);
		}
		else if(checkKind(Kind.SEMI)){ //Type Identifier
			decl = new VariableDeclaration(firstToken, firstToken.kind, name, null);
		}
		else if(checkKind(Kind.COMMA)){ //Type Identifier (, Identifier)+
			List<String> names = new ArrayList<String>();
			names.add(name);
			while (checkKind(Kind.COMMA)){
				match(Kind.COMMA);
				if(checkKind(Kind.IDENTIFIER)){
					name = t.getName();
				}
				names.add(name);
				match(Kind.IDENTIFIER);
			}
			decl = new VariableListDeclaration(firstToken, firstToken.kind, names);
		}
		return decl;
	}
	
	/*	
	 * Statement -> IfStatement | AssignmentStatement | SleepStatement 
		| PrintStatement | WhileStatment
	*/
	public Statement statement() throws SyntaxException {
		Statement stat = null;
		Token firstToken = t;
		if (checkKind(Kind.KW_print)){ //PrintStatement -> print Expression
			match(Kind.KW_print);
			Expression e = expression();
			stat = new PrintStatement(firstToken, e);
		}
		else if (checkKind(Kind.KW_sleep)){ //SleepStatement -> sleep Expression
			match(Kind.KW_sleep);
			Expression e = expression();
			stat = new SleepStatement(firstToken, e);
		}
		else if (checkKind(Kind.KW_if)){  //IfStatement -> if ( Expression ) Block
			match(Kind.KW_if);
			match(Kind.LPAREN);
			Expression e = expression();
			match(Kind.RPAREN);
			Block b = block();
			stat = new IfStatement(firstToken, e, b);
		}
		else if (checkKind(Kind.KW_while)){ //WhileStatement -> while ( Expression ) Block
			match(Kind.KW_while);
			match(Kind.LPAREN);
			Expression e = expression();
			match(Kind.RPAREN);
			Block b = block();
			stat = new WhileStatement(firstToken, e, b);
		}
		else if (checkKind(Kind.IDENTIFIER)){ //AssignmentStatement -> Identifier = Expression
			ExpressionIdentifier indentifier = new ExpressionIdentifier(firstToken, t.getName());
			match(Kind.IDENTIFIER);
			match(Kind.OP_ASSIGN);
			Expression e = expression();
			String indentifierName = indentifier.name;
			stat = new AssignmentStatement(firstToken, indentifierName, e);
		}
		else throw new UnsupportedOperationException();
		return stat;
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
	//ExpressionConditional -> Expression Expression Expression
	public Expression expression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = OrExpression();
		if(checkKind(Kind.OP_QUESTION)){
			match(Kind.OP_QUESTION);
			Expression e1 = expression();
			match(Kind.OP_COLON);
			Expression e2 = expression();
			e0 = new ExpressionConditional(firstToken, e0, e1, e2);
		}
		return e0;
	}
	
	//OrExpression -> AndExpression ( | AndExpression )*
	//ExpressionBinary -> Expression op Expression 
	public Expression OrExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = AndExpression();
		while (checkKind(Kind.OP_OR)){
			match(Kind.OP_OR);
			Expression e1 = AndExpression();
			e0 = new ExpressionBinary(firstToken, e0, Kind.OP_OR, e1);
		}
		return e0;
	}
	
	//AndExpression -> EqExpression ( & EqExpression )*
	//ExpressionBinary -> Expression op Expression 
	public Expression AndExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = EqExpression();
		while (checkKind(Kind.OP_AND)){
			match(Kind.OP_AND);
			Expression e1 = EqExpression();
			e0 = new ExpressionBinary(firstToken, e0, Kind.OP_AND, e1);
		}
		return e0;
	}
	
	//EqExpression -> RelExpression ( ( == | != ) RelExpression )*
	public Expression EqExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = RelExpression();
		while (checkKind(Kind.OP_NEQ) || checkKind(Kind.OP_EQ)){
			Kind op = t.kind;
			match(t.kind);
			Expression e1 = RelExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	//RelExpression -> AddExpression ( ( < | > | <= | >= ) AddExpression )*
	public Expression RelExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = AddExpression();
		while (checkKind(rel)){
			Kind op = t.kind;
			match(t.kind);
			Expression e1 = AddExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	//AddExpression -> MultExpression ( ( + | - ) MultExpression )*
	public Expression AddExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = MultExpression();
		while (checkKind(Kind.OP_MINUS) || checkKind(Kind.OP_PLUS)){
			Kind op = t.kind;
			match(t.kind);
			Expression e1 = MultExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	//MultExpression -> PowerExpression ( ( * | / | % ) PowerExpression )*
	public Expression MultExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = PowerExpression();
		while (checkKind(mult)){
			Kind op = t.kind;
			match(t.kind);
			Expression e1 = PowerExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	//PowerExpression -> UnaryExpression ( ** PowerExpression | E )
	public Expression PowerExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = UnaryExpression();
		while (checkKind(Kind.OP_POWER)){
			match(Kind.OP_POWER);
			Expression e1 = UnaryExpression();
			e0 = new ExpressionBinary(firstToken, e0, Kind.OP_POWER, e1);
		}
		return e0;
	}
	
	/*
	 * UnaryExpression -> + UnaryExpression | - UnaryExpression |
	 *	! UnaryExpression | Primary
	 *	ExpressionUnary -> Op Expression
	 */
	public Expression UnaryExpression() throws SyntaxException{
		Token firstToken = t;
		Expression unexp = null;
		if (checkKind(UnaryExpFront)){ // + - !
			Kind op = t.kind;
			match(t.kind);
			Expression e0 = UnaryExpression();
			unexp = new ExpressionUnary(firstToken, op, e0);
		}
		else if (checkKind(Kind.LPAREN)){ //Primary -> ( Expression )
			match(Kind.LPAREN);
			unexp = expression();
			match(Kind.RPAREN);
		}
		else if (checkKind(functionNames)){//Primary ->  Function
			unexp = function();
		}
		/*
		 * Primary -> INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | CHAR_LITERAL
 			| STRING_LITERAL | IDENTIFIER 
		*/
		else if (checkKind(primary)){
			unexp = primary();
			//match(t.kind);
		}
		else throw new SyntaxException
		(t,"Syntax Error, invalid expression at line " + t.line() + ", position " + t.pos);
		return unexp;
	}
	
	public Expression primary() throws SyntaxException{
		Expression e = null;
		Token firstToken = t;
		switch (t.kind){
		case INTEGER_LITERAL:
			e = new ExpressionIntegerLiteral(firstToken, Integer.valueOf(t.getName()));
			match(t.kind);
			break;
		case BOOLEAN_LITERAL:
			e = new ExpressionBooleanLiteral(firstToken, Boolean.valueOf(t.getName()));
			match(t.kind);
			break;
		case FLOAT_LITERAL:
			e = new ExpressionFloatLiteral(firstToken, Float.valueOf(t.getName()));
			match(t.kind);
			break;
		case CHAR_LITERAL:
			e = new ExpressionCharLiteral(firstToken, t.getName().charAt(1));// 'c' : c at pos 1
			match(t.kind);
			break;
		case STRING_LITERAL:
			String text = t.getName();  // "\"string\""
			text = text.substring(1, text.length()-1); //remove " "
			e = new ExpressionStringLiteral(firstToken, text);
			match(t.kind);
			break;
		case IDENTIFIER:
			e = new ExpressionIdentifier(firstToken, t.getName());
			match(t.kind);
			break;
		default:
			throw new SyntaxException(t,"Syntax Error");
		}
		return e;
	}

	/*
	 * Function -> FunctionName ( Expression ) 
	 */
	public FunctionWithArg function() throws SyntaxException{
		Token firstToken = t;
		match(t.kind);
		match(Kind.LPAREN);
		Expression e = expression();
		match(Kind.RPAREN);
		return new FunctionWithArg(firstToken, firstToken.kind, e);
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
	
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error when match EOF");
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

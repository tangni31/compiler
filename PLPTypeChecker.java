package cop5556fa18;

 

import java.util.List;

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
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes.Type;

public class PLPTypeChecker implements PLPASTVisitor {
	
	PLPTypeChecker() {
	}
	
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}
	
	SymbolTable symTable = new SymbolTable(); //create symbol table
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}
	
	//Block ->  enterScope  ( Declaration | Statement )*  leaveScope
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symTable.enterScope();
		for(PLPASTNode n : block.declarationsAndStatements){
			n.visit(this, arg);
		}
		symTable.leaveScope();
		return null;
	}
	
	/* 
	 * VariableDeclaration -> Type IDENTIFIER ( null | Expression )
	 * Declaration.name <- IDENTIFIER.name
	 * Declaration.name not in SymbolTable.currentScope
	 * Expression == null   or  Expression.type == type
	 * SymbolTable <- SymbolTable.insert(name, Declaration)
	 */
	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		boolean res = symTable.insert(declaration.name, declaration);
		if(!res){
			throw new SemanticException
					(declaration.firstToken, "Error in visitVariablDeclaration, "
							+ "failed to insert declaration: " + declaration.name);
		}
		Type t0 = PLPTypes.getType(declaration.type); //type
		Expression e = declaration.expression;
		if(e != null){
			Type t1 = (Type) e.visit(this, arg); //Expression.type
			if(!t0.equals(t1)){	//Expression.type != type
				throw new SemanticException(declaration.firstToken, "Error in visitVariablDeclaration, "
						+ "type of identifier must be equal to type of expression");
			}
		}
		declaration.setType(t0);
		return declaration.getType();
	}
	
	
	/*
	 * VariableListDeclaration -> Type IDENTIFIER  IDENTIFIER +
	 * for each IDENTIFIER: SymbolTable.insert(name, Declaration)
	 */
	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		List<String> names = declaration.names;
		for(String name : names){
			boolean res = symTable.insert(name, declaration);
			if(!res){
				throw new SemanticException
					(declaration.firstToken, "Error in visitVariableListDeclaration, "
							+ "fail to insert declaration: "+ name);
			}
		}
		Type t = PLPTypes.getType(declaration.type);
		declaration.setType(t);
		return declaration.getType();
	}

	//ExpressionBooleanLiteral.type <- boolean
	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.setType(Type.BOOLEAN);
		return expressionBooleanLiteral.getType();
	}

	//ExpressionBinary.type <- inferredType(Expression0.type, Expression1.type, op)
	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Kind op = expressionBinary.op;
		Type t0 = (Type) expressionBinary.leftExpression.visit(this, arg);
		Type t1 = (Type) expressionBinary.rightExpression.visit(this, arg);
		//boolean op boolean -> boolean   op: &, |, ==, !=, >,>=, <, <=   
		if(t0.equals(Type.BOOLEAN) && t1.equals(Type.BOOLEAN)){
			if(op.equals(Kind.OP_AND) || op.equals(Kind.OP_OR) || op.equals(Kind.OP_NEQ) || op.equals(Kind.OP_EQ) 
			|| op.equals(Kind.OP_GE) || op.equals(Kind.OP_GT) || op.equals(Kind.OP_LE) || op.equals(Kind.OP_LT)){
				expressionBinary.setType(Type.BOOLEAN);
			}
			else{
				throw new SemanticException(expressionBinary.firstToken, "Error in visitExpressionBinary, "
						+ "Type.BOOLEAN expression does not support "+ op.toString());
			}
		}
		//integer op integer -> integer   op: +,-,*,/,%,**, &, |   
		//integer op integer -> boolean   op: ==, !=, >,>=, <, <=   
		else if(t0.equals(Type.INTEGER) && t1.equals(Type.INTEGER)){
			if(op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_MINUS) || op.equals(Kind.OP_TIMES) || op.equals(Kind.OP_DIV)
			|| op.equals(Kind.OP_MOD) || op.equals(Kind.OP_POWER) || op.equals(Kind.OP_AND) || op.equals(Kind.OP_OR)){
				expressionBinary.setType(Type.INTEGER);
			}
			else if(op.equals(Kind.OP_NEQ) || op.equals(Kind.OP_EQ) || op.equals(Kind.OP_GE) || op.equals(Kind.OP_GT) 
			|| op.equals(Kind.OP_LE) || op.equals(Kind.OP_LT)){
				expressionBinary.setType(Type.BOOLEAN);
			}
			else{
				throw new SemanticException(expressionBinary.firstToken, "Error in visitExpressionBinary, "
						+ "Type.INTEGER expression does not support "+ op.toString());
			}
		}
		//string op string -> string   op: +   	
		else if(t0.equals(Type.STRING) && t1.equals(Type.STRING)){
			if(op.equals(Kind.OP_PLUS)){
				expressionBinary.setType(Type.STRING);
			}
			else{
				throw new SemanticException(expressionBinary.firstToken, "Error in visitExpressionBinary, "
						+ "Type.STRING expression does not support "+ op.toString());
			}
		}
		//float op float -> float     op: +,-,*,/,**
		//float op float -> boolean   op: ==, !=, >,>=, <, <=   
		else if(t0.equals(Type.FLOAT) && t1.equals(Type.FLOAT)){
			if(op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_MINUS) || op.equals(Kind.OP_TIMES) || op.equals(Kind.OP_DIV)
			|| op.equals(Kind.OP_POWER)){
				expressionBinary.setType(Type.FLOAT);
			}
			else if(op.equals(Kind.OP_NEQ) || op.equals(Kind.OP_EQ) || op.equals(Kind.OP_GE) || op.equals(Kind.OP_GT) 
			|| op.equals(Kind.OP_LE) || op.equals(Kind.OP_LT)){
				expressionBinary.setType(Type.BOOLEAN);
			}
			else{
				throw new SemanticException(expressionBinary.firstToken, "Error in visitExpressionBinary, "
					+ "Type.FLOAT expression does not support "+ op.toString());
			}
		}
		//(float op integer) || (integer op float) -> float	   op:  +,-,*,/,**
		else if((t0.equals(Type.FLOAT) || t0.equals(Type.INTEGER)) && (t1.equals(Type.FLOAT) || t1.equals(Type.INTEGER))){
			if(op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_MINUS) || op.equals(Kind.OP_TIMES) || op.equals(Kind.OP_DIV)
			|| op.equals(Kind.OP_POWER)){
				expressionBinary.setType(Type.FLOAT);
			}
			else{
				throw new SemanticException(expressionBinary.firstToken, "Error in visitExpressionBinary, "
					+ "Type.FLOAT and Type.INGETER expressions do not support "+ op.toString());
			}
		}
		else{
			throw new SemanticException(expressionBinary.firstToken, "Error in visitExpressionBinary, "
				+ "can not combine type " + t0.toString() + " and type " + t1.toString());
		}
		return expressionBinary.getType();
	}
	
	//ExpressionConditional -> Expression0 Expression1 Expression2
	//Expression0.type == boolean   Expression1.type == Expression2.type
	//ExpressionConditional.type <- Expression1.type
	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Type t0 = (Type) expressionConditional.condition.visit(this, arg);
		Type t1 = (Type) expressionConditional.trueExpression.visit(this, arg);
		Type t2 = (Type) expressionConditional.falseExpression.visit(this, arg);
		if(!t0.equals(Type.BOOLEAN)){ //Expression0 .type != boolean
			throw new SemanticException(expressionConditional.firstToken, "Error in expressionConditional, "
				+ "type of condition expression must be BOOLEAN");
		}
		if(!t1.equals(t2)){ //Expression1.type != Expression2.type
			throw new SemanticException(expressionConditional.firstToken, "Error in expressionConditional, "
				+ "type of true and false expressions must be the same");
		}
		expressionConditional.setType(t1);
		return expressionConditional.getType();
	}

	//ExpressionFloatLiteral.type <- float
	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.setType(Type.FLOAT);
		return expressionFloatLiteral.getType();
	}

	//FunctionWithArg.type <- inferredTypeFunctionWithArg (FunctionName, Expression.type)
	@Override
	public Object visitFunctionWithArg(FunctionWithArg functionWithArg, Object arg) throws Exception {
		Type t = (Type) functionWithArg.expression.visit(this, arg);
		Kind name = functionWithArg.functionName;
		switch (name){
		//abs(integer||float) -> integer||float
		case KW_abs:{
			if(!t.equals(Type.INTEGER) && !t.equals(Type.FLOAT)){
				throw new SemanticException(functionWithArg.firstToken, "Expression Type for function "
					+ "abs() must be integer or float!");
			}
			functionWithArg.setType(t);
		}
		break;
		//sin,cos,atan,log(float) -> float
		case KW_sin:
		case KW_cos:
		case KW_atan:
		case KW_log:
		{
			if(!t.equals(Type.FLOAT)){
				throw new SemanticException(functionWithArg.firstToken, "Expression Type for function "
					+ name.name() + "() must be float!");
			}
			functionWithArg.setType(t);
		}
		break;
		//float(float||int) -> float
		case KW_float:{
			if(!t.equals(Type.INTEGER) && !t.equals(Type.FLOAT)){
				throw new SemanticException(functionWithArg.firstToken, "Expression Type for function "
					+ "float() must be integer or float!");
			}
			functionWithArg.setType(Type.FLOAT);
		}
		break;
		//int(int||float) -> int
		case KW_int:{
			if(!t.equals(Type.INTEGER) && !t.equals(Type.FLOAT)){
				throw new SemanticException(functionWithArg.firstToken, "Expression Type for function "
					+ "int() must be integer or float!");
			}
			functionWithArg.setType(Type.INTEGER);
		}
		break;
		default: //should never reach here
			throw new SemanticException(functionWithArg.firstToken, "No such function");	
		}
		return functionWithArg.getType();
	}

	/*
	 * (ExpressionIdent.dec <- SymbolTable.lookup(ExpressionIdent.name)
	 * ExpressionIdent.dec != null
	 * ExpressionIdent.type <- ExpressionIdent.dec.type
	 */
	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		Declaration d = symTable.lookup(expressionIdent.name);
		if(d == null){ //ExpressionIdent.dec == null
			throw new SemanticException(expressionIdent.firstToken, "Error in visitExpressionIdent, can not "
					+ "find ident:" + expressionIdent.name + " in symbol table.");
		}
		Type t = d.getType();
		expressionIdent.setType(t);
		return expressionIdent.getType();
	}

	//ExpressionIntegerLiteral.type <- integer
	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.setType(Type.INTEGER);
		return expressionIntegerLiteral.getType();
	}

	//ExpressionCharLiteral <- string
	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		expressionStringLiteral.setType(Type.STRING);
		return expressionStringLiteral.getType();
	}

	//ExpressionCharLiteral <- char
	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		expressionCharLiteral.setType(Type.CHAR);
		return expressionCharLiteral.getType();
	}

	//LHS.type == Expression.type
	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		Type t1 = (Type) statementAssign.lhs.visit(this, arg);
		Type t2 = (Type) statementAssign.expression.visit(this, arg);
		if(!t1.equals(t2)){
			throw new SemanticException(statementAssign.firstToken,"Error in AssignmentStatement,"
					+ " type of lhs should be equal to type of expression");
		}
		return null;
	}

	//Expression.type == boolean
	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Type t = (Type) ifStatement.condition.visit(this, arg); //Expression.type == boolean
		if(!t.equals(Type.BOOLEAN)){ 
			throw new SemanticException(ifStatement.firstToken,"Error in IfStatement, "
					+ "type of condition should be BOOLEAN, but " + t.toString() + " encounter");
		}
		ifStatement.block.visit(this, arg);
		return null;
	}

	//Expression.type == boolean
	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Type t = (Type) whileStatement.condition.visit(this, arg); //Expression.type == boolean
		if(!t.equals(Type.BOOLEAN)){ 
			throw new SemanticException(whileStatement.firstToken,"Error in whileStatement,"
				+ "type of condition should be BOOLEAN, but " + t.toString() + " encounter");
		}
		whileStatement.b.visit(this, arg);
		return null;
	}

	//Expression.type == {int, boolean, float, char, string}
	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		Type t = (Type) printStatement.expression.visit(this, arg); 
		if(t.equals(Type.NONE)){ //Expression.type != boolean
			throw new SemanticException(printStatement.firstToken,"Error in visitPrintStatement "
					+ "content can not be empty");
		}
		return null;
	}

	//Expression.type == integer
	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Type t = (Type) sleepStatement.time.visit(this, arg); //Expression.type == INTEGER
		if(!t.equals(Type.INTEGER)){ 
			throw new SemanticException(sleepStatement.firstToken,"Error in visitSleepStatement "
					+ "time should be INTEGER, but " + t.toString() + " encounter");
		}
		return null;
	}

	//ExpressionUnary -> Op Expression	 ExpressionUnary.type <- Expression.type
	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		Type t = (Type) expressionUnary.expression.visit(this, arg);
		expressionUnary.setType(t);
		return expressionUnary.getType();
	}

	//LHS -> IDENTIFIER    LHS.name <- IDENTIFIER.name   LHS.dec <- SymbolTable.lookup(LHS.name)
	//LHS.dec != null   LHS.type <- LHS.dec.type	
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		String name = lhs.identifier;
		Declaration d = symTable.lookup(name);
		if(d == null){
			throw new SemanticException(lhs.firstToken,"Error in visitLHS, " + name + " not found!");
		}
		Type t = d.getType();
		lhs.setType(t);
		return lhs.getType();
	}

}

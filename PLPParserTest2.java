package cop5556fa18;


import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPParser.SyntaxException;
import cop5556fa18.PLPScanner;
import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.Statement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPScanner.Kind;



@SuppressWarnings("unused")
public class PLPParserTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static final boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}


		//creates and returns a parser for the given input.
		private PLPParser makeParser(String input) throws LexicalException {
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			PLPParser parser = new PLPParser(scanner);
			return parser;
		}
		
		/**
		 * Test case with an empty program.  This throws an exception 
		 * because it lacks an identifier and a block
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testEmpty() throws LexicalException, SyntaxException {
			String input = "";  //The input is the empty string.  
			thrown.expect(SyntaxException.class);
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
		}
		
		/**
		 * Smallest legal program.
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testSmallest() throws LexicalException, SyntaxException {
			String input = "b{}";  
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);
			assertEquals("b", p.name);
			assertEquals(0, p.block.declarationsAndStatements.size());
		}	
		
		
		/**
		 * Utility method to check if an element of a block at an index is a declaration with a given type and name.
		 * 
		 * @param block
		 * @param index
		 * @param type
		 * @param name
		 * @return
		 */
		Declaration checkDec(Block block, int index, Kind type, String name) {
			PLPASTNode node = block.declarationsAndStatements(index);
			assertEquals(VariableDeclaration.class, node.getClass());
			VariableDeclaration dec = (VariableDeclaration) node;
			assertEquals(type, dec.type);
			assertEquals(name, dec.name);
			return dec;
		}	
		
		@Test
		public void testDec0() throws LexicalException, SyntaxException {
			String input = "b{int i; char c;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);	
			checkDec(p.block, 0, Kind.KW_int, "i");
			checkDec(p.block, 1, Kind.KW_char, "c");
		}
		
		Declaration checkDecList(Block block, int indDec, int indName, Kind type, String name) {
			PLPASTNode node = block.declarationsAndStatements(indDec);
			VariableListDeclaration dec = (VariableListDeclaration) node;
			assertEquals(type, dec.type);
			assertEquals(name, dec.names(indName));
			return dec;
		}	
		
		@Test
		public void testDecList0() throws LexicalException, SyntaxException {
			String input = "b{int i,b,c,d;}"; //test VariableListDeclaration
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);
			checkDecList(p.block, 0, 0, Kind.KW_int, "i");
			checkDecList(p.block, 0, 1, Kind.KW_int, "b");
			checkDecList(p.block, 0, 2, Kind.KW_int, "c");
			checkDecList(p.block, 0, 3, Kind.KW_int, "d");
		}
		/** 
		 * Test a specific grammar element by calling a corresponding parser method rather than parse.
		 * This requires that the methods are visible (not private). 
		 * 
		 * @throws LexicalException
		 * @throws SyntaxException
		 */
		
		@Test
		public void testExpression0() throws LexicalException, SyntaxException {
			String input = "x + 2";
			PLPParser parser = makeParser(input);
			Expression e = parser.expression();  //call expression here instead of parse
			show(e);	
			assertEquals(ExpressionBinary.class, e.getClass());
			ExpressionBinary b = (ExpressionBinary)e;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
			ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("x", left.name);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(2, right.value);
			assertEquals(Kind.OP_PLUS, b.op);
		}
		
		@Test
		public void testExpression1() throws LexicalException, SyntaxException {
			String input = "10 + x * y";  //test expression
			PLPParser parser = makeParser(input);
			Expression e = parser.expression();
			show(e);	
			assertEquals(ExpressionBinary.class, e.getClass());
			ExpressionBinary b = (ExpressionBinary)e;
			assertEquals(ExpressionIntegerLiteral.class, b.leftExpression.getClass());
			ExpressionIntegerLiteral left = (ExpressionIntegerLiteral)b.leftExpression;
			assertEquals(10, left.value);
			assertEquals(Kind.OP_PLUS, b.op);
			assertEquals(ExpressionBinary.class, b.rightExpression.getClass());
			ExpressionBinary b1 = (ExpressionBinary)b.rightExpression;
			ExpressionIdentifier left1 = (ExpressionIdentifier)b1.leftExpression;
			ExpressionIdentifier right1 = (ExpressionIdentifier)b1.rightExpression;
			assertEquals("x", left1.name);
			assertEquals(Kind.OP_TIMES, b1.op);
			assertEquals("y", right1.name);
		}
		
		Statement getStatement(Block block, int index) { //get a statement from a block
			PLPASTNode node = block.declarationsAndStatements(index);
			Statement stat = (Statement) node;
			return stat;
		}
		
		@Test
		public void testFunction0() throws LexicalException, SyntaxException {
			String input = "float z = abs(x+1)*sin(y)"; // declaration, expression, function
			PLPParser parser = makeParser(input);
			Declaration d = parser.declaration();
			assertEquals(VariableDeclaration.class, d.getClass());
			VariableDeclaration dV = (VariableDeclaration) d; //float z = abs(x+1)*sin(y)
			assertEquals("z", dV.name); // z
			assertEquals(Kind.KW_float, dV.type); //float
			// abs(x+1)*sin(y)
			assertEquals(ExpressionBinary.class, dV.expression.getClass());
			ExpressionBinary e = (ExpressionBinary) dV.expression; 
			assertEquals(Kind.OP_TIMES, e.op); // *
			// abs(x+1)
			assertEquals(FunctionWithArg.class, e.leftExpression.getClass()); 
			FunctionWithArg el = (FunctionWithArg) e.leftExpression;
			assertEquals(Kind.KW_abs, el.functionName);
			assertEquals(ExpressionBinary.class, el.expression.getClass());
			ExpressionBinary f1 = (ExpressionBinary) el.expression; // x+1
			assertEquals(ExpressionIdentifier.class, f1.leftExpression.getClass()); // x
			ExpressionIdentifier f1l = (ExpressionIdentifier) f1.leftExpression;
			assertEquals("x", f1l.name);
			assertEquals(Kind.OP_PLUS, f1.op);
			assertEquals(ExpressionIntegerLiteral.class, f1.rightExpression.getClass()); // 1
			ExpressionIntegerLiteral f1r = (ExpressionIntegerLiteral) f1.rightExpression;
			assertEquals(1, f1r.value);
			// sin(y)
			assertEquals(FunctionWithArg.class, e.rightExpression.getClass()); 
			FunctionWithArg er = (FunctionWithArg) e.rightExpression;
			assertEquals(Kind.KW_sin, er.functionName);
			assertEquals(ExpressionIdentifier.class, er.expression.getClass()); // y
			ExpressionIdentifier f2 = (ExpressionIdentifier) er.expression;
			assertEquals("y", f2.name);
		}
		
		@Test
		public void testStatement0() throws LexicalException, SyntaxException {
			String input = "if(a==true){x = 1;}"; //test if statement
			PLPParser parser = makeParser(input);
			Statement stat = parser.statement();
			assertEquals(IfStatement.class, stat.getClass());
			IfStatement ifStat = (IfStatement) stat;
			assertEquals(ExpressionBinary.class, ifStat.condition.getClass());
			ExpressionBinary condition = (ExpressionBinary) ifStat.condition;
			ExpressionIdentifier left = (ExpressionIdentifier)condition.leftExpression;
			assertEquals("a", left.name);
			assertEquals(Kind.OP_EQ, condition.op);
			ExpressionBooleanLiteral right = (ExpressionBooleanLiteral)condition.rightExpression;
			assertEquals(true, right.value);
			Block b2 = ifStat.block;
			Statement stat2 = getStatement(b2, 0);
			assertEquals(AssignmentStatement.class, stat2.getClass());
			AssignmentStatement asStat = (AssignmentStatement) stat2;
			assertEquals("x", asStat.identifier);
			ExpressionIntegerLiteral asStatExp = (ExpressionIntegerLiteral) asStat.expression;
			assertEquals(1, asStatExp.value);
		}
		
		@Test
		public void testPrintStatement0() throws LexicalException, SyntaxException {
			String input = "print 'c'"; //test print char
			PLPParser parser = makeParser(input);
			Statement s = parser.statement();
			assertEquals(PrintStatement.class, s.getClass());
			PrintStatement s1 = (PrintStatement) s;
			Expression e = s1.expression;
			assertEquals(ExpressionCharLiteral.class, e.getClass());
			ExpressionCharLiteral e1 = (ExpressionCharLiteral) e;
			assertEquals('c', e1.text);
		}
		
		@Test
		public void testPrintStatement1() throws LexicalException, SyntaxException {
			String input = "print \"fafaag\""; //test print string
			PLPParser parser = makeParser(input);
			Statement s = parser.statement();
			assertEquals(PrintStatement.class, s.getClass());
			PrintStatement s1 = (PrintStatement) s;
			Expression e = s1.expression;
			assertEquals(ExpressionStringLiteral.class, e.getClass());
			ExpressionStringLiteral e1 = (ExpressionStringLiteral) e;
			assertEquals("fafaag", e1.text);
		}
		
		@Test
		public void testSleepStatement0() throws LexicalException, SyntaxException {
			String input = "sleep 2*3-10/5"; //test sleep time   (2*2) - (10/5)
			PLPParser parser = makeParser(input);
			Statement s = parser.statement();
			assertEquals(SleepStatement.class, s.getClass());
			SleepStatement s1 = (SleepStatement) s;
			Expression e = s1.time;
			assertEquals(ExpressionBinary.class, e.getClass());
			ExpressionBinary b = (ExpressionBinary)e;
			assertEquals(ExpressionBinary.class, b.leftExpression.getClass());
			//bN = bNl op bNr,  op = -,	bNl = 2*3, 	bNr = 10/5
			ExpressionBinary bN = (ExpressionBinary)b;
			assertEquals(Kind.OP_MINUS, bN.op); // -
			// bNl = 2*3
			assertEquals(ExpressionBinary.class, bN.leftExpression.getClass());
			ExpressionBinary bNl = (ExpressionBinary)bN.leftExpression;  
			assertEquals(ExpressionIntegerLiteral.class, bNl.leftExpression.getClass()); // 2
			ExpressionIntegerLiteral bNll = (ExpressionIntegerLiteral)bNl.leftExpression;
			assertEquals(2, bNll.value);
			assertEquals(Kind.OP_TIMES, bNl.op);	// *
			assertEquals(ExpressionIntegerLiteral.class, bNl.rightExpression.getClass()); // 3
			ExpressionIntegerLiteral bNlr = (ExpressionIntegerLiteral)bNl.rightExpression;
			assertEquals(3, bNlr.value);
			// bNr = 10/5
			ExpressionBinary bNr = (ExpressionBinary)bN.rightExpression;  
			assertEquals(ExpressionIntegerLiteral.class, bNr.leftExpression.getClass()); // 10
			ExpressionIntegerLiteral bNrl = (ExpressionIntegerLiteral)bNr.leftExpression;
			assertEquals(10, bNrl.value);
			assertEquals(Kind.OP_DIV, bNr.op);	// /
			assertEquals(ExpressionIntegerLiteral.class, bNr.rightExpression.getClass()); // 5
			ExpressionIntegerLiteral bNrr = (ExpressionIntegerLiteral)bNr.rightExpression;
			assertEquals(5, bNrr.value);
		}
}

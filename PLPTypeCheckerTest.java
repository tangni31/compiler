package cop5556fa18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner;
import cop5556fa18.PLPTypeChecker.SemanticException;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.Program;

public class PLPTypeCheckerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scan, parse, and type check an input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		PLPScanner scanner = new PLPScanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new PLPParser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		PLPASTVisitor v = new PLPTypeChecker();
		ast.visit(v, null);
	}
	
	
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void dec1() throws Exception {
		String input = "prog {int a;}";
		typeCheck(input);
	}
	
	@Test
	public void dec2() throws Exception {
		String input = "prog {int a,b,c;}";
		typeCheck(input);
	}
	
	@Test
	public void dec3() throws Exception {
		String input = "prog {int a = 1;}";
		typeCheck(input);
	}
	
	@Test
	public void statment1() throws Exception {
		String input = "prog {int a=1; if(a==1){a=a-1;};}";
		typeCheck(input);
	}
	
	@Test
	public void expression1() throws Exception {
		String input = "prog {print 1+2;}";
		typeCheck(input);
	}
	
	@Test
	public void expression2() throws Exception {
		String input = "prog {int a = 1+2;}";
		typeCheck(input);
	}
	
	@Test
	public void expression3() throws Exception {
		String input = "prog {int a; int b; int c; a = 1 == 2 ? b : c;}";
		typeCheck(input);
	}
	
	@Test
	public void expression4() throws Exception {
		String input = "prog {print \'c\';}"; //print char
		typeCheck(input);
	}
	
	@Test
	public void expression5() throws Exception {
		String input = "prog {print \"string\";}"; //print string
		typeCheck(input);
	}
	
	@Test
	public void expression6() throws Exception {
		String input = "prog {float a; int b; a=float(b);}";
		typeCheck(input);
	}
	
	@Test
	public void expression7() throws Exception {
		String input = "prog {float a; float b; a=float(b);}";
		typeCheck(input);
	}
	
	@Test
	public void expression8() throws Exception {
		String input = "prog {int a;int b;boolean c;a=a&b;a=a|b;a=a+b;a=a*b;a=a/b;a=a%b;a=a**b;a=a-b;"
				+ "c=a==b;c=a>=b;c=a<=b;c=a>b;c=a<b;c=a!=b;}";
		typeCheck(input);
	}
	
	@Test
	public void expression9() throws Exception {
		String input = "prog {float a; float b;boolean c;a=a+b;a=a*b;a=a/b;a=a**b;a=a-b;"
				+ "c=a==b;c=a>=b;c=a<=b;c=a>b;c=a<b;c=a!=b;}";
		typeCheck(input);
	}
	
	@Test
	public void expression10() throws Exception {
		String input = "prog {float a,b; int c; a=b+c; a=c+b; a=c-b;a=b-c;a=b*c;a=c/b;a=b**c;}";
		typeCheck(input);
	}
	
	@Test
	public void expression11() throws Exception {
		String input = "prog {boolean a,b,c;c=a==b;c=a>=b;c=a<=b;c=a>b;c=a<b;c=a!=b;}";
		typeCheck(input);
	}
	
	@Test
	public void expression12() throws Exception {
		String input = "prog {string a,b; b=a+b;}";
		typeCheck(input);
	}
	
	@Test
	public void function1() throws Exception {
		String input = "prog {float a = cos(3.14);}";
		typeCheck(input);
	}
	
	@Test
	public void function2() throws Exception {
		String input = "prog {float a = abs(-3.14);}";
		typeCheck(input);
	}
	
	@Test
	public void function3() throws Exception {
		String input = "prog {float a = log(-3.14);}";
		typeCheck(input);
	}
	
	@Test
	public void program1() throws Exception {
		String input = "prog {float a,b,c; int d; a=1.0;b=a**2;c=b*2.5; d=10%2; if(a!=b){c=log(a+b);};}";
		typeCheck(input);
	}
	
	@Test
	public void dec1_fail1() throws Exception {
		String input = "prog {boolean a = 1;}";//can't assign int to boolean 
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void dec1_fail2() throws Exception {
		String input = "prog {boolean a; int a;}";//Declare 'a' twice in one scope 
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression1_fail() throws Exception {
		String input = "prog {boolean a = true+false;}";//boolean doesn't support + 
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression2_fail() throws Exception {
		String input = "prog {boolean a = 1+2;}";//can't assign int to boolean 
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression3_fail() throws Exception {
		String input = "prog { print true+4; }"; //incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression4_fail() throws Exception {
		String input = "prog {float a,b; a=a%b; }"; //float doesn't support %
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression5_fail() throws Exception {
		String input = "prog {char a,b; a=a+b; }"; //char doesn't support +
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression6_fail() throws Exception {
		String input = "prog {int a; a = true;}"; //can't assign boolean to int
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression7_fail() throws Exception {
		String input = "prog {int a,b;float c; a = b+c;}"; //can assign float to int
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression8_fail() throws Exception {
		String input = "prog {int a; b=1;}"; //b not declared
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression9_fail() throws Exception {
		String input = "prog {float a,b; a = a|b;}"; //float doesn't support |
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void statement1_fail() throws Exception {
		String input = "prog {if(2){sleep 4;};}"; //Expression is not boolean
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void statement2_fail() throws Exception {
		String input = "prog {sleep 4.1;}"; //sleep only support int
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void function1_fail() throws Exception {
		String input = "prog {float a = sin(4);}"; //sin only support float
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
}

package cop5556fa18;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner;
import cop5556fa18.PLPParser;
import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPParser.SyntaxException;

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
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();
			show(scanner);
			PLPParser parser = new PLPParser(scanner);
			return parser;
		}	

		/**
		 * An empty program.  This throws an exception because it lacks an identifier and a block. 
		 * The test case passes because the unit test expects an exception.
		 *  
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testEmpty() throws LexicalException, SyntaxException {
			String input = "";  
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
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
			parser.parse();
		}	
		
		//This test will fail in the starter code. However, it should pass in a complete parser.
		@Test
		public void testDec0() throws LexicalException, SyntaxException {
			String input = "b{int c;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testDec1() throws LexicalException, SyntaxException {
			String input = "b{int c,d,f,g,h;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testFun0() throws LexicalException, SyntaxException {
			String input = "bbb{f=sin(aaa)+x;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testFun1() throws LexicalException, SyntaxException {
			String input = "bbvvv{f=log(x+a-c**2)*abs(-x);}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testPrint0() throws LexicalException, SyntaxException {
			String input = "b{print (aaa);}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testPrint1() throws LexicalException, SyntaxException {
			String input = "b{print 1+1;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testIf0() throws LexicalException, SyntaxException {
			String input = "b{if(a==b){print (aaa);};}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testExp0() throws LexicalException, SyntaxException {
			String input = "b{a=b==c;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testExp1() throws LexicalException, SyntaxException {
			String input = "b{a=b|c&d;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		@Test
		public void testExp2() throws LexicalException, SyntaxException {
			String input = "b{a= 2*3.1%c-(h+j)**3/4;}";  
			PLPParser parser = makeParser(input);
			parser.parse();
		}	
		
		
		/*		Invalid Test		*/
		@Test
		public void testInvalidExp() throws LexicalException, SyntaxException {
			String input = "b{a=;}"; 
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			try {
				parser.parse();
			} catch (SyntaxException e) { 
				show(e);                   
				throw e;                  
			}	
		}
		@Test
		public void testInvalidDeclar() throws LexicalException, SyntaxException {
			String input = "b{int a,b,}"; 
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			try {
				parser.parse();
			} catch (SyntaxException e) { 
				show(e);                   
				throw e;                  
			}	
		}
		@Test
		public void testInvalidStat() throws LexicalException, SyntaxException {
			String input = "b{if(a==b)}"; 
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			try {
				parser.parse();
			} catch (SyntaxException e) { 
				show(e);                   
				throw e;                  
			}	
		}
		@Test
		public void testInvalidStat1() throws LexicalException, SyntaxException {
			String input = "b{while(a==b)}"; 
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			try {
				parser.parse();
			} catch (SyntaxException e) { 
				show(e);                   
				throw e;                  
			}	
		}
		@Test
		public void testInvalidStat2() throws LexicalException, SyntaxException {
			String input = "b{sleep(a}"; 
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			try {
				parser.parse();
			} catch (SyntaxException e) { 
				show(e);                   
				throw e;                  
			}	
		}
		@Test
		public void testInvalidStat3() throws LexicalException, SyntaxException {
			String input = "b{*f=1;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			try {
				parser.parse();
			} catch (SyntaxException e) { 
				show(e);                   
				throw e;                  
			}	
		}
		@Test
		public void testInvalidFun0() throws LexicalException, SyntaxException {
			String input = "bbb{f=sin()+x;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			try {
				parser.parse();
			} catch (SyntaxException e) { 
				show(e);                   
				throw e;                  
			}	
		}
}

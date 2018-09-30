/**
 * JUunit tests for the Scanner
 */

package cop5556fa18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPScanner.Token;

public class PLPScannerTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}

		/**
		 *Retrieves the next token and checks that it is an EOF token. 
		 *Also checks that this was the last token.
		 *
		 * @param scanner
		 * @return the Token that was retrieved
		 */
		
		Token checkNextIsEOF(PLPScanner scanner) {
			PLPScanner.Token token = scanner.nextToken();
			assertEquals(PLPScanner.Kind.EOF, token.kind);
			assertFalse(scanner.hasTokens());
			return token;
		}


		/**
		 * Retrieves the next token and checks that its kind, position, length, line, and position in line
		 * match the given parameters.
		 * 
		 * @param scanner
		 * @param kind
		 * @param pos
		 * @param length
		 * @param line
		 * @param pos_in_line
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind, int pos, int length, int line, int pos_in_line) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(pos, t.pos);
			assertEquals(length, t.length);
			assertEquals(line, t.line());
			assertEquals(pos_in_line, t.posInLine());
			return t;
		}

		/**
		 * Retrieves the next token and checks that its kind and length match the given
		 * parameters.  The position, line, and position in line are ignored.
		 * 
		 * @param scanner
		 * @param kind
		 * @param length
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind, int length) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(length, t.length);
			return t;
		}
		


		/**
		 * Simple test case with an empty program.  The only Token will be the EOF Token.
		 *   
		 * @throws LexicalException
		 */
		@Test
		public void testEmpty() throws LexicalException {
			String input = "";  //The input is the empty string.  This is legal
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
		}

		
		/**
		 * This example shows how to test that your scanner is behaving when the
		 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
		 * 
		 * The example shows catching the exception that is thrown by the scanner,
		 * looking at it, and checking its contents before rethrowing it.  If caught
		 * but not rethrown, then JUnit won't get the exception and the test will fail.  
		 * 
		 * The test will work without putting the try-catch block around 
		 * new Scanner(input).scan(); but then you won't be able to check 
		 * or display the thrown exception.
		 * 
		 * @throws LexicalException
		 */
		@Test
		public void failIllegalChar() throws LexicalException {
			String input = ";;~";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(2,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		
		/**
		 * Using the two previous functions as a template.  You can implement other JUnit test cases. 
		 * 
		 */
		/***************************Valid Cases************************/
		@Test
		public void vaildFloat() throws LexicalException {
			String input = "float=-0.3145;";  //a valid float
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			checkNext(scanner, PLPScanner.Kind.KW_float,0,5,1,1);
		}
		@Test
		public void vaildPrint() throws LexicalException {
			String input = "print(\"abcd\"); print(score); print(B);";  //The input is the empty string.  This is legal
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			checkNext(scanner, PLPScanner.Kind.KW_print,0,5,1,1);
			checkNext(scanner, PLPScanner.Kind.LPAREN,5,1,1,6);
		}
		@Test
		public void vaildIf() throws LexicalException {
			String input = "if(a!=b){if(c==false){f='c';}a=x/0.1;};"; //valid if,char,float,div,assign,equal,not equal
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			//checkNextIsEOF(scanner);
		}
		@Test
		public void vaildIdentifier() throws LexicalException {
			String input = "_____a1_2_fc";//valid identifier
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			//checkNextIsEOF(scanner);
		}
		@Test
		public void vaildBoolean() throws LexicalException {
			String input = "boolean _adr13__ = true;";  
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			//(scanner);
		}
		@Test
		public void vailAssign() throws LexicalException {
			String input = "double d,a,f;";  
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			//checkNextIsEOF(scanner);
		}
		@Test
		public void vailExper() throws LexicalException {
			String input = "t=(((4-2)*5.6)/3)+2; %{test valid comments}";  
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			//checkNextIsEOF(scanner);
		}
		@Test
		public void vailComm() throws LexicalException {
			String input = "%{test valid {{{{comments{}";  
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			//checkNextIsEOF(scanner);
		}
		@Test
		public void vailStr() throws LexicalException {
			String input = "  \"say \"Hi\" \"  ";  
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			//checkNextIsEOF(scanner);
		}
		
		/******************	Invalid Cases******************************/
		@Test
		public void failChar() throws LexicalException {
			String input = "\'abcd\'";//invalid char
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(2,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		@Test
		public void failIdentifier() throws LexicalException {
			String input = "_1a_2_fc_";//invalid identifier
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(1,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		@Test
		public void failInt() throws LexicalException {
			String input = " 2147483648;";//test Int out range
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(11,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		@Test
		public void failFloat() throws LexicalException {
			String input = "-340282366920938463463374607431768211458.9999;";//test float
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(45,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		@Test
		public void failComment() throws LexicalException {
			String input = "%{affeafe%{}";//invalid comments
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(10,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		@Test
		public void failStr() throws LexicalException {
			String input = "\"affeafe";//invalid str 
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(8,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		@Test
		public void failChar2() throws LexicalException {
			String input = "\'a";//invalid char
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(2,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		@Test
		public void failFlaot() throws LexicalException {
			String input = "0..";//invalid float
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(2,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		@Test
		public void failComm() throws LexicalException {
			String input = "%{fafeagegag";//invalid comment
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(12,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}		
		
}

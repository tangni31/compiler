/**
* Initial code for the Scanner
*/
package cop5556fa18;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PLPScanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}
	}
	
	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, STRING_LITERAL,
		CHAR_LITERAL,
		KW_string	/*string       */,
		KW_char		/*char		   */,
		KW_sleep        /*sleep		   */,
		KW_print        /* print       */,
		KW_int          /* int         */,
		KW_float        /* float       */,
		KW_boolean      /* boolean     */,
		KW_if           /* if          */,
		
		KW_abs			/* abs 		   */,
		KW_sin			/* sin 		   */,
		KW_cos			/* cos 		   */, 
		KW_atan			/* atan        */,
		KW_log			/* log 		   */,
		KW_while 		/* while 	   */,
		OP_QUESTION		/* ? 		   */,
		OP_AND			/* & 		   */, 
		OP_OR			/* | 		   */,
		OP_COLON		/* : 		   */,
		
		OP_ASSIGN       /* =           */, 
		OP_EXCLAMATION  /* !           */,
		OP_EQ           /* ==          */,
		OP_NEQ          /* !=          */, 
		OP_GE           /* >=          */,
		OP_LE           /* <=          */,
		OP_GT           /* >           */,
		OP_LT           /* <           */,
		OP_PLUS         /* +           */,
		OP_MINUS        /* -           */,
		OP_TIMES        /* *           */,
		OP_DIV          /* /           */,
		OP_MOD          /* %           */,
		OP_POWER        /* **          */, 
		LPAREN          /* (           */,
		RPAREN          /* )           */,
		LBRACE          /* {           */, 
		RBRACE          /* }           */,
		LSQUARE		/*[			   */,
		RSQUARE		/*]			   */,
		SEMI            /* ;           */,
		COMMA           /* ,           */,
		DOT             /* .           */,
		EOF;            /* end of file */
	}
	
	/**
	 * Class to represent Tokens.
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos; // position of first character of this token in the input. Counting starts at 0
								// and is incremented for every character.
		public final int length; // number of characters in this token

		public Token(Kind kind, int pos, int length) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}
		
		/**
		 * Calculates and returns the line on which this token resides. The first line
		 * in the source code is line 1.
		 * 
		 * @return line number of this Token in the input.
		 */
		public int line() {
			return PLPScanner.this.line(pos) + 1;
		}

		/**
		 * Returns position in line of this token.
		 * 
		 * @param line.
		 *            The line number (starting at 1) for this token, i.e. the value
		 *            returned from Token.line()
		 * @return
		 */
		public int posInLine(int line) {
			return PLPScanner.this.posInLine(pos, line - 1) + 1;
		}

		/**
		 * Returns the position in the line of this Token in the input. Characters start
		 * counting at 1. Line termination characters belong to the preceding line.
		 * 
		 * @return
		 */
		public int posInLine() {
			return PLPScanner.this.posInLine(pos) + 1;
		}

		public String toString() {
			int line = line();
			return "[" + kind + "," +
			       String.copyValueOf(chars, pos, length) + "," +
			       pos + "," +
			       length + "," +
			       line + "," +
			       posInLine(line) + "]";
		}

		/**
		 * Since we override equals, we need to override hashCode, too.
		 * 
		 * See
		 * https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#hashCode--
		 * where it says, "If two objects are equal according to the equals(Object)
		 * method, then calling the hashCode method on each of the two objects must
		 * produce the same integer result."
		 * 
		 * This method, along with equals, was generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		/**
		 * Override equals so that two Tokens are equal if they have the same Kind, pos,
		 * and length.
		 * 
		 * This method, along with hashcode, was generated by eclipse.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (pos != other.pos)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated with.
		 * 
		 * @return
		 */
		private PLPScanner getOuterType() {
			return PLPScanner.this;
		}
	}
	
	/**
	 * Array of positions of beginning of lines. lineStarts[k] is the pos of the
	 * first character in line k (starting at 0).
	 * 
	 * If the input is empty, the chars array will have one element, the synthetic
	 * EOFChar token and lineStarts will have size 1 with lineStarts[0] = 0;
	 */
	int[] lineStarts;

	int[] initLineStarts() {
		ArrayList<Integer> lineStarts = new ArrayList<Integer>();
		int pos = 0;

		for (pos = 0; pos < chars.length; pos++) {
			lineStarts.add(pos);
			char ch = chars[pos];
			while (ch != EOFChar && ch != '\n' && ch != '\r') {
				pos++;
				ch = chars[pos];
			}
			if (ch == '\r' && chars[pos + 1] == '\n') {
				pos++;
			}
		}
		// convert arrayList<Integer> to int[]
		return lineStarts.stream().mapToInt(Integer::valueOf).toArray();
	}
	
	int line(int pos) {
		int line = Arrays.binarySearch(lineStarts, pos);
		if (line < 0) {
			line = -line - 2;
		}
		return line;
	}

	public int posInLine(int pos, int line) {
		return pos - lineStarts[line];
	}

	public int posInLine(int pos) {
		int line = line(pos);
		return posInLine(pos, line);
	}
	
	/**
	 * Sentinal character added to the end of the input characters.
	 */
	static final char EOFChar = 128;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters from
	 * the input string plus an additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;
	
	static HashMap<String,Kind> keyWordMap;	// a HashMap store keywords and their kind
	
	PLPScanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFChar;
		tokens = new ArrayList<Token>();
		keyWordMap = new HashMap<String, Kind>();
		lineStarts = initLineStarts();
		/*****insert all key words into HashMap****/
		keyWordMap.put("int", Kind.KW_int);
		keyWordMap.put("float", Kind.KW_float);
		keyWordMap.put("if", Kind.KW_if);
		keyWordMap.put("boolean", Kind.KW_boolean);
		keyWordMap.put("print", Kind.KW_print);
		keyWordMap.put("char", Kind.KW_char);
		keyWordMap.put("string", Kind.KW_string);
		keyWordMap.put("sleep", Kind.KW_sleep);
		keyWordMap.put("abs", Kind.KW_abs);
		keyWordMap.put("sin", Kind.KW_sin);
		keyWordMap.put("cos", Kind.KW_cos);
		keyWordMap.put("atan", Kind.KW_atan);
		keyWordMap.put("log", Kind.KW_log);
		keyWordMap.put("while", Kind.KW_while);
	}
	
	private enum State {START,IN_STRING,IN_DIGIT,IN_IDENTIFIER,AFTER_TIMES,AFTER_ASSIGN,AFTER_EXCLAMATION,IN_FLOAT,
		AFTER_GT,AFTER_LT,UNDER_SCORE_START,AFTER_ZERO,IN_CHAR,AFTER_MOD,IN_COMMENT,COMMENT_END};   
	
	
	public PLPScanner scan() throws LexicalException {
		int pos = 0;
		State state = State.START;
		int startPos = 0;
		//TODO:  
		while (pos < chars.length) {
			char ch = chars[pos];
			switch(state) {
				case START: {
					startPos = pos;
					switch (ch) {
						case ' ':
						case '\n':
						case '\r':
						case '\t':
						case '\f':
						{
							pos++;
						}
						break;
						case EOFChar: {
							tokens.add(new Token(Kind.EOF, startPos, 0));
							pos++; // next iteration will terminate loop
						}
						break;
						case '"': {
							state=State.IN_STRING;
							pos++;
						}
						break;
						case '\'':{
							state = State.IN_CHAR;
							pos++;
						}
						break;
						case '0':{
							state = State.AFTER_ZERO;
							pos++;
						}
						break;
						/*************operators*******/
						case '?': {
							tokens.add(new Token(Kind.OP_QUESTION,startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ':': {
							tokens.add(new Token(Kind.OP_COLON,startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '|': {
							tokens.add(new Token(Kind.OP_OR,startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '&': {
							tokens.add(new Token(Kind.OP_AND,startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '-': {
							tokens.add(new Token(Kind.OP_MINUS,startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '+': {
							tokens.add(new Token(Kind.OP_PLUS,startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '/': {
							tokens.add(new Token(Kind.OP_DIV,startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '%': {
							state=State.AFTER_MOD;
							pos++;
						}
						break;
						case '*':{
							state=State.AFTER_TIMES;
							pos++;
						}
						break;
						case '=':{
							state=State.AFTER_ASSIGN;
							pos++;
						}
						break;
						case '!':{
							state=State.AFTER_EXCLAMATION;
							pos++;
						}
						break;
						case '>':{
							state=State.AFTER_GT;
							pos++;
						}
						break;
						case '<':{
							state=State.AFTER_LT;
							pos++;
						}
						break;
						/*		separators		*/
						case ';': {
							tokens.add(new Token(Kind.SEMI, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ',': {
							tokens.add(new Token(Kind.COMMA, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '(': {
							tokens.add(new Token(Kind.LPAREN, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ')': {
							tokens.add(new Token(Kind.RPAREN, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '[': {
							tokens.add(new Token(Kind.LSQUARE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ']': {
							tokens.add(new Token(Kind.RSQUARE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '{': {
							tokens.add(new Token(Kind.LBRACE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '}': {
							tokens.add(new Token(Kind.RBRACE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						default: {
							if (Character.isDigit(ch)){
								state=State.IN_DIGIT;
								pos++;
							}
							else if(ch=='_'){
								state=State.UNDER_SCORE_START;
								pos++;
							}
							else if(Character.isLetter(ch)){
								state=State.IN_IDENTIFIER;
								pos++;
							}
							else if(Character.isLetter(ch)){
								state=State.IN_IDENTIFIER;
								pos++;
							}
							else{
								error(pos, line(pos), posInLine(pos),"An illegal character is encountered: "+(char)ch+"\n");
							}
						}
					}//switch ch
				}
				break;
				/*******OPERATORS**************************/
				case AFTER_MOD:{
					if(ch=='{'){
						state = State.IN_COMMENT;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.OP_MOD,startPos, pos - startPos));
						state=State.START;
					}
				}
				break;
				case AFTER_TIMES:{ //power or times?
					switch(ch){
					case '*':{
						tokens.add(new Token(Kind.OP_POWER, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					break;
					default: {
						tokens.add(new Token(Kind.OP_TIMES, startPos, pos - startPos ));
						state = State.START;
					}
					}
				}
				break;
				case AFTER_ASSIGN:{
					switch(ch){
					case '=':{
						tokens.add(new Token(Kind.OP_EQ, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					break;
					default: {
						tokens.add(new Token(Kind.OP_ASSIGN, startPos, pos - startPos));
						state = State.START;
					}
					}
				}
				break;
				case AFTER_EXCLAMATION:{
					switch(ch){
					case '=':{
						tokens.add(new Token(Kind.OP_NEQ, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					break;
					default: {
						tokens.add(new Token(Kind.OP_EXCLAMATION, startPos, pos - startPos));
						state = State.START;
					}
					}
				}
				break;
				case AFTER_GT:{
					switch(ch){
					case '=':{
						tokens.add(new Token(Kind.OP_GE, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					break;
					default: {
						tokens.add(new Token(Kind.OP_GT, startPos, pos - startPos));
						state = State.START;
					}
					}
				}
				break;
				case AFTER_LT:{
					switch(ch){
					case '=':{
						tokens.add(new Token(Kind.OP_LE, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					break;
					default: {
						tokens.add(new Token(Kind.OP_LT, startPos, pos - startPos));
						state = State.START;
					}
					}
				}
				break;
				case IN_COMMENT:{
					switch(ch){
					case '%':{
						state = State.COMMENT_END;
						pos++;
					}
					break;
					case '}':{
						state = State.START;
						pos++;
					}
					break;
					default:{
						if(ch=='\n' || ch=='\r' || ch==EOFChar){
							error(pos, line(pos), posInLine(pos),"Unexpected line terminator or EOF in Comment!\n");
						}
						pos++;
					}
					}	
				}
				break;
				case COMMENT_END:{
					switch(ch){
					case '{':{
						error(pos, line(pos), posInLine(pos),"Invalid character in comment, '%' followed by '{'\n");
					}
					break;
					case '}':{
						state = State.START;
						pos++;
					}
					break;
					case '%':{
						pos++;
					}
					break;
					default: {
						if(ch=='\n' || ch=='\r' || ch==EOFChar){
							error(pos, line(pos), posInLine(pos),"Unexpected line terminator or EOF in Comment!\n");
						}
						state = State.IN_COMMENT;
						pos++;
					}
					}
				}
				break;
				case IN_CHAR:{
					if(pos-startPos==2){
						if(ch!='\''){
							error(pos, line(pos), posInLine(pos),"Invalid character constant\n");
						}
						else{
							tokens.add(new Token(Kind.CHAR_LITERAL, startPos, pos - startPos+1));
							state = State.START;
							pos++;
						}
					}
					else{
						pos++;
					}
				}
				break;
				case IN_STRING:{
					switch(ch){
					case '"':{
						tokens.add(new Token(Kind.STRING_LITERAL,startPos, pos - startPos+1));
						state = State.START;
						pos++;
					}
					break;
					default: {
						if(ch=='\n' || ch=='\r' || ch==EOFChar){
							error(pos, line(pos), posInLine(pos),"Unexpected line terminator or EOF in string!\n");
						}
						pos++;
					}
					}
				}
				break;
				case AFTER_ZERO:{
					if(ch=='.'){
						state = State.IN_FLOAT;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.INTEGER_LITERAL,startPos, 1));
						state = State.START;
					}
				}
				break;
				case IN_DIGIT:{
					switch(ch){
					case '.':{
						state = State.IN_FLOAT;
						pos++;
					}
					break;
					default: {
						if (!Character.isDigit(ch)){
							String num = new String(chars, startPos, pos-startPos);
							try{
								Integer.parseInt(num);
							}catch(NumberFormatException e){
								error(pos, line(pos), posInLine(pos)," The literal of type int is out of range!:"+num+"\n");
							}
							tokens.add(new Token(Kind.INTEGER_LITERAL,startPos, pos - startPos));	
							state = State.START;
						}
						else pos++;
					}
					}
				}
				break;
				case IN_FLOAT:{
					if (!Character.isDigit(ch)){
						if((pos-startPos)==1){
							tokens.add(new Token(Kind.DOT,startPos, pos - startPos));
							state = State.START;
						}
						else{
							String num = new String(chars, startPos, pos-startPos);
							boolean valid = Float.isFinite(Float.parseFloat(num));
							if (!valid){
								error(pos, line(pos), posInLine(pos)," The literal of type float is out of range!:"+num+"\n");
							}
							tokens.add(new Token(Kind.FLOAT_LITERAL,startPos, pos - startPos));
							state = State.START;
						}
					}
					else pos++;
				}
				break;
				case IN_IDENTIFIER:{
					if(Character.isLetter(ch) || Character.isDigit(ch) || ch=='_'){
						pos++;
					}
					else{
						String identifier = new String(chars, startPos, pos-startPos);
						Kind val = keyWordMap.get(identifier);
						if(val!=null){ //token is a key word
							tokens.add(new Token(val, startPos, pos - startPos));
						}
						else if(identifier.equals("true") || identifier.equals("false")){
							tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, pos - startPos));
						}
						else{
							tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos));
						}
						state = State.START;
					}
				}
				break;
				case UNDER_SCORE_START:{
					if(ch=='_'){
						pos++;
					}
					else if(Character.isLetter(ch)){
						state=State.IN_IDENTIFIER;
						pos++;
					}
					else{
						error(pos, line(pos), posInLine(pos)," Invalid Identifier! UnderScores must be followed by Alphabetic Letter\n");
					}
				}
				break;
				default: {
					error(pos, 0, 0, "Undefined state");
				}	
			////////////////////////////////////
			}// switch state
		} // while
		
		return this;
	}
	
	private void error(int pos, int line, int posInLine, String message) throws LexicalException {
		String m = (line + 1) + ":" + (posInLine + 1) + " " + message;
		throw new LexicalException(m, pos);
	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This means
	 * that the next call to nextToken or peek will return the same Token as
	 * returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken will
	 * return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens and line starts
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		sb.append("Line starts:\n");
		for (int i = 0; i < lineStarts.length; i++) {
			sb.append(i).append(' ').append(lineStarts[i]).append('\n');
		}
		return sb.toString();
	}


}

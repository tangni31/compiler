# Compiler
## A compiler for a simple language (Not finished)   

### PLPScanner: 
Scanner groups input characters into tokens. It saves the text of “interesting” tokens and tags tokens with line and column numbers. It uses “longest possible token” rule. Scanner using a finite automaton, it starts in a distinguished initial state (START). It then moves from state to state based on the next available character of input. When it reaches one of a designated set of final states it recognizes the token associated with that state.  Keywords are stored in a HashMap.    
##### Grammar:    [Lexical Analysis](https://github.com/tangni31/compiler/blob/master/lexical%20analysis)    

### PLPParser:

The parser is the heart of a typical compiler. It calls the scanner to obtain the tokens of the input program, assembles the tokens together, checks tokens violate CFG or not and passes them to the later phases of the compiler.  Parser will return an instance of Program if the sentence is legal (see Abstract Syntax). If the sentence does not parse it will throw a SyntaxException. 

##### Grammar:  [Context Free Grammar and Abstract Syntax](https://github.com/tangni31/compiler/blob/master/ContextFreeGrammar.txt)

### Symbol Table:

This  language used static scoping, the compiler uses an insert operation to place a name-to-object binding into the symbol table for each newly encountered declaration. When it encounters the use of a name that should already have been declared, the compiler uses a lookup operation to search for an existing binding. The symbol table used in this project is a Leblanc-Cook symbol table. The symbol table has two parts: the a scope stack that indicates the scopes that compose the current referencing environment and a hash table keyed by name, stores symbol name, its category, scope number and type. As the semantic analyzer scans the program, it pushes and pops the scope stack whenever it enters or leaves a scope.

### PLPTypeChecker:

Type Checker is used for type checking. If a type error is discovered, it throws a SemanticException.

Grammar:  [Semantic Rules and Conditions](https://github.com/tangni31/compiler/blob/master/SemanticRulesAndConditions.txt)







## Reference:
   Programming Language Pragmatics SECOND EDITION.   Author: Michael L. Scott      
   Compilers: Principles, Techniques, and Tools.   Author: Alfred V. Aho, Ravi Sethi, Jeffrey Ullman      
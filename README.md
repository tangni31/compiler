# Compiler
## A compiler for a simple language (Not finished)   

### PLPScanner: 
Scanner groups input characters into tokens. It saves the text of “interesting” tokens and tags tokens with line and column numbers. It uses “longest possible token” rule. Scanner using a finite automaton, it starts in a distinguished initial state (START). It then moves from state to state based on the next available character of input. When it reaches one of a designated set of final states it recognizes the token associated with that state.  Keywords are stored in a HashMap.    
##### Grammar:    [Lexical Analysis](https://github.com/tangni31/compiler/blob/master/lexical%20analysis)    

### PLPParser:

The parser is the heart of a typical compiler. It calls the scanner to obtain the tokens of the input program, assembles the tokens together, checks tokens violate CFG or not and passes them to the later phases of the compiler.

##### Grammar:  [Context Free Grammar and Abstract Syntax](https://github.com/tangni31/compiler/blob/master/Context%20Free%20Grammar.txt)





## Reference:
   Programming Language Pragmatics SECOND EDITION.   Author: Michael L. Scott      
   Compilers: Principles, Techniques, and Tools.   Author: Alfred V. Aho, Ravi Sethi, Jeffrey Ullman      

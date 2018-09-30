# Compiler
## A compiler for a simple language (Not finished)   

### Scanner: 
Scanner groups input characters into tokens. It saves the text of “interesting” tokens and tags tokens with line and column numbers. It uses “longest possible token” rule. Scanner using a finite automaton, it starts in a distinguished initial state (START). It then moves from state to state based on the next available character of input. When it reaches one of a designated set of final states it recognizes the token associated with that state.  Keywords are stored in a HashMap.    
##### Grammar:    
    LineTerminator → '\n' | '\r' | '\r' '\n'    
    WhiteSpace → ' ' | '\t' | '\f' | LineTerminator    
    Comment → %{  ( (% NOT({) ) | NOT(%) )* %* }    
    Token → Identifier | Keyword | Literal | Separator | Operator    
    Identifier → IdentifierChar but not a keyword or a Boolean literal    
    IdentifierChar → IdentifierStart IdentifierPart*    
    IdentifierStart →  UnderScoreStart IdentifierStart |A..Z IdentifierPart | a..z IdentifierPart    
    UnderScoreStart → _ UnderScoreStart | _    
    IdentifierPart → IdentifierStart | Digit | _    
    Literal → IdengerLiteral | FloatingPointLiteral | BooleanLiteral    
    IntegerLiteral → 0 | NonZeroDigit Digit*    
    FloatingPointLiteral  → IntegerLiteral . Digit*    
    NoneZeroDigit → 1 .. 9    
    Digit → NonZeroDigit | 0    
    BooleanLiteral → true | false    
    Separators → ( | ) | [ | ] | ; | , | { | }        
    Operators → < | > | <= | >= | - | + | * | / | % | ! | ** | == | = | ? | : | | | & | !=       
    Keywords → print | int | float | boolean | char | string | sleep | while | sin | cos | atan | abs | log        

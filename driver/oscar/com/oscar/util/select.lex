package com.oscar.util;

%%

/* public class */
%public

/* yylex() return Word object */
%type Word 

%ignorecase

/* support Chinese */
%unicode

ALPHA=[A-Za-z]
DIGIT=[0-9]
LINE_TERMINATOR = \r|\n|\r\n
WHITE_SPACE_CHAR={LINE_TERMINATOR}|[ \t\f]
STRING_TEXT_DOUBLE=(\"\"|[^\"])
STRING_TEXT_SINGLE=(''|[^'])
SINGLE_QUOTED_STRING='{STRING_TEXT_SINGLE}*?'
DOUBLE_QUOTED_STRING=\"{STRING_TEXT_DOUBLE}*?\"
CHAR=[^\n\r \t\f;()]

IDENTIFIER_START=[A-Za-z\200-\377\u007f-\uffff_]
IDENTIFIER_CONT=[A-Za-z\200-\377\u007f-\uffff_0-9\$\#]
/*IDENTIFIER = ({CHAR})+ */
/* IDENTIFIER = {ALPHA}({ALPHA}|{DIGIT}|_)* */
IDENTIFIER={IDENTIFIER_START}{IDENTIFIER_CONT}*

OPERATOR_CHAR=([\~\!\@\#\^\&\|\`\+\-\*\/\%\<\>\=]|(\!{WHITE_SPACE_CHAR}+\=)|(\<{WHITE_SPACE_CHAR}+\>)|(\>{WHITE_SPACE_CHAR}+\=)|(\<{WHITE_SPACE_CHAR}+\=))
OPERATOR={OPERATOR_CHAR}+

COMMA=,
OTHER=.
COLONPARAM=:\w+
TYPETURN=::\w+
InputCharacter = [^\r\n]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} 
          
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "--" {InputCharacter}* {LINE_TERMINATOR}?

%%
/* keywords */

<YYINITIAL> "explain"          { return new Word(Word.EXPLAIN, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "create"           { return new Word(Word.CREATE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "select"           { return new Word(Word.SELECT, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "into"             { return new Word(Word.INTO, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "update"           { return new Word(Word.UPDATE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "set"              { return new Word(Word.SET, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "insert"           { return new Word(Word.INSERT, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "delete"           { return new Word(Word.DELETE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "drop"             { return new Word(Word.DROP, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "from"             { return new Word(Word.FROM, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "as"               { return new Word(Word.AS, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "prepare"          { return new Word(Word.PREPARE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "grant"            { return new Word(Word.GRANT, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "revoke"           { return new Word(Word.REVOKE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "declare"          { return new Word(Word.DECLARE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "avg"              { return new Word(Word.AVG, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "count"            { return new Word(Word.COUNT, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "max"              { return new Word(Word.MAX, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "min"              { return new Word(Word.MIN, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "stddev"           { return new Word(Word.STDDEV, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "sum"              { return new Word(Word.SUM, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "variance"         { return new Word(Word.VARIANCE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "("                { return new Word(Word.LPAREN, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> ")"                { return new Word(Word.RPAREN, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> ";"                { return new Word(Word.SEMI, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "audit"            { return new Word(Word.AUDIT, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "noaudit"          { return new Word(Word.AUDIT, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "rollback"         { return new Word(Word.ROLLBACK, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "begin"            { return new Word(Word.BEGIN, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "group by"         { return new Word(Word.GROUP_BY, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "alter"            { return new Word(Word.ALTER, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "comment on"       { return new Word(Word.COMMENT, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "show"             { return new Word(Word.SHOW, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "with"             { return new Word(Word.WITH, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "merge"             { return new Word(Word.MERGE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "execute"             { return new Word(Word.EXECUTE, yytext(),  zzStartRead , zzMarkedPos);}
<YYINITIAL> "."                { return new Word(Word.DOT, yytext(),  zzStartRead , zzMarkedPos);} 

<YYINITIAL> "?"                { return new Word(Word.QUESTION_MARK, yytext(),  zzStartRead , zzMarkedPos);}  


<YYINITIAL> {
  /* double quoted string */ 
  {DOUBLE_QUOTED_STRING} 	 { 
    /* String str =  yytext().substring(1, yylength()-1);*/
    return (new Word(Word.STRING_DOUBLE, yytext(),  zzStartRead , zzMarkedPos));  
  }
  
  /* single quoted string */ 
  {SINGLE_QUOTED_STRING} 	 { 
    //String str =  yytext().substring(1,yylength()-1);
    return (new Word(Word.STRING_SINGLE, yytext(),  zzStartRead , zzMarkedPos));    
  }
  
  /* identifiers */ 
  {IDENTIFIER}                   { 
    return new Word(Word.STRING_IDENTIFIER,yytext(), zzStartRead , zzMarkedPos);
  }
  
  /* OPERATOR */ 
  {OPERATOR}                   { 
    return new Word(Word.OPERATOR,yytext(), zzStartRead , zzMarkedPos);
  }
  
  {COMMA}                   { 
    return new Word(Word.COMMA,yytext(), zzStartRead , zzMarkedPos);
  }
  
   {COLONPARAM}                   { 
    return new Word(Word.COLONPARAM,yytext(), zzStartRead , zzMarkedPos);
  }
  {TYPETURN}                   { 
    return new Word(Word.TYPETURN,yytext(), zzStartRead , zzMarkedPos);
  }

     
  /* whitespace */
  {WHITE_SPACE_CHAR}             { /* ignore */ }
  
  /* comments */
  {Comment}                      { /* ignore */ }
  
  {OTHER}                   { 
    return new Word(Word.OTHER,yytext(), zzStartRead , zzMarkedPos);
  }
}

<<EOF>>                          { return new Word(Word.EOF, "EOF"); }
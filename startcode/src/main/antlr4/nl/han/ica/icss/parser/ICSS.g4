grammar ICSS;

//--- PARSER: ---
stylesheet: (variableAssignment | styleRule)* EOF;

// Rules
styleRule: selector OPEN_BRACE (declaration | variableAssignment | ifClause)* CLOSE_BRACE;
selector: LOWER_IDENT | ID_IDENT | CLASS_IDENT;
declaration: propertyName COLON expression SEMICOLON;
literal: TRUE | FALSE | PIXELSIZE | PERCENTAGE | SCALAR | COLOR;
propertyName: COLOR_PROP | BG_COLOR_PROP | WIDTH_PROP | HEIGHT_PROP;

// Variables
variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;
variableReference: CAPITAL_IDENT;
expression: expression operation expression | (literal | variableReference);
operation: MUL | MIN | PLUS;

// IF ELSE
ifClause: IF BOX_BRACKET_OPEN (TRUE | FALSE | variableReference) BOX_BRACKET_CLOSE
          OPEN_BRACE (declaration | ifClause | variableAssignment)* CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE (declaration | ifClause | variableAssignment)* CLOSE_BRACE;

//--- LEXER: ---

// Properties
COLOR_PROP: 'color';
BG_COLOR_PROP: 'background-color';
WIDTH_PROP: 'width';
HEIGHT_PROP: 'height';

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';

//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//Something
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';









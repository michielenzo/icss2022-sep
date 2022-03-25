package nl.han.ica.icss.generator;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

public class Generator {

	public static final String ASSIGNMENT_OPERATOR = ":=";
	public static final String COLON = ":";
	public static final String OPEN_BRACE = "{";
	public static final String CLOSE_BRACE = "}";
	public static final String PIXEL_POSTFIX = "px";
	public static final String PERCENTAGE_SYMBOL = "%";
	public static final char SEMICOLON = ';';
	public static final String SPACE = " ";
	public static final String EOL = "\n";
	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";
    public static final char ID_TAG = '#';
    public static final char CLASS_TAG = '.';



	private final HANStack<ASTNode> container = new HANStack<>();

	public String generate(AST ast) {
        return walkASTRecursive(ast.root, "");
	}

	private String walkASTRecursive(ASTNode node, String str){

		String newString = str;

		if(node instanceof Literal && !(container.peek() instanceof VariableAssignment)){
			newString += getLiteralAsString((Literal) node) + SEMICOLON + EOL;
		}

		if(node instanceof Declaration) newString += SPACE + SPACE + ((Declaration) node).property.name + COLON + SPACE;
		if(node instanceof ClassSelector) newString += CLASS_TAG + ((ClassSelector) node).cls + SPACE + OPEN_BRACE + EOL;
		if(node instanceof IdSelector)  newString += ID_TAG + ((IdSelector) node).id + SPACE + OPEN_BRACE + EOL;
		if(node instanceof TagSelector) newString += ((TagSelector) node).tag + SPACE + OPEN_BRACE + EOL;

		container.push(node);

		for (ASTNode child: node.getChildren()) newString = walkASTRecursive(child, newString);

		container.pop();

		if(node instanceof Stylerule) newString += CLOSE_BRACE + EOL + EOL;

		return newString;
	}

	private String getLiteralAsString(Literal literal) {
		if(literal instanceof ScalarLiteral){
			return String.valueOf(((ScalarLiteral) literal).value);
		} else if(literal instanceof PixelLiteral){
			return ((PixelLiteral) literal).value + PIXEL_POSTFIX;
		} else if(literal instanceof PercentageLiteral){
			return ((PercentageLiteral) literal).value + PERCENTAGE_SYMBOL;
		} else if(literal instanceof BoolLiteral){
			final boolean value = ((BoolLiteral) literal).value;
			if(value) return TRUE;
			else return FALSE;
		} else if(literal instanceof ColorLiteral){
			return ((ColorLiteral) literal).value;
		}

		return null;
	}
}

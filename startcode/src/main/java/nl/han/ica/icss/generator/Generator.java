package nl.han.ica.icss.generator;


import nl.han.ica.datastructures.HANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.checker.VariableManager;

public class Generator {


	public static final String ASSIGNMENT_OPERATOR = ":=";
	public static final String COLON = ":";
	public static final String OPEN_BRACE = "{";
	public static final String CLOSE_BRACE = "}";
	VariableManager variableManager = new VariableManager();
	private final HANStack<ASTNode> container = new HANStack<>();

	public static final char SEMICOLON = ';';
	public static final String SPACE = " ";
	public static final String EOL = "\n";

	public String generate(AST ast) {
        return walkASTRecursive(ast.root, "");
	}

	private String walkASTRecursive(ASTNode node, String str){

		String str2 = str;

		if(node instanceof Literal && !(container.peek() instanceof VariableAssignment)){
			str2 += getLiteralAsString((Literal) node) + SEMICOLON + EOL;
		}

		if(node instanceof Declaration){
			str2 += SPACE + SPACE + ((Declaration) node).property.name + COLON + SPACE;
		}

		if(node instanceof ClassSelector) {
			str2 += ((ClassSelector) node).cls + SPACE + OPEN_BRACE + EOL;
		}

		if(node instanceof IdSelector){
			str2 += ((IdSelector) node).id + SPACE + OPEN_BRACE + EOL;
		}

		if(node instanceof TagSelector){
			str2 += ((TagSelector) node).tag + SPACE + OPEN_BRACE + EOL;
		}

		container.push(node);

		for (ASTNode child: node.getChildren()){
			 str2 = walkASTRecursive(child, str2);
		}

		container.pop();

		if(node instanceof Stylerule){
			str2 += CLOSE_BRACE + EOL;
		}

		return str2;
	}

	private String getLiteralAsString(Literal literal) {
		if(literal instanceof ScalarLiteral){
			return String.valueOf(((ScalarLiteral) literal).value);
		} else if(literal instanceof PixelLiteral){
			return ((PixelLiteral) literal).value + "px";
		} else if(literal instanceof PercentageLiteral){
			return ((PercentageLiteral) literal).value + "%";
		} else if(literal instanceof BoolLiteral){
			final boolean value = ((BoolLiteral) literal).value;
			if(value) return "TRUE";
			else return "FALSE";
		} else if(literal instanceof ColorLiteral){
			return ((ColorLiteral) literal).value;
		}

		return null;
	}

	
}

package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.checker.VariableManager;

public class Generator {

	VariableManager variableManager = new VariableManager();

	public String generate(AST ast) {
        return walkThroughASTRecursive(ast.root, "");
	}

	private String walkThroughASTRecursive(ASTNode node, String str){

		String str2 = str;

		if(node instanceof Literal){
			str2 += getLiteralAsString((Literal) node);
		}

		for (ASTNode child: node.getChildren()){
			 str2 = walkThroughASTRecursive(child, str2);
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

package nl.han.ica.icss.parser;

import java.util.List;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.gen.ICSSBaseListener;
import nl.han.ica.icss.gen.ICSSParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

    public AST getAST() {
        return ast;
    }

	@Override public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = new Stylesheet();
		currentContainer.push(stylesheet);
		ast.setRoot(stylesheet);
	}

	@Override public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
		Stylerule stylerule = new Stylerule();
		ast.root.body.add(stylerule);
		currentContainer.push(stylerule);
	}

	@Override public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterSelector(ICSSParser.SelectorContext ctx) {

		String symbol = ctx.getChild(0).getText();

		Selector selector;

		switch (symbol.charAt(0)){
			case '.': selector = new IdSelector(symbol); break;
			case '#': selector = new ClassSelector(symbol); break;
			default: selector = new TagSelector(symbol);
		}

		currentContainer.peek().addChild(selector);
	}

	@Override public void exitSelector(ICSSParser.SelectorContext ctx) {

	}

	@Override public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();

		currentContainer.peek().addChild(declaration);

		currentContainer.push(declaration);
	}

	@Override public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterLiteral(ICSSParser.LiteralContext ctx) { }

	@Override public void exitLiteral(ICSSParser.LiteralContext ctx) { }

	@Override public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
		PropertyName propertyName = new PropertyName(ctx.getText());

		currentContainer.peek().addChild(propertyName);
	}

	@Override public void exitPropertyName(ICSSParser.PropertyNameContext ctx) { }

	@Override public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = new VariableAssignment();

		currentContainer.peek().addChild(variableAssignment);
		currentContainer.push(variableAssignment);
	}

	@Override public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		VariableReference variableReference = new VariableReference(ctx.getText());

		currentContainer.peek().addChild(variableReference);
	}

	@Override public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) { }

	@Override public void enterExpression(ICSSParser.ExpressionContext ctx) {

		Expression expression;

		if(ctx.children.size() > 1) {
			switch (ctx.getChild(1).getText()){
				case "*":;  expression = new MultiplyOperation(); break;
				case "-":; expression = new SubtractOperation(); break;
				case "+":; expression = new AddOperation(); break;
				default: throw new IllegalStateException("Unexpected value: " + ctx.getChild(1).getText());
			}

			currentContainer.peek().addChild(expression);
			currentContainer.push(expression);
		} else {
			if (ctx.getChild(0).getText().equals("TRUE") | ctx.getChild(0).getText().equals("FALSE")){
				expression = new BoolLiteral(ctx.getChild(0).getText());
			} else if(Character.isUpperCase(ctx.getChild(0).getText().charAt(0))){
				return;
			} else if (ctx.getChild(0).getText().charAt(0) == '#'){
				expression = new ColorLiteral(ctx.getChild(0).getText());
			} else if (ctx.getChild(0).getText().charAt(ctx.getChild(0).getText().length() - 1) == 'x') {
				expression = new PixelLiteral(ctx.getChild(0).getText());
			} else if (ctx.getChild(0).getText().charAt(ctx.getChild(0).getText().length() - 1) == '%') {
				expression = new PercentageLiteral(ctx.getChild(0).getText());
			} else {
				expression = new ScalarLiteral(ctx.getChild(0).getText());
			}

			currentContainer.peek().addChild(expression);
		}
	}

	@Override public void exitExpression(ICSSParser.ExpressionContext ctx) {
		if (ctx.children.size() > 1) { currentContainer.pop(); }
	}

	@Override public void enterOperation(ICSSParser.OperationContext ctx) { }

	@Override public void exitOperation(ICSSParser.OperationContext ctx) { }

	@Override public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = new IfClause();

		currentContainer.peek().addChild(ifClause);
		currentContainer.push(ifClause);
	}

	@Override public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		ElseClause elseClause = new ElseClause();

		currentContainer.peek().addChild(elseClause);
		currentContainer.push(elseClause);
	}

	@Override public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitTerminal(TerminalNode node) { }

	@Override public void visitErrorNode(ErrorNode node) { }

}
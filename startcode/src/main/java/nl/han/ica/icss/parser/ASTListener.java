package nl.han.ica.icss.parser;

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
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public static final String ASTRIX = "*";
	public static final String DASH = "-";
	public static final String PLUS = "+";
	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";
	public static final char X = 'x';
	public static final char POUND_SIGN = '#';
	public static final char PERCENTAGE = '%';
	public static final char DOT = '.';

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

		char symbol = ctx.getChild(0).getText().charAt(0);
        String tagName = ctx.getChild(0).getText().substring(1);
		Selector selector;

		switch (symbol){
			case DOT: selector = new IdSelector(tagName); break;
			case POUND_SIGN: selector = new ClassSelector(tagName); break;
			default: selector = new TagSelector(Character.toString(symbol));
		}

		currentContainer.peek().addChild(selector);
	}

	@Override public void exitSelector(ICSSParser.SelectorContext ctx) { }

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

		if(expressionIsOperation(ctx)) {
			String text = ctx.getChild(1).getText();

			switch (text){
				case ASTRIX:  expression = new MultiplyOperation(); break;
				case DASH: expression = new SubtractOperation(); break;
				case PLUS: expression = new AddOperation(); break;
				default: throw new IllegalStateException("Unexpected value: " + text);
			}

			currentContainer.peek().addChild(expression);
			currentContainer.push(expression);
		} else {
			String text = ctx.getChild(0).getText();

			if (text.equals(TRUE) || text.equals(FALSE)){
				expression = new BoolLiteral(text);
			} else if(expressionIsVariableReference(text)){
				return;
			} else if (expressionIsColorLiteral(text)){
				expression = new ColorLiteral(text);
			} else if (expressionIsPixelLiteral(text)) {
				expression = new PixelLiteral(text);
			} else if (expressionIsPercentageLiteral(text)) {
				expression = new PercentageLiteral(text);
			} else {
				expression = new ScalarLiteral(text);
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

	private boolean expressionIsPercentageLiteral(String text) {
		return text.charAt(text.length() - 1) == PERCENTAGE;
	}

	private boolean expressionIsPixelLiteral(String text) {
		return text.charAt(text.length() - 1) == X;
	}

	private boolean expressionIsColorLiteral(String text) {
		return text.charAt(0) == POUND_SIGN;
	}

	private boolean expressionIsVariableReference(String text) {
		return Character.isUpperCase(text.charAt(0));
	}

	private boolean expressionIsOperation(ICSSParser.ExpressionContext ctx ){
		return ctx.children.size() > 1;
	}
}
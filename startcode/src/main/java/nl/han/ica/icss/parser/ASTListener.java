package nl.han.ica.icss.parser;

import java.util.Stack;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
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

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

    public AST getAST() {
        return ast;
    }

	@Override public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(new Stylesheet());
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

	@Override public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) { }

	@Override public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) { }

	@Override public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) { }

	@Override public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) { }

	@Override public void enterExpression(ICSSParser.ExpressionContext ctx) {
		Expression expression;

		//switch (ctx)
	}

	@Override public void exitExpression(ICSSParser.ExpressionContext ctx) { }

	@Override public void enterOperation(ICSSParser.OperationContext ctx) { }

	@Override public void exitOperation(ICSSParser.OperationContext ctx) { }

	@Override public void enterIfClause(ICSSParser.IfClauseContext ctx) { }

	@Override public void exitIfClause(ICSSParser.IfClauseContext ctx) { }

	@Override public void enterCondition(ICSSParser.ConditionContext ctx) { }

	@Override public void exitCondition(ICSSParser.ConditionContext ctx) { }

	@Override public void enterIfElseBody(ICSSParser.IfElseBodyContext ctx) { }

	@Override public void exitIfElseBody(ICSSParser.IfElseBodyContext ctx) { }

	@Override public void enterElseClause(ICSSParser.ElseClauseContext ctx) { }

	@Override public void exitElseClause(ICSSParser.ElseClauseContext ctx) { }

	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitTerminal(TerminalNode node) { }

	@Override public void visitErrorNode(ErrorNode node) { }

}
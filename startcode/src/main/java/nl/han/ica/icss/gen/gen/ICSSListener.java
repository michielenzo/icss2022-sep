package nl.han.ica.icss.gen.gen;// Generated from C:/dev/projects/HAN/icss2022-sep/startcode/src/main/antlr4/nl/han/ica/icss/parser\ICSS.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ICSSParser}.
 */
public interface ICSSListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ICSSParser#stylesheet}.
	 * @param ctx the parse tree
	 */
	void enterStylesheet(ICSSParser.StylesheetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#stylesheet}.
	 * @param ctx the parse tree
	 */
	void exitStylesheet(ICSSParser.StylesheetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#styleRule}.
	 * @param ctx the parse tree
	 */
	void enterStyleRule(ICSSParser.StyleRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#styleRule}.
	 * @param ctx the parse tree
	 */
	void exitStyleRule(ICSSParser.StyleRuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#selector}.
	 * @param ctx the parse tree
	 */
	void enterSelector(ICSSParser.SelectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#selector}.
	 * @param ctx the parse tree
	 */
	void exitSelector(ICSSParser.SelectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(ICSSParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(ICSSParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(ICSSParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(ICSSParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#propertyName}.
	 * @param ctx the parse tree
	 */
	void enterPropertyName(ICSSParser.PropertyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#propertyName}.
	 * @param ctx the parse tree
	 */
	void exitPropertyName(ICSSParser.PropertyNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#variableAssignment}.
	 * @param ctx the parse tree
	 */
	void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#variableAssignment}.
	 * @param ctx the parse tree
	 */
	void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterVariableReference(ICSSParser.VariableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitVariableReference(ICSSParser.VariableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ICSSParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ICSSParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#operation}.
	 * @param ctx the parse tree
	 */
	void enterOperation(ICSSParser.OperationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#operation}.
	 * @param ctx the parse tree
	 */
	void exitOperation(ICSSParser.OperationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#ifClause}.
	 * @param ctx the parse tree
	 */
	void enterIfClause(ICSSParser.IfClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#ifClause}.
	 * @param ctx the parse tree
	 */
	void exitIfClause(ICSSParser.IfClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link ICSSParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void enterElseClause(ICSSParser.ElseClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ICSSParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void exitElseClause(ICSSParser.ElseClauseContext ctx);
}
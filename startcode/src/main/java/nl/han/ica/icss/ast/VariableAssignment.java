package nl.han.ica.icss.ast;

import java.util.ArrayList;
import java.util.Objects;

/**
 * An assignment binds a expression to an identifier.
 *
 */
public class VariableAssignment extends ASTNode {
	
	public VariableReference variableReference;
	public Expression expression;

	@Override
	public String getNodeLabel() {
		return "VariableAssignment (" + variableReference.name + ")";
	}

	@Override
	public ASTNode addChild(ASTNode child) {
		if(variableReference == null) {
			variableReference = (VariableReference) child;
		} else if(expression == null) {
			expression = (Expression) child;
		}

		return this;
	}

	@Override
	public ASTNode removeChild(ASTNode child) {
		Expression temp = expression;
		expression = null;
		return temp;
	}

	@Override
	public ArrayList<ASTNode> getChildren() {

		ArrayList<ASTNode> children = new ArrayList<>();
		if(variableReference != null)
			children.add(variableReference);
		if(expression != null)
			children.add(expression);
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		VariableAssignment that = (VariableAssignment) o;
		return Objects.equals(variableReference, that.variableReference) &&
				Objects.equals(expression, that.expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(variableReference, expression);
	}
}

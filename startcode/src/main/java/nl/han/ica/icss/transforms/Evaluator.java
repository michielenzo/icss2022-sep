package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;
import nl.han.ica.icss.checker.VariableManager;

import java.util.ArrayList;

public class Evaluator implements Transform {

    private final VariableManager varManager = new VariableManager();
    private final HANStack<ASTNode> container = new HANStack<>();

    @Override
    public void apply(AST ast) {
        walkThroughTreeRecursive(ast.root);
    }

    private void walkThroughTreeRecursive(ASTNode astNode) {

        varManager.determineExpTypeOfVarAssignment(astNode);

        if(astNode instanceof VariableAssignment){
            varManager.setVariableAssignmentValues((VariableAssignment) astNode);
        }

        if(astNode instanceof IfClause) evaluateIfClause((IfClause) astNode);

        if(astNode instanceof Expression) evaluateExpression((Expression) astNode);

        if(astNode.getChildren().size() != 0) container.push(astNode);

        for (ASTNode childNode: astNode.getChildren()) {
            walkThroughTreeRecursive(childNode);
        }

        if(astNode.getChildren().size() != 0) container.pop();
    }

    private void evaluateExpression(Expression exp) {
        if(exp instanceof AddOperation || exp instanceof SubtractOperation)
        {
            if(((Operation) exp).lhs instanceof MultiplyOperation){
                MultiplyOperation multiplyOperation = (MultiplyOperation) ((Operation) exp).lhs;

                int multiplication = calculateMultiplyOpp(multiplyOperation);
                replaceMultiplyOpp((Operation) exp, multiplyOperation, multiplication);

            } else {
                int result = calculateAddOrSubtractOpp((Operation) exp);
                replaceAddOrSubtractOpp(exp);
            }
        }
    }

    private void replaceAddOrSubtractOpp(Expression exp) {

    }

    private int calculateAddOrSubtractOpp(Operation opp) {

        if(opp.lhs instanceof Literal){
            final Literal oppRhs = (Literal) opp.rhs;
            final Literal oppLhs = (Literal) opp.lhs;
            return performAddOrSubtract(opp, getValueOfLiteral(oppLhs), getValueOfLiteral(oppRhs));
        } else if (opp.lhs instanceof AddOperation){
            final Literal oppLhs = (Literal) ((AddOperation) opp.lhs).rhs;
            final Literal oppRhs = (Literal) opp.rhs;
            return performAddOrSubtract(opp, getValueOfLiteral(oppLhs), getValueOfLiteral(oppRhs));
        } else if (opp.lhs instanceof SubtractOperation){
            final Literal oppLhs = (Literal) ((SubtractOperation) opp.lhs).rhs;
            final Literal oppRhs = (Literal) opp.rhs;
            return performAddOrSubtract(opp, getValueOfLiteral(oppLhs), getValueOfLiteral(oppRhs));
        }

        return 0;
    }

    private int performAddOrSubtract(Operation opp, int lhs, int rhs){
        return opp instanceof AddOperation ? lhs + rhs : lhs - rhs;
    }

    private void replaceMultiplyOpp(Operation exp, MultiplyOperation multiplyOpp, int multiplication) {
        if(multiplyOpp.lhs instanceof Literal){
            ExpressionType multiplyOppExpType = varManager.getExpressionType(multiplyOpp);
            Literal literal;

            if(multiplyOppExpType == ExpressionType.SCALAR) {
                literal = new ScalarLiteral(multiplication);
            } else if(multiplyOppExpType == ExpressionType.PIXEL){
                literal = new PixelLiteral(multiplication);
            } else if(multiplyOppExpType == ExpressionType.PERCENTAGE){
                literal = new PercentageLiteral(multiplication);
            } else{
                literal = new ScalarLiteral(multiplication);
            }

            exp.lhs = literal;
        }

        if(multiplyOpp.lhs instanceof AddOperation){
           AddOperation newAddOpp = new AddOperation();
           newAddOpp.lhs = ((AddOperation) multiplyOpp.lhs).lhs;
           newAddOpp.rhs = new ScalarLiteral(multiplication);
           exp.lhs = newAddOpp;
        }

        if(multiplyOpp.lhs instanceof SubtractOperation){
            SubtractOperation newSubtractOpp = new SubtractOperation();
            newSubtractOpp.lhs = ((SubtractOperation) multiplyOpp.lhs).lhs;
            newSubtractOpp.rhs = new ScalarLiteral(multiplication);
            exp.lhs = newSubtractOpp;
        }
    }

    private int calculateMultiplyOpp(MultiplyOperation exp){
        Integer lhsValue = null;
        Integer rhsValue = null;

        if(exp.lhs instanceof Literal){
            lhsValue = getValueOfLiteral((Literal) exp.lhs);
        } else if(exp.lhs instanceof Operation) {
            lhsValue = getValueOfLiteral((Literal) ((Operation) exp.lhs).rhs);
        }

        if(exp.rhs instanceof Literal){
            rhsValue = getValueOfLiteral((Literal) exp.rhs);
        }

        if(lhsValue != null && rhsValue != null){
            return lhsValue * rhsValue;
        }

        return 0;
    }

    public Integer getValueOfLiteral(Literal exp) {
        if(exp instanceof ScalarLiteral){
            return ((ScalarLiteral) exp).value;
        } else if(exp instanceof PixelLiteral){
            return ((PixelLiteral) exp).value;
        } else if(exp instanceof PercentageLiteral){
            return ((PercentageLiteral) exp).value;
        }

        return null;
    }

    private void evaluateIfClause(IfClause ifClause){
        ASTNode parent = (ASTNode) container.peek();

        if(conditionIsTrue(ifClause.conditionalExpression)){
            parent.removeChild(ifClause);
            adoptChildren(ifClause, parent, ifClause.conditionalExpression);
            parent.removeChild(ifClause.elseClause);
        } else {
            parent.removeChild(ifClause);
            adoptChildren(ifClause.elseClause, parent);
        }
    }

    private void adoptChildren(ASTNode bioParent, ASTNode adopter, ASTNode exceptFor) {
        ArrayList<ASTNode> children = bioParent.getChildren();
        for (ASTNode child : children) {
            if(child.equals(exceptFor)) continue;
            adopter.addChild(child);
        }
    }

    private void adoptChildren(ASTNode bioParent, ASTNode adopter) {
        ArrayList<ASTNode> children = bioParent.getChildren();
        for (ASTNode child : children) {
            adopter.addChild(child);
        }
    }

    private boolean conditionIsTrue(Expression exp) {
        if(exp instanceof BoolLiteral) return ((BoolLiteral) exp).value;

        if(exp instanceof VariableReference) {
            Literal literal = varManager.getValueOfVarReference((VariableReference) exp);

            if(literal instanceof BoolLiteral) return ((BoolLiteral) literal).value;
            else return false;
        }

        return false;
    }
}

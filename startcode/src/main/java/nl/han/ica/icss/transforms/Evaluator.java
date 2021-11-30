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
import nl.han.ica.icss.checker.VariableManager;

import java.util.ArrayList;

public class Evaluator implements Transform {

    private final VariableManager varManager = new VariableManager();
    private final HANStack<ASTNode> container = new HANStack<>();

    private AST ast = null;

    @Override
    public void apply(AST ast) {
        this.ast = ast;
        walkThroughASTRecursive(ast.root);
        System.out.println();
    }

    private void walkThroughASTRecursive(ASTNode astNode) {

        varManager.determineExpTypeOfVarAssignment(astNode);

        if(astNode instanceof VariableAssignment){
            varManager.setVariableAssignmentValues((VariableAssignment) astNode);
        }

        if(astNode instanceof IfClause) evaluateIfClause((IfClause) astNode);

        if(astNode instanceof Expression && !(astNode instanceof VariableReference)) {
            evaluateExpression((Expression) astNode);
        }

        if(astNode.getChildren().size() != 0) container.push(astNode);

        for (ASTNode childNode: astNode.getChildren()) {
            if(!(astNode instanceof Expression)) walkThroughASTRecursive(childNode);
        }

        if(astNode.getChildren().size() != 0) container.pop();
    }

    private void evaluateExpression(Expression exp) {

        container.push(exp);

        for(ASTNode child: exp.getChildren()){
            evaluateExpression((Expression) child);
        }

        container.pop();

        if(exp instanceof Operation && isExpressionDoneCalculating(exp)) return;

        if(exp instanceof AddOperation || exp instanceof SubtractOperation){
            ASTNode parent = (ASTNode) container.peek();

            if(parent instanceof MultiplyOperation){
                ASTNode grandParent = (ASTNode) container.peek(2);

                int multiplication = calculateMultiplyOpp((MultiplyOperation) parent);

                replaceMultiplyOpp(grandParent, (MultiplyOperation) parent, multiplication);

                int result = calculateAddOrSubtractOpp((Operation) exp);

                if(grandParent instanceof Operation){
                    ((Operation) grandParent).lhs = constructLiteral((Expression) parent, result);
                } else {
                    grandParent.removeChild(parent);
                    grandParent.addChild(constructLiteral((Expression) parent, result));
                }

            } else {
                int result = calculateAddOrSubtractOpp((Operation) exp);

                parent.removeChild(exp);
                parent.addChild(constructLiteral(exp, result));
            }
        } else if(exp instanceof MultiplyOperation){
            ASTNode parent = (ASTNode) container.peek();

            int multiplication = calculateMultiplyOpp((MultiplyOperation) exp);

            Literal newLiteral = constructLiteral(exp, multiplication);
            parent.removeChild(exp);
            parent.addChild(newLiteral);
        }
    }

    private boolean isExpressionDoneCalculating(Expression exp){

        ASTNode current = exp;
        int indexCount = 0;

        while (current instanceof Operation){
            if(indexCount != 0) current = (ASTNode) container.peek(indexCount);
            else                current = (ASTNode) container.peek();

            indexCount++;
        }

        ArrayList<ASTNode> children = current.getChildren();

        for (ASTNode child: children) if(child instanceof Operation) return false;

        return true;
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

    private void replaceMultiplyOpp(ASTNode grandParent, MultiplyOperation multiplyOpp, int multiplication) {
        final Expression lhs = multiplyOpp.lhs;

        if(lhs instanceof Operation){
            ((Operation) lhs).rhs = constructLiteral(multiplyOpp, multiplication);
        }

        if(grandParent instanceof Operation){
            ((Operation) grandParent).lhs = lhs;
        } else {
            grandParent.removeChild(multiplyOpp);
            grandParent.addChild(multiplyOpp.lhs);
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

    private Literal constructLiteral(Expression exp, int value){
        switch(varManager.getExpressionType(exp)){
            case PIXEL: return new PixelLiteral(value);
            case PERCENTAGE: return new PercentageLiteral(value);
            default: return new ScalarLiteral(value);
        }
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

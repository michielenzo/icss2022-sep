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
    private final HANStack<ASTNode> expContainer = new HANStack<>();

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
            evaluateExpression2((Expression) astNode);
        }

        if(astNode.getChildren().size() != 0) container.push(astNode);

        for (ASTNode childNode: astNode.getChildren()) {
            if(!(astNode instanceof Expression)) walkThroughASTRecursive(childNode);
        }

        if(astNode.getChildren().size() != 0) container.pop();
    }

    private void evaluateExpression2(Expression exp) {

        container.push(exp);

        for(ASTNode child: exp.getChildren()){
            evaluateExpression2((Expression) child);
        }

        container.pop();


        if(exp instanceof AddOperation || exp instanceof SubtractOperation){
            ASTNode parent = (ASTNode) container.peek();

            if(parent instanceof MultiplyOperation){
                ASTNode grandParent = (ASTNode) container.peek(2);

                int multiplication = calculateMultiplyOpp((MultiplyOperation) parent);
                replaceMultiplyOpp2((Operation) grandParent, (MultiplyOperation) parent, multiplication);
                int result = calculateAddOrSubtractOpp((Operation) exp);
                ((Operation) grandParent).lhs = new ScalarLiteral(result);
            } else {
                parent.removeChild(exp);

                int result = calculateAddOrSubtractOpp((Operation) exp);
                ScalarLiteral newScalar = new ScalarLiteral(result);
                parent.addChild(newScalar);
            }
        }
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

    private void replaceMultiplyOpp2(Operation mainExp, MultiplyOperation multiplyOpp, int multiplication) {
        ScalarLiteral scalarLiteral = new ScalarLiteral(multiplication);

        final Expression lhs = multiplyOpp.lhs;

        if(lhs instanceof Operation){
            ((Operation) lhs).rhs = scalarLiteral;
        }

        mainExp.lhs = lhs;
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

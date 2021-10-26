package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes = new HANLinkedList<>();

    public void check(AST ast) {
        variableTypes.clear();
        walkThroughTreeRecursive(ast.root);
    }

    private void walkThroughTreeRecursive(ASTNode astNode){

        determineExpTypeOfVarDeclaration(astNode);
        checkVarRefsDeclared(astNode);

        for (ASTNode childNode: astNode.getChildren()) {
            walkThroughTreeRecursive(childNode);
        }
    }

    private void determineExpTypeOfVarDeclaration(ASTNode astNode){
        if (astNode instanceof VariableAssignment) {
            HashMap<String, ExpressionType> variableAssignment = new HashMap<>();

            variableAssignment.put(
                    ((VariableAssignment) astNode).name.name,
                    determineExpType(((VariableAssignment) astNode).expression));

            variableTypes.addFirst(variableAssignment);
        }
    }

    private void checkVarRefsDeclared(ASTNode astNode){
        if(astNode instanceof VariableReference) {
            if(!isVarRefDeclared(astNode)) {
                astNode.setError("Variable is not declared.");
            }
        }
    }

    private boolean isVarRefDeclared(ASTNode astNode) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if(variableTypes.get(i).get(((VariableReference) astNode).name) != null){
                return true;
            }
        }

        return false;
    }

    private ExpressionType determineExpType(Expression exp) {
        Set<ExpressionType> expTypesInExp = new HashSet<>();
        getDistinctExpTypes(exp, expTypesInExp);

        expTypesInExp.remove(ExpressionType.SCALAR);
        expTypesInExp.remove(ExpressionType.UNDEFINED);

        if(expTypesInExp.size() > 1) {
            exp.setError("TypeError, literals in expression dont match.");
            return ExpressionType.UNDEFINED;
        }

        return (ExpressionType) expTypesInExp.toArray()[0];
    }

    private void getDistinctExpTypes(Expression expression, Set<ExpressionType> expTypesInExp) {

        if(expression instanceof Operation){
            getDistinctExpTypes(((Operation) expression).lhs, expTypesInExp);
            getDistinctExpTypes(((Operation) expression).rhs, expTypesInExp);
        } else {
            if(expression instanceof ScalarLiteral){
                expTypesInExp.add(ExpressionType.SCALAR);
            } else if (expression instanceof PixelLiteral) {
                expTypesInExp.add(ExpressionType.PIXEL);
            }else if (expression instanceof BoolLiteral) {
                expTypesInExp.add(ExpressionType.BOOL);
            }else if (expression instanceof ColorLiteral) {
                expTypesInExp.add(ExpressionType.COLOR);
            }else if (expression instanceof PercentageLiteral) {
                expTypesInExp.add(ExpressionType.PERCENTAGE);
            }
        }
    }
}

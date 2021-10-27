package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Checker {

    private final IHANLinkedList<HashMap<String, ExpressionType>> variableTypes = new HANLinkedList<>();

    public void check(AST ast) {
        variableTypes.clear();
        walkThroughTreeRecursive(ast.root);
    }

    private void walkThroughTreeRecursive(ASTNode astNode){

        determineExpTypeOfVarDeclaration(astNode);
        checkVarRefsDeclared(astNode);
        checkOperation(astNode);

        checkPropertyDeclaration(astNode);

        for (ASTNode childNode: astNode.getChildren()) {
            walkThroughTreeRecursive(childNode);
        }
    }

    private void checkPropertyDeclaration(ASTNode astNode) {
        if(astNode instanceof Declaration){
            if (propertyIsAColorAndNotAssignedByColorLiteral((Declaration) astNode)){
                astNode.setError("Color property must be assigned by a color literal.");
            }

            if(propertyIsWidthAndNotAssignedByPixelLiteral((Declaration) astNode)){
                astNode.setError("Width property must be assigned with a pixel literal.");
            }
        }
    }

    private boolean propertyIsWidthAndNotAssignedByPixelLiteral(Declaration astNode) {
        ExpressionType expressionType = determineExpType(astNode.expression);

        return Objects.equals(astNode.property.name, "width") ||
               Objects.equals(astNode.property.name, "height") &&
               expressionType != ExpressionType.PIXEL;
    }

    private boolean propertyIsAColorAndNotAssignedByColorLiteral(Declaration astNode) {
        return Objects.equals(astNode.property.name, "color") ||
               Objects.equals(astNode.property.name, "background-color") &&
               !(astNode.expression instanceof ColorLiteral);
    }

    private void checkOperation(ASTNode astNode) {
        if(astNode instanceof Operation) {
            if(operationHasColorLiteral((Operation) astNode)){
                astNode.setError("TypeError: Cannot operate on a color literal");
            }
        }

        if(astNode instanceof SubtractOperation || astNode instanceof AddOperation) {
            if(isAddOrSubtractOperationWithDistinctLiterals((Operation) astNode)) {
                astNode.setError("TypeError: Cannot add or subtract with distinct literals.");
            }
        }

        if(astNode instanceof MultiplyOperation) {
            if(isMultiplyOperationWithoutScalars((MultiplyOperation) astNode)) {
                astNode.setError("TypeError: Cannot multiply with only non scalars");
            }
        }
    }

    private boolean operationHasColorLiteral(Operation astNode) {
        return astNode.lhs instanceof ColorLiteral || astNode.rhs instanceof ColorLiteral;
    }

    private boolean isAddOrSubtractOperationWithDistinctLiterals(Operation astNode) {
        return astNode.lhs.getClass() != astNode.rhs.getClass();
    }

    private boolean isMultiplyOperationWithoutScalars(MultiplyOperation astNode) {
        return !(astNode.rhs instanceof ScalarLiteral) &&
               !(astNode.lhs instanceof ScalarLiteral);
    }

    private void checkVarRefsDeclared(ASTNode astNode){
        if(astNode instanceof VariableReference) {
            if(!isVarRefDeclared(astNode)) {
                astNode.setError("Variable is not declared.");
            }
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

        int countOfNonScalars = 0;
        if(expTypesInExp.contains(ExpressionType.PIXEL)) countOfNonScalars++;
        if(expTypesInExp.contains(ExpressionType.COLOR)) countOfNonScalars++;
        if(expTypesInExp.contains(ExpressionType.PERCENTAGE)) countOfNonScalars++;

        if(countOfNonScalars > 1) {
            exp.setError("TypeError, literals in expression dont match.");
            return ExpressionType.UNDEFINED;
        } else if (countOfNonScalars == 1){
            expTypesInExp.remove(ExpressionType.SCALAR);
            return (ExpressionType) expTypesInExp.toArray()[0];
        } else {
            return ExpressionType.SCALAR;
        }
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

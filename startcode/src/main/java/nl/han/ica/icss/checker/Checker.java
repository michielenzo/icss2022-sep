package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.Objects;

public class Checker {

    private final VariableManager varManager = new VariableManager();
    private final Scope scopeTree = new Scope();
    private final HANStack<Scope> scopeContainer = new HANStack<>();

    public void check(AST ast) {
        varManager.variableTypes.clear();
        scopeContainer.push(scopeTree);
        walkThroughASTRecursive(ast.root);
    }

    private void walkThroughASTRecursive(ASTNode astNode){

        varManager.determineExpTypeOfVarAssignment(astNode);

        checkVarRefsDeclared(astNode);
        checkOperation(astNode);
        checkPropertyDeclaration(astNode);
        checkIfClause(astNode);

        checkScope(astNode);

        for (ASTNode childNode: astNode.getChildren()) {
            walkThroughASTRecursive(childNode);
        }

        if(nodeAffectsScope(astNode)) scopeContainer.pop();

    }

    private void checkScope(ASTNode astNode) {
        if(nodeAffectsScope(astNode)) {
            Scope scope = new Scope();
            scope.parent = (Scope) scopeContainer.peek();
            ((Scope) scopeContainer.peek()).subScopes.add(scope);
            scopeContainer.push(scope);
        }

        if(astNode instanceof VariableAssignment) {
            ((Scope) scopeContainer.peek()).varAssignments.add((VariableAssignment) astNode);
        }

        if(astNode instanceof VariableReference) {
            if(findAssignmentInScope(((VariableReference) astNode).name) == null) {
                astNode.setError("Scope error: variable not defined in current scope.");
            }
        }
    }

    private VariableAssignment findAssignmentInScope(String name) {
        Scope currentScope = (Scope) scopeContainer.peek();

        while(currentScope != null) {
            for (VariableAssignment assignment: currentScope.varAssignments) {
                if(Objects.equals(assignment.variableReference.name, name)) return assignment;
            }

            currentScope = currentScope.parent;
        }

        return null;
    }

    private void checkIfClause(ASTNode astNode) {
        if(astNode instanceof IfClause){
            Expression conditionalExp = ((IfClause) astNode).conditionalExpression;

            if(conditionalExp instanceof VariableReference) {
                for (int i = 0; i < varManager.variableTypes.getSize(); i++) {
                    HashMap<String, ExpressionType> varType = varManager.variableTypes.get(i);

                    if (varTypeMatchesWithConditionalExp((VariableReference) conditionalExp, varType)){
                        if(!declarationIsOfTypeBool((VariableReference) conditionalExp, varType)){
                            astNode.setError("Variable reference must be of type boolean.");
                        }
                    }
                }
            } else if(conditionalExp instanceof Operation){
                astNode.setError("Conditional expression cannot be calculated.");
            } else if(!(conditionalExp instanceof BoolLiteral)){
                astNode.setError("Conditional expression must be a boolean.");
            }
        }
    }

    private void checkPropertyDeclaration(ASTNode astNode) {
        if(astNode instanceof Declaration){
            if (propertyIsAColorAndNotAssignedByColorLiteral((Declaration) astNode)){
                astNode.setError("Color/background-color property must be assigned by a color literal.");
            }

            if(propertyIsWidthAndNotAssignedByPixelLiteral((Declaration) astNode)){
                astNode.setError("Width/height property must be assigned with a pixel literal.");
            }
        }
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

    private void checkVarRefsDeclared(ASTNode astNode){
        if(astNode instanceof VariableReference) {
            if(!isVarRefDeclared(astNode)) {
                astNode.setError("Variable is not declared.");
            }
        }
    }

    private boolean isVarRefDeclared(ASTNode astNode) {
        for (int i = 0; i < varManager.variableTypes.getSize(); i++) {
            if(varManager.variableTypes.get(i).get(((VariableReference) astNode).name) != null){
                return true;
            }
        }

        return false;
    }

    private boolean declarationIsOfTypeBool(VariableReference conditionalExp, HashMap<String, ExpressionType> varType) {
        return varType.get(conditionalExp.name) == ExpressionType.BOOL;
    }

    private boolean varTypeMatchesWithConditionalExp(VariableReference conditionalExp, HashMap<String, ExpressionType> variableType) {
        return variableType.containsKey(conditionalExp.name);
    }

    private boolean operationHasColorLiteral(Operation operation) {
        return operation.lhs instanceof ColorLiteral || operation.rhs instanceof ColorLiteral;
    }

    private boolean isAddOrSubtractOperationWithDistinctLiterals(Operation operation) {
        ExpressionType lhsExpType = varManager.getExpressionType(operation.lhs);
        ExpressionType rhsExpType = varManager.getExpressionType(operation.rhs);
        return lhsExpType != rhsExpType;
    }

    private boolean isMultiplyOperationWithoutScalars(MultiplyOperation multiplyOperation) {
        ExpressionType lhsExpType = varManager.getExpressionType(multiplyOperation.lhs);
        ExpressionType rhsExpType = varManager.getExpressionType(multiplyOperation.rhs);

        return rhsExpType != ExpressionType.SCALAR &&
               lhsExpType != ExpressionType.SCALAR;
    }

    private boolean nodeAffectsScope(ASTNode astNode) {
        return astNode instanceof Stylerule || astNode instanceof IfClause || astNode instanceof ElseClause;
    }

    private boolean propertyIsWidthAndNotAssignedByPixelLiteral(Declaration astNode) {
        ExpressionType expressionType = varManager.getExpressionType(astNode.expression);

        return (Objects.equals(astNode.property.name, "width") ||
                Objects.equals(astNode.property.name, "height")) &&
                expressionType != ExpressionType.PIXEL;
    }

    private boolean propertyIsAColorAndNotAssignedByColorLiteral(Declaration declaration) {
        ExpressionType expressionType = varManager.getExpressionType(declaration.expression);

        return (Objects.equals(declaration.property.name, "color") ||
                Objects.equals(declaration.property.name, "background-color")) &&
                expressionType != ExpressionType.COLOR;
    }
}

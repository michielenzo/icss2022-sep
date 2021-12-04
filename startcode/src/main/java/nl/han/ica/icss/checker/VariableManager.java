package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;

public class VariableManager {

    public final IHANLinkedList<HashMap<String, ExpressionType>> variableTypes = new HANLinkedList<>();

    public final IHANLinkedList<HashMap<String, Literal>> variableValues = new HANLinkedList();

    public void determineExpTypeOfVarAssignment(ASTNode astNode){
        if (astNode instanceof VariableAssignment) {
            HashMap<String, ExpressionType> variableAssignment = new HashMap<>();

            variableAssignment.put(
                    ((VariableAssignment) astNode).variableReference.name,
                    getExpressionType(((VariableAssignment) astNode).expression));

            variableTypes.addFirst(variableAssignment);
        }
    }

    public ExpressionType getExpressionType(Expression exp){
        if(exp instanceof Operation){
            ExpressionType lhs = getExpressionType(((Operation) exp).lhs);
            ExpressionType rhs = getExpressionType(((Operation) exp).rhs);

            if(lhs != ExpressionType.SCALAR) return lhs;
            else if(rhs != ExpressionType.SCALAR) return rhs;
            else return ExpressionType.SCALAR;

        } else if(exp instanceof VariableReference) {
            return getExpressionTypeForVarAssignment((VariableReference) exp);
        } else if(exp instanceof ScalarLiteral){
            return ExpressionType.SCALAR;
        } else if(exp instanceof PixelLiteral){
            return ExpressionType.PIXEL;
        } else if(exp instanceof BoolLiteral){
            return ExpressionType.BOOL;
        } else if(exp instanceof PercentageLiteral){
            return ExpressionType.PERCENTAGE;
        } else if(exp instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        }

        return ExpressionType.UNDEFINED;
    }

    public void setVariableAssignmentValues(VariableAssignment varAssignment){
        if(varAssignment.expression instanceof Literal){
            HashMap<String, Literal> mapping = new HashMap<>();
            mapping.put(varAssignment.variableReference.name, ((Literal) varAssignment.expression));
            variableValues.addFirst(mapping);
        }
    }

    public Literal getValueOfVarReference(VariableReference varReference){
        for (int i = 0; i < variableValues.getSize(); i++) {
            if(variableValues.get(i).containsKey(varReference.name)){
                return variableValues.get(i).get(varReference.name);
            }
        }

        return null;
    }

    private ExpressionType getExpressionTypeForVarAssignment(VariableReference reference) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> varType = variableTypes.get(i);

            if (varType.containsKey(reference.name)){
                return varType.get(reference.name);
            }
        }

        reference.setError("Variable is not declared.");

        return ExpressionType.UNDEFINED;
    }
}

package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.VariableAssignment;

import java.util.ArrayList;
import java.util.List;

public class Scope {

    List<VariableAssignment> varAssignments = new ArrayList<>();
    List<Scope> subScopes = new ArrayList<>();
    Scope parent = null;
}
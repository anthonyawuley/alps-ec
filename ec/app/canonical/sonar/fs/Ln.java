/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.canonical.sonar.fs;

import ec.*;
import ec.app.canonical.pima.DoubleData;
import ec.gp.*;
import ec.util.*;

public class Ln extends GPNode
    {
    public String toString() { return "ln"; }

    public void checkConstraints(final EvolutionState state,
        final int tree,
        final GPIndividual typicalIndividual,
        final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=1) /** one arity function */
            state.output.error("Incorrect number of children for node " + 
                toStringForError() + " at " +
                individualBase);
        }

    public int expectedChildren() { return 1; }
    
    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        double result;
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        /** Return natural log base of terminal value */
        rd.x = Math.log((double)Math.abs(rd.x));
        }
    }


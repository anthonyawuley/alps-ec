/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.vanilla.pima.fs;

import ec.*;
import ec.app.vanilla.pima.DoubleData;
import ec.gp.*;
import ec.util.*;

public class Min extends GPNode
    {
    public String toString() { return "min"; }

    public void checkConstraints(final EvolutionState state,
        final int tree,
        final GPIndividual typicalIndividual,
        final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=2)
            state.output.error("Incorrect number of children for node " + 
                toStringForError() + " at " +
                individualBase);
        }

    public int expectedChildren() { return 2; }
    
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
        result = rd.x;

        children[1].eval(state,thread,input,stack,individual,problem);
        //return minimum terminal value
        rd.x = (result<rd.x)?result:rd.x;
        }
    }


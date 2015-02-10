/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

/*
 Anthony Awuley
 https://archive.ics.uci.edu/ml/datasets/Ionosphere
 Ionosphere Dataset
*/

package ec.app.fsalps.ionosphere.ts;

import ec.*;
import ec.app.fsalps.ionosphere.DoubleData;
import ec.app.fsalps.ionosphere.Ionosphere;
import ec.gp.*;
import ec.util.*;

public class Ion7 extends GPNode
    {
    public String toString() { return "ion7"; }

    public void checkConstraints(final EvolutionState state,
        final int tree,
        final GPIndividual typicalIndividual,
        final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=0)
            state.output.error("Incorrect number of children for node " + 
                toStringForError() + " at " +
                individualBase);
        }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
          DoubleData rd = ((DoubleData)(input));
          rd.x = ((Ionosphere)problem).ion7;
        }
    }


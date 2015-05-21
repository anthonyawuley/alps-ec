/*
  Copyright 2015 by Anthony Awuley and Brian Ross
  Department of Computer Science
  Brock University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
  
  Cuprite Hyperspectral Data
  http://www.ehu.eus/ccwintco/index.php?title=Hyperspectral_Remote_Sensing_Scenes
*/



package ec.app.alps.indianpines.ts;

import ec.*;
import ec.app.alps.indianpines.DoubleData;
import ec.app.alps.indianpines.IndianPines;
import ec.gp.*;
import ec.util.*;

public class B76 extends GPNode
    {
    public String toString() { return "b76"; }

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
          rd.x = ((IndianPines)problem).b76;
        }
    }


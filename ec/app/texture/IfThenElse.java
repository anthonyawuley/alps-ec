/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.texture;
import ec.*;
import ec.gp.*;
//import ec.util.*;

public class IfThenElse extends GPNode
    {
    public String toString() { return "IfThenElse"; }

/*
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
*/
    public int expectedChildren() { return 4; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
    	double resultA,resultB,resultC,resultD;
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        resultA = rd.x;

        children[1].eval(state,thread,input,stack,individual,problem);
        resultB = rd.x;
        
        children[2].eval(state,thread,input,stack,individual,problem);
        resultC = rd.x;
        
        children[3].eval(state,thread,input,stack,individual,problem);
        resultD = rd.x;
        
        if (resultA > resultB)
            rd.x= resultC;
        else
            rd.x=resultD;
        }


    }


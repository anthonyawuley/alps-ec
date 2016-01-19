package ec.app.texture;

/*
Copyright 2006 by Sean Luke
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import ec.*;
import ec.app.texture.DoubleData;
import ec.gp.*;
import ec.util.*;

/* 
* X.java
* 
* Created: Wed Nov  3 18:26:37 1999
* By: Sean Luke
*/


public class Avg9x9 extends GPNode
  {
  public String toString() { return "avg9x9"; }

/*
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
*/
  public int expectedChildren() { return 0; }

  public void eval(final EvolutionState state,
      final int thread,
      final GPData input,
      final ADFStack stack,
      final GPIndividual individual,
      final Problem problem)
      {
      DoubleData rd = ((DoubleData)(input));
      rd.x = ((MultiValuedRegression)problem).avg9x9;
      }
  }




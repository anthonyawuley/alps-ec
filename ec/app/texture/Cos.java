
package ec.app.texture;
import ec.*;
import ec.gp.*;
import ec.util.*;



public class Cos extends GPNode
    {
    public String toString() { return "Cos"; }

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
        rd.x = Math.cos(rd.x);
        }
    }
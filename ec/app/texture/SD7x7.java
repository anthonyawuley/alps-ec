
package ec.app.texture;
import ec.*;
import ec.gp.*;
import ec.util.*;


public class SD7x7 extends GPNode
    {
    public String toString() { return "SD7x7"; }


    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        DoubleData rd = ((DoubleData)(input));
        rd.x = ((MultiValuedRegression)problem).SD7x7;
        }
    }


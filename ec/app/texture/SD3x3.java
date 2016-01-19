
package ec.app.texture;
import ec.*;
import ec.gp.*;
import ec.util.*;


public class SD3x3 extends GPNode
    {
    public String toString() { return "SD3x3"; }


    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        DoubleData rd = ((DoubleData)(input));
        rd.x = ((MultiValuedRegression)problem).SD3x3;
        }
    }


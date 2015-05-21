package ec.app.canonical.cuprite.ts;
import ec.*;
import ec.app.canonical.cuprite.DoubleData;
import ec.gp.*;
import ec.util.*;

import java.io.*;

public class RegERC extends ERC
    {
    public double value;

    /*
     * ERC.checkConstraints() verifies that arity is 0
     * Number are generated in the range of -1.0, 1.0
     */
    public void resetNode(final EvolutionState state, final int thread)
        { value = state.random[thread].nextDouble() * 2 - 1.0; }

    public int nodeHashCode()
        {
        // a reasonable hash code
        return this.getClass().hashCode() + Float.floatToIntBits((float)value);
        }

    public boolean nodeEquals(final GPNode node)
        {
        /** Check to ensure ERC value is maintained after initial generation */
        if (this.getClass() != node.getClass()) return false;
        
        return (((RegERC)node).value == value);
        }

    public void readNode(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        value = dataInput.readDouble();
        }

    public void writeNode(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeDouble(value);
        }

    public String encode()
        { return Code.encode(value); }

    public boolean decode(DecodeReturn dret)
        {
        /** Keep copy */
        int pos = dret.pos;
        String data = dret.data;

        /** decode */
        Code.decode(dret);

        if (dret.type != DecodeReturn.T_DOUBLE) // uh oh!
            {
            /** restore the position and the string; it was an error */
            dret.data = data;
            dret.pos = pos;
            return false;
            }

         /** Keep copy */
        value = dret.d;
        return true;
        }

    public String toStringForHumans()
        { return ""+(float)value; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        DoubleData rd = ((DoubleData)(input));
        rd.x = value;
        }
    }
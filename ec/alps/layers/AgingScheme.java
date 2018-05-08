package ec.alps.layers;


import ec.alps.Engine;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;


/**
 * An aging scheme is used to define the age properties of ALPS layers. This is used to seperate individuals
 * into age layers. Examples of such schemes are
 * Linear Fibonacci(n) Polynomial(n^2 ) Exponential(2^n)
 * These values are multiplied by an age gap parameter to determine the maximum age of a layer.
 * As an example, given an exponential aging scheme with an age gap of 10 and 6 layers,
 * the maximum ages for the layers  will be 10, 20, 40, 80, 160, 320.
 * This means Individuals within a layer are not allowed to outgrow the maximum allowed age
 * <p>
 * At initialization, the age layers are setup for evolution using parameters from the parameter file
 *
 * @author Anthony Awuley
 */
public abstract class AgingScheme {
    /** */
    public final static String AGING_SCHEME = "aging-scheme";
    /** */
    public final static String AGE_GAP = "age-gap";
    /** */
    public final static String AGE_LAYERS = "number-of-layers";
    /** */
    public static int alpsAgeGap = 0;
    /** */
    public static int alpsAgeLayers = 0;
    /** */
    public ArrayList<Layer> layers;

    /** */
    public Parameter defaultBase() {
        return Engine.base().push(AGING_SCHEME);
    }

    public void setup(final ParameterDatabase base) {
		/*
		if (!state.parameters.exists(base.push(AGE_GAP), null))
			 state.output.fatal("age gap parameter \"alps."+AGE_GAP+"\" has not been defined ");
		if (!state.parameters.exists(base.push(AGE_LAYERS), null))
			 state.output.fatal("number of layers parameter \"alps."+AGE_LAYERS+"\" has not been defined ");       
		 */

        alpsAgeGap = base.getInt(Engine.base().push(AGE_GAP), null);
        alpsAgeLayers = base.getInt(Engine.base().push(AGE_LAYERS), null);

    }


    public abstract String toString();

    /**
     * Every age schemes implments agingScheme() to determine the age properties of
     * all ALPS layers. This includes basic initialization of the ALPS layers
     *
     * @return ArrayList<Layer>
     */
    public abstract ArrayList<Layer> agingScheme();


}

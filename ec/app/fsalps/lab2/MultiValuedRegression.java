/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.app.fsalps.lab2;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;

public class MultiValuedRegression extends GPProblem implements SimpleProblemForm
{
	private static final long serialVersionUID = 1;

	public double currentQ;
	public double currentR;
	public double currentS;
	public double currentT;
	public double currentU;
	public double currentV;
	public double currentW;
	public double currentX;
	public double currentY;
	public double currentZ;

	public void setup(final EvolutionState state,final Parameter base)
	{
		super.setup(state, base);

		// verify our input is the right class (or subclasses from it)
		if (!(input instanceof DoubleData))
			state.output.fatal("GPData class must subclass from " + DoubleData.class,
					base.push(P_DATA), null);
	}

	public void evaluate(final EvolutionState state, final Individual ind,final int subpopulation,final int threadnum)
	{
		if (!ind.evaluated)  // don't bother reevaluating
		{
			DoubleData input = (DoubleData)(this.input);

			int hits = 0;
			double sum = 0.0;
			double expectedResult;
			double result;
			for (int y=0;y<10;y++)
			{
				currentQ = state.random[threadnum].nextDouble();
				currentR = state.random[threadnum].nextDouble();
				currentS = state.random[threadnum].nextDouble();
				currentT = state.random[threadnum].nextDouble();
				currentU = state.random[threadnum].nextDouble();
				currentV = state.random[threadnum].nextDouble();
				currentW = state.random[threadnum].nextDouble();
				currentX = state.random[threadnum].nextDouble();
				currentY = state.random[threadnum].nextDouble();
				currentZ = state.random[threadnum].nextDouble();
				
				//expectedResult = currentZ*currentX*currentY + currentX*currentZ - currentQ*currentR + currentY*currentU*currentW;
				expectedResult = currentX*currentX*currentY + currentX*currentY + currentY;
				
				((GPIndividual)ind).trees[0].child.eval(
						state,threadnum,input,stack,((GPIndividual)ind),this);

				result = Math.abs(expectedResult - input.x);
				if (result <= 0.01) hits++;
				sum += result;                  
			}

			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness)ind.fitness);
			f.setStandardizedFitness(state, sum);
			f.hits = hits;
			ind.evaluated = true;
		}
	}
}


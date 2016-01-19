package ec.alps.gp.breed;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.alps.Engine;
import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.gp.koza.MutationPipeline;

public class Mutation extends MutationPipeline {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	private final String SELECTION_PRESSURE = "selection-pressure";

	/*
	public MutationALPS() {
		// TODO Auto-generated constructor stub
	}*/





	public int produce(final int min, 
			final int max, 
			final int start,
			final int subpopulation,
			final Individual[] inds,
			final EvolutionState state,
			final int thread) 
	{ 

		double selectionPressure = state.parameters.getDouble(Engine.base().push(SELECTION_PRESSURE), null);

		int n;
		

		if(Engine.alps.layers.get(Engine.alps.index).getIsBottomLayer() || 
				state.random[0].nextDouble()<=selectionPressure || 
						Engine.alps.layers.get(Engine.alps.index-1).evolutionState.population.subpops[subpopulation].individuals.length==0)
		{
			// grab individuals from our source and stick 'em right into inds.
			// we'll modify them from there
			n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

			// should we bother?
			if (!state.random[thread].nextBoolean(likelihood))
				return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did
		}
		else//select individual from lower layer
		{ 
			// grab individuals from our source and stick 'em right into inds.
			// we'll modify them from there
			n = sources[0].produce(min,max,start,subpopulation,inds,
					Engine.alps.layers.get(Engine.alps.index-1).evolutionState,thread);

			// should we bother?
			if (!state.random[thread].nextBoolean(likelihood))
				return reproduce(n, start, subpopulation, inds, 
						Engine.alps.layers.get(Engine.alps.index-1).evolutionState, thread, false);  // DON'T produce children from source -- we already did
			
			/* When this flag is enabled, prevent increasing age for idividuals selected from lower layer for breeding  */
			if(Engine.alps_age_only_current_layer)
				for(int q=start; q < n+start; q++)
					inds[q].generationCount       = state.generation;
				
		}
		
		GPInitializer initializer = ((GPInitializer)state.initializer);

		// now let's mutate 'em
		for(int q=start; q < n+start; q++)
		{ 
			/**
			 * ALPS: AGE INCREMENT
			 * increase age of parent 
			 * 
			 * Each generation in which an individual is used as a parent to create an offspring its age is 
			 * increases by 1 since its genetic material has been used in evolution in another generation. 
			 * Even if an individual is selected to reproduce multiple times in one generation its age is 
			 * still only increased by 1 so that good individuals that reproduce a lot are not penalized for 
			 * being more fit than similarly aged individuals.  --- GREG Hornby
			 * 
			 * There's no need modifying evaluation count for individuals because its still the same
			 * 
			 * @author Anthony
			 */
			if(state.generation != inds[q].generationCount/*!parents[0].parentFlag*/) 
			{
				inds[q].age++;
				inds[q].generationCount = state.generation;
			}


			GPIndividual i = (GPIndividual)inds[q];

			if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
				// uh oh
				state.output.fatal("GP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 


			int t;
			// pick random tree
			if (tree==TREE_UNFIXED)
				if (i.trees.length>1) t = state.random[thread].nextInt(i.trees.length);
				else t = 0;
			else t = tree;

			// validity result...
			boolean res = false;

			// prepare the nodeselector
			nodeselect.reset();

			// pick a node

			GPNode p1=null;  // the node we pick
			GPNode p2=null;

			for(int x=0;x<numTries;x++)
			{
				// pick a node in individual 1
				p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.trees[t]);

				// generate a tree swap-compatible with p1's position


				int size = GPNodeBuilder.NOSIZEGIVEN;
				if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);

				p2 = builder.newRootedTree(state,
						p1.parentType(initializer),
						thread,
						p1.parent,
						i.trees[t].constraints(initializer).functionset,
						p1.argposition,
						size);

				// check for depth and swap-compatibility limits
				res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!

				// did we get something that had both nodes verified?
				if (res) break;
			}

			GPIndividual j;

			if (sources[0] instanceof BreedingPipeline)
				// it's already a copy, so just smash the tree in
			{
				j=i;
				if (res)  // we're in business
				{
					p2.parent = p1.parent;
					p2.argposition = p1.argposition;
					if (p2.parent instanceof GPNode)
						((GPNode)(p2.parent)).children[p2.argposition] = p2;
					else ((GPTree)(p2.parent)).child = p2;
					j.evaluated = false;  // we've modified it
				}
			}
			else // need to clone the individual
			{
				j = (GPIndividual)(i.lightClone());

				// Fill in various tree information that didn't get filled in there
				j.trees = new GPTree[i.trees.length];

				// at this point, p1 or p2, or both, may be null.
				// If not, swap one in.  Else just copy the parent.
				for(int x=0;x<j.trees.length;x++)
				{
					if (x==t && res)  // we've got a tree with a kicking cross position!
					{
						j.trees[x] = (GPTree)(i.trees[x].lightClone());
						j.trees[x].owner = j;
						j.trees[x].child = i.trees[x].child.cloneReplacingNoSubclone(p2,p1);
						j.trees[x].child.parent = j.trees[x];
						j.trees[x].child.argposition = 0;
						j.evaluated = false; 
					} // it's changed
					else 
					{
						j.trees[x] = (GPTree)(i.trees[x].lightClone());
						j.trees[x].owner = j;
						j.trees[x].child = (GPNode)(i.trees[x].child.clone());
						j.trees[x].child.parent = j.trees[x];
						j.trees[x].child.argposition = 0;                   
					}
				}
			}
			
			// add the new individual, replacing its previous source
			inds[q] = j;
			
			/**
			 * ALPS: AGE INCREMENT
			 * increase age of offsping 
			 * @author Anthony
			 * 
			 * age of parent already increased -- will automatically increase child's age
			 * 
			 * inds[q].age++;
			 */
			
			
		}
		return n;
	}


}

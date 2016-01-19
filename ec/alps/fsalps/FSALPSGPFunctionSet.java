/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.alps.fsalps;
import java.io.*;

import ec.*;
import ec.alps.Engine;
import ec.EvolutionState;
import ec.gp.*;
import ec.util.*;

import java.util.*;

/* 
 * GPFunctionSet.java
 * 
 * Created: Wed Oct 13 22:35:06 1999
 * By: Sean Luke
 */

/**
 * GPFunctionSet is a Clique which represents a set of GPNode prototypes
 * forming a standard function set for forming certain trees in individuals.
 * GPFunctionSets instances have unique names with which they're referenced by
 * GPTreeConstraints objects indicating that they're used for certain trees.
 * GPFunctionSets store their GPNode Prototypes in three hashtables,
 * one for all nodes, one for nonterminals, and one for terminals.  Each
 * hashed item is an array of GPNode objects,
 * hashed by the return type of the GPNodes in the array.
 *
 * GPFunctionSets also contain prototypical GPNode nodes which they
 * clone to form their arrays.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>name</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of function set.  Must be different from other function set instances)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of functions in the function set)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>func.</tt><i>n</i><br>
 <font size=-1>classname, inherits and != ec.gp.GPNode</font></td>
 <td valign=top>(class of function node <i>n</i> in the set)</td></tr>

 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>func.</tt><i>n</i></td>
 <td>function node <i>n</i></td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class FSALPSGPFunctionSet extends GPFunctionSet 
{
	private static final long serialVersionUID = 1;

	/**
	 * @author anthony
	 * individual node probability
	 */
	public final static String P_PROB    = "pr";




	/** Must be done <i>after</i> GPType and GPNodeConstraints have been set up */

	public void setup(final EvolutionState state, final Parameter base)
	{
		// What's my name?
		name = state.parameters.getString(base.push(P_NAME),null);
		if (name==null)
			state.output.fatal("No name was given for this function set.",
					base.push(P_NAME));
		// Register me
		FSALPSGPFunctionSet old_functionset = (FSALPSGPFunctionSet)(((GPInitializer)state.initializer).functionSetRepository.put(name,this));
		if (old_functionset != null)
			state.output.fatal("The GPFunctionSet \"" + name + "\" has been defined multiple times.", base.push(P_NAME));

		// How many functions do I have?
		int numFuncs = state.parameters.getInt(base.push(P_SIZE),null,1); 
		if (numFuncs < 1)
			state.output.error("The GPFunctionSet \"" + name + "\" has no functions.",
					base.push(P_SIZE));

		nodesByName = new Hashtable();

		Parameter p = base.push(P_FUNC);
		Vector tmp = new Vector(); 
		for(int x = 0; x < numFuncs; x++)
		{
			// load
			Parameter pp = p.push(""+x);
			GPNode gpfi = (GPNode)(state.parameters.getInstanceForParameter(
					pp, null, GPNode.class));
			gpfi.setup(state,pp);


			/*
			 * FSALPS
			 * Note that: (....,null,1) assigns 0 to terminal sets
			 * @author anthony
			 */
			gpfi.nodeFrequency  =  state.parameters.getInt(pp.push(P_PROB),null,1);
			
			/** build a map of node to frequency count: initial assignment is based on default settings */
			if(gpfi.children.length ==0 /*gpfi.expectedChildren()==0*/)
				/*
				 * Engine.nodeCountTerminalSet.put(gpfi.toString(), state.parameters.getInt(pp.push(P_PROB),null,1));
				 * Not using gpfi.toString() in the event that multiple nodes have the same gpfi.toString() name
				 * The class name however, is guaranteed to always be the same.
				 */
				if(Engine.fsalps_use_only_default_node_pr || Engine.completeGenerationalCount==0 )
				{
					/* This is done during setup,
					 * Engine.nodeCountTerminalSet holds terminals with the default frequency settings
					 * NB: This is never modified if Engine.fsalps_use_only_default_node_pr is true */
					Engine.nodeCountTerminalSet.put(gpfi.getClass().getName(), (double) state.parameters.getInt(pp.push(P_PROB),null,1));
					/* This is used to setup available nodes and their default frequencies
					 * Values are modified in NodeStatitics when every EvolutionState counts terminal usage
					 * in its trees*/
					state.nodeCountTerminalSet.put(gpfi.getClass().getName(), (double) state.parameters.getInt(pp.push(P_PROB),null,1));
				}
				/*
				else if (Engine.completeGenerationalCount==0) //perform assigment only at generation 0
				{   
					state.nodeCountTerminalSet.put(gpfi.getClass().getName(), (double) state.parameters.getInt(pp.push(P_PROB),null,1));
					Engine.nodeCountTerminalSet.put(gpfi.getClass().getName(), (double) state.parameters.getInt(pp.push(P_PROB),null,1));
				}*/
		    
			
			// add to my collection
			tmp.addElement(gpfi);

			// Load into the nodesByName hashtable
			GPNode[] nodes = (GPNode[])(nodesByName.get(gpfi.name()));
			if (nodes == null)
				nodesByName.put(gpfi.name(), new GPNode[] { gpfi });
			else
			{
				// O(n^2) but uncommon so what the heck.
				GPNode[] nodes2 = new GPNode[nodes.length + 1];
				System.arraycopy(nodes, 0, nodes2, 0, nodes.length);
				nodes2[nodes2.length - 1] = gpfi;
				nodesByName.put(gpfi.name(), nodes2);
			}
		}

		// Make my hash tables
		nodes_h = new Hashtable();
		terminals_h = new Hashtable();
		nonterminals_h = new Hashtable();

		// Now set 'em up according to the types in GPType

		Enumeration e = ((GPInitializer)state.initializer).typeRepository.elements();
		GPInitializer initializer = ((GPInitializer)state.initializer);
		while(e.hasMoreElements())
		{
			GPType typ = (GPType)(e.nextElement());

			// make vectors for the type.
			Vector nodes_v = new Vector();
			Vector terminals_v = new Vector();
			Vector nonterminals_v = new Vector();

			// add GPNodes as appropriate to each vector
			Enumeration v = tmp.elements();
			while (v.hasMoreElements())
			{
				GPNode i = (GPNode)(v.nextElement());
				if (typ.compatibleWith(initializer,i.constraints(initializer).returntype))
				{
					nodes_v.addElement(i);
					if (i.children.length == 0)
						terminals_v.addElement(i);
					else nonterminals_v.addElement(i);
				}
			}

			// turn nodes_h' vectors into arrays
			GPNode[] ii = new GPNode[nodes_v.size()];
			nodes_v.copyInto(ii);
			nodes_h.put(typ,ii);

			// turn terminals_h' vectors into arrays
			ii = new GPNode[terminals_v.size()];
			terminals_v.copyInto(ii);
			terminals_h.put(typ,ii);

			// turn nonterminals_h' vectors into arrays
			ii = new GPNode[nonterminals_v.size()];
			nonterminals_v.copyInto(ii);
			nonterminals_h.put(typ,ii);
		}

		// I don't check to see if the generation mechanism will be valid here
		// -- I check that in GPTreeConstraints, where I can do the weaker check
		// of going top-down through functions rather than making sure that every
		// single function has a compatible argument function (an unneccessary check)

		state.output.exitIfErrors();  // because I promised when I called n.setup(...)

		// postprocess the function set
		postProcessFunctionSet();
	}


	/** Returns the function set for a given name.
        You must guarantee that after calling functionSetFor(...) one or
        several times, you call state.output.exitIfErrors() once. 

	public static GPFunctionSetFSALPS functionSetFor(final String functionSetName,
			final EvolutionState state)
	{
		GPFunctionSetFSALPS set = (GPFunctionSetFSALPS)(((GPInitializer)state.initializer).functionSetRepository.get(functionSetName));
		if (set==null)
			state.output.error("The GP function set \"" + functionSetName + "\" could not be found.");
		return set;
	}
  

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		// this wastes an hashtable pointer, but what the heck.
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
	  */
	
	
}

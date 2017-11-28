
Welcome to Age Layered Population Structure (ALPS) and Feature Selection ALPS (FSALPS) ECJ!
================

The system provides anALPS extension to the open source ECJ System

Copyright 2015 by Anthony Awuley 
Brock University Computer Science Department
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information

The ALPS[Hornby,2006] strategy is a diversity–enhancing algorithm that works with algorithms 
with elements of randomness in them . It uses an age–layered population and restricts breeding 
and competition between individuals. ALPS ability to maintain diversity in its population is largely 
due to regular introduction of individuals from different fitness basins and the novel control of 
competition between individuals. 

The implementation is based on the version 22 of the open source Evolutionary Computation system in Java
developed by Sean Luke [ECJ,V22]

[Hornby,2006] Gregory Hornby. Alps: the age-layered population structure for reducing the problem of 
premature convergence. In Mike Cattolico, editor, GECCO, pages 815–822. ACM, 2006.

[ECJ,V22] S. Luke, G. Balan S. Paus Z. Skolicki E. Popovici J. Harrison J. Bassett R. Hubley, L. Panait and 
A. Chhircop. Ecj: A java-based evolutionary computation research system, version 22, 06 2000-2015. 
http://www.cs.gmu.edu/~eclab/projects/ecj/ [Online; Accessed: 2 April 2014].
____

## ALPS CONFIGURATION
The parameter files are located in the directory  io/params/ and the main class is in ec.main.Run.
Three tutorials have been set up and can be found in the directories

1. ec/app/alps/tutorial1/ 
2. ec/app/alps/tutorial2/
3. ec/app/alps/tutorial3/
4. ec/app/fsalps/tutorial4/

A brief introduction is given to setting up **1** and **2**.

**Setting up ALPS GP (see ec/app/alps/tutorial2/params/tutorial2.params).** 

[ALPS CONFIGURATION](https://github.com/aawuley/alps-ec/wiki/ALPS-Configuration)


### SAMPLE ALPS OUTPUT
[SAMPLE ALPS OUTPUT](https://github.com/aawuley/alps-ec/wiki/ALPS-Output)

____

### FSALPS

Feature Selection ALPS is a modification of Hornby's ALPS algorithm directed towards the selection of relevant terminals (features) in a GP tree. It uses a frequency counting system to rank the terminals. The ranked values are converted into probabilities and are used in the selection of terminals during construction of trees/sub-trees.

```
  // Pseudocode for FSALPS

  procedure FSALPS GEN()
  AgeScheme ← SelectAgeingScheme() 
  layers ← CreateLayers(AgeScheme)
  i ← SequentialLayerSelection(layers) 
  probVector ← InitialFeatureProbabilities() 
  while not TerminationCondition() do
      if BottomLayer(i) & TooOld(i) then 
         probVector ← ComputeFeatureProbs()
         j ← CreateRandomGenome(probVector)
      else
         if mutation then
            j ← DoMutation(probVector) 
         else
            if crossover then
               j ← DoCrossover()
            end if 
         end if
      end if
      offspringIndex ← SelectSlotNextGeneration(i) 
      j ← CreateChild(offspringIndex) 
      EvaluateChild(j)
      TryMoveUp(i,j )
  end while 
  end procedure

```

### FSALPS Configuration
[FSALPS CONFIGURATION](https://github.com/aawuley/alps-ec/wiki/FSALPS-Configuration)


### SAMPLE FSALPS OUTPUT
[SAMPLE FSALPS OUTPUT](https://github.com/aawuley/alps-ec/wiki/FSALPS-Output)

================

### Authors and Contributors
ALPS & FSALPS : Anthony Awuley @aawuley

ECJ : Sean Luke

### Support or Contact
Having trouble with setting up ALPS & FSALPS? Check out our [documentation](https://github.com/aawuley/alps-ec) or [contact support](http://anthonyawuley.com/contact)

Project page [Prof. Brian Ross](http://www.cosc.brocku.ca/~bross/FSALPS/)

For all ECJ related supports contact [Sean Luke](http://cs.gmu.edu/~eclab/projects/ecj/)
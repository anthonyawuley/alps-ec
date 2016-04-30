
Welcome ALPS and FSALPS ECJ!
================

The system provides an  ALPS extension to the open so ECJ System

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

## Start here for GA
The parameter files are located in the directory  io/params/ and the main class is in ec.main.Run.
Three tutorials have been set up and can be found in the directories

1. ec/app/alps/tutorial2/ 
2. ec/app/alps/tutorial1/
3. ec/app/alps/tutorial3/

A brief introduction is given to setting up **1** and **2**.

**Setting up ALPS GP (see ec/app/alps/tutorial2/params/tutorial2.params).** 

only alps related changes are discussed. For full descrption of all related parameters, see tutorial4 of the [ECJ system](https://cs.gmu.edu/~eclab/projects/ecj/)

* parent.0                = ../../../../gp/koza/koza.params
* #ALPS configuration 
* parent.1                = ../../../../alps/alps.params
* quit-on-run-complete    = false
* #jobs specifies the number of runs you would wish to perform
* state                   = ec.alps.GenEvolutionState
* exch                    = ec.simple.SimpleExchanger
* breed                   = ec.alps.ALPSBreeder
* eval                    = ec.alps.ALPSEvaluator
* stat                    = ec.alps.statistics.ALPSStatistics

**FUNCTION SETS**
* #We have one function set, of class GPFunctionSet
* gp.fs.size                 = 1
* gp.fs.0                    = ec.gp.GPFunctionSet
* #We'll call the function set "f0".
* gp.fs.0.name               = f0
* #We have five functions in the function set.  They are:
* gp.fs.0.size               = 5
* -------


## Content of directory              = ../../../../alps/alps.params

**jobs specifies the number of runs you would wish to perform**
* state                                 = ec.alps.GenEvolutionState
* breed                                 = ec.alps.ALPSBreeder
* eval                                  = ec.alps.ALPSEvaluator
* stat                                  = ec.alps.statistics.ALPSStatistics
* #Begin ALPS configuration ==============================================================
* alps.age-gap                           = 5
* alps.number-of-layers                  = 5
* alps.number-of-evaluations             = 2000
* alps.selection-pressure                = 0.8
* alps.tournament-size                   = 4
* alps.aging-scheme                      = ec.alps.layers.agingscheme.Polynomial
* alps.layer-replacement                 = ec.alps.layers.replacement.ReverseTournamentWorst
* alps.assign-max-parent-age             = true

when using selection pressure, individual aging isnt uniform especially when parents are selected from lower layer. When some individuals are aged faster than others, a population will contain less than expected required number ECJ by default breeds a maximum of the number of populations contained in a population.
* alps.always-breed-maximum-population   = true

when true, only individuals selected from breeding from current layer have their age increased
else both both individuals coming from current and lower layer used as parents will have their age increased
* alps.age-only-current-layer            = true

when moving old individuals from bottom layer to next higher layer, use this flag to determine
replacement strategy in higher layer. if false lower layer individual only replace higher layer tournament individual
if its fitness is better that of the higher layer tournament individual. if true, selected tournament individual from higher
layer is always replaced
* alps.layer-replacement.replace-weakest = true
* #END================================================================================= 
* #Begin GP parameters ================================================================
* pop.subpop.0.species.pipe                        = ec.alps.gp.ALPSMultiBreedingPipeline
* pop.subpop.0.species.pipe.num-sources            = 2
* pop.subpop.0.species.pipe.source.0               = ec.alps.gp.breed.Crossover
* -----
* pop.subpop.0.species.pipe.source.1               = ec.alps.gp.breed.Mutation
* pop.subpop.0.species.pipe.source.0.source.0      = ec.select.TournamentSelection
* #END================================================================================= 



## SAMPLE OUTPUT

ALPS GP with 6 Layers using Generational Replacement strategy
<img src="http://greyintel.org/resources/img/works/gp/generationalPolynomial.png" height="212" width="462" alt="ALPS GP with 6 Layers using Generational Replacement strategy" />

================

ALPS  Vector Representation with 6 Layers Using Steady State strategy
<img src="http://greyintel.org/resources/img/works/gp/steadyStatePolynomial.png" height="212" width="462" alt="ALPS  Vector Representation with 6 Layers Using Steady State strategy" />

================

Comparing performance plot of last layers of ALPS and Canonical GP
<img src="http://greyintel.org/resources/img/works/gp/comparegp.png" height="212" width="462" alt="Comparing performance plot of last layers of ALPS and Canonical G" />

================

Comparing ALPS and Canonical Vector Representation using 6 layers
<img src="http://greyintel.org/resources/img/works/alps/compare-replacement-strategy.png" height="212" width="462" alt="Comparing ALPS and Canonical Vector Representation using 6 layers" />

================

ALPS  Vector Representation with 6 Steady State Polynomical age layer
<img src="http://greyintel.org/resources/img/works/alps/ga/gasspolynomial.png" height="212" width="462" alt="ALPS  Vector Representation with 6 Steady State Polynomical age layer" />

================

ALPS  Vector Representation Generational with Polynomical age layer
<img src="http://greyintel.org/resources/img/works/alps/ga/gagenpolynomial.png" height="212" width="462" alt="ALPS  Vector Representation Generational with Polynomical age layer" />

================



### FSALPS

## Configuration

**Content of directory              = ../../../../fsalps/alps.params**

* init                                  = ec.alps.fsalps.FSLPSGPInitializer

use only default node probability
* alps.use-only-default-node-pr         = false

 used to determine if frequency count is used during mutation
 DEPRECATED: a simpler solutioin involves setting gp.koza.mutate.build.0 = ec.gp.koza.GrowBuilder for situations
 where mutation is to be excluded from FSALPS
* alps.fsalps-in-mutation               = true

used to determine if frequency in last layer is used to generate probality selection on a generational basis
* alps.fsalps-gen-freq-count            = false

 when enabled, nodes are counted in all layers and converted
 to probability values used during mutation and tree initialization in bottom layer
* alps.fsalps-count-all-layers          = true

 Determine which frequency selection criteria is used for generating probability
* alps.fsalps.probability-selection     = ec.alps.fsalps.probability.NormalFrequency


 Point Mutation will use Tournament Selection, try only 1
 time, have a max depth of 17, and use KozaNodeSelector
 and GROW for building.  Also, Point Mutation uses a GrowBuilder
 by default, with a default of min-depth=max-depth=5
 as shown a ways below
* gp.koza.mutate.source.0      = ec.select.TournamentSelection
* gp.koza.mutate.ns.0          = ec.gp.koza.KozaNodeSelector
* gp.koza.mutate.build.0       = ec.alps.fsalps.gp.koza.GrowBuilder

This is to ensure that the modified KozaBuilder and GrowBuilder in alps.gp.koza are used during tree initialization
* gp.tc.0.init                 = ec.alps.fsalps.gp.koza.HalfBuilder

We set the default for HalfBuilder to be a ramp of 2--6, with a grow probability of 0.5
* gp.koza.half.min-depth       = 2
* gp.koza.half.max-depth       = 6
* gp.koza.half.growp           = 0.5


FSALPS  - Feature evolution in Layers

<img src="http://greyintel.org/resources/img/works/fsalps/freq/0.png" height="240" width="462" alt="FSALPS Layer 0" />
Layer 0

<img src="http://greyintel.org/resources/img/works/fsalps/freq/2.png" height="240" width="462" alt="FSALPS Layer2" />
Layer 2

<img src="http://greyintel.org/resources/img/works/fsalps/freq/4.png" height="240" width="462" alt="FSALPS Layer 4" />
Layer 4

<img src="http://greyintel.org/resources/img/works/fsalps/freq/5.png" height="240" width="462" alt="FSALPS Layer 5" />
Layer 5

<img src="http://greyintel.org/resources/img/works/fsalps/freq/6.png" height="240" width="462" alt="FSALPS Layer 6" />
Layer 6

<img src="http://greyintel.org/resources/img/works/fsalps/freq/8.png" height="240" width="462" alt="FSALPS Layer 8" />
Layer 8

<img src="http://greyintel.org/resources/img/works/fsalps/freq/9.png" height="240" width="462" alt="FSALPS Layer 9" />
Layer 9


================




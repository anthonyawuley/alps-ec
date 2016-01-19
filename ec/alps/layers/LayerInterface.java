package ec.alps.layers;


 public interface LayerInterface {

    /**
     * 	
     * @return
     */
	public String toString();
	/**
	 * 
	 * @return
	 */
	public int getId();

	
    /**
     * s
     * @param count the number of generations for which a layer must evolve
     * this is given as maxCountOfCurrentLayer - MaximumCountOfPreviousLayer
     */
	public void setId(int id);
	/**
	 * 
	 * @param e
	 */
	//public void setEvolutionState(EvolutionState s);
	/**
	 * 
	 * @return
	 */
	//public EvolutionState getEvolutionState();
	/**
	 * 
	 * @param i
	 */
    //public void setParameterDatabase(ParameterDatabase i);
	/**
	 * 
	 * @return
	 */
	//public ParameterDatabase getParameterDatabase();
	
	/**
	 * 
	 * @return
	 */
	public boolean getIsActive();
    /**
     * 
     * @param active
     */
	public void setIsActive(boolean active);
    /**
     * 
     * @return
     */
	public boolean getIsBottomLayer();
    /**
     * 
     * @param active
     */
	public void setIsBottomLayer(boolean active);
    /**
     * 
     * @param pop
     */
	//public void tryMoveUp(int layer, Population pop);
	
	
	/**
	 * 
	 * @return
	 */
	public int getMaxAge();
    /**
     * 
     * @param age
     */
	public void setMaxAgeLayer(int age);
	/**
	 * 
	 * @return
	
	public int getGenerationalCount();
   
	public void setGenerationalCount(int count);
	*/
	
	/**
	 * 
	 * @return
	 */
	public int getGenerations();
    /**
     * s
     * @param count the number of generations for which a layer must evolve
     * this is given as maxCountOfCurrentLayer - MaximumCountOfPreviousLayer
     */
	public void setGenerations(int count);
	
	/**
	 * 
	 * @return
	 */
	public int getEvaluations();
	
	/**
	 * 
	 * @param evaluation
	 */
	public void setEvaluations(int evaluation); 
	
	

}

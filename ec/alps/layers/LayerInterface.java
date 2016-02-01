package ec.alps.layers;

/**
 * ALPS consists of a number of layers evolving in parallel. 
 * The LayerInterface provides a template for an ALPS layer.
 * 
 * @author Anthony Awuley
 *
 */
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
	 * Used to get the status of a layer. True is returned if the current layer is active
	 * a sequential selection is used to pick layers, which means a layer is active at a time
	 * An alternative selection is random selection of layers, which also means a layer will be active
	 * at a time.
	 * 
	 * TODO Selection of layers does not include multi threading at this time. 
	 * @return
	 */
	public boolean getIsActive();
    /**
     * Set acive status of a layer
     * @param active
     */
	public void setIsActive(boolean active);
    /**
     * 
     * @return true if evolving layer is bottom layer (Layer 0) 
     */
	public boolean getIsBottomLayer();
    /**
     * set bottom layer
     * @param active
     */
	public void setIsBottomLayer(boolean active);
	/**
	 * 
	 * @return maximum age of this layer
	 */
	public int getMaxAge();
    /**
     * SEt maximum age of this layer
     * @param age
     */
	public void setMaxAgeLayer(int age);
	
	/**
	 * 
	 * @return number of generations for which this layer is allowed to run
	 */
	public int getGenerations();
    /**
     * 
     * @param count the number of generations for which a layer must evolve
     * this is given as maxCountOfCurrentLayer - MaximumCountOfPreviousLayer
     */
	public void setGenerations(int count);
	
	/**
	 * 
	 * @return number of evaluations for this layer
	 */
	public int getEvaluations();
	
	/**
	 * set number of evaluations for this layer
	 * @param evaluation
	 */
	public void setEvaluations(int evaluation); 
	
	

}

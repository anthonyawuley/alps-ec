/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.app.texture;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.simple.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Ashkan Entezari
 */

public class Analyzer extends GPProblem implements SimpleProblemForm
{
    private static final long serialVersionUID = 1;
    
    // defined parameters for terminals
    public double avg3x3;
    public double avg7x7;
    public double avg9x9;
    public double std3x3;
    public double std7x7;
    public double std9x9;
    public double ActualValue;
    // two images for training and testing
    public double[][] TrainingImage = new double[602][200];
    public double[][] TestingImage  = new double[459][169];
    // one image for selected input
    public int[][] TrainingSelectedInput = new int[602][200];
    // two images for ground truth
    public int[][] TrainingGroundTruth = new int[602][200];
    public int[][] TestingGroundTruth  = new int[459][169];
    // defined parameters to keep image info
    public double[][] small_avg_tr   = new double[602][200];
    public double[][] small_avg_tst  = new double[459][169];
    public double[][] small_std_tr   = new double[602][200];
    public double[][] small_std_tst  = new double[459][169];
    public double[][] medium_avg_tr  = new double[602][200];
    public double[][] medium_avg_tst = new double[459][169];
    public double[][] medium_std_tr  = new double[602][200];
    public double[][] medium_std_tst = new double[459][169];
    public double[][] large_avg_tr   = new double[602][200];
    public double[][] large_avg_tst  = new double[459][169];
    public double[][] large_std_tr   = new double[602][200];
    public double[][] large_std_tst  = new double[459][169];
    
    
    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state, base);
        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof DoubleData))
            state.output.fatal("GPData class must subclass from " + DoubleData.class,
                base.push(P_DATA), null);
        
        // reading images:
        BufferedImage tst_img = null;
        BufferedImage tr_img = null;
        BufferedImage tst_gt_img = null;
        BufferedImage tr_gt_img = null;
        BufferedImage s_input = null;
        
        try{
        File imagefile = new File("C://Users/ae13cu/Documents/NetBeansProjects/GPASS2/Images/Airports/PNG versions/IKA.png");
        tr_img = ImageIO.read(imagefile);
        imagefile = new File("C://Users/ae13cu/Documents/NetBeansProjects/GPASS2/Images/Airports/PNG versions/IKA-gt.png");
        tr_gt_img = ImageIO.read(imagefile);
        imagefile = new File("C://Users/ae13cu/Documents/NetBeansProjects/GPASS2/Images/Airports/PNG versions/Mehrabad.png");
        tst_img = ImageIO.read(imagefile);
        imagefile = new File("C://Users/ae13cu/Documents/NetBeansProjects/GPASS2/Images/Airports/PNG versions/Mehrabad-gt.png");
        tst_gt_img = ImageIO.read(imagefile);
        imagefile = new File("C://Users/ae13cu/Documents/NetBeansProjects/GPASS2/Images/Airports/PNG versions/IKA-selected.png");
        s_input = ImageIO.read(imagefile);
        
        // Put the image values in my 2D arrays
        int red, green, blue, RGB;
        // Computing the TrainingImage 
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                RGB   = tr_img.getRGB(i,j);
                red   = (RGB >> 16) & 0xFF ;
                green = (RGB >> 8 ) & 0xFF ;
                blue  = (RGB      ) & 0xFF ;
                TrainingImage[i][j]  = (red+green+blue) / 3;
            }
        // Computing the TestingImage
        for(int i=0;i<459;i++)
            for(int j=0;j<169;j++)
            {
                RGB   = tst_img.getRGB(i,j);
                red   = (RGB >> 16) & 0xFF;
                green = (RGB >> 8 ) & 0xFF;
                blue  = (RGB      ) & 0xFF;
                TestingImage[i][j] = (red+green+blue) / 3;
            }
        // setting up the ground truth images
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                RGB   = tr_gt_img.getRGB(i,j);
                red   = (RGB >> 16) & 0xFF;
                green = (RGB >> 8 ) & 0xFF;
                blue  = (RGB      ) & 0xFF;
                if (red>=245 && green>=245 && blue<=10)
                    TrainingGroundTruth[i][j] = 1;
                else
                    TrainingGroundTruth[i][j] = 0;
            }
        
        for(int i=0;i<459;i++)
            for(int j=0;j<169;j++)
            {
                RGB   = tst_gt_img.getRGB(i,j);
                red   = (RGB >> 16) & 0xFF;
                green = (RGB >> 8 ) & 0xFF;
                blue  = (RGB      ) & 0xFF;
                if (red>=245 && green>=245 && blue<=10)
                    TestingGroundTruth[i][j] = 1;
                else
                    TestingGroundTruth[i][j] = 0;
            }
        // coumputing the selected input
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                RGB   = s_input.getRGB(i,j);
                red   = (RGB >> 16) & 0xFF;
                green = (RGB >> 8 ) & 0xFF;
                blue  = (RGB      ) & 0xFF;
                if (red==0 && green==255 && blue==0)
                    TrainingSelectedInput[i][j] = 1;       // planes
                else if (red==0 && green==0 && blue==255)
                    TrainingSelectedInput[i][j] = 2;       // not planes
                else
                    TrainingSelectedInput[i][j] = 0;
            }
        
        //////////////////////////////////////////////
        ///COMPUTING AVERAGE AND STANDARD DEVIATION///
        //////////////////////////////////////////////
        
        int sum;
        
        /* AVG3x3 for training */
        
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                sum = 0;
                for(int k=-1;k<2;k++)
                    for(int z=-1;z<2;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 199) )   &&   ( (i+k >= 0) && (i+k <= 601) ) )
                            sum += TrainingImage[i+k][j+z];
                    }
                small_avg_tr[i][j] = sum/9;
            }
        
        /* AVG3x3 for testing */
        
        for(int i=0;i<459;i++)
            for(int j=0;j<169;j++)
            {
                sum = 0;
                for(int k=-1;k<2;k++)
                    for(int z=-1;z<2;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 168) )   &&   ( (i+k >= 0) && (i+k <= 458) ) )
                            sum += TestingImage[i+k][j+z];
                    }
                small_avg_tst[i][j] = sum/9;
            }
        
        /* AVG7x7 for training */
        
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                sum = 0;
                for(int k=-3;k<4;k++)
                    for(int z=-3;z<4;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 199) )   &&   ( (i+k >= 0) && (i+k <= 601) ) )
                            sum += TrainingImage[i+k][j+z];
                    }
                medium_avg_tr[i][j] = sum/49;
            }
        
        /* AVG7x7 for testing */
        
        for(int i=0;i<459;i++)
            for(int j=0;j<169;j++)
            {
                sum = 0;
                for(int k=-3;k<4;k++)
                    for(int z=-3;z<4;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 168) )   &&   ( (i+k >= 0) && (i+k <= 458) ) )
                            sum += TestingImage[i+k][j+z];
                    }
                medium_avg_tst[i][j] = sum/49;
            }
        
        /* AVG9x9 for training */
        
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                sum = 0;
                for(int k=-4;k<5;k++)
                    for(int z=-4;z<5;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 199) )   &&   ( (i+k >= 0) && (i+k <= 601) ) )
                            sum += TrainingImage[i+k][j+z];
                    }
                large_avg_tr[i][j] = sum/81;
            }
        
        /* AVG9x9 for testing */
        
        for(int i=0;i<459;i++)
            for(int j=0;j<169;j++)
            {
                sum = 0;
                for(int k=-4;k<5;k++)
                    for(int z=-4;z<5;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 168) )   &&   ( (i+k >= 0) && (i+k <= 458) ) )
                            sum += TestingImage[i+k][j+z];
                    }
                large_avg_tst[i][j] = sum/81;
            }
        
        /* SD3x3 for training */
        
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                sum = 0;
                for(int k=-1;k<2;k++)
                    for(int z=-1;z<2;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 199) )   &&   ( (i+k >= 0) && (i+k <= 601) ) )
                            sum += ( (TrainingImage[i+k][j+z]-small_avg_tr[i][j]) * (TrainingImage[i+k][j+z]-small_avg_tr[i][j]) );
                        else
                            sum += (small_avg_tr[i][j])*(small_avg_tr[i][j]);
                    }
                small_std_tr[i][j] = Math.sqrt(sum/9);
            }
        
        /* SD3x3 for testing */
        
        for(int i=0;i<459;i++)
            for(int j=0;j<169;j++)
            {
                sum = 0;
                for(int k=-1;k<2;k++)
                    for(int z=-1;z<2;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 168) )   &&   ( (i+k >= 0) && (i+k <= 458) ) )
                            sum += ( (TestingImage[i+k][j+z]-small_avg_tst[i][j]) * (TestingImage[i+k][j+z]-small_avg_tst[i][j]) );
                        else
                            sum += (small_avg_tst[i][j])*(small_avg_tst[i][j]);
                    }
                small_std_tst[i][j] = Math.sqrt(sum/9);
            }
        
        /* SD7x7 for training */
        
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                sum = 0;
                for(int k=-3;k<4;k++)
                    for(int z=-3;z<4;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 199) )   &&   ( (i+k >= 0) && (i+k <= 601) ) )
                            sum += ( (TrainingImage[i+k][j+z]-small_avg_tr[i][j]) * (TrainingImage[i+k][j+z]-small_avg_tr[i][j]) );
                        else
                            sum += (small_avg_tr[i][j])*(small_avg_tr[i][j]);
                    }
                small_std_tr[i][j] = Math.sqrt(sum/49);
            }
        
        /* SD7x7 for testing */
        
        for(int i=0;i<459;i++)
            for(int j=0;j<169;j++)
            {
                sum = 0;
                for(int k=-3;k<4;k++)
                    for(int z=-3;z<4;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 168) )   &&   ( (i+k >= 0) && (i+k <= 458) ) )
                            sum += ( (TestingImage[i+k][j+z]-small_avg_tst[i][j]) * (TestingImage[i+k][j+z]-small_avg_tst[i][j]) );
                        else
                            sum += (small_avg_tst[i][j])*(small_avg_tst[i][j]);
                    }
                small_std_tst[i][j] = Math.sqrt(sum/49);
            }
        
        /* SD9x9 for training */
        
        for(int i=0;i<602;i++)
            for(int j=0;j<200;j++)
            {
                sum = 0;
                for(int k=-4;k<5;k++)
                    for(int z=-4;z<5;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 199) )   &&   ( (i+k >= 0) && (i+k <= 601) ) )
                            sum += ( (TrainingImage[i+k][j+z]-small_avg_tr[i][j]) * (TrainingImage[i+k][j+z]-small_avg_tr[i][j]) );
                        else
                            sum += (small_avg_tr[i][j])*(small_avg_tr[i][j]);
                    }
                small_std_tr[i][j] = Math.sqrt(sum/81);
            }
        
        /* SD9x9 for testing */
        
        for(int i=0;i<459;i++)
            for(int j=0;j<169;j++)
            {
                sum = 0;
                for(int k=-4;k<5;k++)
                    for(int z=-4;z<5;z++)
                    {
                        if( ( (j+z >= 0) && (j+z <= 168) )   &&   ( (i+k >= 0) && (i+k <= 458) ) )
                            sum += ( (TestingImage[i+k][j+z]-small_avg_tst[i][j]) * (TestingImage[i+k][j+z]-small_avg_tst[i][j]) );
                        else
                            sum += (small_avg_tst[i][j])*(small_avg_tst[i][j]);
                    }
                small_std_tst[i][j] = Math.sqrt(sum/81);
            }
        
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**** TRAINING ****/
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum)
    {
        if (!ind.evaluated)  // don't bother reevaluating
        {
            DoubleData input = (DoubleData)(this.input);

            int hits = 0;
            
            for (int i=0;i<602;i++)
                for (int j=0;j<200;j++)
                {
                    if(TrainingSelectedInput[i][j] != 0)
                    {
                        ActualValue = TrainingImage[i][j];
                        avg3x3 = small_avg_tr[i][j];
                        avg7x7 = medium_avg_tr[i][j];
                        avg9x9 = large_avg_tr[i][j];
                        std3x3 = small_std_tr[i][j];
                        std7x7 = medium_std_tr[i][j];
                        std9x9 = large_std_tr[i][j];

                        ((GPIndividual)ind).trees[0].child.eval(
                            state,threadnum,input,stack,((GPIndividual)ind),this);

                        if(input.x >= 0 && TrainingGroundTruth[i][j]==1) hits++;
                        else if(input.x<0 && TrainingGroundTruth[i][j]==0) hits++;
                    }
                }
            
            // the fitness better be KozaFitness!
            //KozaFitness f = ((KozaFitness)ind.fitness);
            //f.setStandardizedFitness(state,480-hits);
            //f.hits = hits;
            double fitnessValue = (double)hits/480;
            SimpleFitness f = (SimpleFitness)ind.fitness;
            f.setFitness(state, (float)fitnessValue, fitnessValue>=0.99);
            ind.evaluated = true;
         }
    }
    
    /**** TESTING ****/
    public void describe(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum, int log)
    {
        try {
            DoubleData input = (DoubleData)(this.input);
            int hits = 0;
            int tp = 0, tn = 0, fp = 0, fn = 0;
            BufferedImage PerformanceImage = new BufferedImage(459, 169, BufferedImage.TYPE_INT_RGB);
            
            for (int i=0;i<459;i++)
                for (int j=0;j<169;j++)
                {
                    ActualValue = TestingImage[i][j];
                    avg3x3 = small_avg_tst[i][j];
                    avg7x7 = medium_avg_tst[i][j];
                    avg9x9 = large_avg_tst[i][j];
                    std3x3 = small_std_tst[i][j];
                    std7x7 = medium_std_tst[i][j];
                    std9x9 = large_std_tst[i][j];

                    ((GPIndividual)ind).trees[0].child.eval(
                        state,threadnum,input,stack,((GPIndividual)ind),this);

                    if(input.x >= 0 && TestingGroundTruth[i][j] == 1)
                    {
                        hits++;
                        tp++;
                        PerformanceImage.setRGB(i, j, 65280); //65280 is the integer value of GREEN
                    }
                    else if(input.x <0 && TestingGroundTruth[i][j] == 0)
                    {
                        hits++;
                        tn++;
                        PerformanceImage.setRGB(i, j, 0); //0 is the integer value of BLACK
                    }
                    else if(input.x >= 0 && TestingGroundTruth[i][j] == 0)
                    {
                        fp++;
                        PerformanceImage.setRGB(i, j, 16711680); //16711680 is the integer value of RED
                    }
                    else if(input.x < 0 && TestingGroundTruth[i][j] == 1) 
                    {
                        fn++;
                        PerformanceImage.setRGB(i, j, 16776960); //16776960 is the integer value of YELLOW
                    }

                }

            File performance = new File("C://Users/ae13cu/Documents/NetBeansProjects/GPASS2/ASS3/Images/"+state.generation+state.randomSeedOffset+"PerformanceImage.png");
            ImageIO.write(PerformanceImage, "png", performance);
            
            // the fitness better be KozaFitness!
            //KozaFitness f = ((KozaFitness)ind.fitness);
            //f.setStandardizedFitness(state,77571-hits);
            //f.hits = hits;
            double fitnessValue = ( (double)tp/3182.0 + (double)tn/74389.0 ) * 50;
            SimpleFitness f = (SimpleFitness)ind.fitness;
            f.setFitness(state, (float)fitnessValue, fitnessValue>=99);
            
            DecimalFormat df = new DecimalFormat("####0.00");
            System.out.println("________________________________________________");
            System.out.println("True Positive: "+df.format(((double)tp/77571)*100)+"% - True Negative: "+df.format(((double)tn/77571)*100)+"%\nFalse Positive: "+df.format(((double)fp/77571)*100)+"% - False Negative: "+df.format(((double)fn/77571)*100)+"%");
            System.out.println(df.format(((double)tp/3182)*100)+"% of airplane pixels have been detected.");
            System.out.println("Fitness Value: "+fitnessValue);
            
        } catch (IOException ex) {
            Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}


/**
 *  Based on main test class of neuroConstruct
 *
 */

package org.neuroml.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.runner.*;
import org.junit.runner.notification.*;
import org.lemsml.util.E;

/**
 *
 * Test core functionality of NeuroML2
 *
 * @author Padraig Gleeson
 * 
 */
public class MainTest 
{

    public static void main(String[] args)
            
    {
        System.out.println("Running the main LEMS/NeuroML v2alpha tests...");
        E.setDebug(false);


        Result r = null;

        
        r = org.junit.runner.JUnitCore.runClasses(org.neuroml.test.ValidityTest.class,
                                                  org.neuroml.test.ImportNmlTest.class,
                                                  org.neuroml.test.export.SBMLWriterTest.class,
                                                  org.neuroml.test.export.NeuronTest.class,
                                                  org.neuroml.test.export.NineMLWriterTest.class,
                                                  org.neuroml.test.ExamplesTest.class);
        
        Date now = new java.util.Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss, EEE dd-MMM-yyyy");

        System.out.println("\n\nFinished all the NeuroML v2alpha tests at "+formatter.format(now)+".");
        
        checkResults(r);

    }
    
    public static void checkResults(Result r)
    {

        if (!r.wasSuccessful())
        {
            for (Failure f: r.getFailures())
            {
                System.out.println("Failure: "+f.getDescription());
                System.out.println("Exception: "+f.getMessage());
                System.out.println("Trace: "+f.getTrace());
            }
        }

        System.out.println("");
        System.out.println("**********************************************");
        System.out.println("      Number of tests:      "+r.getRunCount());
        System.out.println("      Number of ignores:    "+r.getIgnoreCount());
        System.out.println("      Number of failures:   "+r.getFailures().size());
        System.out.println("      Number of successes:  "+(r.getRunCount() - r.getFailures().size() - r.getIgnoreCount()));
        System.out.println("**********************************************");
        System.out.println("");

        if (r.wasSuccessful())
        {
            System.out.println("");
            System.out.println("*******************************");
            System.out.println("*****      Success!!!     *****");
            System.out.println("*******************************");
        }
        else
        {
            System.out.println("");
            System.out.println("-------------------------------");
            System.out.println("-----      Failure!!!     -----");
            System.out.println("-------------------------------");
        }

    }
}
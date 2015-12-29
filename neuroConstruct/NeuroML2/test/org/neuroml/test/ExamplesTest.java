/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neuroml.test;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import org.lemsml.expression.ParseError;
import org.lemsml.run.ConnectionError;
import org.lemsml.sim.Sim;
import org.lemsml.util.ContentError;
import org.lemsml.util.E;
import org.lemsml.util.RuntimeError;


/**
 *
 * @author Padraig
 */
public class ExamplesTest {


    @Before
    public void setUp() {
    }


   
    @Test
    public void testNML0() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex0_IaF.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    @Test
    public void testNML1() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex1_HH.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    @Test
    public void testNML2() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex2_Izh.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    
    @Test
    public void testNML3() throws ContentError, ConnectionError, RuntimeError, ParseError   {
        String ex = "examples/LEMS_NML2_Ex3_Net.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    
    
    @Test
    public void testNML4() throws ContentError, ConnectionError, RuntimeError, ParseError   {
        String ex = "examples/LEMS_NML2_Ex4_KS.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    @Test
    public void testNML5() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex5_DetCell.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    @Test
    public void testNML6() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex6_NMDA.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
 
    @Test
    public void testNML7() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex7_STP.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    
    @Test
    public void testNML8() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex8_AdEx.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    
    @Test
    public void testNML9() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex9_FN.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }

    @Test
    public void testNML10() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex10_Q10.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }

    @Test
    public void testNML12() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex12_Net2.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }

    @Test
    public void testNML14() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex14_PyNN.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }

    @Test
    public void testNML15() throws ContentError, ConnectionError, RuntimeError, ParseError  {
        String ex = "examples/LEMS_NML2_Ex15_CaDynamics.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }


    @Test
    public void testExampleN() throws ContentError, ConnectionError, RuntimeError, ParseError   {
        String ex = "examples/LEMS_NML2_ExN_AllCells.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }

    public void executeExample(String file) throws ContentError, ConnectionError, RuntimeError, ParseError
    {
        Sim sim = new Sim(new File(file));

        sim.readModel();
        sim.build();
        try {
                sim.run(false);
        } catch (IOException ex) {
                throw new RuntimeError(ex.getMessage());
        }

        E.info("  ****  Successfully ran example in "+file+"  **** ");

    }


    public static void main(String[] args)
    {
        ExamplesTest ct = new ExamplesTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }

}
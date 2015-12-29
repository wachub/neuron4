/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neuroml.test;

import org.lemsml.sim.LemsProcess;
import org.lemsml.sim.Sim;
import org.lemsml.util.ContentError;
import org.lemsml.util.E;

import java.io.File;
import org.junit.runner.Result;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Padraig
 */
public class SimTest {


   String simpleModel = "examples/ex2dims.xml";
   String complexModel = "examples/LEMS_NML2_Ex5_DetCell.xml";
   //String complexModel2 = "../../temp/LEMS_NML2_Ex5_DetCell.xml";

    public SimTest() {
    }


    /**
     * Test of readModel method, of class Sim.
     */
    @Test
    public void testReadAndPrintModel() throws Exception {

        LemsProcess sim = new Sim(new File(simpleModel));

        E.info("testReadModel()");
        sim.readModel();

        sim.print();
        
    }


    /**
     * Test of build method, of class Sim.
     */
    @Test
    public void testBuild() throws Exception {

        LemsProcess sim = new Sim(new File(complexModel));

        E.info("testBuild()");
        sim.readModel();

        E.info("Comp classes: "+sim.getLems().componentTypes.listAsText());
        E.info("Comps: "+sim.getLems().components.listAsText());

        //sim.build();

        //sim.print();
    }

    @Test
    public void testStringRead() throws Exception {

        //String src = "<Lems>\n<Dimension name=\"time\" t=\"1\"/>\n</Lems>";
        String src = "<Lems>\n"
                /*+ "<DefaultRun component=\"sim1\"/>\n"*/
                + "<Include file=\"NeuroML2CoreTypes/Cells.xml\" />\n"
                + "</Lems>";
        LemsProcess sim = new Sim(src);

        E.info("testStringRead()");
        sim.readModel();

        E.info("Comps: "+sim.getLems().components.listAsText());

        /*sim.build();

        sim.print();*/


    }
    
    @Test
    public void testNML2StringRead() throws Exception {
       
        String src = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<neuroml xmlns=\"http://www.neuroml.org/schema/neuroml2\"\n"
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "xsi:schemaLocation=\"http://www.neuroml.org/schema/neuroml2  ../../../lems/Schemas/NeuroML2/NeuroML_v2alpha.xsd\""
            + "id=\"AbstractCells\">\n\n"
            + "<iafTauCell id=\"iaf\" leakReversal=\"-50mV\" thresh=\"-55mV\" reset=\"-70mV\" tau=\"30ms\"/>\n"
            + "</neuroml>";

        LemsProcess sim = new Sim(src);

        E.info("testNML2StringRead()");
        sim.readModel();
        
        E.info("Comps: "+sim.getLems().components.listAsText());

    }


    /**
     * Test of canonicalText method, of class Sim.
     */
    @Test
    public void testCanonicalText() throws ContentError {

        LemsProcess sim = new Sim(new File(simpleModel));

        E.info("testReadModel()");
        sim.readModel();

        String ct = sim.canonicalText();

        E.info(ct);

        assertTrue(ct.indexOf("<name>voltage</name>")>0);
    }


    public static void main(String[] args)
    {
        SimTest ct = new SimTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }

}
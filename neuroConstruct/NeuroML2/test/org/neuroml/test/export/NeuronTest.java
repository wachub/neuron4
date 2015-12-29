/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neuroml.test.export;

import java.util.ArrayList;

import org.lemsml.sim.LemsProcess;
import org.lemsml.sim.Sim;
import org.neuroml.test.MainTest;
import org.lemsml.util.*;
import org.neuroml.exporters.NeuronWriter;
import java.io.File;
import org.junit.runner.Result;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class NeuronTest {


    @Test
    public void testNML0() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex0_IaF.xml";

        executeExample(ex);
    }
    /*
    @Test
    public void testNML2() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex2_Izh.xml";

        executeExample(ex);
    }*/

    @Test
    public void testNML8() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex8_AdEx.xml";

        executeExample(ex);
    }

    @Test
    public void testNML9() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex9_FN.xml";

        executeExample(ex);
    }


    public void executeExample(String file) throws Exception
    {
        E.info("----------------------------------------------------------"
            + "\n       Testing standard example: "+file);
        LemsProcess sim = new Sim(new File(file));

        sim.readModel();

        String sc = file.replace(".xml", "_nrn.py");
        File fout = new File(sc);
        NeuronWriter nw = new NeuronWriter(sim.getLems());
        ArrayList<File> genFiles = nw.generateMainScriptAndMods(fout);

        assertTrue(genFiles.size()>=2);

        E.info("Written model behaviour to:");

        for(File f: genFiles){
                E.info(f.getAbsolutePath());
                assertTrue(f.exists());
                assertTrue(f.length()>0);
        }

    }


        public static void main(String[] args)
        {
            E.setDebug(false);
            NeuronTest ct = new NeuronTest();
            Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
            MainTest.checkResults(r);

        }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neuroml.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.Before;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.Result;
import org.lemsml.run.ConnectionError;
import org.lemsml.sim.LemsProcess;
import org.lemsml.sim.Sim;
import org.lemsml.type.Lems;
import org.lemsml.util.ContentError;
import org.lemsml.util.E;
import org.lemsml.util.FileUtil;

import org.xml.sax.SAXException;

/**
 *
 * @author Padraig
 */
public class ImportNmlTest {

        File exFilesLocation = new File("NMLVer2_Test/web/NeuroMLFiles/");


        @Before
        public void setUp() {
        }


        @Test
        public void testImportNmlExamples() throws IOException {
                if (!exFilesLocation.exists()){
                        System.out.println("Directory not found: "+exFilesLocation.getCanonicalPath());
                        return;
                }
                File[] xmlFiles = exFilesLocation.listFiles();

                for (File f : xmlFiles){
                        if (f.getName().endsWith(".xml") && f.getName().indexOf("etwork")<0 && f.getName().indexOf("FullCell")<0 && f.getName().indexOf("CellVariable")<0){
                                System.out.println("\n\n-------------\nTesting: "+ f.getCanonicalPath());
                                String contents = FileUtil.readStringFromFile(f);

                                LemsProcess sim = new Sim(contents);

                                try {
                                        sim.readModel();
                                        System.out.println("Successfully read: "+ f.getCanonicalPath());
                                } catch (ContentError ex) {
                                        fail(ex.getMessage());
                                }
                                //assertTrue(1==2);

                                //lems.
                        }
                }
        }




        public static void main(String[] args)
        {
                ImportNmlTest ct = new ImportNmlTest();
                Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
                MainTest.checkResults(r);

        }

}
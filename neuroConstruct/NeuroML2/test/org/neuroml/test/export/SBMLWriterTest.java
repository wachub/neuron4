/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neuroml.test.export;

import javax.xml.validation.Validator;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.transform.Source;
import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.lemsml.sim.LemsProcess;
import org.lemsml.sim.Sim;
import org.neuroml.test.MainTest;
import org.lemsml.util.*;
import org.neuroml.exporters.SBMLWriter;
import java.io.File;
import org.junit.runner.Result;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class SBMLWriterTest {

    static boolean tryValidate = false;

    @Test
    public void testNML9() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex9_FN.xml";
        E.info("Testing standard example: "+ex);

        testExample(ex, tryValidate);
    }

    @Test
    public void testNML0() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex0_IaF.xml";
        E.info("Testing standard example: "+ex);

        testExample(ex, tryValidate);
    }
    @Test
    public void testNML1() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex1_HH.xml";
        E.info("Testing standard example: "+ex);

        testExample(ex, tryValidate);
    }
    @Test
    public void testNML2() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex2_Izh.xml";
        E.info("Testing standard example: "+ex);

        testExample(ex, tryValidate);
    }
    @Test
    public void testNML8() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex8_AdEx.xml";
        E.info("Testing standard example: "+ex);

        testExample(ex, tryValidate);
    }

    public void testExample(String file, boolean validate) throws Exception
    {
        LemsProcess sim = new Sim(new File(file));

        sim.readModel();

        String sc = file.replace(".xml", ".sbml");
        File fout = new File(sc);
        E.info("Writing out model behaviour to: "+fout.getAbsolutePath());
        SBMLWriter nw = new SBMLWriter(sim.getLems());
        FileUtil.writeStringToFile(nw.getMainScript(), fout);

        assertTrue(fout.exists());

        E.info("  ****  Successfully put SBML into: "+file+"  **** ");

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        Document doc = docBuilder.parse (fout);

        // normalize text representation
        doc.getDocumentElement ().normalize ();
        String root = doc.getDocumentElement().getNodeName();
        E.info("Root element of the doc is " +  root);

        assertEquals(root, "sbml");

        if (validate)
        {

                File sbmlXSD = new File("Schemas/sbml-l2v2-schema/sbml.xsd");

                E.info("Testing validity of: "+ fout.getAbsolutePath()+" against: "+ sbmlXSD.getAbsolutePath());

                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


                Source schemaFileSource = new StreamSource(sbmlXSD);
                Schema schema = factory.newSchema(schemaFileSource);

                Validator validator = schema.newValidator();

                Source xmlFileSource = new StreamSource(fout);

                validator.validate(xmlFileSource);

                E.info("File: "+ fout.getAbsolutePath()+" is valid!!");
        }


    }


        public static void main(String[] args)
        {
                tryValidate = true;
                SBMLWriterTest ct = new SBMLWriterTest();
                Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
                MainTest.checkResults(r);

        }
}
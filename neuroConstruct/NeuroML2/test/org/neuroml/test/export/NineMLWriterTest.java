/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neuroml.test.export;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import javax.xml.validation.*;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import javax.xml.parsers.*;

import org.lemsml.sim.LemsProcess;
import org.lemsml.sim.Sim;
import org.neuroml.test.MainTest;
import org.lemsml.util.*;
import org.neuroml.exporters.NineMLWriter;
import java.io.File;
import org.junit.runner.Result;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class NineMLWriterTest {

    File ninemlXSD = new File("Schemas/NineML/NineML_v0.2.xsd");

    @Test
    public void testNML0() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex0_IaF.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    @Test
    public void testNML1() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex1_HH.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }
    @Test
    public void testNML2() throws Exception  {
        String ex = "examples/LEMS_NML2_Ex2_Izh.xml";
        E.info("Testing standard example: "+ex);

        executeExample(ex);
    }


    public void executeExample(String file) throws Exception
    {
        LemsProcess sim = new Sim(new File(file));

        sim.readModel();

        String sc = file.replace(".xml", ".9ml");
        File fout = new File(sc);
        E.info("Writing out model behaviour to: "+fout.getAbsolutePath());
        NineMLWriter nw = new NineMLWriter(sim.getLems());
        FileUtil.writeStringToFile(nw.getMainScript(), fout);

        assertTrue(fout.exists());

        E.info("  ****  Successfully put NineML into: "+file+"  **** ");

        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        Document doc = docBuilder.parse (fout);

        // normalize text representation
        doc.getDocumentElement ().normalize ();
        String root = doc.getDocumentElement().getNodeName();
        E.info("Root element of the doc is " +  root);

        assertEquals(root, "NineML");


        E.info("Testing validity of: "+ fout.getAbsolutePath()+" against: "+ ninemlXSD.getAbsolutePath());

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Source schemaFileSource = new StreamSource(ninemlXSD);
        Schema schema = factory.newSchema(schemaFileSource);

        Validator validator = schema.newValidator();

        Source xmlFileSource = new StreamSource(fout);

        validator.validate(xmlFileSource);

        E.info("File: "+ fout.getAbsolutePath()+" is valid!!");


    }


        public static void main(String[] args)
        {
                NineMLWriterTest ct = new NineMLWriterTest();
                Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
                MainTest.checkResults(r);

        }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neuroml.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import org.lemsml.util.ContentError;
import org.lemsml.util.E;

import org.xml.sax.SAXException;

/**
 *
 * @author Padraig
 */
public class ValidityTest {

        File lemsXSD = new File("Schemas/LEMS/LEMS_v0.5.xsd");
        File neuroml2XSD = new File("Schemas/NeuroML2/NeuroML_v2alpha.xsd");
        File channelML2Nml2= new File("ChannelMLConvert/ChannelML2NeuroML2.xsl");


        @Before
        public void setUp() {
        }


        @Test
        public void testValidityLEMS() throws ContentError, ConnectionError, SAXException, IOException  {

                E.info("\n--------------------------------------------\n"
                                    + "testValidityLEMS()");

                File nml2Xml = new File("NeuroML2CoreTypes");

                File[] allXml = nml2Xml.listFiles();

                for(File f: allXml){

                        if (f.getName().endsWith(".xml")){
                                E.info("--------------------------------------------\n"
                                    + "Testing validity of: "+ f.getAbsolutePath()+" against: "+ lemsXSD.getAbsolutePath());

                                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                                Source schemaFileSource = new StreamSource(lemsXSD);
                                Schema schema = factory.newSchema(schemaFileSource);

                                Validator validator = schema.newValidator();

                                Source xmlFileSource = new StreamSource(f);

                                try{
                                        validator.validate(xmlFileSource);
                                } catch (Exception e){
                                        throw new ContentError("File: "+ f.getAbsolutePath()+" is NOT valid!!", e);
                                }

                                E.info("File: "+ f.getAbsolutePath()+" is valid!!");
                        }
                }

        }

        @Test
        public void testValidityNeuroML2() throws ContentError, ConnectionError, SAXException, IOException  {

                E.info("\n--------------------------------------------\n"
                                    + "testValidityNeuroML2()");

                File nml2 = new File("examples");

                File[] allNml = nml2.listFiles();

                for(File f: allNml) {

                        if (f.getName().endsWith(".nml")){
                                E.info("\n--------------------------------------------\n"
                                    + "Testing validity of: "+ f.getAbsolutePath()+" against: "+ neuroml2XSD.getAbsolutePath());

                                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                                Source schemaFileSource = new StreamSource(neuroml2XSD);
                                Schema schema = factory.newSchema(schemaFileSource);

                                Validator validator = schema.newValidator();

                                Source xmlFileSource = new StreamSource(f);

                                try{
                                        validator.validate(xmlFileSource);
                                } catch (Exception e){
                                        throw new ContentError("File: "+ f.getAbsolutePath()+" is NOT valid!!", e);
                                }

                                E.info("File: "+ f.getAbsolutePath()+" is valid!!");
                        }
                }

        }

        @Test
        public void testValidityChannelMLConvert() throws ContentError, ConnectionError, SAXException, IOException, TransformerConfigurationException, TransformerException  {

                E.info("\n--------------------------------------------\n"
                                    + "testValidityChannelMLConvert()");

                File convDir = new File("ChannelMLConvert");

                File[] allCml = convDir.listFiles();

                for(File f: allCml){

                        if (f.getName().endsWith(".xml")){

                                File outFile = new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-4)+".nml");

                                E.info("Going to convert: "+ f.getAbsolutePath()+" to"+outFile.getAbsolutePath()+" with: "+ channelML2Nml2.getAbsolutePath());

                                TransformerFactory tFactory = TransformerFactory.newInstance();

                                StreamSource xslFileSource = new StreamSource(new FileReader(channelML2Nml2));

                                Transformer transformer = tFactory.newTransformer(xslFileSource);

                                FileWriter writer = new FileWriter(outFile);

                                transformer.transform(new StreamSource(f), new StreamResult(writer));

                                assertTrue(outFile.exists());

                                E.info("Testing validity of: "+ outFile.getAbsolutePath()+" against: "+ neuroml2XSD.getAbsolutePath());
                                
                                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                                Source schemaFileSource = new StreamSource(neuroml2XSD);
                                Schema schema = factory.newSchema(schemaFileSource);

                                Validator validator = schema.newValidator();

                                Source xmlFileSource = new StreamSource(outFile);

                                try{
                                        validator.validate(xmlFileSource);
                                } catch (Exception e){
                                        throw new ContentError("File: "+ f.getAbsolutePath()+" is NOT valid!!", e);
                                }

                                E.info("File: "+ outFile.getAbsolutePath()+" is valid!!");
                        }
                }
        }


        public static void main(String[] args)
        {
                ValidityTest ct = new ValidityTest();
                E.setDebug(false);
                Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
                MainTest.checkResults(r);

        }

}
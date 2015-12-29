/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 *
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.neuroml;

import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.NeuroMLVersion;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import javax.xml.*;
import java.io.File;
import ucl.physiol.neuroconstruct.project.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class NeuroMLFileManagerTest {

    String projName = "TestNetworkML";
    File projDir = new File(MainTest.getTestProjectDirectory()+ projName);

    ProjectManager pm = null;

    boolean verbose = true;



    @Before
    public void setUp()
    {
        System.out.println("---------------   setUp() NetworkMLReaderTest");

        File projFile = ProjectStructure.findProjectFile(projDir);
        pm = new ProjectManager();

        try
        {
            pm.loadProject(projFile);
            System.out.println("Proj status: "+ pm.getCurrentProject().getProjectStatusAsString());

        }
        catch (ProjectFileParsingException ex)
        {
            fail("Error loading: "+ projFile.getAbsolutePath());
        }
    }


    @Test
    public void testNml2Exporting() throws NeuroMLException
    {

            System.out.println("---  testNml2Exporting");

            Project proj = pm.getCurrentProject();
            SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();

            pm.doGenerate(sc.getName(), 1234);

            while(pm.isGenerating())
            {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    System.err.println("Error: "+ex);
                }
            }

            //proj.generatedNetworkConnections.reset();

            StringBuilder stateString1 = new StringBuilder();

            stateString1.append(proj.generatedCellPositions.toLongString(false));
            stateString1.append(proj.generatedNetworkConnections.details(false));
            stateString1.append(proj.generatedElecInputs.toString());

            System.out.println("Generated proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");


            File saveNetsDir = ProjectStructure.getSavedNetworksDir(projDir);

            File nmlFile = new File(saveNetsDir, "testNml2.xml");

            boolean zipped = false;

            nmlFile = NeuroMLFileManager.saveNetworkStructureXML(proj,
                                                             nmlFile,
                                                             zipped,
                                                             true,
                                                             sc.getName(),
                                                             NetworkMLConstants.UNITS_PHYSIOLOGICAL,
                                                             NeuroMLVersion.NEUROML_VERSION_2);

            assertTrue(nmlFile.exists());

            System.out.println("Saved NetworkML in: "+ nmlFile.getAbsolutePath());

            assertTrue("Checking validity of: "+ nmlFile.getAbsolutePath(), validateAgainstNeuroML2Schema(nmlFile));
    }


    //todo: move to utils class
    public static boolean validateAgainstNeuroML2Schema(File nmlFile)
    {
        File schemaFile = GeneralProperties.getNeuroMLv2SchemaFile();

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


        Source schemaFileSource = new StreamSource(schemaFile);
        try
        {
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(nmlFile);

            validator.validate(xmlFileSource);
        }
        catch (Exception ex)
        {
            System.out.println("Unable to validate saved NetworkML file: "+ nmlFile+" against: "+schemaFile+"\n"+ex.toString());
            return false;
        }
        System.out.println(nmlFile.getAbsolutePath()+" is valid according to: "+ schemaFile);
        return true;
    }



    public static void main(String[] args)
    {
        NeuroMLFileManagerTest ct = new NeuroMLFileManagerTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);

    }
}

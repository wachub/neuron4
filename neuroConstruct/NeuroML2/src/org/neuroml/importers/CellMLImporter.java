package org.neuroml.importers;

 
import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.util.HashMap;

import org.lemsml.behavior.DerivedVariable;
import org.lemsml.behavior.Behavior;
import org.lemsml.behavior.OnCondition;
import org.lemsml.behavior.OnStart;
import org.lemsml.behavior.StateAssignment;
import org.lemsml.behavior.StateVariable;
import org.lemsml.behavior.TimeDerivative;
import org.lemsml.serial.XMLSerializer;
import org.lemsml.sim.LemsProcess;
import org.lemsml.sim.Sim;
import org.lemsml.type.Component;
import org.lemsml.type.ComponentType;
import org.lemsml.type.Constant;
import org.lemsml.type.DefaultRun;
import org.lemsml.type.Dimension;
import org.lemsml.type.Exposure;
import org.lemsml.type.Lems;
import org.lemsml.type.Parameter;
import org.lemsml.type.Requirement;
import org.lemsml.util.E;
import org.lemsml.util.FileUtil;
import org.lemsml.viz.ColorUtil;

import cellml_api.*;
import java.lang.reflect.Field;

public class CellMLImporter  {


    public CellMLImporter() {
            E.info("Created new CellMLImporter...");
    }

    public static String tscaleName = "tscale";

    public static void main(String[] args) throws Exception
    {
        CellMLImporter cmi = new CellMLImporter();

        /*
        String cellmlApiHome = System.getProperty("CELLML_API_HOME");
        E.info("CELLML_API_HOME: "+cellmlApiHome);

        if (cellmlApiHome==null){
            E.warning("\n\n\nThere may be a problem if you've not set the environment variable CELLML_API_HOME to the location of your compiled CellML API!!\n\n\n");
        }*/

        System.setProperty("java.library.path","/usr/local/lib");

        // Trick from http://blog.cedarsoft.com/2010/11/setting-java-library-path-programmatically/
        Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
        fieldSysPath.setAccessible( true );
        fieldSysPath.set( null, null );

        E.info("Library path: "+System.getProperty("java.library.path"));
        
		System.loadLibrary("cellml_java_bridge");
        
        CellMLBootstrap cb = cellml_bootstrap.CellMLBootstrap.createCellMLBootstrap();

        File cellmlFile = new File("exportImportUtils/CellML/hodgkin_huxley_1952.cellml");

		Model model = cb.getModelLoader().loadFromURL(cellmlFile.getAbsolutePath());

		System.out.println("Model Name:" + model.getName() + "\n");

        E.info("Read in CellML from "+cellmlFile.getAbsolutePath());

        File f = new File("NeuroML2CoreTypes/Simulation.xml");
        //File f = new File("NeuroML2CoreTypes/Cells.xml");

        E.info("Loading LEMS file from: "+ f.getAbsolutePath());

        LemsProcess sim = new Sim(f);

        sim.readModel();

        boolean addModel = true;

        Lems lems = sim.getLems();


        ComponentType ct = new ComponentType(model.getName());
        if (addModel)
            lems.addComponentType(ct);

        Component comp = new Component(model.getName()+"_0", ct);
        if (addModel)
            lems.addComponent(comp);



		//Iterating components and their variables
		CellMLComponentSet componentSet = model.getModelComponents();
		CellMLComponentIterator iter = componentSet.iterateComponents();

		for(int i = 0; i < componentSet.getLength(); i++){
			CellMLComponent compC = iter.nextComponent();
			System.out.println("Component Name:"+compC.getName());

            ComponentType ctL = new ComponentType(compC.getName());
            if (addModel)
                lems.addComponentType(ctL);

			CellMLVariableSet variableSet = compC.getVariables();
			CellMLVariableIterator varIter = variableSet.iterateVariables();

			for(int j = 0; j < variableSet.getLength(); j ++){
				CellMLVariable variable = varIter.nextVariable();
				System.out.println("Variable Name:"+variable.getName());

                String name = variable.getName();
                Dimension dim = new Dimension(variable.getUnitsName());
                
                    Exposure ex = new Exposure(name, dim);
                    ctL.exposures.add(ex);

                variable.getSourceVariable().getPublicInterface();
                /*if (variable.getPublicInterface().equals(VariableInterface.INTERFACE_OUT))
                {
                }
                else if(variable.getPublicInterface().equals(VariableInterface.INTERFACE_NONE))
                {
                    Parameter lp = new Parameter(name, dim);
                    ctL.parameters.add(lp);
                }
                else if(variable.getPublicInterface().equals(VariableInterface.INTERFACE_IN))
                {
                    if (!name.equals("time")){
                        Requirement r = new Requirement(name, dim);
                        ctL.requirements.add(r);
                    }
                }
                else*/
                {
                    Parameter lp = new Parameter(name, dim);
                    ctL.parameters.add(lp);
                }

			}
			System.out.println();
		}




        Component sim1 = new Component("sim1", lems.getComponentTypeByName("Simulation"));

        float len = 10;
        float dt = 0.01f;

        sim1.setParameter("length", len+"s");
        sim1.setParameter("step", dt+"s");
        sim1.setParameter("target", comp.getID());

        Component disp1 = new Component("disp1", lems.getComponentTypeByName("Display"));
        disp1.setParameter("timeScale", "1s");
        disp1.setParameter("title", "Tester Frame!");

        sim1.addToChildren("displays", disp1);


        lems.addComponent(sim1);
        DefaultRun dr = new DefaultRun();
        dr.component = sim1.getID();

        if (addModel)
            lems.targets.add(dr);

        // TODO - may need to set some stuff on the serializer to get the format we want
        String lemsString  = XMLSerializer.serialize(lems);

        //E.info("Created: \n"+lemsString);
        //E.info("Info: \n"+lems.textSummary());

        File testFile = new File("examples/"+cellmlFile.getName().replaceAll(".xml", "")+"_CellML.xml");

        FileUtil.writeStringToFile(lemsString, testFile);

        E.info("Written to: "+ testFile.getCanonicalPath());

    }

}



















package org.neuroml.importers;

 
import java.awt.Color;
import java.io.*;
import org.sbml.jsbml.text.parser.ParseException;
import java.util.ArrayList;
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
import org.lemsml.util.ContentError;
import org.lemsml.util.E;
import org.lemsml.util.FileUtil;
import org.lemsml.viz.ColorUtil;

import javax.xml.stream.*;

import org.sbml.jsbml.*;
import org.sbml.jsbml.text.parser.*;

public class SBMLImporter  {


    public SBMLImporter() {
        E.info("Created new SBMLImporter...");
    }

    public static String tscaleName = "tscale";

    public static LemsProcess convertSBMLToLEMSSim(File sbmlFile, float simDuration, float simDt) throws ContentError, FileNotFoundException, XMLStreamException {

        SBMLReader sr = new SBMLReader();

        SBMLDocument doc = sr.readSBML(sbmlFile);

        Model model = doc.getModel();

        E.info("Read in SBML from "+sbmlFile.getAbsolutePath());

        File f = new File("NeuroML2CoreTypes/Simulation.xml");
        //File f = new File("NeuroML2CoreTypes/Cells.xml");

        E.info("Loading LEMS file from: "+ f.getAbsolutePath());

        LemsProcess sim = new Sim(f);

        sim.readModel();

        boolean addModel = true;

        Lems lems = sim.getLems();


        ComponentType ct = new ComponentType(model.getId());
        if (addModel)
            lems.addComponentType(ct);

        Component comp = new Component(model.getId()+"_0", ct);
        if (addModel)
            lems.addComponent(comp);

        Dimension noDim = new Dimension(Dimension.NO_DIMENSION);

        Behavior b = new Behavior();
        ct.behaviors.add(b);

        OnStart os = new OnStart();

        for(Compartment c: model.getListOfCompartments()){
            E.info("Adding: "+c);
            Dimension compDim = noDim;
            String size = c.getSize()+"";

            if (c.isConstant()){
                Constant constComp = new Constant(c.getId(), compDim, size);
                ct.constants.add(constComp);
            } else {
                Exposure ex = new Exposure(c.getId(), compDim);
                ct.exposures.add(ex);

                //StateVariable sv = new StateVariable(c.getId(), compDim, ex);
                //b.stateVariables.add(sv);

                //StateAssignment sa = new StateAssignment(c.getId(), c.getSize()+"");
                //os.stateAssignments.add(sa);
            }


        }

        for(Parameter p: model.getListOfParameters()) {
            //org.neuroml.lems.type.Lems
            E.info("Adding: "+p);
            Dimension paramDim = noDim;

            if (!p.isConstant()) {
                Exposure ex = new Exposure(p.getId(), paramDim);
                ct.exposures.add(ex);

                if (p.isSetValue()){

                    StateVariable sv = new StateVariable(p.getId(), paramDim, ex);
                    b.stateVariables.add(sv);

                    StateAssignment sa = new StateAssignment(p.getId(), p.getValue()+"");
                    os.stateAssignments.add(sa);
                }
            }
            else {
                org.lemsml.type.Parameter lp = new org.lemsml.type.Parameter(p.getId(), paramDim);
                lp.name = p.getId();
                ct.parameters.add(lp);
                comp.setParameter(p.getId(), p.getValue()+"");
            }
        }
        
        ArrayList<FunctionDefinition> functions = new ArrayList<FunctionDefinition>();

        for (FunctionDefinition fd: model.getListOfFunctionDefinitions()){
            functions.add(fd);
        }

        E.info("functions: "+functions);


        for(Species s: model.getListOfSpecies()) {
            Dimension speciesDim = noDim;
            Exposure ex = new Exposure(s.getId(), speciesDim);
            ct.exposures.add(ex);
            StateVariable sv = new StateVariable(s.getId(), speciesDim, ex);
            b.stateVariables.add(sv);

            if (s.isSetValue()){
                StateAssignment sa = new StateAssignment(s.getId(), s.getValue()+"");
                os.stateAssignments.add(sa);
            }

        }

        Constant timeScale = new Constant(tscaleName, lems.dimensions.getByName("per_time"), "1per_s");
        ct.constants.add(timeScale);


        for (Rule r: model.getListOfRules()){

            String formula = r.getFormula();
            
            try {
                formula = replaceFunctionDefinitions(formula, functions);
            }
            catch (Exception ex) {
                throw new ContentError("Problem substituting function definitions in SBML", ex);
            }

            E.info("Adding rule: "+r);

            if (r.isRate()){
                RateRule rr = (RateRule)r;


                TimeDerivative td = new TimeDerivative(rr.getVariable(), timeScale.getName() +" * ("+formula +")");
                b.timeDerivatives.add(td);
            }
            if (r.isAssignment()){
                Dimension speciesDim = noDim;
                AssignmentRule ar = (AssignmentRule)r;
                DerivedVariable dv = new DerivedVariable(ar.getVariable(), speciesDim,  formula, ar.getVariable());
                b.derivedVariables.add(dv);

                //TimeDerivative td = new TimeDerivative(rr.getVariable(), timeScale.getName() +" * ("+ rr.getFormula()+")");
                //b.timeDerivatives.add(td);
            }
        }

        for (Event e: model.getListOfEvents()){
            String testFormula = e.getTrigger().getFormula();
            E.info("Adding event: "+e+", test: "+testFormula);

            try {
                testFormula = replaceFunctionDefinitions(testFormula, functions);
                String test = replaceOperatorsAndTime(testFormula);
                E.info("Test tidied to: "+test);

                OnCondition oc = new OnCondition(test);
                b.onConditions.add(oc);
                for (EventAssignment ea: e.getListOfEventAssignments() ){
                    String formula = ea.getFormula();

                    formula = replaceFunctionDefinitions(formula, functions);


                    StateAssignment sa = new StateAssignment(ea.getVariable(), formula);
                    oc.stateAssignments.add(sa);
                }
            } catch (Exception ex) {
                throw new ContentError("Problem substituting function definitions in SBML", ex);
            }

        }
        if (os.stateAssignments.size()>0)
            b.onStarts.add(os);

        HashMap<String, StringBuilder> rates = new HashMap<String, StringBuilder>();

        for (Reaction reaction: model.getListOfReactions()){
            String rid = reaction.getId();
            KineticLaw kl = reaction.getKineticLaw();
            HashMap<String, String> toReplace = new HashMap<String, String>();

            for(LocalParameter p: reaction.getKineticLaw().getListOfParameters()) {
                //org.neuroml.lems.type.Lems
                String localName = p.getId()+"_"+Math.abs(rid.hashCode());
                toReplace.put(p.getId(), localName);
                E.info("Adding: "+localName);
                Dimension paramDim = noDim;

                org.lemsml.type.Parameter lp = new org.lemsml.type.Parameter(localName, paramDim);
                lp.name = localName;
                ct.parameters.add(lp);
                comp.setParameter(localName, p.getValue()+"");
            }

            String formula = "("+kl.getFormula()+")";

            try{
                formula = replaceFunctionDefinitions(formula, functions);
            }
            catch (Exception ex){
                throw new ContentError("Problem substituting function definitions in SBML", ex);
            }
            
            //
            for(String old: toReplace.keySet()){
                String new_ = toReplace.get(old);
                String[] pres = new String[]{"\\(","\\+","-","\\*","/","\\^"};
                String[] posts = new String[]{"\\)","\\+","-","\\*","/","\\^"};

                for(String pre: pres){
                    for(String post: posts){
                        String o = pre+old+post;
                        String n = pre+" "+new_+" "+post;
                        formula = formula.replaceAll(o, n);
                        //E.info("Replacing "+o+" with "+n+": "+formula);
                    }
                }
                /*
                //formula = formula.replaceAll("/("+old, "/( "+new_);
                formula = formula.replaceAll("/+"+old, "+ "+new_);
                formula = formula.replaceAll("-"+old, "- "+new_);
                formula = formula.replaceAll("/*"+old, "* "+new_);
                formula = formula.replaceAll("//"+old, "/ "+new_);
                //formula = formula.replaceAll(new_+")", new_+" )");
                formula = formula.replaceAll(new_+"+", new_+" +");
                formula = formula.replaceAll(new_+"-", new_+" -");
                formula = formula.replaceAll(new_+"/*", new_+" *");
                formula = formula.replaceAll(new_+"//", new_+" /");*/
            }

            for (SpeciesReference product: reaction.getListOfProducts()){
                String s = product.getSpecies();
                Species sp = model.getListOfSpecies().get(s);

                if (!sp.getBoundaryCondition()) {
                    if (rates.get(s)==null) rates.put(s, new StringBuilder("0"));

                    StringBuilder sb = rates.get(s);
                    sb.append(" + "+formula+"");

                    if (product.getStoichiometry()!=1){
                        sb.append(" * "+product.getStoichiometry()+"");
                    }
                }

            }
            for (SpeciesReference reactant: reaction.getListOfReactants()){

                String s = reactant.getSpecies();
                Species sp = model.getListOfSpecies().get(s);

                if (!sp.getBoundaryCondition()) {
                    if (rates.get(s)==null) rates.put(s, new StringBuilder("0"));

                    StringBuilder sb = rates.get(s);
                    sb.append(" - "+formula+"");
                    if (reactant.getStoichiometry()!=1){
                        sb.append(" * "+reactant.getStoichiometry()+"");
                    }
                }

            }

        }
            System.out.println(">>>> "+rates);

        for(String s: rates.keySet()){
            TimeDerivative td = new TimeDerivative(s, rates.get(s).toString());
            b.timeDerivatives.add(td);
        }

        if (simDuration>0 && simDt>0){

            DefaultRun dr = new DefaultRun();
            Component sim1 = new Component("sim1", lems.getComponentTypeByName("Simulation"));
            sim1.setParameter("length", simDuration+"s");
            sim1.setParameter("step", simDt+"s");
            sim1.setParameter("target", comp.getID());
            //sim1.setParameter("report",comp.getID()+"_report.txt");
            dr.timesFile = "examples/"+model.getId()+"_time.dat";

            Component disp1 = new Component("disp1", lems.getComponentTypeByName("Display"));
            disp1.setParameter("timeScale", "1s");
            disp1.setParameter("title", "Simulation of SBML model: "+ model.getId()+" from file: "+ sbmlFile);

            sim1.addToChildren("displays", disp1);

            int count = 1;

            for(Parameter p: model.getListOfParameters()) {

                if (!p.isConstant()){
                    Component lineCpt = new Component("l_"+p.getId(), lems.getComponentTypeByName("Line"));
                    lineCpt.setParameter("scale", "1");
                    lineCpt.setParameter("quantity", p.getId());
                    Color c = ColorUtil.getSequentialColour(count);
                    String rgb = Integer.toHexString(c.getRGB());
                    rgb = rgb.substring(2, rgb.length());

                    lineCpt.setParameter("color", "#"+rgb);
                    lineCpt.setParameter("save", comp.getID()+"_"+p.getId()+".dat");

                    disp1.addToChildren("lines", lineCpt);
                    count++;
                }
            }
            
            for(Species s: model.getListOfSpecies()) {

                    Component lineCpt = new Component("l_"+s.getId(), lems.getComponentTypeByName("Line"));
                    lineCpt.setParameter("scale", "1");
                    lineCpt.setParameter("quantity", s.getId());
                    Color c = ColorUtil.getSequentialColour(count);
                    String rgb = Integer.toHexString(c.getRGB());
                    rgb = rgb.substring(2, rgb.length());

                    lineCpt.setParameter("color", "#"+rgb);
                    lineCpt.setParameter("save", comp.getID()+"_"+s.getId()+".dat");

                    disp1.addToChildren("lines", lineCpt);
                    count++;
            }

            if (addModel)
                lems.addComponent(sim1);

            dr.component = sim1.getID();

            if (addModel)
                lems.targets.add(dr);
        }

        return sim;

    }

    private static String replaceFunctionDefinitions(String formula, ArrayList<FunctionDefinition> functions) throws SBMLException, ParseException{

        E.info("Substituting function defs in: "+formula);
        for (FunctionDefinition fd: functions){

            ASTNode exp = fd.getBody();
            E.info("-- Function def is: "+fd.getId()+"(...) = "+ exp);
            int count=0;
            String origFormula = new String(formula);

            while (formula.contains(fd.getId())){
                count++;
                if (count>20) throw new ParseException("Problem with formula: "+origFormula);
                
                int start = formula.indexOf(fd.getId());
                int end = formula.indexOf(")", start);
                String orig = formula.substring(start, end+1);
                E.info("orig: "+orig);

                String[] args = orig.substring(orig.indexOf("(")+1,orig.indexOf(")")).split(",");


                for(int i=0;i<args.length;i++){
                    String arg = args[i];
                    ASTNode a = fd.getArgument(i);
                    System.out.println("replacing "+a+" by "+arg);
                    ASTNode newA = JSBML.parseFormula(arg);
                    exp.replaceArgument(a.toString(), newA);
                }
                String newExp = "( "+ ASTNode.formulaToString(exp)+" )";

                orig = orig.replace("(", "\\(").replace(")", "\\)");
                E.info("formula was: "+ formula+", replacing "+ orig+" with "+newExp+", at "+formula.indexOf(orig)+", id: "+fd.getId());
                formula = formula.replaceAll(orig, newExp);
                if (formula.startsWith("( ") && formula.endsWith(" )"))
                    formula = formula.substring(2, formula.length()-2);
                E.info("formula is: "+ formula);
            }

        }
        return formula;
    }

    private static void runTest(File sbmlFile, float simDuration, float simDt, boolean showFrame) throws Exception
    {
        LemsProcess sim = convertSBMLToLEMSSim(sbmlFile, simDuration, simDt);
        Lems lems = sim.getLems();
        lems.resolve();
        String lemsString  = XMLSerializer.serialize(lems);
    
        //E.info("Created: \n"+lemsString);
        //E.info("Info: \n"+lems.textSummary());

        File testFile = new File("examples/"+sbmlFile.getName().replaceAll(".xml", "")+"_SBML.xml");

        FileUtil.writeStringToFile(lemsString, testFile);

        E.info("Written to: "+ testFile.getCanonicalPath());

        E.info("Loading LEMS file from: "+ testFile.getAbsolutePath());

        Sim sim2 = new Sim(testFile);

        sim2.readModel();

        sim2.build();

        sim2.run(showFrame);
    }

    public static void main(String[] args) throws Exception
    {
        E.setDebug(false);
        /*
        SBMLImporter si = new SBMLImporter();

        SBMLDocument doc0 = new SBMLDocument(2, 4);

        E.info("Created doc: "+ doc0);

        new SBMLWriter().write(doc0, new BufferedOutputStream(System.out),
                                "TesterP", "Test2");*/

        //File sbmlFile = new File("exportImportUtils/SBML/Simple3Species.xml");
        File sbmlFile = new File("exportImportUtils/SBML/Izhikevich.xml");

        sbmlFile = new File("exportImportUtils/SBML/BIOMD0000000118.xml");
        
        sbmlFile = new File("exportImportUtils/SBML/BIOMD0000000184.xml");
        
        File sbmlFileDir = new File("exportImportUtils/SBML/sbmlTestSuite/cases/semantic/");
            if (sbmlFileDir.exists()){
            sbmlFile = sbmlFileDir;
        }

        boolean sbmlTestSuite = sbmlFile.getAbsolutePath().indexOf("sbmlTestSuite")>=0;



        float len = 10;
        if (sbmlFile.getName().indexOf("Izh")>=0) len = 140;
        if (sbmlFile.getName().indexOf("0039")>=0) len = 50;
        if (sbmlFile.getName().indexOf("00118")>=0) len = 50;
        if (sbmlFile.getName().indexOf("00184")>=0) len = 1000;
        float dt = (float)(len/100000.0);

        HashMap<Integer, String> problematic = new HashMap<Integer, String>();
        //////problematic.put(21, "Incorrectly reads component size when units=volume ");
        ///problematic.put(22, "Incorrectly reads component size when units=volume ");
        //problematic.put(27, "Incorrectly reads component size when units=volume & init assignment");
        //problematic.put(86, "Complex args for function");

        StringBuilder errors = new StringBuilder();

        if (!sbmlTestSuite){
            runTest(sbmlFile, len, dt, true);
        }
        else {
            int successful = 0;
            int completed = 0;
            //int matching = 0;
            int failed = 0;
            int notFound = 0;

            //int numToRun = 200;
            int numToStart = 1;
            int numToStop = 100;
            numToStop = 980;
            boolean exitOnError = false;
            boolean exitOnMismatch = false;
            String version = "l2v4";
            //String version = "l3v1";
            
            for(int i=numToStart;i<=numToStop;i++){

                if (!problematic.keySet().contains(i)){

                    String testCase= "0"+i;

                    while(testCase.length()<5) testCase ="0"+testCase;


                    sbmlFile = new File("exportImportUtils/SBML/sbmlTestSuite/cases/semantic/"+testCase+"/"+testCase+"-sbml-"+version+".xml");
                    if (!sbmlFile.exists()){
                        System.out.println("   ----  File not found: "+sbmlFile.getAbsolutePath()+"!!   ---- \n\n");
                        notFound++;
                    }
                    else
                    {
                        //File parent
                        File propsFile = new File(sbmlFile.getParentFile(), testCase+"-settings.txt");
                        String info = FileUtil.readStringFromFile(propsFile);
                        String duration = info.substring(info.indexOf("duration: ")+9, info.indexOf("steps:")-1).trim();
                        len = Float.parseFloat(duration);
                        dt = (float)(len/50000.0);


                        System.out.println("\n\n---------------------------------\n\nSBML test: "+testCase+" going to be run!");

                        try{
                            System.gc();
                            runTest(sbmlFile, len, dt, (numToStop-numToStart)<10);

                            System.out.println("SBML test: "+testCase+" completed!");
                            HashMap<String, float[]> targets = new HashMap<String, float[]>();
                            HashMap<String, float[]> results = new HashMap<String, float[]>();

                            File targetDir = sbmlFile.getParentFile();
                            File targetFile = new File(sbmlFile.getParentFile(), testCase+"-results.csv");

                            System.out.println("Loading target data from "+targetFile.getCanonicalPath());
                            BufferedReader reader = new BufferedReader(new FileReader(targetFile));

                            String line = reader.readLine();
                            String[] dataNames = line.split(",");
                            int count = 0;
                            while ((line=reader.readLine()) != null) {
                                count++;
                            }
                            for(int d=0;d<dataNames.length;d++){

                                if (dataNames[d].indexOf("`")>=0)
                                    dataNames[d] = dataNames[d].substring(dataNames[d].indexOf("`") +1);

                                targets.put(dataNames[d], new float[count]);
                            }
                            E.info("Creating data arrays of size: "+count+" for "+targets.keySet());

                            reader = new BufferedReader(new FileReader(targetFile));
                            line = reader.readLine();

                            int index = 0;
                            while ((line=reader.readLine()) != null) {
                                String[] data = line.split(",");
                                for(int d=0;d<data.length;d++){
                                    float[] dataArray = targets.get(dataNames[d]);
                                    //E.info("Adding index "+index+" to "+dataNames[d]+": "+ data[d]);
                                    dataArray[index] = Float.parseFloat(data[d]);
                                }
                                index++;
                            }
                            E.info(targets.toString());


                            SBMLReader sr = new SBMLReader();

                            SBMLDocument doc = sr.readSBML(sbmlFile);

                            Model model = doc.getModel();

                            HashMap<String, File> resultFiles = new HashMap<String, File>();
                            resultFiles.put("time", new File("examples/case"+testCase+"_time.dat"));

                            for(Species s: model.getListOfSpecies()) {
                                File resultFile = new File("examples/case"+testCase+"_0_"+s.getId()+".dat");
                                resultFiles.put(s.getId(), resultFile);
                            }

                            for(String dataName: resultFiles.keySet()){
                                reader = new BufferedReader(new FileReader(resultFiles.get(dataName)));
                                ArrayList<Float> data = new ArrayList<Float>();
                                float factor = 1;
                                if (dataName.equals("time"))
                                    factor = 0.001f;
                                while ((line=reader.readLine()) != null) {
                                    data.add(Float.parseFloat(line)* factor);
                                }
                                float[] ff = new float[data.size()];
                                for(int f=0;f<data.size();f++)
                                    ff[f] = data.get(f);
                                //data.to
                                results.put(dataName, ff);

                            }
                            float[] timeTarg = targets.get("time");
                            float[] timeRes = results.get("time");

                            int ir = 0;
                            boolean match = true;

                            for (int it=0;it<timeTarg.length;it++){
                                float tt = timeTarg[it];
                                float tr = timeRes[ir];

                                while (tr < tt && ir<timeRes.length-1){
                                    //E.info("Testing target time "+tt+" against "+tr+" ("+ir+")");

                                    ir++;
                                    tr = timeRes[ir];
                                }
                                E.info("--- Testing target time "+tt+" against "+tr+" ("+ir+")");


                                for(String dataName: resultFiles.keySet()){
                                    if(!dataName.equals("time")){
                                        E.info("--- Comparing val for "+dataName+" simulated: "+ir+" against target "+it);
                                        float[] dataTarg = targets.get(dataName);
                                        float[] dataRes = results.get(dataName);
                                        float t = dataTarg[it];
                                        float r = dataRes[ir];
                                        //E.info("--- Comparing val for "+dataName+" simulated: "+r+" against target "+t);
                                        float rel = 0.01f;
                                        if (t!=0 && r!=0){
                                            float diff = Math.abs((t-r)/t);
                                            if (diff <=rel)
                                            {
                                                //E.info("--- Points match: Comparing val for "+dataName+" simulated: "+r+" against target "+t);
                                            }
                                            else
                                            {
                                                E.info("--- Points don't match: Comparing val for "+dataName+" simulated: "+r+" against target "+t+ ", diff aimed at: "+rel+", real diff: "+ diff);
                                                match = false;
                                            }
                                        }
                                    }
                                }
                            }
                            completed++;

                            if (match)
                            {
                                successful++;
                                E.info("\n    Success of test: "+testCase+"!!\n    So far: "+successful+" successful out of "+(successful+failed)+" ("+completed+" completed)\n");
                            }
                            else
                            {
                                E.info("Failure of test: "+testCase+"!\n    So far: "+successful+" successful out of "+(successful+failed)+" ("+completed+" completed)\n");
                                if (exitOnMismatch) System.exit(-1);
                                failed++;
                            }
                        }
                        catch(Exception e){
                            System.out.println("\n\nSBML test: "+testCase+" failed!!\n");
                            e.printStackTrace();
                            errors.append(testCase+": "+ e.getMessage()+"\n");
                            if (exitOnError) System.exit(-1);

                            failed++;

                        }
                    }
                }

            }

            E.info("\nAll finished!\n"
                    + "  Number succcessful:   "+successful+" out of "+(successful+failed)+"\n"
                    + "  Number completed:     "+completed+"\n"
                    + "  Number failed:        "+failed+"\n"
                    + "  Number not found:     "+notFound);

            FileUtil.writeStringToFile(errors.toString(), new File("Errors.txt"));
        }


    }

    public static String replaceOperatorsAndTime(String formula)
    {
        String opsReplaced = formula.replaceAll("<=", ".leq.").replaceAll(">=", ".geq.").replaceAll("<", ".lt.").replaceAll(">", ".gt.").replaceAll("=", ".eq.");
        String timeReplaced = opsReplaced.replaceAll("time ", "(t * "+tscaleName+") ").replaceAll(" time", " (t * "+tscaleName+")");
        return timeReplaced;
    }
}



















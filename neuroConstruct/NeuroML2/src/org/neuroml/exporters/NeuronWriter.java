package org.neuroml.exporters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.lemsml.behavior.DerivedVariable;
import org.lemsml.behavior.OnCondition;
import org.lemsml.behavior.OnEntry;
import org.lemsml.behavior.OnEvent;
import org.lemsml.behavior.OnStart;
import org.lemsml.behavior.Regime;
import org.lemsml.behavior.StateAssignment;
import org.lemsml.behavior.StateVariable;
import org.lemsml.behavior.TimeDerivative;
import org.lemsml.behavior.Transition;
import org.lemsml.sim.LemsProcess;
import org.lemsml.sim.Sim;
import org.lemsml.type.Component;
import org.lemsml.type.DefaultRun;
import org.lemsml.type.Dimension;
import org.lemsml.type.Exposure;
import org.lemsml.type.Lems;
import org.lemsml.type.LemsCollection;
import org.lemsml.type.ParamValue;
import org.lemsml.util.ContentError;
import org.lemsml.util.E;
import org.lemsml.util.FileUtil;
import org.neuroml.Utils;

public class NeuronWriter extends BaseWriter {

    final static String NEURON_VOLTAGE = "v";
    //final static String HIGH_CONDUCTANCE_PARAM = "highConductance";
    final static String RESERVED_STATE_SUFFIX = "I";
    final static String RATE_PREFIX = "rate_";
    final static String REGIME_PREFIX = "regime_";
    final static String V_COPY_PREFIX = "copy_";

    //TODO Move to central nml2 file
    public final static String ION_CHANNEL_COMP_TYPE = "baseIonChannel";
    public final static String BASE_CELL_COMP_TYPE = "baseCell";
    public final static String BASE_CELL_CAP_COMP_TYPE = "baseCellMembPotCap";

    public final static String CONC_MODEL_COMP_TYPE = "concentrationModel";
    public final static String CONC_MODEL_SURF_AREA = "surfaceArea";
    public final static String CONC_MODEL_CA_CURR_DENS = "iCa";
    public final static String CONC_MODEL_INIT_CONC = "initialConcentration";
    public final static String CONC_MODEL_INIT_EXT_CONC = "initialExtConcentration";
    public final static String CONC_MODEL_CONC_STATE_VAR = "concentration";

    public final static String BASE_POINT_CURR_COMP_TYPE = "basePointCurrent";
    public final static String BASE_SYNAPSE_COMP_TYPE = "baseSynapse";
    public final static String POINT_CURR_CURRENT = "i";
    public final static String SYNAPSE_PORT_IN = "in";

    public final static String ABSTRACT_CELL_COMP_TYPE_CAP__I_MEMB = "iMemb";

    ArrayList<File> allGeneratedFiles = new ArrayList<File>();
    static final int commentOffset = 40;

    static boolean debug = false;

    public NeuronWriter(Lems l) {
        super(l);
    }

    @Override
    protected void addComment(StringBuilder sb, String comment) {

        String comm = "# ";
        String commPre = "'''";
        String commPost = "'''";
        if (comment.indexOf("\n") < 0) {
            sb.append(comm + comment + "\n");
        } else {
            sb.append(commPre + "\n" + comment + "\n" + commPost + "\n");
        }
    }

    private void reset() {
        allGeneratedFiles.clear();
    }

    public ArrayList<File> generateMainScriptAndMods(File mainFile) throws ContentError {
        String main = generate(mainFile.getParentFile());
        try {
            FileUtil.writeStringToFile(main, mainFile);
            allGeneratedFiles.add(mainFile);
        } catch (IOException ex) {
            throw new ContentError("Error writing to file: " + mainFile.getAbsolutePath(), ex);
        }
        return allGeneratedFiles;
    }

    public String getMainScript() throws ContentError {
        return generate(null);
    }

    private static String getStateVarName(String sv) {
        if (sv.equals(NEURON_VOLTAGE)) {
            return NEURON_VOLTAGE + RESERVED_STATE_SUFFIX;
        } else {
            return sv;
        }
    }

    private static String checkForBinaryOperators(String expr)
    {
        return expr.replace(".gt.", ">").replace(".geq.", ">=").replace(".lt.", "<").replace(".leq.", "<=").replace(".and.", "&&");
    }

    private static String checkForStateVarsAndNested(String expr, Component comp, HashMap<String, HashMap<String, String>> paramMappings) {

        if (expr == null) {
            return null;
        }

        String newExpr = expr.trim();

        newExpr = newExpr.replaceAll("greater_than_or_equal_to", ">=");
        newExpr = newExpr.replaceAll("greater_than", ">");
        newExpr = newExpr.replaceAll("less_than_or_equal_to", "<=");
        newExpr = newExpr.replaceAll("less_than", "<=");
        newExpr = newExpr.replaceAll("equal_to", "==");

        newExpr = newExpr.replaceAll(" ln\\(", " log(");

        HashMap<String, String> paramMappingsComp = paramMappings.get(comp.getUniqueID());
        //E.info("Making mappings in "+newExpr+" for "+comp+": "+paramMappingsComp);
        for (String origName : paramMappingsComp.keySet()) {
            String newName = paramMappingsComp.get(origName);
            newExpr = replaceInFunction(newExpr, origName, newName);
        }

        return newExpr;
    }

    public static String replaceInFunction(String expr, String oldVar, String newVar) {
        String orig = new String(expr);

        if (expr.trim().equals(oldVar)) {
            return newVar;
        }

        //String new_ = toReplace.get(old);
        String[] pres = new String[]{"\\(", "\\+", "-", "\\*", "/", "\\^", " ", "<", ">"};
        String[] posts = new String[]{"\\)", "\\+", "-", "\\*", "/", "\\^", " ", "<", ">"};

        for (String pre : pres) {
            for (String post : posts) {

                String o = pre + oldVar + post;
                String n = pre + " " + newVar + " " + post;
                //E.info("Replacing "+o+" with "+n+": "+expr);
                expr = expr.replaceAll(o, n);
            }
        }
        expr = expr.trim();

        for (String pre : pres) {
            String o = pre + oldVar;
            String n = pre + " " + newVar;
            if (expr.endsWith(o)) {
                expr = expr.substring(0, expr.length() - o.length()) + n;
            }
        }
        for (String post : posts) {
            String o = oldVar + post;
            String n = newVar + " " + post;
            if (expr.startsWith(o)) {
                expr = n + expr.substring(o.length());
            }
        }

        expr = expr.replaceAll("  ", " ");

        if (!expr.equals(orig)) {
            //E.info("----------  Changed "+orig+" to "+ expr);
        }
        return expr;
    }

    public String generate(File dirForMods) throws ContentError {

        reset();
        StringBuilder main = new StringBuilder();

        addComment(main, "Neuron simulator export for:\n\n" + lems.textSummary(false, false));

        main.append("import neuron\n");
        main.append("h = neuron.h\n");
        main.append("h.load_file(\"nrngui.hoc\")\n");

        DefaultRun dr = lems.getDefaultRun();

        Component simCpt = dr.getComponent();
        // String simId = simCpt.getID();
        String targetId = simCpt.getStringValue("target");

        Component tgtNet = lems.getComponent(targetId);
        addComment(main, "Adding simulation " + simCpt + " of network: " + tgtNet.summary());

        ArrayList<Component> pops = tgtNet.getChildrenAL("populations");

        HashMap<String, Integer> compMechsCreated = new HashMap<String, Integer>();
        HashMap<String, String> compMechNamesHoc = new HashMap<String, String>();

        for (Component pop : pops) {
            String compRef = pop.getStringValue("component");

            Component popComp = lems.getComponent(compRef);
            String mechName = popComp.getComponentType().getName();

            int num = Integer.parseInt(pop.getStringValue("size"));

            main.append("print \"Population " + pop.getID() + " contains " + num + " instance(s) of component: "
                    + popComp.getID() + " of type: " + popComp.getComponentType().getName() + " \"\n\n");

            for (int i = 0; i < num; i++) {
                String instName = pop.getID() + "_" + i;
                main.append(instName + " = h.Section()\n");
                double defaultRadius = 5;
                main.append(instName + ".L = " + defaultRadius * 2 + "\n");
                main.append(instName + "(0.5).diam = " + defaultRadius * 2 + "\n");

                if (popComp.getComponentType().isOrExtends(BASE_CELL_CAP_COMP_TYPE)) {
                    double capTotSI = popComp.getParamValue("C").getDoubleValue();
                    double area = 4 * Math.PI * defaultRadius * defaultRadius;
                    double specCapNeu = 10e13 * capTotSI / area;
                    main.append(instName + "(0.5).cm = " + specCapNeu + "\n");
                } else {
                    main.append(instName + "(0.5).cm = 318.31927\n");
                }

                main.append(instName + ".push()\n");
                main.append(mechName + "_" + instName + " = h." + mechName + "(0.5, sec=" + instName + ")\n\n");

                if (!compMechsCreated.containsKey(mechName)) {
                    compMechsCreated.put(mechName, 0);
                }

                compMechsCreated.put(mechName, compMechsCreated.get(mechName) + 1);

                String hocMechName = mechName + "[" + (compMechsCreated.get(mechName) - 1) + "]";

                compMechNamesHoc.put(instName, hocMechName);

                LemsCollection<ParamValue> pvs = popComp.getParamValues();
                for (ParamValue pv : pvs) {
                    main.append("h." + hocMechName + "." + pv.getName() + " = "
                            + (float) pv.getDoubleValue() * getNeuronFactor(pv.getDimensionName()) + "\n");
                }
            }

            // Build the mod file for the Type

            String mod = generateModFile(popComp);


            File modFile = new File(dirForMods, popComp.getComponentType().getName() + ".mod");
            E.info("Writing to: " + modFile);

            //System.out.println(mod);
            //System.out.println(main);

            try {
                FileUtil.writeStringToFile(mod.toString(), modFile);
                allGeneratedFiles.add(modFile);
            } catch (IOException ex) {
                throw new ContentError("Error writing to file: " + modFile.getAbsolutePath(), ex);
            }


        }

        addComment(main, "The following code is based on Andrew's test_HH.py example...");

        main.append("trec = h.Vector()\n");
        main.append("trec.record(h._ref_t)\n\n");

        StringBuilder toRec = new StringBuilder();
        StringBuilder toPlot = new StringBuilder();


        ArrayList<String> displayGraphs = new ArrayList<String>();

        for (Component dispComp : simCpt.getAllChildren()) {
            if (dispComp.getName().indexOf("Display") >= 0) {
                toRec.append("# Display: " + dispComp + "\n");
                String dispId = dispComp.getID();
                int plotColour = 1;

                for (Component lineComp : dispComp.getAllChildren()) {
                    if (lineComp.getName().indexOf("Line") >= 0) {
                        //trace=StateMonitor(hhpop,'v',record=[0])
                        String trace = "trace_" + lineComp.getID();
                        String ref = lineComp.getStringValue("quantity");
                        String pop = ref.split("/")[0].split("\\[")[0];
                        String num = ref.split("\\[")[1].split("\\]")[0];
                        String var = ref.split("/")[1];

                        Component popComp = null;
                        ArrayList<Component> allPops = tgtNet.getChildrenAL("populations");
                        for (Component c : allPops) {
                            if (c.getID().equals(pop)) {
                                //E.info("--- "+c.getStringValue("component"));
                                popComp = lems.getComponent(c.getStringValue("component"));
                            }
                        }

                        //System.out.println("Recording " + var + " on cell " + num + " in " + pop + " of type " + popComp);
                        String objName = var + "rec";
                        //toRec.append(objName+" = h.Vector()\n");
                        String varFull = popComp.getName() + "_" + pop + "_" + num + "._ref_" + var;
                        //toRec.append(objName+".record("+varFull+")\n");

                        //toPlot.append("pylab.plot(trec, "+objName+")\n");
                        //toPlot.append("sampleGraph.addexpr(\""+varFull+"\", \""+varFull+"\", 1, 1, 0.8, 0.9, 2)\n");
                        String varRef = popComp.getName() + "[" + num + "]." + var;

                        varRef = pop + "_" + num;

                        varRef = compMechNamesHoc.get(varRef) + "." + var;


                        if (var.equals(NEURON_VOLTAGE)) {
                            varRef = compMechNamesHoc.get(pop + "_" + num) + "." + V_COPY_PREFIX + var;
                            /*varRef = "v";
                            String instName = pop + "_" + num;
                            varRef = "\"+"+instName+".name() + \".v";*/
                            //varRef = "v";
                            //toPlot.append(instName + ".push()\n");
                        }

                        String dispGraph = "display_" + dispId;
                        if (!displayGraphs.contains(dispGraph)) {
                            displayGraphs.add(dispGraph);
                        }

                        float scale = 1 / ((float) lineComp.getParamValue("scale").getDoubleValue()
                                * getNeuronFactor(popComp.getComponentType().getExposure(var).getDimension().getName()));

                        String plotRef = "\"" + varRef + "\"";

                        if (scale != 1) {
                            plotRef = "\"(" + scale + ") * " + varRef + "\"";
                        }

                        toPlot.append(dispGraph + ".addexpr(" + plotRef + ", " + plotRef + ", " + plotColour + ", 1, 0.8, 0.9, 2)\n");
                        plotColour++;
                        if (plotColour > 10) {
                            plotColour = 1;
                        }

                    }
                }
            }
        }
        main.append(toRec);

        String len = simCpt.getStringValue("length");
        len = len.replaceAll("ms", "");
        if (len.indexOf("s") > 0) {
            len = len.replaceAll("s", "").trim();
            len = "" + Float.parseFloat(len) * 1000;
        }

        String dt = simCpt.getStringValue("step");
        dt = dt.replaceAll("ms", "").trim();
        if (dt.indexOf("s") > 0) {
            dt = dt.replaceAll("s", "").trim();
            dt = "" + Float.parseFloat(dt) * 1000;
        }

        main.append("h.tstop = " + len + "\n\n");
        main.append("h.dt = " + dt + "\n\n");
        main.append("h.steps_per_ms = " + (float) (1d / Double.parseDouble(dt)) + "\n\n");

        //main.append("objref SampleGraph\n");
        for (String dg : displayGraphs) {
            main.append(dg + " = h.Graph(0)\n");
            main.append(dg + ".size(0,h.tstop,-80.0,50.0)\n");
            main.append(dg + ".view(0, -80.0, h.tstop, 130.0, 80, 330, 330, 250)\n");
            main.append("h.graphList[0].append(" + dg + ")\n");
        }

        main.append(toPlot);
        main.append("\n\n");


        main.append("h.load_file('nrngui.hoc')\n");
        main.append("h.nrncontrolmenu()\n");

        main.append("h.run()\n");

        main.append("print \"Done\"\n\n");

        return main.toString();
    }

    public static String generateModFile(Component comp) throws ContentError {
        StringBuilder mod = new StringBuilder();

        String mechName = comp.getComponentType().getName();

        mod.append("TITLE Mod file for component: " + comp + "\n\n");
        StringBuilder blockNeuron = new StringBuilder();
        StringBuilder blockUnits = new StringBuilder();
        StringBuilder blockParameter = new StringBuilder();
        StringBuilder blockAssigned = new StringBuilder();
        StringBuilder blockState = new StringBuilder();
        StringBuilder blockInitial = new StringBuilder();
        StringBuilder blockInitial_v = new StringBuilder();
        StringBuilder blockBreakpoint = new StringBuilder();
        StringBuilder blockBreakpoint_regimes = new StringBuilder();
        StringBuilder blockDerivative = new StringBuilder();
        StringBuilder blockNetReceive = new StringBuilder();
        String blockNetReceiveParams = "";
        StringBuilder ratesMethod = new StringBuilder("\n");

        HashMap<String, HashMap<String, String>> paramMappings = new HashMap<String, HashMap<String, String>>();

        if (comp.getComponentType().isOrExtends(BASE_CELL_COMP_TYPE)) {
            HashMap<String, String> paramMappingsComp = new HashMap<String, String>();
            ///paramMappingsComp.put(NEURON_VOLTAGE, getStateVarName(NEURON_VOLTAGE));
            paramMappings.put(comp.getUniqueID(), paramMappingsComp);
        }

        blockUnits.append("\n(nA) = (nanoamp)\n"
                + "(uA) = (microamp)\n"
                + "(mA) = (milliamp)\n"
                + "(mV) = (millivolt)\n"
                + "(mS) = (millisiemens)\n"
                + "(uS) = (microsiemens)\n"
                + "(molar) = (1/liter)\n"
                + "(mM) = (millimolar)\n"
                + "(um) = (micrometer)\n");
                /*+ "(S) = (siemens)\n"
                + "(um) = (micrometer)\n"
                + "(molar) = (1/liter)\n"
                + "(mM) = (millimolar)\n"
                + "(l) = (liter)\n")*/


        ArrayList<String> locals = new ArrayList<String>();

        if (comp.getComponentType().isOrExtends(ION_CHANNEL_COMP_TYPE)) {
            mechName = comp.getID();
            blockNeuron.append("SUFFIX " + mechName + "\n");

            String species = comp.getTextParam("species");
            if (species != null) {
                blockNeuron.append("USEION " + species + " READ e" + species + " WRITE i" + species + "\n"); //TODO check valence
            }

            blockNeuron.append("\nRANGE gmax                             : Will be set when ion channel mechanism placed on cell!\n");

            blockParameter.append("\ngmax = 0                                : Will be set when ion channel mechanism placed on cell!\n");

        } else if (comp.getComponentType().isOrExtends(CONC_MODEL_COMP_TYPE)) {
            mechName = comp.getID();
            blockNeuron.append("SUFFIX " + mechName + "\n");

            String ion = comp.getStringValue("ion");
            if (ion != null) {
                blockNeuron.append("USEION " + ion + " READ " + ion + "i," + ion + "o, i" + ion + " WRITE " + ion + "i\n"); //TODO check valence
            }
            blockNeuron.append("RANGE cai\n");
            blockNeuron.append("RANGE cao\n");
            
            blockParameter.append("cai (mM)\n");
            blockParameter.append("cao (mM)\n");

            blockAssigned.append("ica		(mA/cm2)\n");

            blockAssigned.append("diam (um)\n");
            
            blockAssigned.append("area (um2)\n");

            ratesMethod.append(CONC_MODEL_SURF_AREA + " = area\n\n");
            ratesMethod.append(CONC_MODEL_CA_CURR_DENS + " = -10000 * ica * "+CONC_MODEL_SURF_AREA+" : To correct units...\n\n");

            locals.add(CONC_MODEL_SURF_AREA);
            locals.add(CONC_MODEL_CA_CURR_DENS);

            blockNeuron.append("GLOBAL "+CONC_MODEL_INIT_CONC + "\n");
            blockNeuron.append("GLOBAL "+CONC_MODEL_INIT_EXT_CONC + "\n");
            blockParameter.append(CONC_MODEL_INIT_CONC + " (mM)\n");
            blockParameter.append(CONC_MODEL_INIT_EXT_CONC + " (mM)\n");

            blockInitial.append(CONC_MODEL_INIT_CONC+" = cai" + "\n");
            blockInitial.append(CONC_MODEL_INIT_EXT_CONC+" = cao" + "\n");
            
        } 
        else {
            blockNeuron.append("POINT_PROCESS " + mechName);
        }

        if (comp.getComponentType().isOrExtends(BASE_SYNAPSE_COMP_TYPE))
        {
            blockNetReceiveParams = "weight (uS)";
            blockAssigned.append("? Standard Assigned variables with baseSynapse\n");
            blockAssigned.append("v (mV)\n");
        }
        else
        {
            blockNetReceiveParams = "flag";
        }

        String prefix = "";

        ArrayList<String> rangeVars = new ArrayList<String>();
        ArrayList<String> stateVars = new ArrayList<String>();

        parseStateVars(comp, prefix, rangeVars, stateVars, blockNeuron, blockParameter, blockAssigned, blockState, paramMappings);

        parseParameters(comp, prefix, rangeVars, stateVars, blockNeuron, blockParameter, paramMappings);


        if (comp.getComponentType().isOrExtends(ION_CHANNEL_COMP_TYPE)) {
            blockAssigned.append("? Standard Assigned variables with ionChannel\n");
            blockAssigned.append("v (mV)\n");
            blockAssigned.append("celsius\n");

            String species = comp.getTextParam("species");
            if (species != null) {
                blockAssigned.append("e" + species + " (mV)\n");
                blockAssigned.append("i" + species + " (mA/cm2)\n");
            }
            blockAssigned.append("\n");
        }

        //ratesMethod.append("? - \n");

        parseDerivedVars(comp, prefix, rangeVars, ratesMethod, blockNeuron, blockParameter, blockAssigned, blockBreakpoint, paramMappings);

        //ratesMethod.append("? + \n");

        if (comp.getComponentType().isOrExtends(ION_CHANNEL_COMP_TYPE)) {

            blockBreakpoint.append("g = gmax * fopen     : Overwriting evaluation of g, assuming gmax set externally\n\n");

            String species = comp.getTextParam("species");
            // Only for ohmic!!
            if (species != null) {
                blockBreakpoint.append("i" + species + " = g * (v - e" + species + ")\n");
            }
        }

        parseTimeDerivs(comp, prefix, locals, blockDerivative, blockBreakpoint, blockAssigned, ratesMethod, paramMappings);

        if (blockDerivative.length() > 0) {
            blockBreakpoint.insert(0, "SOLVE states METHOD cnexp\n\n");
        }


        ArrayList<String> regimeNames = new ArrayList<String>();
        Hashtable<String, Integer> flagsVsRegimes = new Hashtable<String, Integer>();
        if (comp.getComponentType().hasBehavior()) {

            int regimeFlag = 5000;
            for (Regime regime: comp.getComponentType().getBehavior().getRegimes()) {
                flagsVsRegimes.put(regime.name, regimeFlag);  // fill
                regimeFlag++;
            }

            String elsePrefix = "";
            for (Regime regime: comp.getComponentType().getBehavior().getRegimes()) {
                String regimeStateName = REGIME_PREFIX+regime.name;
                regimeNames.add(regimeStateName);

                //StringBuilder test = new StringBuilder(": Testing for "+regimeStateName+ "\n");
                //test.append(elsePrefix+"if ("+regimeStateName+" == 1 ) {\n");
                //elsePrefix = "else ";

                // blockNetReceive.append("if ("+regimeStateName+" == 1 && flag == "+regimeFlag+") { "
                  //      + ": Setting watch for OnCondition in "+regimeStateName+"...\n");
                //////////blockNetReceive.append("    WATCH (" + checkForStateVarsAndNested(cond, comp, paramMappings) + ") "+conditionFlag+"\n");

                for (OnCondition oc: regime.getOnConditions()){

                    String cond = checkForBinaryOperators(oc.test);

                    blockNetReceive.append("\nif (flag == 1) { : Setting watch condition for "+regimeStateName+"\n");
                    blockNetReceive.append("    WATCH (" + checkForStateVarsAndNested(cond, comp, paramMappings) + ") "+regimeFlag+"\n");
                    blockNetReceive.append("}\n\n");

                    //test.append("    if (" + checkForStateVarsAndNested(cond, comp, paramMappings) + ") {\n");

                    blockNetReceive.append("if ("+regimeStateName+" == 1 && flag == "+regimeFlag+") { : Setting actions for "+regimeStateName+"\n");

                    if (debug) blockNetReceive.append("\n        printf(\"+++++++ Start "+regimeStateName+" at time: %g, v: %g\\n\", t, v)\n");

                    blockNetReceive.append("\n        : State assignments\n");
                    for (StateAssignment sa : oc.getStateAssignments()) {
                        blockNetReceive.append("\n        " + getStateVarName(sa.getStateVariable().getName()) + " = "
                                + checkForStateVarsAndNested(sa.getEvaluable().toString(), comp, paramMappings) + "\n");
                    }
                    for (Transition trans: oc.getTransitions()){
                        blockNetReceive.append("\n        : Change regime flags\n");
                        blockNetReceive.append("        "+regimeStateName+" = 0\n");
                        blockNetReceive.append("        "+REGIME_PREFIX+trans.regime+" = 1\n");
                        //int flagTarget = flagsVsRegimes.get(trans.getRegime());
                        //test.append("    net_send(0,"+flagTarget+") : Sending to regime_"+trans.getRegime()+"\n");

                        Regime targetRegime = comp.getComponentType().getBehavior().getRegimes().getByName(trans.regime);
                        if (targetRegime!=null){
                            blockNetReceive.append("\n        : OnEntry to "+targetRegime+"\n");
                            for (OnEntry oe: targetRegime.getOnEntrys()){
                                for (StateAssignment sa: oe.getStateAssignments()){
                                    blockNetReceive.append("\n        " + sa.getStateVariable().getName() + " = "
                                            + checkForStateVarsAndNested(sa.getEvaluable().toString(), comp, paramMappings) + "\n");
                                }
                            }
                        }
                    }

                    if (debug) blockNetReceive.append("\n        printf(\"+++++++ End "+regimeStateName+" at time: %g, v: %g\\n\", t, v)\n");
                    blockNetReceive.append("}\n");
                    
                }
                //blockNetReceive.append("}\n");
                //blockBreakpoint_regimes.insert(0, test.toString()+ "\n");
                //blockBreakpoint_regimes.append(blockNetReceive.toString()+ "\n");
                regimeFlag++;
            }
         }

        String localsLine = "";
        if (!locals.isEmpty()) {
            localsLine = "LOCAL ";
        }
        for (String local : locals) {
            localsLine = localsLine + local;
            if (!locals.get(locals.size() - 1).equals(local)) {
                localsLine = localsLine + ", ";
            } else {
                localsLine = localsLine + "\n";
            }


        }

        ratesMethod.insert(0, localsLine);
        //blockDerivative.insert(0, localsLine);

        blockInitial.append("rates()\n");

        parseOnStart(comp, prefix, blockInitial, blockInitial_v, paramMappings);


        /*for (OnStart os : comp.getComponentClass().getDynamics().getOnStarts()) {
        for (StateAssignment sa : os.getStateAssignments()) {
        blockInitial.append("\n" + getStateVarName(sa.getStateVariable().getName()) + " = " + sa.getEvaluable() + "\n");
        }
        }*/



        int conditionFlag = 1000;
        for (OnCondition oc : comp.getComponentType().getBehavior().getOnConditions()) {

            String cond = checkForBinaryOperators(oc.test);

            boolean resetVoltage = false;
            for (StateAssignment sa : oc.getStateAssignments()) {
                resetVoltage = resetVoltage || sa.getStateVariable().getName().equals(NEURON_VOLTAGE);
            }

            if (!resetVoltage) {
                blockBreakpoint.append("if (" + checkForStateVarsAndNested(cond, comp, paramMappings) + ") {");
                for (StateAssignment sa : oc.getStateAssignments()) {
                    blockBreakpoint.append("\n    " + getStateVarName(sa.getStateVariable().getName()) + " = " + checkForStateVarsAndNested(sa.getEvaluable().toString(), comp, paramMappings) + "\n");
                }
                blockBreakpoint.append("}\n\n");
            } else {

                blockNetReceive.append("\nif (flag == 1) { : Setting watch for top level OnCondition...\n");
                blockNetReceive.append("    WATCH (" + checkForStateVarsAndNested(cond, comp, paramMappings) + ") "+conditionFlag+"\n");

                blockNetReceive.append("}\n");
                blockNetReceive.append("if (flag == "+conditionFlag+") {\n");
                if (debug) blockNetReceive.append("    printf(\"Condition (" + checkForStateVarsAndNested(cond, comp, paramMappings) + "), "+conditionFlag
                        +", satisfied at time: %g, v: %g\\n\", t, v)\n");
                for (StateAssignment sa : oc.getStateAssignments()) {
                    blockNetReceive.append("\n    " + sa.getStateVariable().getName() + " = " + checkForStateVarsAndNested(sa.getEvaluable().toString(), comp, paramMappings) + "\n");
                }
                blockNetReceive.append("}\n");

            }
            conditionFlag++;
        }

        for (OnEvent oe : comp.getComponentType().getBehavior().getOnEvents()) {
            if (oe.getPortName().equals(SYNAPSE_PORT_IN))
            {
                for (StateAssignment sa : oe.getStateAssignments()) {
                    blockNetReceive.append("state_discontinuity("+sa.getStateVariable().getName() + ", "+ checkForStateVarsAndNested(sa.getEvaluable().toString(), comp, paramMappings)+")\n");
                }
            }
        }
        

        if (comp.getComponentType().isOrExtends(CONC_MODEL_COMP_TYPE) &&
            comp.getComponentType().getBehavior().getTimeDerivatives().isEmpty())
        {
            blockBreakpoint.append("\ncai = "+CONC_MODEL_CONC_STATE_VAR+"\n\n");
        }


        /*
        if (comp.getComponentType().hasBehavior()) {
            for (Regime regime: comp.getComponentType().getBehavior().getRegimes()) {
                String regimeStateName = "regime_"+regime.name;
                blockNetReceive.append(": Conditions for "+regimeStateName);
                int regimeFlag = flagsVsRegimes.get(regime.name);
                blockNetReceive.append("if (flag == "+regimeFlag+") { : Entry into "+regimeStateName+"\n");
                for (String r: regimeNames){
                    blockNetReceive.append("    " + r + " = " +(r.equals(regimeStateName)?1:0)+"\n");
                }
                for (OnEntry oe: regime.getOnEntrys()){
                  
                    for (StateAssignment sa: oe.getStateAssignments()){
                        blockNetReceive.append("\n    " + getStateVarName(sa.getStateVariable().getName()) + " = "
                                + checkForStateVarsAndNested(sa.getEvaluable().toString(), comp, paramMappings) + "\n");
                    }
                }
                blockNetReceive.append("}\n");
            }
         }*/
         
         
        if (blockInitial_v.toString().trim().length()>0) {
            blockNetReceive.append("if (flag == 1) { : Set initial states\n");
            blockNetReceive.append(blockInitial_v.toString());
            blockNetReceive.append("}\n");
        }

        for (StateVariable sv : comp.getComponentType().getBehavior().getStateVariables()) {

            if (sv.getName().equals(NEURON_VOLTAGE)) {
                //blockBreakpoint.append("\ni = " + HIGH_CONDUCTANCE_PARAM + "*(v-" + getStateVarName(NEURON_VOLTAGE) + ") ? To ensure v of section rapidly follows " + getStateVarName(sv.getName()));

                blockBreakpoint.append("\n" + V_COPY_PREFIX + NEURON_VOLTAGE + " = " + NEURON_VOLTAGE);

                if (comp.getComponentType().isOrExtends(BASE_CELL_CAP_COMP_TYPE)) {
                    //blockBreakpoint.append("\ni = -1 * " + ABSTRACT_CELL_COMP_TYPE_CAP__I_MEMB + "");
                    blockBreakpoint.append("\ni = " + getStateVarName(NEURON_VOLTAGE) + " * C");
                } else {
                    blockBreakpoint.append("\ni = " + getStateVarName(NEURON_VOLTAGE) + "");
                }
            }
        }



        writeModBlock(mod, "NEURON", blockNeuron.toString());

        writeModBlock(mod, "UNITS", blockUnits.toString());

        writeModBlock(mod, "PARAMETER", blockParameter.toString());

        if (blockAssigned.length() > 0) {
            writeModBlock(mod, "ASSIGNED", blockAssigned.toString());
        }

        writeModBlock(mod, "STATE", blockState.toString());
        writeModBlock(mod, "INITIAL", blockInitial.toString());


        if (blockDerivative.length() == 0) {
            blockBreakpoint.insert(0, "rates()\n");
        }

        if (debug) blockBreakpoint.insert(0, "printf(\"+++++++ Entering BREAKPOINT in "+comp.getName()+" at time: %g, v: %g\\n\", t, v)\n");
        writeModBlock(mod, "BREAKPOINT", blockBreakpoint_regimes.toString()+"\n"+blockBreakpoint.toString());

        if (blockNetReceive.length() > 0) {
            writeModBlock(mod, "NET_RECEIVE("+blockNetReceiveParams+")", blockNetReceive.toString());
        }

        if (blockDerivative.length() > 0) {
            blockDerivative.insert(0, "rates()\n");
            writeModBlock(mod, "DERIVATIVE states", blockDerivative.toString());
        }


        writeModBlock(mod, "PROCEDURE rates()", ratesMethod.toString());

        for (String compK : paramMappings.keySet()) {
            //E.info("  Maps for "+compK);
            for (String orig : paramMappings.get(compK).keySet()) {
                //E.info("      "+orig+" -> "+paramMappings.get(compK).get(orig));
            }
        }
        //System.out.println("----  paramMappings: "+paramMappings);

        return mod.toString();
    }

    private static void parseOnStart(Component comp,
            String prefix,
            StringBuilder blockInitial,
            StringBuilder blockInitial_v,
            HashMap<String, HashMap<String, String>> paramMappings) throws ContentError {

        HashMap<String, String> paramMappingsComp = paramMappings.get(comp.getUniqueID());

        if (comp.getComponentType().hasBehavior()) {

            for (Regime regime: comp.getComponentType().getBehavior().getRegimes()) {
                String regimeStateName = REGIME_PREFIX+regime.name;
                if (regime.initial !=null && regime.initial.equals("true")) {
                        blockInitial.append("\n"+regimeStateName+" = 1\n");
                } else {
                        blockInitial.append("\n"+regimeStateName+" = 0\n");
                }

            }


            for (OnStart os : comp.getComponentType().getBehavior().getOnStarts()) {
                for (StateAssignment sa : os.getStateAssignments()) {
                    String var = getStateVarName(sa.getStateVariable().getName());

                    if (paramMappingsComp.containsKey(var)) {
                        var = paramMappingsComp.get(var);
                    }

                    if (sa.getStateVariable().getName().equals(NEURON_VOLTAGE)) {
                        var = sa.getStateVariable().getName();

                        blockInitial.append("\nnet_send(0, 1) : go to NET_RECEIVE block, flag 1, for initial state\n");
                        blockInitial_v.append("\n    " + var + " = " + checkForStateVarsAndNested(sa.getEvaluable().toString(), comp, paramMappings) + "\n");

                    } else {

                        blockInitial.append("\n" + var + " = " + checkForStateVarsAndNested(sa.getEvaluable().toString(), comp, paramMappings) + "\n");
                    }
                }
            }
        }
        for (Component childComp : comp.getAllChildren()) {
            String prefixNew = prefix + childComp.getID() + "_";
            if (childComp.getID() == null) {
                prefixNew = prefix + childComp.getName() + "_";
            }
            parseOnStart(childComp, prefixNew, blockInitial, blockInitial_v, paramMappings);
        }

    }

    private static void parseParameters(Component comp,
            String prefix,
            ArrayList<String> rangeVars,
            ArrayList<String> stateVars,
            StringBuilder blockNeuron,
            StringBuilder blockParameter,
            HashMap<String, HashMap<String, String>> paramMappings) {

        HashMap<String, String> paramMappingsComp = paramMappings.get(comp.getUniqueID());

        if (paramMappingsComp == null) {
            paramMappingsComp = new HashMap<String, String>();
            paramMappings.put(comp.getUniqueID(), paramMappingsComp);
        }

        for (ParamValue pv : comp.getParamValues()) {
            String mappedName = prefix + pv.getName();
            rangeVars.add(mappedName);
            paramMappingsComp.put(pv.getName(), mappedName);

            String range = "\nRANGE " + mappedName;
            while (range.length() < commentOffset) {
                range = range + " ";
            }

            blockNeuron.append(range + ": parameter");
            float val = ((float) pv.getDoubleValue()) * getNeuronFactor(pv.getDimensionName());
            String valS = val + "";
            if ((int) val == val) {
                valS = (int) val + "";
            }
            blockParameter.append("\n" + mappedName + " = " + valS
                    + " " + getNeuronUnit(pv.getDimensionName()));
        }


        for (Exposure exp : comp.getComponentType().getExposures()) {
            String mappedName = prefix + exp.getName();

            if (!rangeVars.contains(mappedName)
                    && !stateVars.contains(mappedName)
                    && !exp.getName().equals(NEURON_VOLTAGE)) {
                rangeVars.add(mappedName);
                paramMappingsComp.put(exp.getName(), mappedName);

                String range = "\nRANGE " + mappedName;
                while (range.length() < commentOffset) {
                    range = range + " ";
                }
                blockNeuron.append(range + ": exposure");

                if (comp.getComponentType().isOrExtends(BASE_POINT_CURR_COMP_TYPE) &&
                    exp.getName().equals(POINT_CURR_CURRENT))
                {
                    blockNeuron.append("\n\nNONSPECIFIC_CURRENT "+POINT_CURR_CURRENT+"\n");
                }
            }
        }

        for (Component childComp : comp.getAllChildren()) {
            String prefixNew = prefix + childComp.getID() + "_";
            if (childComp.getID() == null) {
                prefixNew = prefix + childComp.getName() + "_";
            }
            parseParameters(childComp, prefixNew, rangeVars, stateVars, blockNeuron, blockParameter, paramMappings);

            /*
            HashMap<String, String> childMaps = paramMappings.get(childComp.getID());
            for (String mapped: childMaps.keySet()){
            if (!paramMappingsComp.containsKey(mapped)){
            paramMappingsComp.put(mapped, childMaps.get(mapped));
            }
            }*/
        }

        if (comp.getComponentType().isOrExtends(BASE_CELL_COMP_TYPE)) {
            blockNeuron.append("\nRANGE " + V_COPY_PREFIX + NEURON_VOLTAGE + "                           : copy of v on section\n");
        }

    }

    private static void parseStateVars(Component comp,
            String prefix,
            ArrayList<String> rangeVars,
            ArrayList<String> stateVars,
            StringBuilder blockNeuron,
            StringBuilder blockParameter,
            StringBuilder blockAssigned,
            StringBuilder blockState,
            HashMap<String, HashMap<String, String>> paramMappings) throws ContentError {

        HashMap<String, String> paramMappingsComp = paramMappings.get(comp.getUniqueID());

        if (paramMappingsComp == null) {
            paramMappingsComp = new HashMap<String, String>();
            paramMappings.put(comp.getUniqueID(), paramMappingsComp);
        }

        if (comp.getComponentType().hasBehavior()) {

            for (Regime regime: comp.getComponentType().getBehavior().getRegimes()) {
                String regimeStateName = REGIME_PREFIX+regime.name;
                stateVars.add(regimeStateName);
                blockState.append(regimeStateName + " (1)\n");
            }


            for (StateVariable sv : comp.getComponentType().getBehavior().getStateVariables()) {

                String svName = prefix + getStateVarName(sv.getName());
                stateVars.add(svName);
                String dim = getNeuronUnit(sv.getDimension().getName());

                if (!svName.equals(NEURON_VOLTAGE) && !getStateVarName(sv.getName()).equals(getStateVarName(NEURON_VOLTAGE))) {
                    paramMappingsComp.put(getStateVarName(sv.getName()), svName);
                }


                if (sv.getName().equals(NEURON_VOLTAGE)) {
                    blockNeuron.append("\n\nNONSPECIFIC_CURRENT i                    : To ensure v of section follows " + svName);
                    ////blockNeuron.append("\nRANGE " + HIGH_CONDUCTANCE_PARAM + "                  : High conductance for above current");
                    ////blockParameter.append("\n\n" + HIGH_CONDUCTANCE_PARAM + " = 1000 (S/cm2)");
                    blockAssigned.append("v (mV)\n");
                    blockAssigned.append("i (mA/cm2)\n\n");


                    blockAssigned.append(V_COPY_PREFIX + NEURON_VOLTAGE + " (mV)\n\n");

                    dim = "(nA)";
                }

                blockState.append(svName + " " + dim + "\n");
            }
        }

        for (Component childComp : comp.getAllChildren()) {
            String prefixNew = prefix + childComp.getID() + "_";
            if (childComp.getID() == null) {
                prefixNew = prefix + childComp.getName() + "_";
            }
            parseStateVars(childComp, prefixNew, rangeVars, stateVars, blockNeuron, blockParameter, blockAssigned, blockState, paramMappings);
        }
    }

    private static void parseTimeDerivs(Component comp,
            String prefix,
            ArrayList<String> locals,
            StringBuilder blockDerivative,
            StringBuilder blockBreakpoint,
            StringBuilder blockAssigned,
            StringBuilder ratesMethod,
            HashMap<String, HashMap<String, String>> paramMappings) throws ContentError {

        StringBuilder ratesMethodFinal = new StringBuilder();

        if (comp.getComponentType().hasBehavior()) {

            Hashtable<String, String> rateNameVsRateExpr = new Hashtable<String, String>();

            for (TimeDerivative td : comp.getComponentType().getBehavior().getTimeDerivatives()) {
               
                String rateName = RATE_PREFIX + prefix + td.getStateVariable().getName();
                blockAssigned.append(rateName + "\n");

                String rateUnits = getNeuronUnit(td.getStateVariable().getDimension().getName());
                rateUnits = rateUnits.replaceAll("\\)", "/ms)");
                rateUnits = "";

                //ratesMethod.append(rateName + " = " + checkForStateVarsAndNested(td.getEvaluable().toString(), comp, paramMappings) + " ? \n");
                String rateExpr = checkForStateVarsAndNested(td.getEvaluable().toString(), comp, paramMappings);
                rateNameVsRateExpr.put(rateName, rateExpr);
                
                if (!td.getStateVariable().getName().equals(NEURON_VOLTAGE)) {

                    String stateVarToUse = getStateVarName(td.getStateVariable().getName());
                    

                    String line = prefix + stateVarToUse + "' = " + rateName + " " + rateUnits;

                    if (comp.getComponentType().isOrExtends(CONC_MODEL_COMP_TYPE) &&
                        td.getStateVariable().getName().equals(CONC_MODEL_CONC_STATE_VAR))
                    {
                        line = line+ "\ncai = "+td.getStateVariable().getName();
                    }

                    if (blockDerivative.toString().indexOf(line)<0)
                        blockDerivative.append(line+" \n");

                } else {
                    ratesMethodFinal.append(prefix + getStateVarName(td.getStateVariable().getName()) + " = -1 * " + rateName + "\n");
                }
                 
            }


            for (Regime regime: comp.getComponentType().getBehavior().getRegimes()) {
                for (TimeDerivative td: regime.getTimeDerivatives()){
                    String rateName = RATE_PREFIX + prefix + td.getStateVariable().getName();
                    if (!rateNameVsRateExpr.containsKey(rateName)) {
                        rateNameVsRateExpr.put(rateName, "0");
                    }

                    String rateExprPart = rateNameVsRateExpr.get(rateName);
                    if (blockAssigned.indexOf("\n"+rateName + "\n")<0) 
                    {
                        blockAssigned.append("\n"+rateName + "\n");
                    }
                    rateExprPart = rateExprPart+" + "+REGIME_PREFIX+regime.getName()+" * ("+checkForStateVarsAndNested(td.getEvaluable().toString(), comp, paramMappings)+")";
                    
                    rateNameVsRateExpr.put(rateName, rateExprPart);
                    
                    String rateUnits = getNeuronUnit(td.getStateVariable().getDimension().getName());
                    rateUnits = rateUnits.replaceAll("\\)", "/ms)");
                    rateUnits = "";

                    if (!td.getStateVariable().getName().equals(NEURON_VOLTAGE)) {
                        String line = prefix + getStateVarName(td.getStateVariable().getName()) + "' = " + rateName + " " + rateUnits;

                        if (blockDerivative.toString().indexOf(line)<0)
                            blockDerivative.append(line+" \n");
                        
                    } else {
                        ratesMethodFinal.append(prefix + getStateVarName(td.getStateVariable().getName()) + " = -1 * " + rateName + "\n"); ////
                    }
                }
            }
            
            for (String rateName: rateNameVsRateExpr.keySet()){
                String rateExpr = rateNameVsRateExpr.get(rateName);
                //ratesMethod.insert(0,rateName + " = " + rateExpr + " \n");
                ratesMethod.append(rateName + " = " + rateExpr + " \n");
            }

            ratesMethod.append("\n"+ratesMethodFinal + " \n");

        }
        
        for (Component childComp : comp.getAllChildren()) {
            String prefixNew = prefix + childComp.getID() + "_";
            if (childComp.getID() == null) {
                prefixNew = prefix + childComp.getName() + "_";
            }
            parseTimeDerivs(childComp, prefixNew, locals, blockDerivative, blockBreakpoint, blockAssigned, ratesMethod, paramMappings);
        }
    }

    private static void parseDerivedVars(Component comp,
            String prefix,
            ArrayList<String> rangeVars,
            StringBuilder ratesMethod,
            StringBuilder blockNeuron,
            StringBuilder blockParameter,
            StringBuilder blockAssigned,
            StringBuilder blockBreakpoint,
            HashMap<String, HashMap<String, String>> paramMappings) throws ContentError {


        HashMap<String, String> paramMappingsComp = paramMappings.get(comp.getUniqueID());
        if (paramMappingsComp == null) {
            paramMappingsComp = new HashMap<String, String>();
            paramMappings.put(comp.getUniqueID(), paramMappingsComp);
        }

        for (Component childComp : comp.getAllChildren()) {
            String prefixNew = prefix + childComp.getID() + "_";
            if (childComp.getID() == null) {
                prefixNew = prefix + childComp.getName() + "_";
            }
            parseDerivedVars(childComp, prefixNew, rangeVars, ratesMethod, blockNeuron, blockParameter, blockAssigned, blockBreakpoint, paramMappings);
        }
        //ratesMethod.append("? Looking at"+comp+"\n");
        if (comp.getComponentType().hasBehavior()) {

            StringBuilder blockForEqns = ratesMethod;
            if (comp.getComponentType().isOrExtends(ION_CHANNEL_COMP_TYPE)) {
                blockForEqns = blockBreakpoint;
            }
            for (DerivedVariable dv : comp.getComponentType().getBehavior().getDerivedVariables()) {

                StringBuilder block = new StringBuilder();

                if (!rangeVars.contains(dv.getName())) {
                    String mappedName = prefix + dv.getName();
                    rangeVars.add(mappedName);

                    String range = "\nRANGE " + mappedName;
                    while (range.length() < commentOffset) {
                        range = range + " ";
                    }

                    blockNeuron.append(range + ": derived var\n");
                    paramMappingsComp.put(dv.getName(), mappedName);
                }

                String assig = "\n" + prefix + dv.getName() + " " + getNeuronUnit(dv.dimension);
                while (assig.length() < commentOffset) {
                    assig = assig + " ";
                }

                blockAssigned.append(assig + ": derived var\n");


                if (dv.getEvaluable() != null) {

                    String rate = checkForStateVarsAndNested(dv.getEvaluable().toString(), comp, paramMappings);

                    if (dv.getEvaluableCondition() == null) {
                        //if (cond.)
                        block.append(prefix + dv.getName() + " = " + rate + " ? evaluable\n\n");
                    } else {
                        String cond = checkForStateVarsAndNested(dv.getEvaluableCondition().toString(), comp, paramMappings);
                        String ifFalse = checkForStateVarsAndNested(dv.getEvaluableIfFalse().toString(), comp, paramMappings);

                        block.append("if (" + cond + ") {\n");
                        block.append("    " + prefix + dv.getName() + " = " + rate + " \n");
                        block.append("} else {\n");
                        block.append("    " + prefix + dv.getName() + " = " + ifFalse + " \n");
                        block.append("} \n\n");

                    }

                } else {
                    String firstChild = dv.getPath().substring(0, dv.getPath().indexOf("/"));
                    if (firstChild.indexOf("[") >= 0) {
                        firstChild = firstChild.substring(0, firstChild.indexOf("["));
                    }

                    Component child = null;

                    try {
                        child = comp.getChild(firstChild);
                    } catch (ContentError ce) {
                        E.info("No child of " + firstChild);// do nothing...
                    }

                    if (child == null) {
                        ArrayList<Component> children = comp.getChildrenAL(firstChild);
                        if (children.size() > 0) {
                            child = children.get(0);
                        }
                    }

                    block.append("? DerivedVariable is based on path: " + dv.getPath() + " from " + firstChild + ": " + child + "\n");

                    if (child == null && dv.getPath().indexOf("synapse") < 0) {
                        String alt = dv.getOnAbsent();
                        block.append("? Path not present in component, using: " + alt + "\n\n");
                        String rate = checkForStateVarsAndNested(alt, comp, paramMappings);
                        block.append(prefix + dv.getName() + " = " + rate + " ? onAbsent\n\n");
                    } else {
                        String var = prefix + dv.getPath().replaceAll("/", "_");
                        if (var.indexOf("[*]") >= 0 && var.indexOf("syn") >= 0) {
                            var = "0 ? Was: " + var + " but insertion of currents from external attachments not yet supported";
                        } else if (var.indexOf("[*]") >= 0) {
                            String children = var.substring(0, var.indexOf("[*]"));
                            String path = var.substring(var.indexOf("[*]_") + 4);
                            String reduce = dv.getReduce();
                            String op = null;
                            if (reduce.equals("multiply")) {
                                op = " * ";
                            }
                            if (reduce.equals("add")) {
                                op = " + ";
                            }
                            var = "";

                            for (Component childComp : comp.getChildrenAL(children)) {
                                //var = var + childComp.getID()+" --";
                                if (var.length() > 0) {
                                    var = var + op;
                                }
                                var = var + childComp.getID() + "_" + path;
                            }


                            var = var + " ? " + reduce + " applied to all instances of " + path + " in " + children;
                        }
                        block.append(prefix + dv.getName() + " = " + var + " ? path\n\n");
                    }
                }
                //blockForEqns.insert(0, block);
                blockForEqns.append(block);
            }
        }

    }

    private static String getNeuronUnit(String dimensionName) {

        if (dimensionName == null) {
            return ": no units???";
        }

        if (dimensionName.equals("voltage")) {
            return "(mV)";
        } else if (dimensionName.equals("per_voltage")) {
            return "(/mV)";
        } else if (dimensionName.equals("conductance")) {
            return "(uS)";
        } else if (dimensionName.equals("capacitance")) {
            return "(microfarads)";
        } else if (dimensionName.equals("time")) {
            return "(ms)";
        } else if (dimensionName.equals("per_time")) {
            return "(megahertz)";
        } else if (dimensionName.equals("current")) {
            return "(nA)";
        } else if (dimensionName.equals("length")) {
            return "(um)";
        } else if (dimensionName.equals("area")) {
            return "(um2)";
        } else if (dimensionName.equals("concentration")) {
            return "(mM)";
        } else if (dimensionName.equals("charge_per_mole")) {
            return "(coulomb)";
        } else if (dimensionName.equals(Dimension.NO_DIMENSION)) {
            return "";
        } else {
            return "? Don't know units for : (" + dimensionName + ")";
        }
    }

    private static float getNeuronFactor(String dimensionName) {

        if (dimensionName.equals("voltage")) {
            return 1000f;
        } else if (dimensionName.equals("per_voltage")) {
            return 0.001f;
        } else if (dimensionName.equals("conductance")) {
            return 1000000f;
        } else if (dimensionName.equals("capacitance")) {
            return 1e6f;
        } else if (dimensionName.equals("per_time")) {
            return 0.001f;
        } else if (dimensionName.equals("current")) {
            return 1e9f;
        } else if (dimensionName.equals("time")) {
            return 1000f;
        } else if (dimensionName.equals("length")) {
            return 1000000f;
        } else if (dimensionName.equals("area")) {
            return 1e12f;
        } else if (dimensionName.equals("concentration")) {
            return 1f;
        } else if (dimensionName.equals("charge_per_mole")) {
            return 1f;
        }
        return 1f;
    }

    public static void writeModBlock(StringBuilder main, String blockName, String contents) {
        contents = contents.replaceAll("\n", "\n    ");
        if (!contents.endsWith("\n")) {
            contents = contents + "\n";
        }
        main.append(blockName + " {\n    "
                + contents + "}\n\n");
    }

    public class CompInfo {

        StringBuilder params = new StringBuilder();
        StringBuilder eqns = new StringBuilder();
        StringBuilder initInfo = new StringBuilder();
    }

    public static void main(String[] args) throws Exception {

        E.setDebug(false);
        ArrayList<File> nml2Channels = new ArrayList<File>();

        //nml2Channels.add(new File("../nCexamples/Ex10_NeuroML2/cellMechanisms/IzhBurst/IzhBurst.nml"));
        //nml2Channels.add(new File("../nCexamples/Ex10_NeuroML2/cellMechanisms/NaCond_NML2/NaCond_NML2.nml"));
        nml2Channels.add(new File("../models/LarkumEtAl2009/cellMechanisms/cad2_NML2/cad2_NML2.nml"));
        nml2Channels.add(new File("../models/LarkumEtAl2009/cellMechanisms/CaClamp/CaClamp.nml"));
        //nml2Channels.add(new File("../NeuroML2/ChannelMLConvert/NMDA.nml"));


        File expDir = new File("../../temp/mods/");
        for (File f : expDir.listFiles()) {
            //f.delete();
        }

        for (File nml2Channel : nml2Channels) {
            String nml2Content = FileUtil.readStringFromFile(nml2Channel);
            //System.out.println("nml2Content: "+nml2Content);
            String lemsified = Utils.convertNeuroML2ToLems(nml2Content);
            //System.out.println("lemsified: "+lemsified);

            LemsProcess sim = new Sim(lemsified);

            sim.readModel();

            for (Component comp : sim.getLems().components.getContents()) {
                E.info("Component: " + comp);
                E.info("baseIonChannel: " + comp.getComponentType().isOrExtends(ION_CHANNEL_COMP_TYPE));
                E.info("baseCell: " + comp.getComponentType().isOrExtends(BASE_CELL_COMP_TYPE));
                E.info("concentrationModel: " + comp.getComponentType().isOrExtends(CONC_MODEL_COMP_TYPE));
                E.info("basePointCurrent: " + comp.getComponentType().isOrExtends(BASE_POINT_CURR_COMP_TYPE));

                if (comp.getComponentType().isOrExtends(ION_CHANNEL_COMP_TYPE)
                        || comp.getComponentType().isOrExtends(BASE_CELL_COMP_TYPE)
                        || comp.getComponentType().isOrExtends(CONC_MODEL_COMP_TYPE)
                        || comp.getComponentType().isOrExtends(BASE_POINT_CURR_COMP_TYPE)) {
                    E.info(comp + " is an " + ION_CHANNEL_COMP_TYPE + " or  " + BASE_CELL_COMP_TYPE + " or  " + CONC_MODEL_COMP_TYPE+ " or  " + BASE_POINT_CURR_COMP_TYPE);
                    String mod = generateModFile(comp);

                    File expFile = new File(expDir, comp.getID() + ".mod");


                    E.info("\n----------------------------------------------------   \n");
                    E.info(mod);

                    E.info("Exporting file to: " + expFile.getCanonicalPath());
                    FileUtil.writeStringToFile(mod, expFile);

                }

            }
        }
    }
}

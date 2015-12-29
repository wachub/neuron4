package org.neuroml.exporters;


import org.lemsml.expression.Parser;
import org.lemsml.type.Component;
import org.lemsml.type.DefaultRun;
import org.lemsml.type.Lems;
import org.lemsml.util.ContentError;

public class SEDMLWriter extends XMLWriter {

    public static final String GLOBAL_TIME_SBML = "t";
    public static final String GLOBAL_TIME_SBML_MATHML = "<csymbol encoding=\"text\" definitionURL=\"http://www.sbml.org/sbml/symbols/time\"> time </csymbol>";

    private String originalFilename;
    private ModelFormat modelFormat;

    public enum ModelFormat {NEUROML2, SBML, CELLML};

    public SEDMLWriter(Lems l, String originalFilename, ModelFormat modelFormat) {
        super(l);
        this.originalFilename = originalFilename;
        this.modelFormat = modelFormat;
    }

    private String getPopPrefix(Component pop) {
        return pop.getID() + "_";
    }

    public String getMainScript() throws ContentError {

        Parser p = new Parser();

        StringBuilder main = new StringBuilder();
        main.append("<?xml version='1.0' encoding='UTF-8'?>\n");

        String[] attrs = new String[]{"xmlns=http://sed-ml.org/",
            "level=1",
            "version=1",
            "xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance",
            "xsi:schemaLocation=http://sed-ml.org/    ../Schemas/SED-ML/sed-ml-L1-V1.xsd"};
        

        startElement(main, "sedML", attrs);
        startElement(main, "notes");
        startElement(main, "p", "xmlns=http://www.w3.org/1999/xhtml");
        main.append("\nSBML export for:\n" + lems.textSummary(false, false) + "\n");
        endElement(main, "p");
        endElement(main, "notes");


        DefaultRun dr = lems.getDefaultRun();

        Component simCpt = dr.getComponent();
        String simId = simCpt.getID();
        String targetId = simCpt.getStringValue("target");

        Component tgtNet = lems.getComponent(targetId);
        addComment(main, "Adding simulation " + simCpt + " of network: " + tgtNet.summary() + "", true);




        String netId = tgtNet.getID();

        startElement(main, "listOfSimulations");
        main.append("\n");
        int numPts = (int)Math.ceil(simCpt.getParamValue("length").getDoubleValue() / simCpt.getParamValue("step").getDoubleValue());
        startElement(main,
                     "uniformTimeCourse",
                     "id = "+simId,
                     "initialTime=0",
                     "outputStartTime=0",
                     "outputEndTime="+simCpt.getParamValue("length").getDoubleValue(),
                     "numberOfPoints="+numPts);

        startEndElement(main, "algorithm", "kisaoID=KISAO:0000030");
        
        endElement(main, "uniformTimeCourse");
        main.append("\n");
        endElement(main, "listOfSimulations");

        main.append("\n");

        startElement(main, "listOfModels");

        if (modelFormat == ModelFormat.NEUROML2)
        {
            startEndElement(main,
                            "model",
                            "id="+netId,
                            "language=urn:sedml:language:neuroml2",
                            "source="+originalFilename);
        }
        else if (modelFormat == ModelFormat.SBML)
        {
            startEndElement(main,
                            "model",
                            "id="+netId,
                            "language=urn:sedml:language:sbml",
                            "source="+originalFilename.replaceAll(".xml", ".sbml"));
        }
        else if (modelFormat == ModelFormat.CELLML)
        {
            startEndElement(main,
                            "model",
                            "id="+netId,
                            "language=urn:sedml:language:cellml",
                            "source="+originalFilename.replaceAll(".xml", ".cellml"));
        }

        endElement(main, "listOfModels");
        main.append("\n");

        startElement(main, "listOfTasks");

        //<task simulationReference="Sim_45" id="RUN_Sim_45" modelReference="Ex1_Simple"/>
        String taskId = simId+"_"+netId;
        startEndElement(main,
                        "task",
                        "id="+taskId,
                        "simulationReference="+simId,
                        "modelReference="+netId);

        endElement(main, "listOfTasks");
        main.append("\n");



        startElement(main, "listOfDataGenerators");
        /*<dataGenerator id="time" name="time">
            <listOfVariables>
                <variable id="var_time_0" taskReference="task1" symbol="urn:sedml:symbol:time" />
            </listOfVariables>
            <math xmlns="http://www.w3.org/1998/Math/MathML">
                <ci> var_time_0 </ci>
            </math>
        </dataGenerator>*/

        startElement(main, "dataGenerator", "id=time", "name=time");
        startElement(main, "listOfVariables");
        startEndElement(main,
                        "variable",
                        "id=var_time_0",
                        "taskReference="+taskId,
                        "symbol=urn:sedml:symbol:time");
        endElement(main, "listOfVariables");

        startElement(main, "math", "xmlns=http://www.w3.org/1998/Math/MathML");
        addTextElement(main, "ci", " var_time_0 ");
        endElement(main, "math");
        endElement(main, "dataGenerator");

        for (Component dispComp : simCpt.getAllChildren()) {
            if (dispComp.getName().indexOf("Display") >= 0) {
                String dispId = dispComp.getID();

                for (Component lineComp : dispComp.getAllChildren()) {
                    if (lineComp.getName().indexOf("Line") >= 0) {
                        //trace=StateMonitor(hhpop,'v',record=[0])
                        
                        String ref = lineComp.getStringValue("quantity");
                        String pop = ref.split("/")[0].split("\\[")[0];
                        String num = ref.split("\\[")[1].split("\\]")[0];
                        String var = ref.split("/")[1];
                        
                        String genId = dispId+"_"+lineComp.getID();
                        String varFull = pop+"_"+num+"_"+var;

                        startElement(main, "dataGenerator", "id="+genId, "name="+genId);
                        startElement(main, "listOfVariables");
                        startEndElement(main,
                                        "variable",
                                        "id="+varFull,
                                        "taskReference="+taskId,
                                        "target="+ref);
                        endElement(main, "listOfVariables");

                        startElement(main, "math", "xmlns=http://www.w3.org/1998/Math/MathML");
                        addTextElement(main, "ci", ref);
                        endElement(main, "math");
                        endElement(main, "dataGenerator");

                    }
                }
            }
        }

        endElement(main, "listOfDataGenerators");
        main.append("\n");


        startElement(main, "listOfOutputs");

        for (Component dispComp : simCpt.getAllChildren()) {
            if (dispComp.getName().indexOf("Display") >= 0) {
                String dispId = dispComp.getID();

                startElement(main, "plot2D", "id="+dispId);
                startElement(main, "listOfCurves");

                for (Component lineComp : dispComp.getAllChildren()) {
                    if (lineComp.getName().indexOf("Line") >= 0) {
                        //trace=StateMonitor(hhpop,'v',record=[0])
                        String ref = lineComp.getStringValue("quantity");
                        String pop = ref.split("/")[0].split("\\[")[0];
                        String num = ref.split("\\[")[1].split("\\]")[0];
                        String var = ref.split("/")[1];

                        String genId = dispId+"_"+lineComp.getID();
                        String varFull = pop+"_"+num+"_"+var;
                        //<curve id="curve_0" logX="false" logY="false" xDataReference="time" yDataReference="v_1" />
                        startEndElement(main,
                                     "curve",
                                     "id="+lineComp.getID(),
                                     "logX=false",
                                     "logY=false",
                                     "xDataReference=time",
                                     "yDataReference="+varFull);

                    }
                }
                endElement(main, "listOfCurves");

                endElement(main, "plot2D");
            }
        }
        
        endElement(main, "listOfOutputs");
        main.append("\n");

        
        endElement(main, "sedML");
        System.out.println(main);
        return main.toString();
    }

    private String getSuitableId(String str) {
        return str.replace(" ", "_").replace(".", "").replace("(", "_").replace(")", "_").replace("*", "mult").replace("+", "plus").replace("/", "div");
    }
}

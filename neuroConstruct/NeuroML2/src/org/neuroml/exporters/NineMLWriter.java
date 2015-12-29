package org.neuroml.exporters;


import java.util.ArrayList;

import org.lemsml.behavior.Behavior;
import org.lemsml.behavior.EventOut;
import org.lemsml.behavior.OnCondition;
import org.lemsml.behavior.StateAssignment;
import org.lemsml.behavior.StateVariable;
import org.lemsml.behavior.TimeDerivative;
import org.lemsml.type.Component;
import org.lemsml.type.ComponentType;
import org.lemsml.type.DefaultRun;
import org.lemsml.type.Dimension;
import org.lemsml.type.FinalParam;
import org.lemsml.type.Lems;
import org.lemsml.type.ParamValue;
import org.lemsml.util.ContentError;
import org.lemsml.util.DimensionsExport;

public class NineMLWriter extends XMLWriter {
	
	
	public NineMLWriter(Lems l) {
		super(l);
	}

        

	public String getMainScript() throws ContentError {

		StringBuilder main = new StringBuilder();
		StringBuilder nodes = new StringBuilder();

		StringBuilder userLayer = new StringBuilder();

                main.append("<?xml version='1.0' encoding='UTF-8'?>\n");

                addComment(main,"NineML export from LEMS of model:\n\n"+lems.textSummary(false, false), true);

                //mainHoc.append("{load_file(\"nrngui.hoc\")}\n\n");

                DefaultRun dr = lems.getDefaultRun();

                Component simCpt = dr.getComponent();
                // String simId = simCpt.getID();
                String targetId = simCpt.getStringValue("target");
                
                Component tgtNet = lems.getComponent(targetId);
                addComment(main,"Adding simulation "+simCpt+" of network: "+tgtNet.summary()+"", true);

                //indent="";
                startElement(main,"NineML", "xmlns=http://nineml.org/9ML/0.2",
                        "xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance",
                        "xmlns:comodl=CoMoDL",
                        "xsi:schemaLocation=http://nineml.org/9ML/0.2 ../Schemas/NineML/NineML_v0.2.xsd");

                       //  http://neuroml.svn.sourceforge.net/viewvc/neuroml/NeuroML2/Schemas/NineML/NineML_v0.2.xsd");
                       // ");

                ArrayList<String> compsAdded = new ArrayList<String>();

                ArrayList<Component> pops = tgtNet.getChildrenAL("populations");

                for(Component pop: pops) {
                    String compRef = pop.getStringValue("component");
                    Component popComp = lems.getComponent(compRef);
                    addComment(main,"Population "+pop.getID()+" contains components of: "+popComp, true);
                    String compName = popComp.getComponentType().getName();

                    if (!compsAdded.contains(compName)){
                            startElement(main,"ComponentClass", "name="+compName, "xmlns=CoMoDL");

                            String prefix = "";

                            parseType(popComp.getComponentType(), main);

                            endElement(main,"ComponentClass");
                            compsAdded.add(compName);
                    }

                }
                ArrayList<Component> conns = tgtNet.getChildrenAL("explicitConnections");

                for(Component conn: conns) {
                    String compRef = conn.getStringValue("synapse");
                    Component synComp = lems.getComponent(compRef);

                    addComment(main,"Connection contains synapse of: "+synComp, true);
                    String compName = synComp.getComponentType().getName();

                    if (!compsAdded.contains(compName)){
                            startElement(main,"ComponentClass", "name="+compName, "xmlns=CoModL");

                            String prefix = "";

                            parseType(synComp.getComponentType(), main);

                            endElement(main,"ComponentClass");
                            compsAdded.add(compName);

                            addComment(main,"Node "+synComp.getID()+" is an instance of: "+synComp.getComponentType().getName(), true);

                                startElement(main,"node", "name="+synComp.getID());
                                startElement(main,"definition");
                                startEndTextElement(main,"url",".");
                                addComment(main, "Note: there needs to be some way to indentify the component (type) of this node...");
                                startEndTextElement(main,"componentType", synComp.getComponentType().getName());
                                endElement(main,"definition");


                                startElement(main,"properties");

                                for (ParamValue pv: synComp.getParamValues())
                                {

                                        startElement(main,pv.getName());
                                        startEndTextElement(main,"value", (float)pv.value+"");
                                        String dimName = pv.getDimensionName();
                                        String si = null;
                                        for(Dimension d: lems.getDimensions()){
                                                if (d.getName().equals(dimName)){
                                                        si = DimensionsExport.getSIUnit(d);
                                                }
                                        }

                                        startEndTextElement(main,"unit", si);
                                        endElement(main,pv.getName());
                                }

                                endElement(main,"properties");
                                endElement(main,"node");

                    }

                }

                for(Component pop: pops) {
                        String compRef = pop.getStringValue("component");
                        Component popComp = lems.getComponent(compRef);

                       addComment(main,"Component "+popComp.getID()+" is an instance of: "+popComp.getComponentType().getName(), true);

                        startElement(main,"component", "name="+popComp.getID());
                        startElement(main,"definition", "language=NineML");
                        startEndTextElement(main,"url",".");
                        addComment(main, "Note: there needs to be some way to indentify the component (type) of this component...");
                        startEndTextElement(main,"componentType", popComp.getComponentType().getName());
                        endElement(main,"definition");

                      

                        for (ParamValue pv: popComp.getParamValues())
                        {

                                startElement(main,"property", "name="+pv.getName());

                                startElement(main,"quantity");
                                startElement(main,"value");
                                startEndTextElement(main,"scalar", (float)pv.value+"");

                                String dimName = pv.getDimensionName();
                                String si = null;
                                for(Dimension d: lems.getDimensions()){
                                        if (d.getName().equals(dimName)){
                                                si = DimensionsExport.getSIUnit(d);
                                        }
                                }

                                startEndTextElement(main,"unit", si);
                                endElement(main,"value");
                                endElement(main,"quantity");
                                endElement(main,"property");
                        }

                        endElement(main,"component");

                }

                startElement(main,"group", "name="+lems.getDefaultRun().getComponent().getID());
                
                for(Component pop: pops) {
                        String compRef = pop.getStringValue("component");
                        Component popComp = lems.getComponent(compRef);
                        addComment(main,"Population "+pop.getID()+" contains components of: "+popComp, true);

                        startElement(main,"population", "name="+pop.getID());
                        startEndTextElement(main, "number", pop.getStringValue("size"));
                        startEndTextElement(main, "prototype", popComp.getID());
                        endElement(main,"population");
                }


                for(Component conn: conns) {
                    String compRef = conn.getStringValue("synapse");
                    String from = conn.getStringValue("from");
                    String to = conn.getStringValue("to");
                    Component synComp = lems.getComponent(compRef);

                    addComment(main,"Connection from "+from+" to "+to+" consists of synapse of: "+synComp, true);

                    addComment(main,"NOTE: No example found yet in UL of explicit connection between 2 nodes!", true);
                }

                endElement(main,"group");


                endElement(main,"NineML");
                System.out.println(main);
		return main.toString();
	}

        public void parseType(ComponentType type, StringBuilder main) throws ContentError
        {
                for (FinalParam param: type.getFinalParams()){
                        startEndElement(main, "Parameter", "name="+param.getName(), "dimension=none");
                }

                Behavior b = type.getBehavior();
                for(StateVariable sv: b.getStateVariables()){
                        startEndElement(main, "AnalogPort", "name="+sv.getName(), "mode=send", "dimension=none");

                }
                String defaultRegime = "default_regime";
                //startElement(main,"sequence","name="+defaultRegime); // default regime

                startElement(main,"Dynamics");

                startElement(main,"Regime","name="+defaultRegime);

                for(TimeDerivative timeDerivative: b.getTimeDerivatives()){
                        String depVar = timeDerivative.getStateVariable().getName();
                        startElement(main,"TimeDerivative", "name="+depVar+"_equation", "variable="+depVar);
                        startElement(main,"MathInline");
                        main.append(getIndent()+INDENT+timeDerivative.getEvaluable()+"\n");
                        endElement(main,"MathInline");
                        endElement(main,"TimeDerivative");
                }

                /*
                for(OnCondition oc: b.getOnConditions()){
                        String check = "check__"+oc.test.replace(" ", "_").replace(".", "");
                        String trueVal = "true__"+oc.test.replace(" ", "_").replace(".", "");
                        if (oc.getEventOuts().size()>0)
                        {
                                trueVal = "spike";
                        }


                        startElement(main, "assignment", "to="+ trueVal, "name="+ check);
                        startElement(main,"math-inline");
                        main.append(getIndent()+INDENT+oc.test.replace(".gt.","&gt;").replace(".lt.","&lt;")+"\n");
                        endElement(main,"math-inline");
                        endElement(main, "assignment");

                }*/
                //endElement(main,"sequence");

                for(OnCondition oc: b.getOnConditions()){
                        //String tempRegime = "transe__"+oc.test.replace(" ", "_").replace(".", "");
                        addComment(main, "Adding check for "+ oc.test);


                        //startElement(main, "transition", "name="+ tempRegime, "from="+defaultRegime, "to="+defaultRegime, "condition="+trueVal);
                        startElement(main, "OnCondition");

                        startElement(main, "Trigger");
                        startElement(main,"MathInline");
                        main.append(getIndent()+INDENT+oc.test+"\n");
                        endElement(main,"MathInline");
                        endElement(main, "Trigger");


                        for(StateAssignment sa: oc.stateAssignments){
                                startElement(main, "StateAssignment","variable="+ sa.getStateVariable().getName());
                                startElement(main,"MathInline");
                                main.append(getIndent()+INDENT+sa.getEvaluable()+"\n");
                                endElement(main,"MathInline");
                                endElement(main, "StateAssignment");

                        }
                        for (EventOut eo: oc.getEventOuts())
                        {
                                startEndElement(main, "EventOut","port="+eo.getPortName());
                        }

                        endElement(main, "OnCondition");
                        
                }

                endElement(main,"Regime");

                endElement(main,"Dynamics");

        }


}



















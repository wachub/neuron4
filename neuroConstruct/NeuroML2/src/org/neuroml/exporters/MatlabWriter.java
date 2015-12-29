package org.neuroml.exporters;


import java.util.ArrayList;

import org.lemsml.eval.DVal;
import org.lemsml.expression.ParseError;
import org.lemsml.run.ActionBlock;
import org.lemsml.run.ExpressionDerivedVariable;
import org.lemsml.run.PathDerivedVariable;
import org.lemsml.run.VariableAssignment;
import org.lemsml.run.VariableROC;
import org.lemsml.type.Component;
import org.lemsml.type.DefaultRun;
import org.lemsml.type.Lems;
import org.lemsml.type.LemsCollection;
import org.lemsml.type.ParamValue;
import org.lemsml.type.Parameter;
import org.lemsml.type.Unit;
import org.lemsml.util.ContentError;
import org.lemsml.util.E;

public class MatlabWriter extends BaseWriter {


        public MatlabWriter(Lems lems)
        {
                super(lems);
        }

        @Override
        protected void addComment(StringBuilder sb, String comment) {
                
                String comm = "% ";
                comment = comm + comment;
                comment = comment.replaceAll("\n", "\n% ");
                sb.append(comment);
                
                //String commPre = "'''";
                //String commPost = "'''";
                //if (comment.indexOf("\n")<0)
                //        sb.append(comm+comment+"\n");
                //else
                //        sb.append(commPre+"\n"+comment+"\n"+commPost+"\n");
        }


	

	public String getMainScript() throws ContentError, ParseError {
		StringBuilder sb = new StringBuilder();

        addComment(sb,"MATLAB export for:\n"+lems.textSummary(false, false)+"\n\n");

        //sb.append("from brian import *\n\n");

        DefaultRun dr = lems.getDefaultRun();

        Component simCpt = dr.getComponent();
        // String simId = simCpt.getID();
        String targetId = simCpt.getStringValue("target");

        Component tgtNet = lems.getComponent(targetId);
        addComment(sb,"Adding simulation "+simCpt+" of network: "+tgtNet.summary());

        ArrayList<Component> pops = tgtNet.getChildrenAL("populations");

        ArrayList<String> stateVars = new ArrayList<String>();

        for(Component pop: pops) {
            String compRef = pop.getStringValue("component");
            Component popComp = lems.getComponent(compRef);
            addComment(sb,"   Population "+pop.getID()+" contains components of: "+popComp+" ");

            String prefix = popComp.getID()+"_";

            CompInfo compInfo = new CompInfo();

            getCompEqns(compInfo, popComp, pop.getID(), stateVars, "");

            sb.append("\n"+compInfo.params.toString());

            //sb.append(prefix+"eqs=Equations('''\n");
            sb.append(compInfo.eqns.toString());
            //sb.append("''')\n\n");

            String flags = "";//,implicit=True, freeze=True
            sb.append(pop.getID()+" = NeuronGroup("+pop.getStringValue("size")+", model="+prefix+"eqs"+flags+")\n");


            sb.append(compInfo.initInfo.toString());
        }

        StringBuilder toTrace = new StringBuilder();
        StringBuilder toPlot = new StringBuilder();

        for(Component dispComp: simCpt.getAllChildren()){
                if(dispComp.getName().indexOf("Display")>=0){
                        toTrace.append("# Display: "+dispComp+"\n");
                        for(Component lineComp: dispComp.getAllChildren()){
                                if(lineComp.getName().indexOf("Line")>=0){
                                        //trace=StateMonitor(hhpop,'v',record=[0])
                                        String trace = "trace_"+lineComp.getID();
                                        String ref = lineComp.getStringValue("quantity");
                                        String pop = ref.split("/")[0].split("\\[")[0];
                                        String num = ref.split("\\[")[1].split("\\]")[0];
                                        String var = ref.split("/")[1];

                                        //if (var.equals("v")){

                                                toTrace.append(trace+" = StateMonitor("+pop+",'"+var+"',record=["+num+"]) # "+lineComp.summary()+"\n");
                                                toPlot.append("plot("+trace+".times/ms,"+trace+"["+num+"]/mV)\n");
                                        //}
                                }
                        }
                }
        }
        //sb.append(toTrace);

        float len = Float.parseFloat(simCpt.getStringValue("length").replace("m", "").replace("s", "")); //TODO Fix!!
        float dt = Float.parseFloat(simCpt.getStringValue("step").replace("m", "").replace("s", ""));
        int steps = (int)(len/dt);

        sb.append("states = [0,0];\n");

        for (String sv: stateVars){
            
        }

        sb.append("tspan = [0,"+steps+"];\n");

        sb.append("...");

        for (String stv: stateVars){
            sb.append("plot(t,"+stv+");\n");
        }


        System.out.println(sb);
		return sb.toString();
	}


/*
        private String getBrianUnits(String siDim)
        {
                if(siDim.equals("voltage")) return "V";
                if(siDim.equals("conductance")) return "S";
                return null;
        }*/


        public void getCompEqns(CompInfo compInfo, Component comp, String popName, ArrayList<String> stateVars, String prefix) throws ContentError, ParseError
        {
                LemsCollection<Parameter> ps = comp.getComponentType().getDimParams();

                String localPrefix = comp.getID()+"_";

                if (comp.getID()==null)
                        localPrefix = comp.getName()+"_";

                for(Parameter p: ps)
                {
                        ParamValue pv = comp.getParamValue(p.getName());
                        //////////////String units = "*"+getBrianUnits(pv.getDimensionName());
                        String units = "";
                        if (units.indexOf(Unit.NO_UNIT)>=0)
                                units = "";
                        compInfo.params.append(prefix+p.getName()+" = "+(float)pv.getDoubleValue()+units+"; \n");
                }
                
                if (ps.size()>0)
                        compInfo.params.append("\n");

                ArrayList<VariableROC> rates = comp.getComponentBehavior().getRates();

                for(String svar: comp.getComponentBehavior().getSvars())
                {
                        String localName = prefix+svar;
                        if (!stateVars.contains(localName)) // i.e. no TimeDerivative of StateVariable
                        {
                                stateVars.add(localName);
                                //compInfo.eqns.append("d"+localName+"/dt = 0/ms : 1\n");
                        }
                }

                String allSvs = "(";
                for (String sv: stateVars){
                    allSvs = allSvs+sv+", ";
                }
                allSvs = allSvs.substring(0,allSvs.length()-2)+")";

                for(VariableROC vroc: rates)
                {
                        String localName = prefix+vroc.getVarName();
                        stateVars.add(localName);
                        String expr = ((DVal)vroc.getRateexp().getRoot()).toString(prefix, stateVars);
                        expr = expr.replace("^", "**");
                        
                        compInfo.eqns.append("function rate = d"+localName+"_dt"+allSvs+"\n");

                        compInfo.eqns.append("    rate = "+expr+"\n\n");
                }



                ArrayList<PathDerivedVariable> pathDevVar = comp.getComponentBehavior().getPathderiveds();
                for(PathDerivedVariable pdv: pathDevVar)
                {
                        String path = pdv.getPath();
                        String bits[] = pdv.getBits();
                        StringBuilder info = new StringBuilder("# "+path +" (");
                        for (String bit: bits)
                                info.append(bit+", ");
                        info.append("), simple: "+pdv.isSimple());

                        String right = "";
                        
                        if (pdv.isSimple())
                        {
                                Component parentComp = comp;
                                for(int i=0;i<bits.length-1;i++){
                                        String type = bits[i];
                                        Component child = parentComp.getChild(type);
                                        if (child.getID()!=null)
                                                right=right+prefix+child.getID()+"_";
                                        else
                                                right=right+prefix+child.getName()+"_";
                                }
                                right=right+bits[bits.length-1];
                        } else {
                                Component parentComp = comp;
                                ArrayList<String> found = new ArrayList<String>();
                                //String ref = "";
                                for(int i=0;i<bits.length-1;i++){
                                        String type = bits[i];
                                        E.info("Getting children of "+comp+" of type "+type);
                                        ArrayList<Component>  children = parentComp.getChildrenAL(type);
                                        if (children!=null && children.size()>0){

                                                for(Component child: children){
                                                        E.info("child: "+ child);
                                                        if (child.getID()!=null)
                                                                found.add(child.getID());
                                                        else
                                                                found.add(child.getName());
                                                }
                                        }
                                }
                                for(String el: found){
                                        if (!found.get(0).equals(el))
                                                right=right+" + ";
                                        right=right+prefix+el+"_"+bits[bits.length-1];
                                }
                                if (found.isEmpty())
                                        right = "0";
                                E.info("found: "+found);

                        }
                        String line = prefix+pdv.getVarName()+" = "+right+" : 1        # "+info;
                        E.info("line: "+line);

                        compInfo.eqns.append(line+"\n");
                }
                
                ArrayList<ExpressionDerivedVariable> expDevVar = comp.getComponentBehavior().getExderiveds();
                for(ExpressionDerivedVariable edv: expDevVar)
                {
                        String expr = ((DVal)edv.getRateexp().getRoot()).toString(prefix, stateVars);
                        expr = expr.replace("^", "**");
                        compInfo.eqns.append(prefix+edv.getVarName()+" = "+expr+" : 1\n");
                }

                ArrayList<ActionBlock> initBlocks = comp.getComponentBehavior().getInitBlocks();
                
                for(ActionBlock ab: initBlocks)
                {
                        ArrayList<VariableAssignment> assigs = ab.getAssignments();
                        for(VariableAssignment va: assigs)
                        {
                                compInfo.initInfo.append(popName+"."+prefix+va.getVarName()+" = "+va.getValexp().getRoot()+"\n");
                        }
                }

                for(Component child: comp.getAllChildren()) {
                        String childPre = child.getID()+"_";
                        if (child.getID()==null)
                                childPre = child.getName()+"_";
                        
                        getCompEqns(compInfo, child, popName, stateVars, prefix+childPre);
                }

		return;

        }

}



















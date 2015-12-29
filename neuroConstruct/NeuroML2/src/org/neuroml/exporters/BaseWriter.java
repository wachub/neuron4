package org.neuroml.exporters;


import org.lemsml.expression.ParseError;
import org.lemsml.type.Lems;
import org.lemsml.util.ContentError;

public abstract class BaseWriter {

	Lems lems;
	
	public BaseWriter(Lems l) {
		lems = l;
	}

    protected abstract void addComment(StringBuilder sb, String comment);
        

	public abstract String getMainScript() throws ContentError, ParseError;

    public class CompInfo
    {
            StringBuilder stateVars = new StringBuilder();
            StringBuilder params = new StringBuilder();
            StringBuilder eqns = new StringBuilder();
            StringBuilder initInfo = new StringBuilder();
    }


}



















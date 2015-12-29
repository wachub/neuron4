package org.neuroml.examples;

import java.io.File;

import org.lemsml.sim.Sim;
import org.lemsml.util.E;


public class NML2FileExample {
 
	String filename;
	
	public NML2FileExample(String fnm) {
		filename = fnm;
	}
	
		public void run() {
			File fex = new File("examples");
			File fs = new File(fex, filename);
			
			Sim sim = new Sim(fs);
			
			try {
				sim.readModel();	
		 	
				sim.build();
				
				sim.run();
			 
			
			} catch (Exception ex) {
				E.error(" " + ex);
				ex.printStackTrace();
			}
		}
	    
}

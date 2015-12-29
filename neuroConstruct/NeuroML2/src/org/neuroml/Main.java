/**
 *  libNeuroML for Java
 *
 *  Copyright (c) 2012 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Wellcome Trust
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.getd

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.neuroml;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.lemsml.sim.Sim;
import org.lemsml.util.E;
import org.lemsml.util.FileUtil;
import org.neuroml.exporters.BrianWriter;
import org.neuroml.exporters.ModelicaWriter;
import org.neuroml.exporters.GraphWriter;
import org.neuroml.exporters.MatlabWriter;
import org.neuroml.exporters.NeuronWriter;
import org.neuroml.exporters.NineMLWriter;
import org.neuroml.exporters.SBMLWriter;
import org.neuroml.exporters.SEDMLWriter;
import org.neuroml.exporters.XppWriter;


public class Main {

    public static void main(String[] argv) {

        String usage = "\nThis is the nml2 utility of libNeuroML. This wraps around the LEMS Interpreter (in lib/lems/lems-x.x.x.jar) and is aware of the Component "
            + "definitions of NeuroML v2.0 (in NeuroML2CoreTypes) and adds various import & export features to other model specification formats. See "
            + "http://www.neuroml.org/neuroml2 for more details.\n\n"
            + "Please specify the location of the LEMS based XML file to parse, e.g. \n\n"
                + "    nml2.bat examples/LEMS_NML2_Ex1_HH.xml [-options]    (Windows)\n"
                + "    ./nml2 examples/LEMS_NML2_Ex1_HH.xml [-options]      (Linux/Mac)\n"
                + "\n"
                + "Running nml2 *without any optional arguments* makes the interpreter run a simulation with the LEMS Interpreter of the Component specified in <DefaultRun>\n"
                + "\n"
                + "Optional attributes include:\n"
                + "    -nogui       Run simulation of model in interpreter but suppress any graphical elements (just save results)\n"
                + "    -c           Generate canonical form of model\n"
                + "    -neuron      Generate model in NEURON format (experimental..)\n"
                + "    -graph       Generate model in GraphViz format (experimental)\n"
                + "    -nineml      Generate model in NineML format (tested on Izhikevich)\n"
                + "    -sbml        Generate model in SBML format (tested on Izhikevich)\n"
                + "    -sedml       Generate model in SED-ML format (experimental)\n"
                + "    -brian       Generate model in Brian format (very experimental!)\n"
                + "    -modelica    Generate model in Modelica format (very experimental!)\n"
                + "    -xpp         Generate model in XPP format (very experimental, works with Ex9_FN!)\n";

        if (argv.length == 0) {
            System.out.println(usage);
            System.exit(1);
        }
        File simFile = new File(argv[0]);
        if (!simFile.exists()) {
            simFile = new File("examples/" + argv[0]);
            if (!simFile.exists()) {
                System.out.println("Error, neither " + (new File(argv[0])).getAbsolutePath() + " nor " + simFile.getAbsolutePath() + " found!");
                System.out.println(usage);
                System.exit(1);
            }
        }
        
        E.setDebug(false);

        Sim sim = new Sim(simFile);

        try {
            sim.readModel();

            // sim.print();

            if (argv.length > 1) {
                String opt = argv[1];
                if (opt.equals("-nogui")) {
                    Sim.setDisableFrames(true);
                    sim.build();
                    sim.run();
                    E.info("Finished reading, building & running LEMS model in NO GUI mode");

                } else if (opt.equals("-c")) {
                    String sc = argv[0].replace(".xml", "-ccl.xml");
                    File fout = new File(sc);
                    E.info("Writing out canonical form to: " + fout.getAbsolutePath());
                    FileUtil.writeStringToFile(sim.canonicalText(), fout);


                } else if (opt.equals("-brian")) {
                    sim.build();

                    String sc = argv[0].replace(".xml", "_brian.py");
                    File fout = new File(sc);
                    E.info("Writing out model behaviour to: " + fout.getAbsolutePath());
                    BrianWriter bw = new BrianWriter(sim.getLems());
                    FileUtil.writeStringToFile(bw.getMainScript(), fout);


                } else if (opt.equals("-modelica")) {
                    sim.build();

                    String moFilename = argv[0].replace(".xml", ".mo");
                    File moFile = new File(moFilename);
                    String mosFilename = argv[0].replace(".xml", ".mos");
                    File mosFile = new File(mosFilename);
                    ModelicaWriter mw = new ModelicaWriter(sim.getLems());

                    E.info("Writing out model behaviour to: " + moFile.getAbsolutePath() +" and "+ mosFile.getAbsolutePath());
                    FileUtil.writeStringToFile(mw.getMainScript(), moFile);
                    FileUtil.writeStringToFile(mw.getRunScript(moFilename), mosFile);


                } else if (opt.equals("-xpp")) {
                    sim.build();

                    String sc = argv[0].replace(".xml", ".ode");
                    File fout = new File(sc);
                    E.info("Writing out model behaviour to: " + fout.getAbsolutePath());
                    XppWriter xw = new XppWriter(sim.getLems());
                    FileUtil.writeStringToFile(xw.getMainScript(), fout);


                } else if (opt.equals("-matlab")) {
                    sim.build();

                    String sc = argv[0].replace(".xml", ".m");
                    File fout = new File(sc);
                    E.info("Writing out model behaviour to: " + fout.getAbsolutePath());
                    MatlabWriter mw = new MatlabWriter(sim.getLems());
                    FileUtil.writeStringToFile(mw.getMainScript(), fout);


                } else if (opt.equals("-neuron")) {
                    sim.build();

                    String sc = argv[0].replace(".xml", "_nrn.py");
                    File fout = new File(sc);
                    NeuronWriter nw = new NeuronWriter(sim.getLems());
                    ArrayList<File> genFiles = nw.generateMainScriptAndMods(fout);

                    E.info("Written model behaviour to:");

                    for (File f : genFiles) {
                        E.info(f.getAbsolutePath());
                    }


                } else if (opt.equals("-nineml")) {
                    sim.build();

                    String sc = argv[0].replace(".xml", ".9ml");
                    File fout = new File(sc);
                    NineMLWriter nw = new NineMLWriter(sim.getLems());
                    FileUtil.writeStringToFile(nw.getMainScript(), fout);

                    E.info("Written out model behaviour to: " + fout.getAbsolutePath());


                } else if (opt.equals("-sbml")) {
                    sim.build();

                    String sc = argv[0].replace(".xml", ".sbml");
                    File fout = new File(sc);
                    E.info("Writing out model behaviour to: " + fout.getAbsolutePath());
                    SBMLWriter nw = new SBMLWriter(sim.getLems());
                    FileUtil.writeStringToFile(nw.getMainScript(), fout);

                    E.info("Written out model behaviour in SBML to: " + fout.getAbsolutePath());


                } else if (opt.equals("-sedml")) {
                    sim.build();

                    String sc = argv[0].replace(".xml", ".sedml");
                    File fout = new File(sc);
                    E.info("Writing out model behaviour to: " + fout.getAbsolutePath());
                    SEDMLWriter nw = new SEDMLWriter(sim.getLems(), simFile.getName(), SEDMLWriter.ModelFormat.SBML);
                    FileUtil.writeStringToFile(nw.getMainScript(), fout);

                    E.info("Written out model behaviour in SED-ML to: " + fout.getAbsolutePath());


                } else if (opt.equals("-graph")) {
                    sim.build();

                    String sc = argv[0].replace(".xml", ".gv");
                    File fout = new File(sc);
                    E.info("Writing out model behaviour to: " + fout.getAbsolutePath());
                    GraphWriter gw = new GraphWriter(sim.getLems());

                    FileUtil.writeStringToFile(gw.getMainScript(), fout);

                    String imgFile = fout.getAbsolutePath().replace(".gv", ".png");

                    String cmd = "dot -Tpng  " + fout.getAbsolutePath() + " -o " + imgFile;
                    String[] env = new String[]{};
                    Runtime run = Runtime.getRuntime();
                    Process pr = run.exec(cmd, env, fout.getParentFile());

                    pr.waitFor();

                    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                    String line;
                    while ((line = buf.readLine()) != null) {
                        System.out.println("----" + line);
                    }


                    E.info("Have run command: " + cmd);


                } else {
                    E.info("Error, unrecognized command line element: (" + opt + ")");
                    System.out.println(usage);
                }


            } else {
                sim.build();
                sim.run();
                E.info("Finished reading, building, running & displaying LEMS model");
            }


        } catch (Exception ex) {
            E.error(" " + ex);
            ex.printStackTrace();
        }
    }
}

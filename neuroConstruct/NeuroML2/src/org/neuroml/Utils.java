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
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


package org.neuroml;


public class Utils {

    /*
     * Convert a string of XML in NeuroML2 (i.e. root element <neuroml>) to LEMS
     * i.e. roor element <Lems>
     */
    public static String convertNeuroML2ToLems(String nml2string)
    {
        if (nml2string.startsWith("<?xml")) {
            int index = nml2string.indexOf(">");
            nml2string = nml2string.substring(index + 1).trim();
        }
        
        if (nml2string.startsWith("<neuroml")) {

            int index = nml2string.indexOf(">");
            nml2string = nml2string.substring(index + 1);
            // Assume </neuroml> at end...
            nml2string = nml2string.replace("</neuroml>", "");

            nml2string = "<Lems>\n\n"
                    + "    <Include file=\"NeuroML2CoreTypes/NeuroMLCoreDimensions.xml\"/>\n"
                    + "    <Include file=\"NeuroML2CoreTypes/Cells.xml\"/>\n"
                    + "    <Include file=\"NeuroML2CoreTypes/Networks.xml\"/>\n"
                    + "    <Include file=\"NeuroML2CoreTypes/Simulation.xml\"/>\n\n"
                    + nml2string + "\n"
                    + "</Lems>";


        }
        return nml2string;
    }

}

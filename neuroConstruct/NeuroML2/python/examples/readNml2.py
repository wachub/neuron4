# -*- coding: utf-8 -*-
"""
Initial version of Python API for LEMS
Author: Padraig Gleeson
"""

from libneuroml import *


if __name__ == "__main__":

    print "Reading in NeuroML 1/2 files..."

    reader = NeuroMLReader()

    filename = "../../examples/NMLv1.x/GranuleCell.xml"

    nml1Doc = reader.readNeuroML(filename)

    print "Read in cell in v1.x, Level 2: "+nml1Doc.getCells().getCell()[0].getName()

    filename = "../../examples/NML2_AbstractCells.nml"

    nml2Doc = reader.readNeuroML(filename)

    #print nml2Doc.__class__

    print "Read in cells in v2: "+nml2Doc.getId()

    

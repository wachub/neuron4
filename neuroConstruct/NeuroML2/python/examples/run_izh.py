# -*- coding: utf-8 -*-
"""
Initial version of Python API for LEMS
Author: Padraig Gleeson
"""

from libneuroml.lems import *
from libneuroml.lems.util import *
from libneuroml.neuroml2 import *


if __name__ == "__main__":

    print "Building simulation using Python API for LEMS & NeuroML 2"
    
    comp = Component("izhikevichCell", "burster")
    comp.setAnyAttributes_({"v0":"-70mV", "thresh":"30mV", "a":"0.02", "b":"0.2", \
           "c":"-50.0", "d":"2", "Iamp":"15", "Idel":"22ms", "Idur":"2000ms"})

    lemsDocument = Lems()
    lemsDocument.addComponent(comp)

    net = Component(type_="network",id="Network1")

    pop = Component("population", id="pop1")
    pop.setAnyAttributes_({"component":comp.id, "size":1})

    net.addComponent(pop)

    lemsDocument.addComponent(net)

    genSimWithDefaultPlots(lemsDocument, net.id, 250, 0.01)

    writeLemsAndRun(lemsDocument, "Izh.xml")

    
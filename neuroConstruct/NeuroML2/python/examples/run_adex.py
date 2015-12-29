# -*- coding: utf-8 -*-
"""
Initial version of Python API for LEMS
Author: Padraig Gleeson
"""

from libneuroml.lems import *
from libneuroml.lems.util import *
import sys


if __name__ == "__main__":

    print "Building simulation using Python API for LEMS/NeuroML 2"

    lemsDocument = Lems()

    comp = Component("adExIaFCell", "burster")
    
    comp.setAnyAttributes_({"C":"281pF", "gL":"30nS", \
                     "EL":"-70.6mV", "reset":"-47.2mV", "VT" : "-50.4mV", \
                     "thresh":  "-20.4mV", "delT":"2mV", "tauw":"40ms",  \
                     "a" :"4nS",   "b": "0.08nA", "refract" : "0ms", \
                     "Iamp":"0.8nA", "Idel":"0ms", "Idur":"2000ms"})

    lemsDocument.addComponent(comp)
    
    net = Component(type_="network",id="Network1")

    pop = Component("population", id="pop1")
    pop.setAnyAttributes_({"component":comp.id, "size":1})

    net.addComponent(pop)

    lemsDocument.addComponent(net)

    lemsDocument.export(sys.stdout,0)

    genSimWithDefaultPlots(lemsDocument, net.id, 250, 0.01)

    writeLemsAndRun(lemsDocument, "AdEx.xml")

    
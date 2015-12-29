# -*- coding: utf-8 -*-
"""
Initial version of Python API for LEMS
Author: Padraig Gleeson
"""

from libneuroml.lems import *
from libneuroml.lems.util import *


if __name__ == "__main__":

    print "Building simulation using Python API for LEMS"
 
    lemsDocument = Lems()

    comp1 = Component("adExIaFCell", "adExChaotic")

    comp1.setAnyAttributes_({"C":"281pF", "gL":"30nS", \
                     "EL":"-70.6mV", "reset":"-48mV", "VT" : "-50.4mV", \
                     "thresh":  "-20.4mV", "delT":"2mV", "tauw":"40ms",  \
                     "a" :"4nS",   "b": "0.08nA", "refract" : "0ms", \
                     "Iamp":"0.8nA", "Idel":"0ms", "Idur":"2000ms"})

    lemsDocument.addComponent(comp1)

    comp2 = Component("adExIaFCell", "adExSilent")

    comp2.setAnyAttributes_({"C":"281pF", "gL":"30nS", \
                     "EL":"-70.6mV", "reset":"-48mV", "VT" : "-50.4mV", \
                     "thresh":  "-20.4mV", "delT":"2mV", "tauw":"40ms",  \
                     "a" :"4nS",   "b": "0.08nA", "refract" : "0ms", \
                     "Iamp":"0nA", "Idel":"0ms", "Idur":"2000ms"})
    
    lemsDocument.addComponent(comp2)
    
    syn1 = Component("expOneSynapse", "syn1")
    syn1.setAnyAttributes_({"gbase":"65nS", "erev":"0mV", "tauDecay":"3ms"})
    lemsDocument.addComponent(syn1)
    
    net = Component(type_="network",id="Network1")

    pop1 = Component("population", id="pop1")
    pop1.setAnyAttributes_({"component":comp1.id, "size":1})
    net.addComponent(pop1)
    pop2 = Component("population", id="pop2")
    pop2.setAnyAttributes_({"component":comp2.id, "size":1})
    net.addComponent(pop2)

    
    conn = Component("synapticConnection", id="conn1")
    
    conn.setAnyAttributes_({"from":"pop1[0]", "to":"pop2[0]", "synapse":syn1.id})
    net.addComponent(conn)
    

    lemsDocument.addComponent(net)

    genSimWithDefaultPlots(lemsDocument, net.id, 500, 0.01)

    writeLemsAndRun(lemsDocument, "Net.xml")

    
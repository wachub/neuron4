# -*- coding: utf-8 -*-
"""
Initial version of Python API for LEMS
Author: Padraig Gleeson
"""

import libneuroml as lnml

import libneuroml.neuroml2 as nml2

import libneuroml.lems as lems
import libneuroml.lems.util as lemsutil

import libneuroml.neuroml2.brian as n2brian

from brian import *
import numpy

if __name__ == "__main__":

    print "Reading in NeuroML 2 files..."

    filename = "../../examples/NML2_AbstractCells.nml"

    reader = lnml.NeuroMLReader()
    nml2Doc = reader.readNeuroML(filename)

    print "Read in cells in v2: "+nml2Doc.getId()

    #print nml2Doc.__class__

    

    iaf0 = nml2Doc.getIafCell()[0]
    iafTau0 = nml2Doc.getIafTauCell()[0]
    iz0 = nml2Doc.getIzhikevichCell()[0]

    lemsDoc = lemsutil.readLems("../../NeuroML2CoreTypes/Cells.xml")

    izCompType = lemsutil.getComponentType(lemsDoc, iz0.__class__.__name__)
    iafCompType = lemsutil.getComponentType(lemsDoc, iaf0.__class__.__name__)
    iafTauCompType = lemsutil.getComponentType(lemsDoc, iafTau0.__class__.__name__)


    eqnsIaf = n2brian.toBrianEqnsResetThresh(lemsDoc, iaf0, iafCompType)
    eqnsIafTau = n2brian.toBrianEqnsResetThresh(lemsDoc, iafTau0, iafTauCompType)
    eqnsIz = n2brian.toBrianEqnsResetThresh(lemsDoc, iz0, izCompType)

    print eqnsIafTau[0]
    print eqnsIafTau[1]
    print eqnsIafTau[2]


    Iaf = NeuronGroup(1, model=eqnsIaf[0],reset=eqnsIaf[1],threshold=eqnsIaf[2])
    IafTau = NeuronGroup(1, model=eqnsIafTau[0],reset=eqnsIafTau[1],threshold=eqnsIafTau[2])
    Iz = NeuronGroup(1, model=eqnsIz[0],reset=eqnsIz[1],threshold=eqnsIz[2])


    for k in eqnsIaf[3].keys():
        setattr(Iaf, k, eqnsIaf[3][k])
    for k in eqnsIafTau[3].keys():
        setattr(IafTau, k, eqnsIafTau[3][k])
    for k in eqnsIz[3].keys():
        setattr(Iz, k, eqnsIz[3][k])

    for i in eqnsIz[4].keys():
        expr = eqnsIz[4][i]
        #paramsUsed = expr.split
        for j in eqnsIz[3].keys():
            temp = str(eqnsIz[3][j])
            if " " in temp:
                temp = temp.replace(" ", " * ")
            expr = expr.replace(j, "(%s)"%temp)
        if i == "I": expr = "15" # TODO remove...
        print "Setting %s to %s"%(i,expr)
        setattr(Iz, i, eval(expr))

    MvIaf = StateMonitor(Iaf, 'v', record=True)
    MvIafTau = StateMonitor(IafTau, 'v', record=True)
    MvIz = StateMonitor(Iz, 'v', record=True)
    MvIzU = StateMonitor(Iz, 'U', record=True)

    defaultclock.dt = 0.025*ms
    run(300*ms)
    plot(MvIaf.times/ms, MvIaf[0]/mV)
    plot(MvIafTau.times/ms, MvIafTau[0]/mV)
    plot(MvIz.times/ms, MvIz[0]/mV)
    plot(MvIzU.times/ms, MvIzU[0])

    show()


    

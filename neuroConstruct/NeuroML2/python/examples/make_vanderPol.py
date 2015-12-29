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

    # Define ComponentType based on http://www.scholarpedia.org/article/Van_der_Pol_oscillator
    compType = ComponentType(name="vanderPolOscillator")

    compType.addParameter(Parameter(name="epsilon"))
    compType.addExposure(Parameter(name="x"))
    compType.addExposure(Parameter(name="y"))

    # To ensure correct units
    compType.addConstant(Constant(name="SEC", dimension="time", value="1s"))

    behavior = Behavior()
    behavior.addStateVariable(StateVariable(name="x", exposure="x"))
    behavior.addStateVariable(StateVariable(name="y", exposure="y"))

    os = OnStart()
    os.addStateAssignment(StateAssignment("x", "0.5"))

    behavior.setOnStart(os)

    behavior.addTimeDerivative(TimeDerivative("x", "epsilon * (x - (x^3)/3 -y) / SEC"))
    behavior.addTimeDerivative(TimeDerivative("y", "x/(epsilon * SEC)"))
    compType.addBehavior(behavior)

    lemsDocument.addComponentType(compType)

    # Add instance of ComponentType
    comp = Component(compType.name, "dampedOscillator")
    comp.setAnyAttributes_({"epsilon":"0.1"})
    lemsDocument.addComponent(comp)

    
    genSimWithDefaultPlots(lemsDocument, comp.id, 100000, 1) # 100 seconds

    writeLemsAndRun(lemsDocument, "vanderPol.xml")

    
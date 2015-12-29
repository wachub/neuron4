#
#
#   File to test current configuration of HH project.
#
#   To execute this type of file, type '..\..\..\nC.bat -python XXX.py' (Windows)
#   or '../../../nC.sh -python XXX.py' (Linux/Mac). Note: you may have to update the
#   NC_HOME and NC_MAX_MEMORY variables in nC.bat/nC.sh
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#

import sys
import os

try:
    from java.io import File
except ImportError:
    print "Note: this file should be run using ..\\..\\..\\nC.bat -python XXX.py' or '../../../nC.sh -python XXX.py'"
    print "See http://www.neuroconstruct.org/docs/python.html for more details"
    quit()

sys.path.append(os.environ["NC_HOME"]+"/pythonNeuroML/nCUtils")

import ncutils as nc

projFile = File(os.getcwd(), "../Ex4_HHcell.ncx")

print "Project file for this test: "+ projFile.getAbsolutePath()


##############  Main settings  ##################

simConfigs = []

simConfigs.append("Default Simulation Configuration")

simDt =                 0.001

simulators =            ["NEURON", "GENESIS_PHYS", "GENESIS_SI", "MOOSE_PHYS", "MOOSE_SI"]
#simulators =            ["NEURON", "LEMS"]


numConcurrentSims =     4

varTimestepNeuron =     False

plotSims =              True
plotVoltageOnly =       True
analyseSims =           True
runInBackground =       True
verbose =               False

#############################################


def testAll(argv=None):
    if argv is None:
        argv = sys.argv

    print "Loading project from "+ projFile.getCanonicalPath()


    simManager = nc.SimulationManager(projFile,
                                      numConcurrentSims,
                                      verbose)

    simManager.runMultipleSims(simConfigs =      simConfigs,
                               simDt =           simDt,
                               simulators =      simulators,
                               runInBackground = runInBackground)
    

    simManager.reloadSims(plotVoltageOnly =   plotVoltageOnly,
                          plotSims =          plotSims,
                          analyseSims =       analyseSims)


    # These were discovered using analyseSims = True above.
    # They need to hold for all simulators
    spikeTimesToCheck = {'CG1_0' : [21.13, 32.47, 43.28, 54.05, 64.82, 75.58],
                         'CG2_0' : [27.06, 38.52, 49.36, 60.13, 70.9, 81.67] }

    spikeTimeAccuracy = 0.01

    report = simManager.checkSims(spikeTimesToCheck = spikeTimesToCheck,
                                  spikeTimeAccuracy = spikeTimeAccuracy)

    print report

    return report


if __name__ == "__main__":
    testAll()

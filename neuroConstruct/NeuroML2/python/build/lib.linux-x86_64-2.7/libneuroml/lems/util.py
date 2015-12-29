# -*- coding: utf-8 -*-
"""

Initial version of Python API for LEMS

Thanks to Andrew Davison & Eilif Muller's scripts for interacting with 9ML for ideas for use of lxml

Author: Padraig Gleeson

"""

import os
import sys
import random

from libneuroml.lems import *

LEMS_EL = "Lems"
DEFAULT_RUN="DefaultRun"
SIMULATION="Simulation"
DISPLAY="Display"
LINE="Line"


NETWORK="network"
POPULATION="population"

def status():
	print "NeuroML 2/LEMS successfully installed!"


def getNextHexColor():
    return "#"+str(random.randint(0,100))+str(random.randint(0,100))+str(random.randint(0,100))

def getComponent(lemsDoc, id):
    compFound = None
    for comp in lemsDoc.getComponent():
        if comp.getId() == id:
            compFound = comp
    return compFound

def getComponentType(lemsDoc, name):
    compTypeFound = None
    for compType in lemsDoc.getComponentType():
        print "Checking %s against %s"%(compType.getName(), name)
        if compType.getName().capitalize() == name.capitalize():
            compTypeFound = compType
    return compTypeFound

nml2ComponentTypes = {}

def loadNml2ComponentTypes():
    print "???"


def readLems(lemsFile, includedAlready=[], incIncludes=True):

    srcDir = os.path.dirname(lemsFile)+"/"

    from libneuroml.lems import parse as lemsparse
    lemsDoc = lemsparse(lemsFile)

    print "All includes..."
    for inc in lemsDoc.getInclude():
        if incIncludes and inc.getFile() not in includedAlready:
            print "Including: "+ inc.getFile()
            incFilename = srcDir+inc.getFile()
            incDoc = readLems(incFilename, includedAlready)

            for dim in incDoc.getDimension():
                lemsDoc.addDimension(dim)
            for cc in incDoc.getComponentType():
                print cc.getName()

            includedAlready.append(inc.getFile())
            print "Included all from "+incFilename

    return lemsDoc
    

def genSimWithDefaultPlots(lemsDoc, target_id, dur, dt):

    lemsDoc.addInclude(Include("NeuroML2CoreTypes/Simulation.xml"))
    lemsDoc.addInclude(Include("NeuroML2CoreTypes/Cells.xml"))
    lemsDoc.addInclude(Include("NeuroML2CoreTypes/Networks.xml"))

    simulation = Component(SIMULATION, "Sim_"+target_id,)
    args = {"length":"%fms"%dur, "step":"%fms"%dt, "target":target_id}
    simulation.setAnyAttributes_(args)

    lemsDoc.addComponent(simulation)
    lemsDoc.setDefaultRun(DefaultRun(simulation.getId()))

    
    comp = getComponent(lemsDoc, target_id)
    print "Adding displays for "+comp.getId()

    if comp is not None and comp.getType() != NETWORK:
        display = Component(DISPLAY, "disp_"+target_id)
        display.setAnyAttributes_({"title": "Plot: "+target_id, "timeScale":"1s"})
        simulation.addComponent(display)

        compType = getComponentType(lemsDoc, comp.getType())
        stateVars = compType.getBehavior()[0].getStateVariable()

        for sv in stateVars:
             id = "line_"+target_id+"_"+sv.getName()
             line = Component(LINE, id)
             line.setAnyAttributes_({"quantity": sv.name, "color":getNextHexColor(), "scale":1, "save":id+".dat"})
             display.addComponent(line)
    else:
        netComp = comp

        for pop in netComp.getComponent():

            if pop.getType() == POPULATION:
                display = Component(DISPLAY, "disp_"+pop.id)
                display.setAnyAttributes_({"title": "Plot: "+pop.id, "timeScale":"1s"})

                size = int(pop.anyAttributes_["size"])
                compInPop = pop.anyAttributes_["component"]

                for i in range(size):

                    print "Adding displays for cell %i in %s, component %s"%(i,pop.getId(), compInPop)
                    try:
                        compType = getComponentType(lemsDoc, compInPop)
                        stateVars = compType.getBehavior()[0].getStateVariable()
                        for sv in stateVars:
                            id = "line_"+pop.id+"_"+str(i)+"_"+sv.name
                            line = Component(LINE, id)
                            line.setAnyAttributes_({"quantity": "%s[%i]/%s"%(pop.id, i, sv.name), "color":getNextHexColor(), "scale":1, "save":id+".dat"})
                            display.addComponent(line)
                    except Exception:
                        #  e.g. if using NeuroML 2 ComponentType...
                        id = "line_"+pop.id+"_"+str(i)+"_v"
                        line = Component(LINE, id)
                        line.setAnyAttributes_({"quantity": "%s[%i]/%s"%(pop.id, i, "v"), "color":getNextHexColor(), "scale":1, "save":id+".dat"})
                        display.addComponent(line)


                simulation.addComponent(display)
    

'''
  def read_9ml(self, components_9ml, model):
      print "Reading elements from 9ML: "+model.name

      self.description = model.name

      for comp9 in components_9ml:
          print "  Adding component of type: %s"%comp9.name
          componentType = ComponentType(comp9.name)
          componentType.read_9ml(comp9)
          self.componentTypes[componentType.name] = componentType

      for component_name9 in model.components.keys():
          print "  Adding component_name9 %s in the LEMS object model"%component_name9
          component9 = model.components[component_name9]
          comp = Component(component9.definition.url, component9.name)

          for param_name9 in component9.parameters.keys():
              param9 = component9.parameters[param_name9]
              comp.parameterValues[param9.name] = str(param9.value)

          self.components[comp.id] = comp

      for group9 in model.groups.keys():
          print "  Adding group %s as a network in the LEMS object model"%group9
          network = Network(group9)
          self.networks[network.id] = network
          
          for pop9 in model.groups[group9].populations.values():
              print "  Adding population %s as a population in the LEMS object model"%pop9
              population = Population(pop9.name, pop9.prototype.name, pop9.number)
              network.addPopulation(population)
              
      print "Added all elements from 9ML..."

'''
def writeLemsAndRun(lemsDocument, lemsFile):

    newfile = open(lemsFile,"w")
    lemsDocument.export(newfile, 0,namespacedef_='xmlns="http://www.neuroml.org/lems/0.4" \
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
        xsi:schemaLocation="http://www.neuroml.org/lems/0.4 ../../../neuroConstruct/NeuroML2/Schemas/LEMS/LEMS_v0.4.xsd"')

    newfile.close()
    runLemsFile(lemsFile)
'''
def write(self, lems_file):
   
    etree.ElementTree(self.to_xml()).write(lems_file, encoding="UTF-8",
                                     pretty_print=True, xml_declaration=True)

    print "Saved file to %s"%lems_file'''

def runLemsFile(lemsFile):
          command = "nml2 "+lemsFile
          print "Trying to run LEMS file using command: "+command

          ret = os.system(command)

          if ret is not 0:
              print "\n    There was a problem running the command:\n        %s\n"%command
              print "    The file %s has been generated. To execute this file, please ensure that libNeuroML is correctly installed,"%lemsFile
              print "    that it is compiled (use make.sh/make.bat or ant), that the NML2_HOME environment variable points to its "
              print "    installation directory and that this directory is present in PATH, to find the nml2 script.\n"
          


def test_9ml_al(test_ref, AL_file_ref, instance_name, params, dur, dt, nineml_src_dir=None):

    import nineml.user_layer as UL

    file_name = test_ref+".xml"
    '''file_al_9ml = test_ref+"_AL.9ml"'''
    file_ul_9ml = test_ref+"_UL.9ml"
    
    print "Testing LEMS export..."

    if nineml_src_dir is not None:
        al_def_dir = nineml_src_dir+"/lib9ml/python/nineml/examples/AL"
        print "Appending %s to path"%al_def_dir
        sys.path.append(al_def_dir)

    exec("from %s import *" % AL_file_ref)

    my_cell_comp = c1
    ''' To handle Abigail's models...'''
    if my_cell_comp.name.lower() != AL_file_ref.lower():
        print my_cell_comp.name +" is not "+AL_file_ref+"..."
        try:
            my_cell_comp = leaky_iaf
        except NameError:
            print "Going with the flow..."

    print "Loaded abstraction layer definition: %s from file %s/%s.py" % (my_cell_comp.name, al_def_dir, AL_file_ref)

    components_9ml = []
    components_9ml.append(my_cell_comp)

    catalog = "../../catalog/"
    network = UL.Group("Network1")
    model = UL.Model("Simple 9ML example model (based on %s AL definition) to run on LEMS"%AL_file_ref)
    model.add_group(network)

    al_definition_name = my_cell_comp.name

    comp_instance = UL.SpikingNodeType(instance_name, al_definition_name, params)

    model.addComponent(comp_instance)

    unstructured = UL.Structure("Unstructured", catalog + "networkstructures/Unstructured.xml")

    cellPop = UL.Population("CellsA", 1, comp_instance, UL.PositionList(structure=unstructured))

    network.add(cellPop)

    model.write(file_ul_9ml)
    
    lems = Lems()

    lems.read_9ml(components_9ml, model)

    lems.genSimWithDefaultPlots(lems.networks.values()[0].id, 200, 0.01)

    lems.writeLemsAndRun(file_name)
    

if __name__ == "__main__":

    import nineml.user_layer as UL
    
    print "Running main test on lems.py"

    params = UL.ParameterSet(a =(0.02, ""), b=(0.2, ""), c=(-50, ""), d=(2, ""), theta=(30, ""), Isyn=(15, ""))

    instance_name = "BurstingIzh"

    test_9ml_al("TestLEMS", "izhikevich", instance_name, params, 200, 0.01)
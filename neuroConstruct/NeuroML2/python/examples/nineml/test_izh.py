# -*- coding: utf-8 -*-
"""

Initial version of converter 9ML -> LEMS
Can be used to convert AL models in 9ml to LEMS to be executed with the LEMS interpreter

Based on scripts from Andrew Davison & Eilif Muller's libnineml

Author: Padraig Gleeson

"""


from neuroml2 import *

import nineml.user_layer as UL


if __name__ == "__main__":

    nineml_src_dir = "/home/padraig/nineml/trunk"

    al_ref = "izhikevich"

    print "Running test on lems.py with AL created with %s.py"%al_ref

    params = UL.ParameterSet(theta = (30,""), a =(0.02,""), b = (0.2,""), c = (-50.0,""), d = (2,""), Isyn=(15,""))

    instance_name = "Izh1"

    lems.test_9ml_al("TestIzh", al_ref, instance_name, params, 400, 0.01, nineml_src_dir=nineml_src_dir)
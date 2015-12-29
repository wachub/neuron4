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

    al_ref = "gLIFid1"

    print "Running test on lems.py with AL created with %s.py"%al_ref

    params = UL.ParameterSet(Vrest = (-70,""), tau_m=(30,""), Vth=(-50,""), Isyn=(25, ""))

    instance_name = "IaF1"

    lems.test_9ml_al("TestIaF", al_ref, instance_name, params, 400, 0.01, nineml_src_dir=nineml_src_dir)
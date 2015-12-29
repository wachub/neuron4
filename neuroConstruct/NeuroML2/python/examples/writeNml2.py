# -*- coding: utf-8 -*-
"""
Initial version of Python API for LEMS
Author: Padraig Gleeson
"""

from libneuroml.neuroml2 import *
from libneuroml.neuroml2.NeuroMLDocument import *


if __name__ == "__main__":

    #####################################################

    print "Writing NeuroML 2 files..."

    nmlDoc = NeuroMLDocument(id="IzhNet")

    izhCell = IzhikevichCell(id="izBurst", v0 = "-70mV", thresh = "30mV", a ="0.02", b = "0.2", c = "-50.0", d = "2", Iamp="15", Idel="22ms", Idur="2000ms")
    nmlDoc.addIzhikevichCell(izhCell)

    newnmlfile = "testNml2.xml"
    nmlDoc.writeNeuroML(newnmlfile)


    ############################################################




    from lxml import etree
    from urllib import urlopen

    schema_file = urlopen("http://neuroml.svn.sourceforge.net/viewvc/neuroml/NeuroML2/Schemas/NeuroML2/NeuroML_v2alpha.xsd")
    xmlschema_doc = etree.parse(schema_file)
    xmlschema = etree.XMLSchema(xmlschema_doc)

    print "Validating %s against %s" %(newnmlfile, schema_file.geturl())

    doc = etree.parse(newnmlfile)
    xmlschema.assertValid(doc)
    print "It's valid!"
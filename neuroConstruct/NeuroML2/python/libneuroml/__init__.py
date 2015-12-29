# -*- coding: utf-8 -*-
"""

Initial version of Python API for LEMS

Author: Padraig Gleeson

"""

import xml
import xml.sax

__version__ = "0.2"


class NeuroMLReader:

    def readNeuroML(self, filename):

        '''nmlFile = open()'''
        import neuroml2
        import neuroml1x
        import neuroml1x.level2
        
        try:
            nml2Doc = neuroml2.parse(filename)
            print "Read in NeuroML 2 doc with id: %s"%nml2Doc.getId()

            if nml2Doc.getId() is None:
                nml1Doc = neuroml1x.level2.parse(filename)
                print "Read in NeuroML 1 doc: %s"%nml1Doc
                return nml1Doc

            return nml2Doc
        except Exception:
            print "Not a valid NeuroML 2 doc!"
            return None


        '''
        from neuroml1x.NeuroMLHolder import *
        from neuroml1x.NeuroMLSaxHandler import *
        print "Reading in: "+filename
        parser = xml.sax.make_parser()   # A parser for any XML file

        nmlHolder = NeuroMLHolder()	# Stores the structure of NML file

        currHandler = NeuroMLSaxHandler(nmlHolder) # The SAX handler knows of the structure of NetworkML and calls appropriate functions in NetworkHolder

        parser.setContentHandler(currHandler) # Tells the parser to invoke the NetworkMLSaxHandler when elements, characters etc. parsed

        parser.parse(open(filename)) # The parser opens the file and ultimately the appropriate functions in NetworkHolder get called

        return nmlHolder.nmlDoc'''
        

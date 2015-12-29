#!/usr/bin/env python
# -*- coding: utf-8 -*-

from distutils.core import setup
from libneuroml.__init__ import __version__
      
setup(
    name = "libNeuroML",
    version = __version__,
    packages = ['libneuroml', 'libneuroml.lems', 'libneuroml.neuroml1x', 'libneuroml.neuroml2', 'libneuroml.neuroml2.brian'],
    author = "Padraig Gleeson, Robert Cannon",
    author_email = "p.gleeson@ucl.ac.uk",
    description = "Python API for NeuroML 2/LEMS with some support for NeuroML v1.8.1",
    license = "GPLv3",
    keywords = "computational neuroscience XML",
    url = "http://www.neuroml.org/neuroml2",
    classifiers = ['Development Status :: 3 - Alpha',
                   'Environment :: Console',
                   'Intended Audience :: Science/Research',
                   'License :: OSI Approved :: BSD License',
                   'Natural Language :: English',
                   'Operating System :: OS Independent',
                   'Programming Language :: Python :: 2',
                   'Topic :: Scientific/Engineering'],
    requires = ['lxml'],
)



set LIB_NEUROML_VERSION=2.0.0
set LEMS_VERSION=0.8.3

set CLASSPATH=.;libNeuroML-%LIB_NEUROML_VERSION%.jar;lib/lems/lems-%LEMS_VERSION%.jar;%NML2_HOME%/libNeuroML-%LIB_NEUROML_VERSION%.jar;%NML2_HOME%/lib/lems/lems-%LEMS_VERSION%.jar

echo Running the NeuroML 2/LEMS utility...

java -classpath %CLASSPATH% org.neuroml.Main %1 %2 %3 %4

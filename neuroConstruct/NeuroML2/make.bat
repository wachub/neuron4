set LIB_NEUROML_VERSION=2.0.0
set LEMS_VERSION=0.8.2

set CLASSPATH=.;libNeuroML-%LIB_NEUROML_VERSION%.jar;lib/lems/lems-%LEMS_VERSION%.jar;lib/jsbml/jsbml-0.8-b1-with-dependencies.jar;lib/cellmlapi/cellml.jar

echo Building the libNeuroML application from source...
mkdir build

javac -sourcepath src -d build -classpath %CLASSPATH% src\org\neuroml\*.java src\org\neuroml\importers\*.java
jar -cvfe libNeuroML-%LIB_NEUROML_VERSION%.jar org.neuroml.Main -C build .
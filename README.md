# neuron4


Major Project: neuroConstruct
=========================


> Just a few important points for our project.
 > - Mostly relate to the code base and where the code that matters lies.
 > - The relationship between the classes are specified in the (kind of) UML diagram I've tried to put together towards the end. 



Few Important Points
-------------

StackEdit stores your documents in your browser, which means all your documents are automatically saved locally and are accessible **offline!**

> **Note:**

> - There are basically 3 folders we need to take care of in */neuroConstruct/src/ucl/physiol/neuroconstruct*
>> - *gui*
>> - *project*
>> - *j3d*

> - Files that matter in */neuroConstruct/src/ucl/physiol/neuroconstruct/gui*
>> - MainFrame.java
>> - RegionInfo.java

> - Files that matter in */neuroConstruct/src/ucl/physiol/neuroconstruct/project*
>> - Region.java
>> - RegionTypeHelper.java
>> - IrregularCylindricalRegion.java
>>> Refer to SphericalRegion.java and RectangularBox.java for details of how the Region class is extended and a new region is implemented.

> - Files that matter in  */neuroConstruct/src/ucl/physiol/neuroconstruct/j3d*
> > - Utils.3djava




####  Installation

> * Remove the first 3 folders leaving just the 3 archives and the install file.
> * Run  sh install.sh  

>> You may try just copypasting the thing directly without installing, and it may work too....

Good news: Once you have installed it in the system, just copy paste the neuron4 folder and they can effectively serve as multiple builds

####  Testing Java3d functions

> * If you can install Java3d on your system and follow some online tutorial for the same, skip this part
> * Else just run your java3d programs from neuron4/neuroConstruct and use the j3dtest.sh script to compile... For example if your J3d program is **graphics.java** the shell script **sh j3dtest.sh graphics** will compile and run the program.


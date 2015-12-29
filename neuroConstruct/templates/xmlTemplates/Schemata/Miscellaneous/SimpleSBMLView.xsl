<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:sbml="http://www.sbml.org/sbml/level2/version2">

<!--

    This is a simple XSL file for providing a quick view of the contents of an SBML file
    
    Funding for this work has been received from the Medical Research Council and the 
    Wellcome Trust. This file was initially developed as part of the neuroConstruct project
    
    Author: Padraig Gleeson
    Copyright 2010 University College London
    
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
-->

<xsl:output method="html" indent="yes" />

<!--Main template-->

<xsl:template match="/sbml:sbml">
<h3>SBML file</h3>


<p>Model: <b><xsl:value-of select="sbml:model/@id"/></b></p>

<p><xsl:value-of select="sbml:model/sbml:notes"/></p>

<xsl:apply-templates  select="sbml:model/sbml:listOfCompartments"/>
<xsl:apply-templates  select="sbml:model/sbml:listOfSpecies"/>
<xsl:apply-templates  select="sbml:model/sbml:listOfParameters"/>

</xsl:template>
<!--End Main template-->



<xsl:template match="sbml:listOfCompartments">

<p>Compartments: <b><xsl:value-of select="count(sbml:compartment)"/></b></p>
<table border="1">
    <tr><th width="100">Name</th><th width="300">Size</th></tr>
    <xsl:for-each select='sbml:compartment'>
        <tr><td><xsl:value-of select="@id"/></td><td><xsl:value-of select="@size"/></td></tr>
    </xsl:for-each>
</table>
<br/>
</xsl:template>

<xsl:template match="sbml:listOfSpecies">

<p>Species: <b><xsl:value-of select="count(sbml:species)"/></b></p>
<table border="1">
    <tr>
        <th width="100">Id</th>
        <th width="100">Name</th>
        <th width="100">Compartment</th>
        <th width="100">Initial amount</th>
    </tr>
    <xsl:for-each select='sbml:species'>
        <tr>
            <td><xsl:value-of select="@id"/></td>
            <td><xsl:value-of select="@name"/></td>
            <td><xsl:value-of select="@compartment"/></td>
            <td><xsl:value-of select="@initialAmount"/></td>
        </tr>
    </xsl:for-each>
</table>
<br/>
</xsl:template>


<xsl:template match="sbml:listOfParameters">

<p>Parameters: <b><xsl:value-of select="count(sbml:parameter)"/></b></p>
<table border="1">
    <tr>
        <th width="100">Id</th>
        <th width="100">Value</th>
        <th width="100">Constant?</th>
    </tr>
    <xsl:for-each select='sbml:parameter'>
        <tr>
            <td><xsl:value-of select="@id"/></td>
            <td><xsl:value-of select="@value"/></td>
            <td><xsl:value-of select="@constant"/></td>
        </tr>
    </xsl:for-each>
</table>
<br/>
</xsl:template>


</xsl:stylesheet>

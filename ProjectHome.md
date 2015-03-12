Configurable GUI to execute Ant build files

![http://jantrunner.googlecode.com/svn/wiki/jantrunner-ubuntu.png](http://jantrunner.googlecode.com/svn/wiki/jantrunner-ubuntu.png)

Example of a configuration file:
```
<?xml version="1.0" encoding="UTF-8"?>
<config>
<options logger="org.apache.tools.ant.listener.ProfileLogger" />
<timer interval="20" execute="js.timer" file="javascript.xml" />
<buttons>
  <button name="#1" execute="js.simpletest1" file="javascript.xml" description="executes js.simpletest1"/>
  <button name="#2" execute="js.simpletest2" file="javascript.xml" description="executes js.simpletest1"/>
  <button name="fail" execute="js.fails.after.5seconds" file="javascript.xml" description="executes js.fails.after.5seconds"/>
</buttons>
<tabs>
 <tab name="JS" type="TaskList" file="javascript.xml" />
 <tab name="TextField" type="" source="AntTaskField.bsh" file="taskfield.xml" />
 <tab file="javascript.xml" />
 <tab name="Tree" type="TreeList" file="javascript.xml" />
</tabs>
</config>

```

simple GUI config (for properties) with XUL see [examples/properties.xul](http://code.google.com/p/jantrunner/source/browse/trunk/examples/properties.xul) <br />
[ToDo](ToDo.md)


---


Other Ant runner projects:
  * AntRunner http://antrunner.sourceforge.net/
  * AntRunner (Java 1.4 or later) http://www.varnernet.com/~bryan/software/java/antrunner/
  * AntRunner - a simple GUI for running Ant (.NET Framework 3.5) http://visualdrugs.net/antrunner/
  * Ant's Nest ? http://antsnest.sourceforge.net/
  * Antelope 3.5.1 (January 9, 2009) - A graphical user interface for Ant http://antelope.tigris.org/
  * Virtual Ant - http://placidsystems.com/virtualant/

other nice Ant projects:
  * AntForm - Form-based interaction in Ant scripts http://antforms.sourceforge.net/
  * Antigen - Ant Installer GENerator http://antigen.sourceforge.net/
  * Grand: Graphical Representation of ANt Dependencies http://www.ggtools.net/grand/
  * Vizant - Ant task to visualize buildfile http://vizant.sourceforge.net/
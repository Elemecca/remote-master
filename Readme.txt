The command line parameters to RemoteMaster have been changed
to make it easier to use file associations.  This will allow
users to simply double-click on a .rmdu file to launch RemoteMaster
and edit the device upgrade.

The new parameters are:

-h (home directory) - the directory containing protocols.ini,
                      RemoteMaster.properties, the rdf and
                      Upgrades directories.  It typically also
                      includes RemoteMaster.jar.  If not specified, it
                      defaults to the working directory.

-p (properties file) - the properties files to be used.  Defaults to
                       RemoteMaster.properties in the home directory.

(device upgrade file) - the device upgrade file to load.

System Requirements
-------------------

You need either the Java SDK (for coding/testing/using) or
JRE (for only testing/using) version 1.4.1 or later.
If you don't have either, get one from the Java Download page.


Installating RemoteMaster
-------------------------

To install, follow these steps:
1. Create an empty directory (e.g. c:\rmaster),
2. Unzip to the directory you created above, making sure to
   replace all files, and preserving directory structure.

File Associations
-----------------

In order to simplify the process of setting up File Associations in
Windows, the Setup.jar file is now included with RemoteMaster.
Setting up the file associations is a two step process.
1. Double-click on Setup.jar
2. Double-click on Setup.reg

Now you should be able to simply double-click any .rmdu or .km file to
open that Remote Master Device Upgrade file in RemoteMaster.

Running RemoteMaster
--------------------

To run, just double-click RemoteMaster.jar.

From the command line, type

java -jar RemoteMaster.jar

Or, you can simply type

rmaster

If you see some unexpected results, close RemoteMaster and look in
rmaster.err for some diagnostic messages about the error. These
should help me find the bug. By unexpected result, I mean the GUI
crashing or going haywire or becoming unresponsive.

Importing KM Files
------------------
RemoteMaster now has the ability to import many existing KM upgrade files.
In the upgrade process, all function definition will be imported.
Some button assignments may be lost during the import process.
You can reassign the functions to the desired buttons on the Button panel 
or the Layout panel.

RDF Files
---------

RemoteMaster comes bundled with a number of RDF files.  These RDF files
NOT the same as the RDFs that come bundled with IR, or are distributed with 
some extenders.  Most likely these other RDFs are not compatible with RemoteMaster,
but they can be made compatible with some minor editing.  For help with that, please
make a post in the jp1-km Yahoo Group.

Protocols
---------
RemoteMaster does not yet support all the protocols supported by KM.
Here is a list of the protocols that SHOULD work.  If you find a
problem with one of these, please open a Bug in SourceForge against
the controlremote project.

Aiwa
Aiwa
Aiwa2
Aiwa Combo
Apex 1100W
B&K
Blaupunkt
Boston Accoustics
Boston Official
Denon
Denon Combo (Official)
Denon Combo
Dishplayer
Dishplayer (old)
Dishplayer (old)
Dishplayer (old)
Emerson
F12
GI 4DTV Hacked
GI 4DTV Official
GI Cable
HK Combo
Jerrold
JVC
JVC-48
Kaseikyo
Mitsubishi
NEC1
NEC2
NEC Combo
NEC1 Combo
Nokia Quad
Pace
Panasonic (old)
Panasonic
Panasonic VCR Combo
Panasonic Combo2
Panasonic Combo2
Panasonic Combo (flawed)
Panasonic2 (LCD)
Pioneer
Pioneer DVD
Proton
RC-5
RC-5x
RC-6
RCA
RCA (Old)
RCA Combo (Official)
RECS80 (45)
RECS80 (68)
ReplayTV (Official)
Sharp
Sharp DVD
Sony 12/15
Sony20
Sony Combo (old)
Sony Combo (new)
Sony DSP
Thomson
TiVo Hacked
TiVo Official
X10

Here is a list of the protocols that are not yet supported:

Device Combiner
Echostar 2200
NEC1 2DEV Combo
Pioneer DVD2
Pioneer MIX
Pioneer 2CMD
Pioneer 3DEV
RC-5/5x Combo
RCA Combo (w/Duration)
ReplayTV (Simple)
ReplayTV (Advanced)
Zenith

Command Line Parameters
-----------------------

Most users will never need to know the command-line parameters, but they are
documented here for completeness

The parameters are:

-h (home directory) - the directory containing protocols.ini,
                      RemoteMaster.properties, the rdf, images, and
                      Upgrades directories.  It typically also
                      includes RemoteMaster.jar.  If not specified, it
                      defaults to the working directory.

-p (properties file) - the properties files to be used.  Defaults to
                       RemoteMaster.properties in the home directory.

(device upgrade file) - the optional device upgrade file to load.

System Requirements
-------------------

You need either the Java SDK (for coding/testing/using) or
JRE (for only testing/using) version 1.4.1 or later.
If you don't have either, get one from the Java Download page
at http://java.sun.com/j2se/downloads.html


Installing RemoteMaster
-----------------------

To install, follow these steps:
1. Create an empty directory (e.g. c:\rmaster),
2. Unzip to the directory you created above, making sure to
   replace all files, and preserving directory structure.

RDF Files
---------

RemoteMaster does not ship with any RDF files, but they are
required. They are available as a seperate download from the
Tools folder in the Files section of the JP1 group at Yahoo! Groups.
( http://groups.yahoo.com/group/jp1/files/2.%20Tools/ )
These can be installed in any directory. If RemoteMaster can't
find them the first time you run it, it will prompt you for their
location. It will remember that location for future use.

File Associations
-----------------

In order to simplify the process of setting up File Associations in
Windows, the Setup.jar file is now included with RemoteMaster.
Set up the file associations as follows:

1. Double-click on Setup.jar
2. Double-click on Setup.reg
3. Click Yes.
4. Click OK.

Now you should be able to simply double-click any .rmdu or .km file to
open that Remote Master Device Upgrade file in RemoteMaster.

Running RemoteMaster
--------------------

There are a number of different ways to run RemoteMaster:

- Double-click in an existing .rmdu or .km file
- Double-click RemoteMaster.jar.
- Double-click rmaster.bat
- From the command line, type

  rmaster

Unexpected Results
------------------

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
Echostar 2200
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
NEC1 2DEV Combo
NEC1 2DEV Combo
NECx1
NECx2
Nokia Quad
Pace
Panasonic (old)
Panasonic
Panasonic VCR Combo
Panasonic VCR Combo
Panasonic Mixed Combo
Panasonic Combo2
Panasonic Combo2
Panasonic Combo (flawed)
Panasonic2 (LCD)
Pioneer
Pioneer DVD
Pioneer DVD2
Pioneer MIX
Pioneer 2CMD
Pioneer 3DEV
Proton
RC-5
RC-5x
RC-6
RC6-M-20n
RC6-M-20n
RC6-M-24n
RCA
RCA (Old)
RCA Combo (Official)
RECS80 (45)
RECS80 (68)
ReplayTV (Simple)
ReplayTV (Simple)
ReplayTV (Simple)
ReplayTV (Advanced)
ReplayTV (Advanced)
ReplayTV (Official)
Sharp
Sharp DVD
Sony 12/15
Sony20
Sony Combo (old)
Sony Combo (new)
Sony DSP
Thomson
Thomson
TiVo (Advanced)
TiVo (Official)
TiVo (Official 2-byte)
X10
Zenith

Here is a list of the protocols that are not yet supported:

Device Combiner
RC-5/5x Combo
RCA Combo (w/Duration)

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

RemoteMaster ships with a large number of RDF files, which
are slightly modified versions of many of the RDFs
that ship with IR.  Standard RDFs shipped with IR or
downloaded from the jp1 Yahoo! group will NOT work
with RemoteMaster.

RemoteMaster now uses aliases for device types, much like KM does.
It is possible that some previously created .km files will
not load correctly.  If it seems that this has happened to
one of your .km files, edit it with a text editor and
change the DeviceType= to use one of the following values:

Cable, TV, VCR, CD, Tuner, DVD, SAT, Tape, Laserdisc,
DAT, Home Auto, Misc Audio, Phono, Video Acc, Amp

System Requirements
-------------------

You need either the Java SDK (for coding/testing/using) or
JRE (for only testing/using) version 1.4.1 or later.
If you don't have either, get one from the Java Download page.


Installating RemoteMaster
-------------------------

To install, follow these steps:
1. Create an empty directory (e.g. c:\rmaster),
2. Unzip to the directory you created above, making sure to replace all files,
   and preserving directory structure.

Running RemoteMaster
----------

To run, just double-click RemoteMaster.jar.

From the command line, type

java -jar RemoteMaster.jar

Or, you can simply type

rmaster

If you see some unexpected results, close RemoteMaster and look in rmaster.err
for some diagnostic messages about the error. These should help me find the bug.
By unexpected result, I mean the GUI crashing or going haywire or becoming
unresponsive.

Protocols
-------------------
RemoteMaster does not yet support all the protocols supported by KM.
Here is a list of the protocols that SHOULD work.  If you find a problem
with one of these, please open a Bug in SourceForge againt the controlremote project.

Aiwa
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
F12
GI 4DTV Hacked
GI 4DTV Official
GI Cable
HK Combo
Jerrold
JVC
JVC-48
Mitsubishi
NEC1
NEC2
NEC1 Combo
NEC2 Combo
Nokia Quad
Pace
Panasonic (old)
Panasonic
Panasonic VCR Combo
Panasonic Combo2
Panasonic2 (LCD)
Pioneer
Pioneer DVD
Proton
RC-6
RCA
RCA (Old)
RCA Combo (Official)
RCA Combo (w/Duration)
RECS80 (45)
RECS80 (68)
ReplayTV (Official)
Sharp
Sharp DVD
Sony DSP
TiVo Hacked
TiVo Official
X10

Here is a list of the protocols that not yet supported:

Device Combiner
Dishplayer (old)
Echostar 2200
Emerson
NEC1 2DEV Combo
Panasonic Combo
Pioneer DVD2
Pioneer MIX
Pioneer 2CMD
Pioneer 3DEV
RC-5
RC-5x
RC-5/5x Combo
RC-6a
ReplayTV (Simple)
ReplayTV (Advanced)
Sony 12/15
Sony20
Sony Combo
Zenith

KMJ ships with a large number of RDF files, which
are slightly modified versions of many of the RDFs
that ship with IR.  Standard RDFs shipped with IR or
downloaded from the jp1 Yahoo! group will NOT work
with KM.

In this release KMJ is now using aliases for device types,
much like KMX does.  It is possible that some previously
created .km files will not load correctly.  If it seems that
this has happened to one of your .km files, edit it with a
text editor and change the DeviceType= to use one of
the following values:

Cable, TV, VCR, CD, Tuner, DVD, SAT, Tape, Laserdisc,
DAT, Home Auto, Misc Audio, Phono, Video Acc, Amp

System Requirements
-------------------

You need either the Java SDK (for coding/testing/using) or
JRE (for only testing/using) version 1.4.1 or later.
If you don't have either, get one from the Java Download page.


Installating KM
---------------

To install, follow these steps:
1. Create an empty directory (e.g. c:\km),
2. Unzip to the directory you created above, making sure to replace all files,
   and preserving directory structure.

Running KM
----------

To run, just double-click KeyMapMaster.jar.

From the command line, type

java -jar KeyMapMaster.jar

Or, you can simply type

kmj

If you see some unexpected results, close KM and look in km.err for some
diagnostic messages about the error. These should help me find the bug.
By unexpected result, I mean the GUI crashing or going haywire or becoming
unresponsive.


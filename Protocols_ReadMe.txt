***************** Aiwa Combo ************************************************

We have (rather arbitrarily) assigned the name "Sub Device" to the portion of
Aiwa device information that CAN'T be varied between functions in this combo
protocol.  The decoder and the other Aiwa protocol (in RemoteMaster) both use this
same terminology (the non Combo Aiwa protocol in KM doesn't).  Normally
this part of the device information is zero, so you normally put a 0 in the
Sub Device field on the Setup sheet (or leave it blank, which defaults to 0).

On the functions sheet you can independently specify both the Device and
either EFC or OBC of each funtion.  The two part Hex command encodes both
the device and the OBC.

***************** Panasonic VCR Combo ***************************************

Combines two device codes that differ in bit 4 of their binary form and two
Sub Device codes that differ in bit 0.

Select the default Device and Sub device codes on the Setup page.  The
alternate Device and Sub Device values are computed and made available
on the pull-downs on the functions sheet.

Normally leave the OEM fields blank to default to 2 and 32, which are the only
values used by Panasonic.  To use this combo as a JVC-48 combo, change those
to 3 and 1.

On the functions sheet you can enter an EFC or OBC and have the default Device
and Sub Device automatically filled in and/or you can pull down the choices
for Device or Sub Device and select the alternate value(s).

Given a pair of Device numbers that differ in bit 4, such as 112 and 96, the
choice of which to use on the Setup sheet only affects the default for new
functions on the functions sheet.  It does not affect the generated upgrade.
You could put 96 on the Setup sheet, then switch to the functions sheet and
enter or paste in all the EFCs that use device 96, then change to 112 on the
Setup sheet and enter or paste in all the EFCs that use use device 112.  This
may save time vs. selecting the device for each function on the functions
sheet.  When you're done, it doesn't matter which of the two device numbers
you leave on the Setup sheet (but it must be one of those two.  The individual
functions only remember bit 4 of the device number while the Setup sheet
provides all the other bits).

Behavior is similar for pairs of Sub Devices differing only in bit 0.

Unfortunately, the UEI code for this protocol computes the check bytes
incorrectly for most of the possible combinations of Device and Sub Device
values.  Fortunately, it gets it right for several of the most commonly used
combinations.  An error message will be displayed if two or all four of the
combinations implied by your choice of Device and Subdevice will have wrong
check bytes.  If only two will be wrong and you wantto use just the other
two, you can proceed.  If all four will be wrong, the upgrade would be
useless (the Panasonic device will ignore all signals with wrong check bytes
even though the Device, Subdevice and OBC bytes are all correct).  In that
case you should look into the other Panasonic Combo protocols.

***************** Pioneer DVD  ***********************************************

A Pioneer signal consists of either one or two parts.  Each part has both a
device and a command.

"Device 1" on the Setup Sheet is used as the device for the first part of two
part signals and as the only device for one part signals.

"Prefix Command" on the Setup sheet is used as the first command for two
part signals.  It isn't used for one part signals.

"Device 2" on the Setup sheet is used as the device for the second part of two
part signals.  It isn't used for one part signals.

A function on the functions sheet can be fully specified by any one of EFC or
OBC or Hex.  In case of a two part signal from a decoder, this is the EFC, OBC
or Hex of just the second part of the decoded signal.

The functions sheet also has columns for "Prefix Device", "Prefix Command",
and "Device".

Any OBC with bit 5 set can be sent (by this protocol) only as a one part
signal and will force the "Prefix" columns to say "none" and will force the
device column to be Device 1 from the Setup sheet. 

Any OBC with bit 5 clear can be sent (by this protocol) only as the second
part of a two part signal and will force the "Prefix" columns to indicate the
first part of the two part signal and will force the device column to be
Device 2 from the Setup sheet. 

You can change the selection in one of the Prefix columns or the Device column
and it will change the other two to be consistent as well as changing bit 5
of the OBC to be consistent.

The set of signals you need for your Pioneer device may or may not be covered
by these limits.  If you need more flexibility, you should look into the other
Pioneer protocols and/or use Key Moves on another setup code for some of your
signals.
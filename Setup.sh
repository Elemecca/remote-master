#!/bin/sh
#
# Script to create .desktop files for RMIR and RM in their installation directory. This script must itself be
# in the installation directory to identify correctly the resources to which it links.
#
# For more information on .desktop files, see for example
# https://xpressrazor.wordpress.com/2013/07/07/playing-with-desktop-files-aka-application-launchers-in-linux/
#
# Following their creation, the .desktop files are made executable in accordance with that reference.
# RemoteMaster.jar is also made executable so that it can be run by double-clicking.
# Further, the user is added to the dialout group if not already a member.  This is needed in order to use
# RMIR USB serial interfaces without root access.
#
# Created by Graham Dixon (mathdon), March 22, 2015.

# First identify the directory containing this script.
here=$(dirname $(readlink -sf $0)) 2>/dev/null || here=$(dirname $0)

# Test if we are in the directory containing RemoteMaster.jar
if ! [ -f $here/RemoteMaster.jar ]; then
echo "This script must be in the same directory as RemoteMaster.jar"
exit 1
fi

# Construct paths to the new files and to the jar file. 
desktopRMIR=$here/RMIR.desktop
desktopRM=$here/RemoteMaster.desktop
rmprog=$here/RemoteMaster.jar
desktopdir=$HOME/.local/share/applications
[ -d $desktopdir ] || mkdir -p $desktopdir

# Create the .desktop files.
cat >$desktopRMIR << EOF
[Desktop Entry]
Comment=Edit JP1 remotes
Categories=Application;Java
Terminal=false
Name=RMIR
Exec=java -jar $rmprog -ir
Type=Application
Icon=$here/RMIR.ico
StartupNotify=true
Version=1.0
EOF

cat >$desktopRM << EOF
[Desktop Entry]
Comment=Edit JP1 device upgrades
Categories=Application;Java
Terminal=false
Name=RemoteMaster
Exec=java -jar $rmprog -rm
Type=Application
Icon=$here/RM.ico
StartupNotify=true
Version=1.0
EOF

# Copy desktop files to desktopdir
cp $desktopRMIR $desktopdir
cp $desktopRM $desktopdir

# Set executable permissions.
chmod 775 $rmprog
chmod 775 $desktopRMIR
chmod 775 $desktopRM
echo Desktop files created and executable permissions set.

alldone()
{
echo "Setup complete."
exit 0
}

# Test if the dialout group exists, exit with message if not
grep -q dialout /etc/group || {
cat << EOF

There is no user group named "dialout" in this OS, so this script does not
know how to enable a USB serial interface to be used with RMIR.  As all other
procedures of this script have been performed, setup is now complete.

EOF
exit 0
}

# Dialout group exists, test if user is a member, exit if so
id -Gn | grep -q dialout && alldone

# User is not a member, test if user is root, exit if so
if [ "$(id -u)" -eq 0 ]; then alldone; fi

# User is not root, so ask if user wishes to be added
cat << EOF

To use a USB serial interface with RMIR, you need to be a member of the
dialout group.  Currently you are not a member.  This script can add you 
to that group but you will need to give your sudo password.  Do you want
it to add you to that group?

EOF

# Force user to answer Y or N (or equivalently y or n)
reply=x
while !  echo "YN" | grep -qi $reply; do
echo -n "Please answer Y or N. > "
read reply
done

if echo "Y" | grep -qi $reply; then
# User answered yes
sudo usermod -aG dialout $USER
cat << EOF

Done.  You will need to log out and log back in again for this change
to take effect.

EOF
else
# User answered no, so tell user what to do
cat << EOF

You will need to run the command "usermod -aG dialout $USER" to be added
to the dialout group before you can use a USB serial interface with RMIR.
After running the command, you need to log out and log back in again for
this change to take effect.

EOF
fi

alldone


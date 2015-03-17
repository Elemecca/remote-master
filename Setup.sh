#!/bin/sh
LEN=`expr length $0`
LEN=`expr $LEN - 9`
if [ $LEN -gt 0 ]
then
  RMPATH=`expr substr $0 1 $LEN`
  FIRST=`expr substr $0 1 1`
  if [ $FIRST != "/" ]
  then
    # Relative path
    RMPATH=`pwd`"/$RMPATH"
    # else already Absolute path
  fi
else
  # Current path
  RMPATH=`pwd`
fi
echo $RMPATH
DSK1=${RMPATH}/RMIR.desktop
DSK2=${RMPATH}/RemoteMaster.desktop
RMPROG=${RMPATH}/RemoteMaster.jar
echo "[Desktop Entry]" > ${DSK1}
echo "Comment=Edit JP1 remotes" >> ${DSK1}
echo "Categories=Application;Java" >> ${DSK1}
echo "Terminal=false" >> ${DSK1}
echo "Name=RMIR" >> ${DSK1}
echo "Exec=java -jar "${RMPROG}" -ir" >> ${DSK1}
echo "Type=Application" >> ${DSK1}
echo "Icon="${RMPATH}"/RMIR.ico" >> ${DSK1}
echo "StartupNotify=true" >> ${DSK1}
echo "Version=1.0" >> ${DSK1}
echo "[Desktop Entry]" > ${DSK2}
echo "Comment=Edit JP1 device upgrades" >> ${DSK2}
echo "Categories=Application;Java" >> ${DSK2}
echo "Terminal=false" >> ${DSK2}
echo "Name=RemoteMaster" >> ${DSK2}
echo "Exec=java -jar "${RMPROG}" -rm" >> ${DSK2}
echo "Type=Application" >> ${DSK2}
echo "Icon="${RMPATH}"/RM.ico" >> ${DSK2}
echo "StartupNotify=true" >> ${DSK2}
echo "Version=1.0" >> ${DSK2}
sudo chmod 777 ${RMPROG}
sudo chmod 775 ${DSK1}
sudo chmod 775 ${DSK2}
sudo usermod -a -G dialout $USER
echo "Desktop files RMIR and RemoteMaster constructed"
echo "Setup complete"




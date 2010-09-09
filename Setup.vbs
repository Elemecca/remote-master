On Error Resume Next

Set WshShell = WScript.CreateObject("WScript.Shell")
Set objFS = WScript.CreateObject("Scripting.FileSystemObject")

sCurrDir = objFS.GetParentFolderName(Wscript.ScriptFullName) 
WScript.Echo "Installation folder is " & sCurrDir

Sub makeShortcut( lnkFile, path, args, desc, icon, dir )
    Set shortcut = WshShell.CreateShortcut( lnkFile )
    shortcut.TargetPath = path
    shortcut.Arguments = args
    shortcut.Description = desc
    If Not IsNull(icon) then
        shortcut.IconLocation = icon
    End If
    shortcut.WindowStyle ="1"
    shortcut.WorkingDirectory = dir
    shortcut.save
End Sub

Sub associate( name, desc, icon, command, ext )
    call WshShell.RegWrite( "HKEY_CURRENT_USER\Software\Classes\" & name & "\", desc, "REG_SZ" )
    If Err.Number <> 0 Then
        WScript.Echo "Got an error writing to the registry, launching with UAC"
        objShell.ShellExecute "wscript.exe", """" & Wscript.ScriptFullName & """", sCurrDir, "runas", 1 
        WScript.Quit
    End If
    call WshShell.RegWrite( "HKEY_CURRENT_USER\Software\Classes\" & name & "\DefaultIcon\", icon, "REG_SZ" )
    call WshShell.RegWrite( "HKEY_CURRENT_USER\Software\Classes\" & name & "\Shell\open\command\", command, "REG_SZ" )
    call WshShell.RegWrite( "HKEY_CURRENT_USER\Software\Classes\" & ext & "\", name, "REG_SZ" )
End Sub

sRMFolder = WshShell.SpecialFolders("Programs") & "\Remote Master"
if Not objFS.FolderExists( sRMFolder ) Then
   objFS.CreateFolder( sRMFolder )
End If

sJavaw = WshShell.RegRead("HKCR\jarfile\shell\open\command\")
If Err.Number <> 0 Then
	sJavaw = WshShell.RegRead("HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\1.6\JavaHome") & "\bin\javaw.exe"
    If Err.Number <> 0 Then 
        sJavaw = WshShell.RegRead("HKLM\SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment\1.6\JavaHome") & "\bin\javaw.exe"
    End If
Else
    iPos = InStr( 1, sJavaw, """ -jar" )
    sJavaw = left( sJavaw, iPos - 1 )
    sJavaw = right( sJavaw, len( sJavaw ) -  1 )
End If

if objFs.FileExists( sJavaw ) Then 
	sRunRMIR = """" & sJavaw & """ -jar """ & sCurrDir & "\RemoteMaster.jar"" -h """ & sCurrDir & """"
	sRunRM = sRunRMIR & " -rm" 

    'Make shortcuts in the Start menu
	call makeShortcut( sRMFolder & "\Remote Master.LNK", sJavaw,                               "-jar RemoteMaster.jar -rm", "RemoteMaster", sCurrDir & "\RM.ICO",   sCurrDir )
	call makeShortcut( sRMFolder & "\RMIR.LNK",          sJavaw,                               "-jar RemoteMaster.jar",     "RMIR",         sCurrDir & "\RMIR.ICO", sCurrDir )
	call makeShortcut( sRMFolder & "\Read Me.LNK",       sCurrDir & "\Readme.html",            "",                          "Read Me",      Null,                   sCurrDir )
	call makeShortcut( sRMFolder & "\Tutorial.LNK",      sCurrDir & "\tutorial\tutorial.html", "",                          "Tutorial",     Null,                   sCurrDir & "\tutorial" )
	
	'Make shortcuts in the installation folder as well
	call makeShortcut( sCurrDir & "\Remote Master.LNK", sJavaw,                               "-jar RemoteMaster.jar -rm", "RemoteMaster", sCurrDir & "\RM.ICO",   sCurrDir )
	call makeShortcut( sCurrDir & "\RMIR.LNK",          sJavaw,                               "-jar RemoteMaster.jar",     "RMIR",         sCurrDir & "\RMIR.ICO", sCurrDir )
	call makeShortcut( sCurrDir & "\Read Me.LNK",       sCurrDir & "\Readme.html",            "",                          "Read Me",      Null,                   sCurrDir )
	call makeShortcut( sCurrDir & "\Tutorial.LNK",      sCurrDir & "\tutorial\tutorial.html", "",                          "Tutorial",     Null,                   sCurrDir & "\tutorial" )

	call associate( "RMDeviceUpgrade", "Remote Master Device Upgrade",       sCurrDir & "\RM.ico",   sRunRM & " ""%1""", ".rmdu" )
	call associate( "RMRemoteConfig",  "Remote Master Remote Configuration", sCurrDir & "\RMIR.ico", sRunRMIR & " ""%1""", ".rmir" )
	WScript.Echo "Program shortcuts created successfully."
Else
	WScript.Echo "Program shortcuts were not created because javaw.exe was not found."
End If
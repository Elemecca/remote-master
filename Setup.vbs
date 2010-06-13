Set WshShell = WScript.CreateObject("WScript.Shell")
Set objFS = WScript.CreateObject("Scripting.FileSystemObject")

sCurrDir =  objFS.GetAbsolutePathName(".")

sRMFolder = WshShell.SpecialFolders("Programs") & "\Remote Master"
if Not objFS.FolderExists( sRMFolder ) Then
   objFS.CreateFolder( sRMFolder )
End If

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

call makeShortcut( sRMFolder & "\Remote Master.LNK", sCurrDir & "\RemoteMaster.jar",        "",    "RemoteMaster", sCurrDir & "\RM.ICO",   sCurrDir )
call makeShortcut( sRMFolder & "\RMIR.LNK",          sCurrDir & "\RemoteMaster.jar",        "-ir", "RMIR",         sCurrDir & "\RMIR.ICO", sCurrDir )
call makeShortcut( sRMFolder & "\Read Me.LNK",       sCurrDir & "\Readme.html",             "",    "Read Me",      Null,                   sCurrDir )
call makeShortcut( sRMFolder & "\Tutorial.LNK",      sCurrDir & "\tutorial\tutorial.html",  "",    "Tutorial",     Null,                   sCurrDir & "\tutorial" )

Sub associate( name, desc, icon, command, ext )
    call WshShell.RegWrite( "HKCR\" & name & "\", desc, "REG_SZ" )
    call WshShell.RegWrite( "HKCR\" & name & "\DefaultIcon\", """" & icon & """", "REG_SZ" )
    call WshShell.RegWrite( "HKCR\" & name & "\Shell\open\command\", command, "REG_SZ" )
    call WshShell.RegWrite( "HKCR\" & ext & "\", name, "REG_SZ" )
End Sub

sJarType = WshShell.RegRead("HKCR\.jar\")
sJavaw = WshShell.RegRead("HKCR\" & sJarType & "\shell\open\command\" )
iPos = InStr( 1, sJavaw, " -jar" )
sJavaw = left( sJavaw, iPos + 6 )
sRunRM = sJavaw & sCurrDir & "\RemoteMaster.jar"" -h """ & sCurrDir & """ ""%1""" 

call associate( "RMDeviceUpgrade", "Remote Master Device Upgrade",       sCurrDir & "\RM.ico",   sRunRM,          ".rmdu" )
call associate( "RMRemoteConfig",  "Remote Master Remote Configuration", sCurrDir & "\RMIR.ico", sRunRM & " -ir", ".rmir" )

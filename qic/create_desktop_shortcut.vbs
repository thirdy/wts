Set fso = CreateObject("Scripting.FileSystemObject")
Set objShell = WScript.CreateObject("WScript.Shell")
'The current users Desktop
usersDesktop = objShell.SpecialFolders("Desktop")
'Where to create the new shorcut
Set objShortCut = objShell.CreateShortcut(usersDesktop & "\QIC Search.lnk")
filePath = fso.GetParentFolderName(WScript.ScriptFullName)
'What does the shortcut point to
objShortCut.TargetPath = filePath+"\run.bat"
'Add a description 
objShortCut.Description = "Run QIC Search."
'Set Working Directory
objShortCut.WorkingDirectory = filePath
'Add Icon
objShortCut.IconLocation = filePath+"\resource\qic$.ico"
'Create the shortcut 
objShortCut.Save

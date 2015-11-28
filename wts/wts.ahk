; WTS - Wraeclast Trade System
; Version: 1.1 (2015/11/28)
;
; Written by /u/ProFalseIdol on reddit, ManicCompresion in game
; http://thirdy.github.io/
;
; Latest Version will always be at:
; https://github.com/thirdy/wts/
;
; Feel free to pull/etc.
;

#SingleInstance force ; If it is alReady Running it will be restarted.
#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
#Persistent ; Stay open in background
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.

parm1 = %1%  ; first input parameter
parm2 = %2%  ; Second input parameter

if (parm1 = "$EXIT") 
{
	ExitApp
} 

StringReplace, param1, parm1, $LF, `n, All
StringReplace, param2, parm2, $LF, `n, All

Menu, tray, Tip, Path of Exile Wraeclast Trading System
Menu, tray, Icon, resource/wts$.ico
;CustomColor = EEAA99  ; Can be any RGB color (it will be made transparent below).
CustomColor = 000000  ; Can be any RGB color (it will be made transparent below).
Gui +LastFound +AlwaysOnTop -Caption +ToolWindow  ; +ToolWindow avoids a taskbar button and an alt-tab menu item.
;Gui, Color, %CustomColor%
Gui, Font, s10
CustomFont.Add("resource/Fontin-Bold.ttf")
;Gui, Font, s8 c510B01 q5, Fontin SmallCaps
Gui, Font, s10 000000 q5, Fontin Bold
;Gui, Add, Text, vMyText cLime, XXXXX YYYYY  ; XX & YY serve to auto-size the window.
; Make all pixels of this color transparent and make the text itself translucent (150):
WinSet, TransColor, %CustomColor% 204
;SetTimer, UpdateOSD, 200
;Gosub, UpdateOSD  ; Make the first update immediate rather than waiting for the timer.
Gui, Add, Text,,%param1%
;Get ScreenWidth and align GUI to the right (left border of openend inventory)
WinGetPos, Xpos, Ypos, ScreenWidth, ScreenHeight, Path of Exile
GuiWidthDefault := 330
GuiPositionX := ScreenWidth * 0.35 + GuiWidthDefault
GuiHeightMax := ScreenHeight - 20
Gui +Resize +MaxSize540x%GuiHeightMax%
Gui, Show, x%GuiPositionX% y5 NoActivate  ; NoActivate avoids deactivating the currently active window.
Gosub, CheckWinActivePOE
SetTimer, CheckWinActivePOE, 100
GuiON = 1
return

;UpdateOSD:
;MouseGetPos, MouseX, MouseY
;GuiControl,, MyText, X%MouseX%, Y%MouseY%
;GuiControl,, MyText, %param1%
return

; =======================================================================================
; Scriptname  : Class_CustomFont.ahk
; Description : Load font from a font file, without need installed to system.
; Date        : 2013-12-5
; Tested On   : AutoHotkey 1.1.13.01 A32/U32, Windows XP SP3
; Author      : tmplinshi
; Credits     : ResRead(), and some other codes by SKAN.
; =======================================================================================
Class CustomFont
{
	static FontList    := []
	static MemFontList := []
	static FR_PRIVATE  := 0x10

	Add(FontFile)
	{
		This.FontList.Insert( FontFile )
		Return, DllCall( "AddFontResourceEx", "Str", FontFile, "UInt", This.FR_PRIVATE, "UInt", 0 )
	}

	; Reference: http://www.autohotkey.com/board/topic/29396-crazy-scripting-include-and-use-truetype-font-from-script/
	AddFromResource(hCtrl, FontFile, FontName, FontSize = 30, ByRef hFont="")
	{
		static FW_NORMAL := 400, DEFAULT_CHARSET := 0x1

		if !hFont
		{
			nSize    := This.ResRead(fData, FontFile)
			fh       := DllCall( "AddFontMemResourceEx", "Ptr", &fData, "UInt", nSize, "UInt", 0, "UIntP", nFonts )
			hFont    := DllCall( "CreateFont", Int,FontSize, Int,0, Int,0, Int,0, UInt,FW_NORMAL, UInt,0
						, Int,0, Int,0, UInt,DEFAULT_CHARSET, Int,0, Int,0, Int,0, Int,0, Str,FontName )

			This.MemFontList.Insert( {"fh":fh, "hFont":hFont} )
		}

		SendMessage, 0x30, hFont, 1,, ahk_id %hCtrl%
	}

	Remove()
	{
		Loop, % This.FontList.MaxIndex()
			DllCall( "RemoveFontResourceEx"   , "Str", This.FontList[A_Index], "UInt", This.FR_PRIVATE, "UInt", 0 )

		Loop, % This.MemFontList.MaxIndex()
		{
			DllCall( "RemoveFontMemResourceEx", "UInt", This.MemFontList[A_Index]["fh"]    )
			DllCall( "DeleteObject"           , "UInt", This.MemFontList[A_Index]["hFont"] )
		}
	}

	; ResRead() By SKAN, from http://www.autohotkey.com/board/topic/57631-crazy-scripting-resource-only-dll-for-dummies-36l-v07/?p=609282
	ResRead( ByRef Var, Key ) {
		VarSetCapacity( Var, 128 ), VarSetCapacity( Var, 0 )
		If ! ( A_IsCompiled ) {
			FileGetSize, nSize, %Key%
			FileRead, Var, *c %Key%
			Return nSize
		}

		If hMod := DllCall( "GetModuleHandle", UInt,0 )
			If hRes := DllCall( "FindResource", UInt,hMod, Str,Key, UInt,10 )
				If hData := DllCall( "LoadResource", UInt,hMod, UInt,hRes )
					If pData := DllCall( "LockResource", UInt,hData )
						Return VarSetCapacity( Var, nSize := DllCall( "SizeofResource", UInt,hMod, UInt,hRes ) )
							,  DllCall( "RtlMoveMemory", Str,Var, UInt,pData, UInt,nSize )
		Return 0
	}
}

CheckWinActivePOE: 
	GuiControlGet, focused_control, focus
	if(WinActive("ahk_class Direct3DWindowClass") && WinActive("Path of Exile"))
		If (GuiON = 0) {
			Gui, Show, x%GuiPositionX% y5 NoActivate
			GuiON := 1
		}
	if(!WinActive("ahk_class Direct3DWindowClass") && !WinActive("Path of Exile"))
		If !focused_control
			If (GuiON = 1)
			{
				Gui, Hide
				GuiON = 0
			}
Return

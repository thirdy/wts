; QIC (Quasi-In-Chat) Search
;
; Written by:
; /u/Eruyome87 
; /u/ProFalseIdol
;
; Latest Version will always be at:
; https://github.com/thirdy/wts/
;
; Feel free to make pull-requests.
;

#SingleInstance force ; If it is alReady Running it will be restarted.
#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
#Persistent ; Stay open in background
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.
SetBatchLines, -1

#Include, lib/Gdip_All.ahk
; https://www.autohotkey.com/boards/viewtopic.php?t=1879
#Include, lib/Gdip_Ext.ahk

Menu, tray, Tip, Path of Exile - QIC (Quasi-In-Chat) Search
Menu, tray, Icon, resource/qic$.ico

; Start gdi+
If !pToken := Gdip_Startup()
{
   MsgBox, 48, gdiplus error!, Gdiplus failed to start. Please ensure you have gdiplus on your system
   ExitApp
}
OnExit, Exit

parm1 = %1%  ; first input parameter
parm2 = %2%  ; Second input parameter

if (parm1 = "$EXIT") 
{
	ExitApp
} 

StringReplace, param1, parm1, $LF, `n, All
StringReplace, param2, parm2, $LF, `n, All

; https://github.com/tariqporter/Gdip/blob/master/Gdip.Tutorial.8-Write.text.onto.a.gui.ahk
; Set the width and height we want as our drawing area, to draw everything in. This will be the dimensions of our bitmap
WinGetPos, Xpos, Ypos, ScreenWidth, ScreenHeight, Path of Exile
IniRead, DrawingAreaWidth, overlay_config.ini, Overlay, Width
IniRead, DrawingAreaHeight, overlay_config.ini, Overlay, Height
IniRead, DrawingAreaPosX, overlay_config.ini, Overlay, AbsolutePositionLeft
IniRead, DrawingAreaPosY, overlay_config.ini, Overlay, AbsolutePositionTop
IniRead, Font, overlay_config.ini, Overlay, FontFamily
IniRead, FontSize, overlay_config.ini, Overlay, FontSize
CustomFont.Add("resource/Fontin-Bold.ttf")

If !DrawingAreaWidth 
	DrawingAreaWidth = 310
If !DrawingAreaPosX 
	DrawingAreaPosX := ScreenWidth * 0.33 + DrawingAreaWidth
If !DrawingAreaPosY 
	DrawingAreaPosY := 5
If !DrawingAreaHeight 
	DrawingAreaHeight := ScreenHeight - 50
If !FontSize
	FontSize := 13

; Next we can check that the user actually has the font that we wish them to use
; If they do not then we can do something about it. I choose to default to Arial.
If !hFamily := Gdip_FontFamilyCreate(Font)
{
   Font = Arial
}
Else {
	Gdip_DeleteFontFamily(hFamily)
}
	
Gui, 1:  -Caption +E0x80000 +LastFound +OwnDialogs +Owner +AlwaysOnTop
Gui, 1: Show, NA

hwnd1 := WinExist()
hbm := CreateDIBSection(DrawingAreaWidth, DrawingAreaHeight)
hdc := CreateCompatibleDC()
obm := SelectObject(hdc, hbm)
G := Gdip_GraphicsFromHDC(hdc)
Gdip_SetSmoothingMode(G, 4)

pBrush := Gdip_BrushCreateSolid(0xffb4804b)
; left border
Gdip_FillRectangle(G, pBrush, 0, 0, 1, DrawingAreaHeight)
; right border
Gdip_FillRectangle(G, pBrush, DrawingAreaWidth - 2, 0, 1, DrawingAreaHeight)
; top border
Gdip_FillRectangle(G, pBrush, 0, 1, DrawingAreaWidth, 1)
; bottom border
Gdip_FillRectangle(G, pBrush, 0, DrawingAreaHeight - 2, DrawingAreaWidth, 1)
; background
pBrush := Gdip_BrushCreateSolid(0x47000000)
Gdip_FillRectangle(G, pBrush, 0, 0, DrawingAreaWidth, DrawingAreaHeight)

; Extra options:
; ow4         - Sets the outline width to 4
; ocFF000000  - Sets the outline colour to opaque black
; OF1			- If this option is set to 1 the text fill will be drawn using the same path that the outline is drawn.
Options = x5 y5 w%DrawingAreaWidth%-10 h%DrawingAreaHeight%-10 Left cffffffff ow2 ocFF000000 OF1 r4 s%FontSize%

;Options = x5 y5 w%DrawingAreaWidth%-10 h%DrawingAreaHeight%-10 Left cffffffff r4 s%FontSize%
;Gdip_TextToGraphics(G, param1, Options, Font, DrawingAreaWidth, DrawingAreaHeight) 
Gdip_TextToGraphicsOutline(G, param1, Options, Font, DrawingAreaWidth, DrawingAreaHeight) 

UpdateLayeredWindow(hwnd1, hdc, DrawingAreaPosX, DrawingAreaPosY, DrawingAreaWidth, DrawingAreaHeight)

OnMessage(0x201, "WM_LBUTTONDOWN")

Gosub, CheckWinActivePOE
SetTimer, CheckWinActivePOE, 100
GuiON = 1

return


WM_LBUTTONDOWN() {
   PostMessage, 0xA1, 2
}

; Set Hotkey for toggling GUI overlay completely OFF, default = ctrl + q
; ^p and ^i conflicts with trackpetes ItemPriceCheck macro
^q::
	If (GuiON = 0) {
		Gosub, CheckWinActivePOE
		SetTimer, CheckWinActivePOE, 100
		GuiON = 1
	}
	Else {
		SetTimer, CheckWinActivePOE, Off      
		Gui, 1: Hide	
		GuiON = 0
	}
return
		

Exit:
	Gdip_DeleteBrush(pBrush)
	SelectObject(hdc, obm)
	DeleteObject(hbm)
	DeleteDC(hdc)
	Gdip_DeleteGraphics(G)
	Gdip_Shutdown(pToken)
ExitApp

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
			Gui, 1: Show, NA
			GuiON := 1
		}
	if(!WinActive("ahk_class Direct3DWindowClass") && !WinActive("Path of Exile"))
		;If !focused_control
			If (GuiON = 1)
		{
			Gui, 1: Hide
			GuiON = 0
		}
Return

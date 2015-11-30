# QIC
Quasi-In-Chat Search

QIC is trade search tool (as of 0.1) that aims to allow Path of Exile players to have an in-game items search tool. It features searching by search terms (or keywords) like "50life 60res 4L boots".

It monitors the Path of Exile client log file for commands typed in-game and uses AHK to display an On-Screen Display (OSD). Commands can be extended or modified via .txt files in keywords directory.

# Commands

* `search` or `s` `{search terms}` - runs a search given _search terms_.
* `searchend` or `se` - closes the search result window and clears out results.
* `n` - next page
* `p` - prev page
* `0`,`1`,`2`..`n` - generate WTB message for item `#n` and copy to clipboard
* `reload` - reloads all configuration files (very useful when updating keywords text files)
* `sort`* - sort current results (see keywords/sort.txt)
* `view{n}` - view all stats for item #n
* `searchexit` or `sexit` - stops QIC. You'll need to run _run.bat_ again to execute commands.
* `ctrl+q` - Toggles the GUI ON/OFF, shortcut can be configured in qic.ahk

# Screenshots

![6](https://dl.dropboxusercontent.com/u/13620316/wts-screen01.png)

![7](https://dl.dropboxusercontent.com/u/13620316/wts-screen02.png)

# How to Install/Run

To run QIC, you'll need:

1. Java installed. Go to https://www.java.com/ to download the latest version of Java.
2. AHK installed. Go to http://ahkscript.org/ to download the latest version of AutoHotkey.
3. Configure your config.properties file:
  * poelogpath - path to your Path of Exile Client.txt file
  * ahkpath    - path to your AutoHotKey executable
4. (Not required) Configure your overlay_config.ini file if desired.
5. (Not required) Install the Path of Exile font "Fontin" for a better experience, located in subfolder "resource".
6. Run via run.bat

# Contributors

/u/Eruyome87 - AHK icons and development - see [reddit comment](https://www.reddit.com/message/messages/4i2p30)

---

QIC is 100% free and open source licensed under GPLv2
Created by: /u/ProFalseIdol IGN: ManicCompression
QIC is fan made tool and is not affiliated with Grinding Gear Games in any way.

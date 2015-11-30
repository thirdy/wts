# WTS
Wraeclast Trade System

WTS is trade search tool (as of 0.1) that aims to allow Path of Exile players to have an in-game items search tool. It features searching by search terms (or keywords) like "50life 60res 4L boots".

It monitors the Path of Exile client log file for commands typed in-game and uses AHK to display an On-Screen Display (OSD). Commands can be extended or modified via .txt files in keywords directory.

# Commands

1. search or s {search terms} - runs a search given _search terms_.
2. searchend or se - closes the search result window and clears out results.
3. n - next page
4. p - prev page
5. 0,1,2..n - generate WTB message for item #n and copy to clipboard
5. reload - reloads all configuration files (very useful when updating keywords text files)
6. sort* - sort current results (see keywords/sort.txt)
7. view{n} - view all stats for item #n
8. searchexit or sexit - stops WTS. You'll need to run _run.bat_ again to execute commands.

# Screenshots

![6](https://dl.dropboxusercontent.com/u/13620316/wts-screen01.png)

![7](https://dl.dropboxusercontent.com/u/13620316/wts-screen02.png)

# How to Install/Run

To run WTS, you'll need:

1. Java installed. Go to https://www.java.com/ to download the latest version of Java.
2. AHK installed. Go to http://ahkscript.org/ to download the latest version of AutoHotkey.
3. Configure your config.properties file:
 a. poelogpath - path to your Path of Exile Client.txt file
 b. ahkpath    - path to your AutoHotKey executable
4. Configure your overlay_config.ini file if desired (not required).
5. Install the Path of Exile font "Fontin" for a better experience, located in subfolder "resource" (not required).
6. Run via run.bat

# Contributors

/u/Eruyome87 - AHK icons and development - see [reddit comment](https://www.reddit.com/message/messages/4i2p30)

---

WTS is 100% free and open source licensed under GPLv2
Created by: /u/ProFalseIdol IGN: ManicCompression
WTS is fan made tool and is not affiliated with Grinding Gear Games in any way.

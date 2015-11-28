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

![6](https://cloud.githubusercontent.com/assets/75921/11445426/eeb9d4c2-9566-11e5-814e-5c6f663cfa84.png)

![7](https://cloud.githubusercontent.com/assets/75921/11445437/009bbbf6-9567-11e5-9075-e4dd972a0622.png)

# How to Install/Run

To run WTS, you'll need:

1. Java installed. Go to https://www.java.com/ to download the latest version of Java.
2. AHK installed. Go to http://ahkscript.org/ to download the latest version of AutoHotkey.
3. Configure your config.properties file:
 a. poelogpath - path to your Path of Exile Client.txt file
 b. ahkpath    - path to your AutoHotKey executable
4. Run via run.bat

---

WTS is 100% free and open source licensed under GPLv2
Created by: /u/ProFalseIdol IGN: ManicCompression
WTS is fan made tool and is not affiliated with Grinding Gear Games in any way.

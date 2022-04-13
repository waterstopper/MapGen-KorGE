# Korge mapgen

This is a korge map generator for [Pocket Palm Heroes remastered](https://github.com/SerVB/pph) (PPH).

## Algorithm

Is based mostly on [presentation](https://en.ppt-online.org/29091) by Gus Smedstad, who is the HoMM3 programmer. Unfortunately, presentation does not contain any code (yet there are some guidelines).

5 steps:

- [x] Parse a map template file (examples are in `/resources`)
- [x] Place circles with connecting segments
- [x] Create Voronoi Diagram from circle centers 
- [x] Restructure diagram into a grid of cells, where each cell represents an empty tile. 
- [ ] Make jagged zone edges and rebalance zone sizes
- [x] Add edges between zones
- [x] Add castles and mines in each zone
- [x] Create passages and portals between zones
- [x] Connect castles and mines with roads
- [x] Populate all tiles (obviously the most complex step, but for now it is enough to get the basic idea)

## Parse template map and config

## Place circles and connecting segments
*Looking back, it seems that implementing that algorithm wasn't a good choice. It is not effective for many zones (more than 10 estimate). Maybe I should've looked more into planar graph representations*

## Restructure circles into Array2

Add edges

## Add castles and mines
Now zone can have only one castle which is placed in center of mass. All mines are placed in a circle with center of mass as its center.

## Add passages and portals

## Connect isolate parts
Using [A*](https://www.redblobgames.com/pathfinding/a-star/introduction.html) pathfinding algorithm

## Adding roads

## Adding guards

## Adding treasures
Choose random empty cells in a map, randomly place resources, mana crystals, campfires and artifacts (is represented as color)

##  Export to hmm
hmm is a file format for  PPH map editor

Here is how an incomplete second step looks like:

![](images/map.png)
![](images/dense.png)
![](images/sizes.png)
![](images/normal.png)
{USERDOMAIN_ROAMINGPROFILE=DESKTOP-0DJ965A, PROCESSOR_LEVEL=6, SESSIONNAME=Console, ALLUSERSPROFILE=C:\ProgramData, PROCESSOR_ARCHITECTURE=AMD64, PSModulePath=C:\Users\alex\Documents\WindowsPowerShell\Modules;C:\Users\alex\AppData\Local\Google\Cloud SDK\google-cloud-sdk\platform\PowerShell, SystemDrive=C:, SMLNJ_HOME=C:\Tools\SMLNJ\, USERNAME=alex, ProgramFiles(x86)=C:\Program Files (x86), _MSYS2_BASH=C:\tools\msys64\usr\bin\bash.exe, _MSYS2_PREFIX=x86_64, FPS_BROWSER_USER_PROFILE_STRING=Default, PATHEXT=.COM;.EXE;.BAT;.CMD;.VBS;.VBE;.JS;.JSE;.WSF;.WSH;.MSC;.PY;.PYW, DriverData=C:\Windows\System32\Drivers\DriverData, ProgramData=C:\ProgramData, ProgramW6432=C:\Program Files, HOMEPATH=\Users\alex, PROCESSOR_IDENTIFIER=Intel64 Family 6 Model 142 Stepping 12, GenuineIntel, kubectl=C:\Users\alex\AppData\Local\Google\Cloud SDK\google-cloud-sdk\bin\kubectl.exe, ProgramFiles=C:\Program Files, PUBLIC=C:\Users\Public, windir=C:\WINDOWS, =::=::\, ZES_ENABLE_SYSMAN=1, OneDriveCommercial=C:\Users\alex\OneDrive - Н�?У Высшая школа экономики, LOCALAPPDATA=C:\Users\alex\AppData\Local, ChocolateyLastPathUpdate=132826947073657013, USERDOMAIN=DESKTOP-0DJ965A, FPS_BROWSER_APP_PROFILE_STRING=Internet Explorer, LOGONSERVER=\\DESKTOP-0DJ965A, JAVA_HOME=C:\Users\alex\.jdks\jdk-11.0.13+8, PYTHON=C:\Users\alex\AppData\Local\Programs\Python\, OneDrive=C:\Users\alex\OneDrive - Н�?У Высшая школа экономики, APPDATA=C:\Users\alex\AppData\Roaming, ChocolateyInstall=C:\ProgramData\chocolatey, ChocolateyToolsLocation=C:\tools, CUDA_PATH_V11_6=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v11.6, VBOX_MSI_INSTALL_PATH=C:\Program Files\Oracle\VirtualBox\, NVTOOLSEXT_PATH=C:\Program Files\NVIDIA Corporation\NvToolsExt\, CommonProgramFiles=C:\Program Files\Common Files, Path=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v11.6\bin;C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v11.6\libnvvp;C:\Program Files (x86)\Common Files\Oracle\Java\javapath;C:\ProgramData\Oracle\Java\javapath;C:\Python39\Scripts\;C:\Python39\;C:\Users\alex\.jdks\jdk-11.0.13+8\bin;C:\Windows\system32;C:\Windows;C:\Windows\System32\Wbem;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Windows\System32\OpenSSH\;C:\Program Files (x86)\NVIDIA Corporation\PhysX\Common;C:\Program Files\Git\cmd;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\WINDOWS\System32\WindowsPowerShell\v1.0\;C:\WINDOWS\System32\OpenSSH\;C:\Users\alex\AppData\Local\Programs\Python\Python38\;C:\Users\alex\AppData\Local\Google\Cloud SDK\google-cloud-sdk\bin\kubectl.exe;C:\Users\alex\AppData\Local\Google\Cloud SDK\google-cloud-sdk\bin;C:\Program Files\nodejs\;C:\ProgramData\chocolatey\bin;C:\Program Files\dotnet\;C:\Program Files\NVIDIA Corporation\Nsight Compute 2022.1.1\;C:\Program Files\NVIDIA Corporation\NVIDIA NvDLISR;C:\Users\alex\AppData\Local\Microsoft\WindowsApps;C:\Users\alex\AppData\Local\atom\bin;C:\Users\alex\AppData\Local\GitHubDesktop\bin;C:\Users\alex\AppData\Local\Microsoft\WindowsApps;C:\Users\alex\AppData\Local\Programs\Microsoft VS Code\bin;C:\Users\alex\AppData\Local\Google\Cloud SDK\google-cloud-sdk\bin;C:\Users\alex\AppData\Roaming\npm;C:\Firefox\Firefox\firefox.exe;C:\Users\alex\AppData\Roaming\cabal\bin;C:\tools\ghc-9.2.1\bin;C:\tools\msys64;C:\Users\alex\.dotnet\tools;C:\Tools\GoLand\bin;, d=e, OS=Windows_NT, COMPUTERNAME=DESKTOP-0DJ965A, CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v11.6, arewefd=e, PROCESSOR_REVISION=8e0c, CommonProgramW6432=C:\Program Files\Common Files, ComSpec=C:\WINDOWS\system32\cmd.exe, SystemRoot=C:\WINDOWS, TEMP=C:\Users\alex\AppData\Local\Temp, HOMEDRIVE=C:, USERPROFILE=C:\Users\alex, TMP=C:\Users\alex\AppData\Local\Temp, CommonProgramFiles(x86)=C:\Program Files (x86)\Common Files, NUMBER_OF_PROCESSORS=8, GoLand=C:\Tools\GoLand\bin;, IDEA_INITIAL_DIRECTORY=C:\Users\alex\Desktop}

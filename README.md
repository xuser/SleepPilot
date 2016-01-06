# SleepPilot #

SleepPilot is a software for automated sleep stage classification. 
The classifier has been trained on EEG data of 100+ healthy subjects and achieves an accuracy of 88.2% (Cohen's kappa: 0.82) on an independent test set. 
It should be used for research only. 

### Features ###
* reads BrainVision, edf, edf+ and Spike2 .son files.
* automatic K-complex highlighting & counting
* tSNE powered cluster plot for dataset exploration 

### Installation ###
SleepPilot requires Java 8.
Download the repository from GitHub and unzip the release folder. Run SleepPilot by clicking on SleepPilot.jar.
SleepPilot.jar must stay were the son32.dll and the Classifiers folder is contained to function properly.

### Issues ###
Currently, you must run SleepPilot with a 32-bit Java VM on Windows in order to be able to load Spike2 .son files.

### Bugs & Suggestions ###
If you find bugs or have suggestions regarding parts of the software, please file a report/request on GitHub or [email](weigenand@inb.uni-luebeck.de) me.


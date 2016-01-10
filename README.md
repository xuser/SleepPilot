# SleepPilot #

SleepPilot is a software for automated sleep stage classification. 
The classifier has been trained on EEG data of 100+ healthy subjects and achieves an accuracy of 88.2% (Cohen's kappa: 0.82) on an independent test set. 
It should be used for research only. 

### Features ###
* automatic scoring from a single electrode
* reads BrainVision, edf, edf+ and Spike2 .smr files.
* automatic K-complex highlighting & counting
* tSNE powered cluster plot for dataset exploration 
 
### Installation ###
Download the repository from GitHub and unzip the release folder. Run SleepPilot by clicking on SleepPilot.jar.
SleepPilot.jar must stay with the son32.dll and the Classifiers folder to function properly.
SleepPilot requires [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

### Usage ###
SleepPilot expects a sampling rate of 100 Hz or multiples thereof (200,500,1000). 
Data files must have uniform sampling rate and scaling/resolution across all channels.
In general, no pre-processing of the data is needed. 
Indeed, it should be kept at a minimum and especially lowpass filtering below 50 Hz must be avoided. 
SleepPilot applies the highpass and lowpass filters it needs.  

### Issues ###
* Windows users must run SleepPilot with a 32-bit Java VM in order to be able to load Spike2 .smr files. The release folder contains SleepPilot_SMR.bat for that. You have to adjust the path in SleepPilot_SMR.bat (edit with Notepad) to point to your 32-bit Java JRE.
* The edf reader loads the complete file, hence edf files should be small enough to fit into RAM.
 

### Bugs & Suggestions ###
If you find bugs or have suggestions regarding parts of the software, please file a [report/request](https://github.com/xuser/SleepPilot/issues) on GitHub or [email](weigenand@inb.uni-luebeck.de) me.
Attach the SleepPilot.log in case the program crashes.


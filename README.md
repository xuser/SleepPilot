# SleepPilot #

[SleepPilot](https://github.com/xuser/SleepPilot/releases/download/v0.9.3-beta/SleepPilot_v0.9.3.zip) is a software for automated sleep stage classification. 
The classifier has been trained on EEG data of 100+ healthy subjects and achieves an accuracy of 88.2% (Cohen's kappa: 0.82) on an independent test set. 
It should be used for research only. 

### Features ###
* automatic scoring from a single electrode
* reads BrainVision, edf, edf+ and Spike2 .smr files.
* automatic K-complex highlighting & counting
* tSNE powered cluster plot for dataset exploration 
* batch mode for clustering of many datasets
 
### Installation ###
[Download](https://github.com/xuser/SleepPilot/releases/download/v0.9.4-beta/SleepPilot_v0.9.4.zip) the latest release and unzip. Run SleepPilot by clicking on SleepPilot.jar.
SleepPilot.jar must stay with the son32.dll and the Classifiers folder to function properly.
SleepPilot requires [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).
Contact me if you can't make it run.

### Usage ###
SleepPilot expects a sampling rate of >= 100 Hz or multiples of 50 Hz (150 Hz, 200 Hz, etc.). 
Data files must have uniform sampling rate and scaling/resolution across all channels.
In general, no pre-processing of the data is needed. 
Indeed, it should be kept at a minimum and especially lowpass filtering below 50 Hz must be avoided. 
SleepPilot applies the highpass and lowpass filters it needs.  
Select a feature channel in the toolbar, a classifier that best fits your setting and press the play button to start automatic classification.

### Batch Mode ###
In batch mode all EEG datasets in a user selected folder will be classified. The hypnogram of each dataset will be written as .txt file into the same folder, bearing the name of the original EEG file. For batch mode, all files must contain the same channels in identical order. Make sure all files have a compatible sampling rate (100/150/200 Hz etc.). Currently no feedback is given to the user about the progress of the calculations.

### Issues ###
* Windows users must run SleepPilot with a 32-bit Java VM in order to be able to load Spike2 .smr files. The release folder contains SleepPilot_SMR.bat for that. You have to adjust the path in SleepPilot_SMR.bat (edit with Notepad) to point to your 32-bit Java JRE.
 

### Bugs & Suggestions ###
Contributions are welcome!
If you find bugs or have suggestions regarding parts of the software, please file a [report/request](https://github.com/xuser/SleepPilot/issues) on GitHub or [email](weigenand@inb.uni-luebeck.de) me.
Attach the SleepPilot.log in case the program crashes.


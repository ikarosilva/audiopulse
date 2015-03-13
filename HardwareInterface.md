This page will be updated with more organized details and diagrams.

# Technical requirements #
Describe phone input / output, ER10C input/output characteristics
## ER10C specifications ##
### Input ###
1kHz sensitivity: 72 dB SPL = 1V RMS

Output Z = 50 kOhm

### Output ###
0 dB SPL = 0 dBuV

Output Z = 100 Ohm

## Example phone I/O specifications ##
Input Z ~= 1 kOhm

Max input voltage ~= 500 mV PP

Output Z ~= 50 Ohm

Output Voltage = 1 V PP

# Simple passive interface #

A simple analog connection can be built using a few commercially-available cables and 1 added capacitor for DC-voltage blocking.

Use a 1 uF cap for < 100 Hz high-pass cutoff, inserted into microphone path

<a href='http://www.meelec.com/MEE_TRRS_Adapter_for_P_Version_Headphones_p/adptr-trrs-35.htm'>TRRS RS switch adapter</a> (iPhone - sensible pin configuration)

<a href='http://www.markertek.com/Cables/Audio-Cables/AV-Dubbing-Cables/Delvcam-Monitor-Systems/DELV-RAAVCAB-6.xhtml?DELV-RAAVCAB-6'>TRRS - RCA(f) cable</a>
This cable uses S-ground configuration, and needs the above adapter to work with most smartphones.

<a href='http://www.markertek.com/Connectors-Adapters/Video-Adapters/BNC-to-RCA-Adapters/TecNec-Cables-Connectors/BM-P.xhtml'>RCA-BCN adapter</a>

Describe possible modifications to phone gain arrays, etc. for improved dynamic range

# Passive filter interface #

E.g., notch filter for input stimulus frequencies, allowing more analog gain prior to phone ADC input

# ADC-DAC stage interface #

An ADC stage prior to phone to allow our own specs
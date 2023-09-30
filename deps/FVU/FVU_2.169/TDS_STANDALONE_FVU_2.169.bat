@echo off
set path=C:\Program Files (x86)\Java\jre1.8.0_77\bin;%path%
set CLASSPATH=%CLASSPATH%;.;./TDS_STANDALONE_FVU_2.169.jar
start javaw -jar TDS_STANDALONE_FVU_2.169.jar
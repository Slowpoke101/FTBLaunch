# Launcher debugging
Basic of the debugging are explained in [support site](http://support.feed-the-beast.com/questions/19/finding-the-logs).

## How to run launcher in debugger
### IDEA
 1. Run &gt; Edit configurations &gt; Add new configuration &gt; Remote 
 2. Use defaults
 3. Start launcher from command line. E.g. `java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar  build/libs/FTB_Launcher-1.4.0-9999999.jar` or
    Use `suspend=y` to start JVM in suspended state
 4. Run &gt; Debug &gt; Select task you created in steps 1 and 2

### Eclipse
 1. TODO
 
## Generate thread dump
Also found in [support site](http://support.feed-the-beast.com/questions/19/finding-the-logs#threaddump).
Quartz Composer client for the SAPO Broker
==========================================

Done:

- Patch connects to the Broker cloud
- Patch subscribes the topic and spits out the resulting events
- Thread safe

To-Do:

- Trigger isn't working. Don't know if it should.
- Only one topic can be subscribed per patch/connection for now
- If you change the input parameters (it: change the topic), you must stop and start the composition

Installation:

1. Compile this project with XCode
2. Copy to QCBroker.plugin to ~/Library/Graphics/Quartz\ Composer Plug-Ins/
3. Try the examples/heatmap.qtz composition

Suggestions?

Bug me: celso at sapo dot pt

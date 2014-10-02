
function onBuildExperimentScript(scriptBuilder)
	local takeVideoSheet = scriptBuilder:create("Sheet")
	scriptBuilder:add(takeVideoSheet)
	takeVideoSheet:setTitle("Microphone Experiment:")
	experimentView = takeVideoSheet:addMicrophoneExperiment();
	experimentView:setDescriptionText("Please record some sound:")
	local micExperiment = experimentView:getExperiment()

	local experimentAnalysis = scriptBuilder:create("ExperimentAnalysis")
	scriptBuilder:add(experimentAnalysis)
	experimentAnalysis:setTitle("Analyze the audio recording:")
	experimentAnalysis:setExperiment(micExperiment)

end

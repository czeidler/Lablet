Lablet = {
    interface = 1.0
}


function Lablet.buildActivity(builder)
	local takeVideoSheet = builder:create("Sheet")
    builder:add(takeVideoSheet)
	takeVideoSheet:setTitle("Microphone Experiment:")
	experimentView = takeVideoSheet:addMicrophoneExperiment();
	experimentView:setDescriptionText("Please record some sound:")
	local micExperiment = experimentView:getExperiment()

	local experimentAnalysis = builder:create("FrequencyAnalysis")
    builder:add(experimentAnalysis)
	experimentAnalysis:setTitle("Analyze the audio recording:")
	experimentAnalysis:setExperiment(micExperiment)
end

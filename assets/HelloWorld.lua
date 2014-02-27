
function onBuildExperimentScript(scriptBuilder)
	local questions1 = scriptBuilder:create("QuestionsComponent")
	questions1:setTitle("Q1:")
	questions1:addTextOnlyQuestion("Text only Question")
	questions1:addTextOnlyQuestion("Text only Question2")
	scriptBuilder:add(questions1)

	local cameraExperiment = scriptBuilder:create("CameraExperiment")
	cameraExperiment:setTitle("Free Fall Experiment")
	cameraExperiment:setDescriptionText("Please take a free fall video:")
	scriptBuilder:add(cameraExperiment)
	local experiment = cameraExperiment:getExperiment()

	local experimentAnalysis = scriptBuilder:create("ExperimentAnalysis")
	experimentAnalysis:setExperiment(experiment)
	scriptBuilder:add(experimentAnalysis)

	local questions2 = scriptBuilder:create("QuestionsComponent")
	questions2:setTitle("Q2:")
	scriptBuilder:add(questions2)
	
end

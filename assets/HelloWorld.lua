
function onBuildExperimentScript(scriptBuilder)
	local questions1 = scriptBuilder:create("QuestionsComponent")
	questions1:setTitle("Q1:")
	scriptBuilder:add(questions1)

	local takeCameraExperiment = scriptBuilder:create("CameraExperiment")
	scriptBuilder:add(takeCameraExperiment)
	local experiment = takeCameraExperiment:getExperiment()

	local experimentAnalysis = scriptBuilder:create("ExperimentAnalysis")
	experimentAnalysis:setExperiment(experiment)
	scriptBuilder:add(experimentAnalysis)

	local questions2 = scriptBuilder:create("QuestionsComponent")
	questions2:setTitle("Q2:")
	scriptBuilder:add(questions2)
	
end

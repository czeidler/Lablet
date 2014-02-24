
function onBuildExperimentScript(scriptBuilder)
	questions1 = scriptBuilder:create("QuestionsComponent")
	questions1:setTitle("Q1:")
	scriptBuilder:add(questions1)
	
	local questions2 = scriptBuilder:create("QuestionsComponent")
	questions2:setTitle("Q2:")
	scriptBuilder:add(questions2)
	
	local takeCameraExperiment = scriptBuilder:create("CameraExperiment")
	scriptBuilder:add(takeCameraExperiment)
end


function onBuildExperimentScript(scriptBuilder)
	local questions1 = scriptBuilder:create("Sheet")
	questions1:setTitle("Q1:")
	questions1:addText("Text only Question")
	questions1:addText("Text only Question2")
	questions1:addCheckQuestion("check")
	scriptBuilder:add(questions1)

	local cameraExperiment = scriptBuilder:create("CameraExperiment")
	cameraExperiment:setTitle("Free Fall Experiment")
	cameraExperiment:setDescriptionText("Please take a free fall video:")
	scriptBuilder:add(cameraExperiment)
	local experiment = cameraExperiment:getExperiment()

	local experimentAnalysis = scriptBuilder:create("ExperimentAnalysis")
	experimentAnalysis:setExperiment(experiment)
	experimentAnalysis:setDescriptionText("Please analyze the free fall video:")
	scriptBuilder:add(experimentAnalysis)
	
	local experimentSheet = scriptBuilder:create("ExperimentSheet")
	experimentSheet:setExperiment(experiment)
	experimentSheet:setLayoutType("horizontal")
	experimentSheet:setTitle("Experiment Sheet:")
	experimentSheet:addPositionGraph()
	experimentSheet:addPositionGraph()
	scriptBuilder:add(experimentSheet)
	
	local calculateYSpeed = scriptBuilder:create("CalculateYSpeed")
	calculateYSpeed:setExperiment(experiment)
	scriptBuilder:add(calculateYSpeed)

	local questions2 = scriptBuilder:create("Sheet")
	questions2:setTitle("Q2:")
	scriptBuilder:add(questions2)
	
end

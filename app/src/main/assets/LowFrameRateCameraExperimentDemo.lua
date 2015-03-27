Lablet = {
    interface = 1.0,
    label = "Demo: Camera (low frame rate)"
}


function Lablet.buildActivity(builder)
	local takeVideoSheet = builder:create("Sheet")
	builder:add(takeVideoSheet)
	takeVideoSheet:setTitle("Low frame rate camera experiment (use the video menu to change settings):")
	cameraExperimentView = takeVideoSheet:addCameraExperiment();
	cameraExperimentView:setDescriptionText("Please take a video:")
	--cameraExperimentView:setRequestedResolution(352, 288)
	cameraExperimentView:setRecordingFrameRate(1)
	local cameraExperiment = cameraExperimentView:getExperiment()

	local experimentAnalysis = builder:create("MotionAnalysis")
	builder:add(experimentAnalysis)
	experimentAnalysis:setTitle("Analyze the Video:")
	experimentAnalysis:setExperiment(cameraExperiment)
	experimentAnalysis:setDescriptionText("Please tag data points from the video:")

	local calculateYSpeed = builder:create("CalculateYSpeed")
	calculateYSpeed:setExperiment(cameraExperiment)
	builder:add(calculateYSpeed)

	local graphSheet = builder:create("Sheet")
	builder:add(graphSheet)
	graphSheet:setTitle("Graphs")
	-- some standard graphs:
	graphSheet:addMotionAnalysisGraph(cameraExperiment):showXVsYPosition()
	graphSheet:addMotionAnalysisGraph(cameraExperiment):showTimeVsXSpeed()
	graphSheet:addMotionAnalysisGraph(cameraExperiment):showTimeVsYSpeed()
	--[[ build a custom graph, possible axis are:
	time, x-position, y-position, x-speed, y-speed
	--]]
	local graph = graphSheet:addMotionAnalysisGraph(cameraExperiment)
	graph:setTitle("Height vs. Time")
	graph:setXAxisContent("time")
	graph:setYAxisContent("y-position")
	
	local question = builder:create("Sheet")
	builder:add(question)
	question:setTitle("Potential Energy Question")
	local graph = question:addMotionAnalysisGraph(cameraExperiment)
	graph:setTitle("Height vs. Time")
	graph:setXAxisContent("time")
	graph:setYAxisContent("y-position")
	pbjQuestion = question:addPotentialEnergy1Question()
	pbjQuestion:setMass(1)
	pbjQuestion:setHeightQuestionText("What was the height of the ball at its peak?")
	pbjQuestion:setEnergyQuestionText("How much energy input enabled the ball to reach this height?")
	question:addText("Please export your data:")
    local exportButton = question:addExportButton()
    exportButton:setMailerClassName("nz.ac.auckland.lablet.mailer", "nz.ac.auckland.lablet.mailer.Mailer");
end

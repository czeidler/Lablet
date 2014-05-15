
function onBuildExperimentScript(scriptBuilder)
    -- sheet 1 (group layout)
	local sheet = scriptBuilder:create("Sheet")
	scriptBuilder:add(sheet)
	--sheet:setMainLayoutOrientation("horizontal")
	sheet:setTitle("Group Layout Demo")
	sheet:addQuestion("header line")
	-- start a new horizontal layout
	local horizontalLayout = sheet:addHorizontalGroupLayout()
        sheet:addText("left", horizontalLayout)
        -- add a vertical layout into the horizontal layout
        local verticalLayout = sheet:addVerticalGroupLayout(horizontalLayout)
            sheet:addQuestion("top", verticalLayout)
            sheet:addQuestion("bottom", verticalLayout)
	    sheet:addText("right", horizontalLayout)
	sheet:addCheckQuestion("check box")
	sheet:addQuestion("footer line")


    -- sheet 2 (empty
    local sheet = scriptBuilder:create("Sheet")
    scriptBuilder:add(sheet)

    -- sheet 3
    local takeVideoSheet = scriptBuilder:create("Sheet")
    scriptBuilder:add(takeVideoSheet)
    takeVideoSheet:setTitle("Camera Experiment:")
    cameraExperimentView = takeVideoSheet:addCameraExperiment();
    cameraExperimentView:setDescriptionText("Please take a video:")
    local cameraExperiment = cameraExperimentView:getExperiment()

    -- sheet 4
    local experimentAnalysis = scriptBuilder:create("ExperimentAnalysis")
    scriptBuilder:add(experimentAnalysis)
    experimentAnalysis:setTitle("Analyze the Video:")
    experimentAnalysis:setExperiment(cameraExperiment)
    experimentAnalysis:setDescriptionText("Please tag data points from the video:")

    -- sheet 5
    local calculateYSpeed = scriptBuilder:create("CalculateYSpeed")
    calculateYSpeed:setExperiment(cameraExperiment)
    scriptBuilder:add(calculateYSpeed)

    -- sheet 6
    local graphSheet = scriptBuilder:create("ExperimentSheet")
    scriptBuilder:add(graphSheet)
    graphSheet:setTitle("Graphs")
    -- some standard graphs:
    graphSheet:addPositionGraph(cameraExperiment)
    graphSheet:addXSpeedGraph(cameraExperiment)
    graphSheet:addYSpeedGraph(cameraExperiment)
    --[[ build a custom graph, possible axis are:
    time, x-position, y-position, x-speed, y-speed
    --]]
    local graph = graphSheet:addGraph(cameraExperiment)
    graph:setTitle("Height vs. Time")
    graph:setXAxisContent("time")
    graph:setYAxisContent("y-position")

    -- sheet 7
    local potentialEnergySheet = scriptBuilder:create("ExperimentSheet")
    scriptBuilder:add(potentialEnergySheet)
    potentialEnergySheet:setTitle("Potential Energy Question")
    local graph = potentialEnergySheet:addGraph(cameraExperiment)
    graph:setTitle("Height vs. Time")
    graph:setXAxisContent("time")
    graph:setYAxisContent("y-position")
    pbjQuestion = potentialEnergySheet:addPotentialEnergy1Question();
    pbjQuestion:setMass(1);
    pbjQuestion:setHeightQuestionText("What was the height of the ball at its peak?")
    pbjQuestion:setEnergyQuestionText("How much energy input enabled the ball to reach this height?")

end

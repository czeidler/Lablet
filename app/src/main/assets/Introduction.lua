Lablet = {
    interface = 1.0,
    title = "Demo 0: Short introduction"
}


function Lablet.buildActivity(builder)
	local sheet = builder:create("Sheet")
	--sheet:setMainLayoutOrientation("horizontal")
    builder:add(sheet)

	sheet:setTitle("Lab Activity Introduction")
	sheet:addText("Experiments in Lablet can be presented in one of the following two forms:")

	-- start a new horizontal layout
	local horizontalLayout = sheet:addHorizontalGroupLayout()

	-- add a vertical layout into the horizontal layout
	local verticalLayout = sheet:addVerticalGroupLayout(horizontalLayout)
	sheet:addHeader("Lab Activities", verticalLayout)
	sheet:addText("- Each Lab Activity can have one ore more pages", verticalLayout)
	sheet:addText("- A page can have experiments, analysis, questions, graphs and other components", verticalLayout)
	sheet:addText("- You can create you own custom Lab Activities, e.g. to design an experiment for your classes", verticalLayout)

	local verticalLayout = sheet:addVerticalGroupLayout(horizontalLayout)
	sheet:addHeader("Single Experiments", verticalLayout)
	sheet:addText("- The sensors of the tablet can be used to record data", verticalLayout)
	sheet:addText("- The recorded data can then be analysed", verticalLayout)


	sheet:addHeader("Even this introduction is a simple Lab Activity.")
	sheet:addText("A Lab Activity can have multiple pages. Once all tasks on a page are solved, you can swipe to the next page or press the \"Next\" button.")
	sheet:addCheckQuestion("Check this box to unlock the next page")

	local sheet = builder:create("Sheet")
	builder:add(sheet)

	sheet:setTitle("Thanks for trying Lablet!")
	sheet:addText("- To get a better overview of Lablet's capabilities try the other Lab Activity demos")
	sheet:addText("- To start a single experiment select \"Single Experiments\" from the action bar once you left this introduction")
end

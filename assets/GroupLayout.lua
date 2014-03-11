
function onBuildExperimentScript(scriptBuilder)
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

end

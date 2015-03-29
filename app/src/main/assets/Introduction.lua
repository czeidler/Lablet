Lablet = {
    interface = 1.0,
    label = "Demo: Group Layout (Lab Activity Script)"
}


function Lablet.buildActivity(builder)
	local sheet = builder:create("Sheet")
    builder:add(sheet)
	--sheet:setMainLayoutOrientation("horizontal")

	sheet:setTitle("Group Layout Demo")
	sheet:addHeader("This is just a one page demonstration of how questions can be layout on a sheet.")
	sheet:addText("Below you see a horizontal layout with a nested vertical layout in the middle column:")

	-- start a new horizontal layout
	local horizontalLayout = sheet:addHorizontalGroupLayout()
	sheet:addText("left column", horizontalLayout)

	-- add a vertical layout into the horizontal layout
	local verticalLayout = sheet:addVerticalGroupLayout(horizontalLayout)
	sheet:addText("top", verticalLayout)
	sheet:addText("bottom", verticalLayout)

	sheet:addText("right column", horizontalLayout)

	sheet:addCheckQuestion("This is a check box question, please tick!")
	sheet:addQuestion("A text only question, can you see?")
	sheet:addTextQuestion("A text input field. Please enter some text:")
end

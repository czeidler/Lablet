
function onBuildExperimentScript(scriptBuilder)
	questions1 = scriptBuilder:create("QuestionsComponent");
	scriptBuilder:add(questions1);
	
	questions2 = scriptBuilder:create("QuestionsComponent");
	scriptBuilder:add(questions2);
end
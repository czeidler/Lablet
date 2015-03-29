Lablet = {
    interface = 1.0,
    label = "Demo: Microphone Activity"
}


function Lablet.buildActivity(builder)
	local takeVideoSheet = builder:create("Sheet")
	builder:add(takeVideoSheet)
	takeVideoSheet:setTitle("Camera Sensor:")
	cameraExperimentView = takeVideoSheet:addCameraExperiment();
	cameraExperimentView:setDescriptionText("Please take a video:")
	local cameraExperiment = cameraExperimentView:getExperiment()

	local experimentAnalysis = builder:create("MotionAnalysis")
	builder:add(experimentAnalysis)
	experimentAnalysis:setTitle("Analyze the Video:")
	experimentAnalysis:setExperiment(cameraExperiment)
	experimentAnalysis:setDescriptionText("Please tag data points from the video:")

	local recordAudioSheet = builder:create("Sheet")
    builder:add(recordAudioSheet)
	recordAudioSheet:setTitle("Microphone Sensor:")
	experimentView = recordAudioSheet:addMicrophoneExperiment();
	experimentView:setDescriptionText("Please record some sound:")
	local micExperiment = experimentView:getExperiment()

	local experimentAnalysis = builder:create("FrequencyAnalysis")
    builder:add(experimentAnalysis)
	experimentAnalysis:setTitle("Analyze the audio recording:")
	experimentAnalysis:setExperiment(micExperiment)

	local takeAccelerometerDataSheet = builder:create("Sheet")
	builder:add(takeAccelerometerDataSheet)
	takeAccelerometerDataSheet:setTitle("Accelerometer Sensor:")
	experimentView = takeAccelerometerDataSheet:addAccelerometerExperiment();
	experimentView:setDescriptionText("Please record accelerometer data:")
	local accelerometerExperiment = experimentView:getExperiment()

	local experimentAnalysis = builder:create("AccelerometerAnalysis")
	builder:add(experimentAnalysis)
	experimentAnalysis:setTitle("Analyze the accelerometer data:")
	experimentAnalysis:setExperiment(accelerometerExperiment)
end

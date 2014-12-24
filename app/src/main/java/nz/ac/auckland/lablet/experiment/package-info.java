/**
 * All experiment related (model) classes.
 *
 * <p>
 * The class {@link nz.ac.auckland.lablet.experiment.Experiment} is used to conduct an experiment
 * This class has a list of runs {@link nz.ac.auckland.lablet.experiment.ExperimentRun}.
 * A run can be seen as an independent part of an experiment.
 * For example, to measure a certain value multiple times.
 * Usually an experiment has only one run.
 * A run can has multiple sensors {@link nz.ac.auckland.lablet.experiment.IExperimentSensor}.
 * Each sensors produces data {@link nz.ac.auckland.lablet.experiment.ISensorData}.
 *
 * When finishing an experiment all taken data is collected in the
 * {@link nz.ac.auckland.lablet.experiment.ExperimentData} class.
 * This data class has the same run structure as the {@link nz.ac.auckland.lablet.experiment.Experiment} class.
 *
 * To analyse the sensor data {@link nz.ac.auckland.lablet.experiment.ISensorData} a suitable
 * {@link nz.ac.auckland.lablet.experiment.IDataAnalysis} analysis must be used.
 * Note, that sensor data can be analysed by different analysis classes.
 * </p>
 * <p>
 * To support new sensors the {@link nz.ac.auckland.lablet.experiment.ISensorPlugin} interface has to be
 * implemented.
 * Similar {@link nz.ac.auckland.lablet.experiment.IAnalysisPlugin} needs to be implemented to support a new analysis.
 * {@link nz.ac.auckland.lablet.experiment.IAnalysisPlugin} and {@link nz.ac.auckland.lablet.experiment.ISensorPlugin}
 * have to be added to {@link nz.ac.auckland.lablet.experiment.ExperimentPluginFactory} to make them visible to Lablet.
 *
 * There is also a {@link nz.ac.auckland.lablet.experiment.IImportPlugin} interface to import raw data that not directly
 * comes from any sensor. For example, a WAV or video file.
 * </p>
 */
package nz.ac.auckland.lablet.experiment;
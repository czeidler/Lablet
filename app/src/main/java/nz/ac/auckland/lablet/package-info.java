/**
 * Lablet: Physics Experiments on the Tablet
 * <p>
 * <h2>Overview:</h2>
 * Lablet can run single experiments or lab activities. A lab activity is an "electronic" handout that is described in
 * a lua script file.
 *
 * <h3>Single Experiments:</h3>
 * <p>
 * New sensors, data types and analyses can be implemented using the plugin architecture:
 * {@link nz.ac.auckland.lablet.experiment.ISensorPlugin}, {@link nz.ac.auckland.lablet.experiment.IDataTypePlugin}
 * and {@link nz.ac.auckland.lablet.experiment.IAnalysisPlugin}. Data files can be imported by implementing
 * {@link nz.ac.auckland.lablet.experiment.IImportPlugin}.
 * There is currently one common
 * {@link nz.ac.auckland.lablet.ExperimentAnalysisActivity} to analyze experiments. This Activity displays each
 * experiment analysis in an {@link nz.ac.auckland.lablet.ExperimentAnalysisFragment}.
 * </p>
 * <p>
 * Experiments can be managed in the {@link nz.ac.auckland.lablet.ExperimentHomeActivity}. For example, to
 * start a new or resume and delete an old experiment.
 * </p>
 *
 * <h3>Lab Activities (Scripts):</h3>
 * <p>
 * It is possible to script a whole lab activity using a lua script. The script can have multiple pages including
 * questions, text boxes, experiments, analyses... The script sensors in the
 * {@link nz.ac.auckland.lablet.script.ScriptRunnerActivity}.
 * </p>
 * <p>
 * Scripts can be managed in the {@link nz.ac.auckland.lablet.ScriptHomeActivity}. For example, to
 * start a new or resume and delete an old script.
 * </p>
 */
package nz.ac.auckland.lablet;
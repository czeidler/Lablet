/**
 * Lablet: Physics Experiments on the Tablet
 * <p>
 * <h2>Overview:</h2>
 * Lablet can run single experiments or lab activities. A lab activity is an "electronic" handout that is described in
 * a lua script file.
 *
 * <h3>Single Experiments:</h3>
 * <p>
 * New experiments can be implemented using the plugin architecture:
 * {@link nz.ac.auckland.lablet.experiment.ISensorPlugin}, {@link nz.ac.auckland.lablet.experiment.IDataTypePlugin},
 * {@link nz.ac.auckland.lablet.experiment.IImportPlugin} and {@link nz.ac.auckland.lablet.experiment.IAnalysisPlugin}.
 * There is currently one common
 * {@link nz.ac.auckland.lablet.ExperimentAnalysisActivity} to analyze the experiments.
 * </p>
 * <p>
 * Experiments can be managed in the {@link nz.ac.auckland.lablet.ExperimentHomeActivity}. For example,
 * start a new or resume and delete an old experiment.
 * </p>
 *
 * <h3>Scripts (Lab Activities):</h3>
 * <p>
 * It is possible to script a whole lab activity using a lua script. The script can have multiple pages including
 * questions, text, experiments, analysis... The script sensors in the
 * {@link nz.ac.auckland.lablet.script.ScriptRunnerActivity}.
 * </p>
 * <p>
 * Scripts can be managed in the {@link nz.ac.auckland.lablet.ScriptHomeActivity}. For example,
 * start a new or resume and delete an old script.
 * </p>
 */
package nz.ac.auckland.lablet;
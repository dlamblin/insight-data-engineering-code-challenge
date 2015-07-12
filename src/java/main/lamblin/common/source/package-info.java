/**
 * The common source package sets up different kinds of iterable sources of lines of text.
 * <dl>
 *   <dt>{@link LineSource}</dt>
 *   <dd>Is the simple string iterating interface for all lines interated from sources.</dd>
 *   <dt>{@link EmptySource}</dt>
 *   <dd>Is a no-op implementation of the interface.</dd>
 *   <dt>{@link InputStreamLineSource}</dt>
 *   <dd>Uses {@link System#in} {@code (stdin)} as a {@link FileLineSource}.</dd>
 *   <dt>{@link FileLineSource}</dt>
 *   <dd>Reads a default file system file as a {@link FileLineSource}.</dd>
 *   <dt>{@link DirectoryLineSource}</dt>
 *   <dd>Reads all the files in the directory and unifies each valid {@link FileLineSource}
 *        as a {@link LineSource}.</dd>
 *   <dt>{@link NoneToManyLineSource}</dt>
 *   <dd>Unifies a list of file paths as a {@link LineSource} of files or directories.</dd>
 *   <dt>{@link ReaderLineSource}</dt>
 *   <dd>Is the implementation superclass of {@link FileLineSource} and
 *   {@link InputStreamLineSource}.</dd>
 * </dl>
 *
 * @author Daniel Lamblin
 */
package lamblin.common.source;
package org.openengsb.xlinkSQLViewer.xlink.matching;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.xlinkSQLViewer.SqlCreateViewer;
import org.openengsb.xlinkSQLViewer.exceptions.SQLFileNotWellFormedException;
import org.openengsb.xlinkSQLViewer.model.SQLCreateModel;
import org.openengsb.xlinkSQLViewer.parseUtils.SQLParseUtils;

/**
 * Searchlogic to find local Matches to potential Matches from the OpenEngSB
 */
public class MatchingLogic {

	private File workingDirectory;
	private Queue<File> directoriesAndFilesToSearch;
	private FileFilter bfsFileFilter;
	private List<MatchingResult> allCreateStatementsWithTheirFile;

	public MatchingLogic(File workingDirectory) {
		super();
		this.workingDirectory = workingDirectory;
		this.directoriesAndFilesToSearch = new LinkedList<File>();
		this.bfsFileFilter = new BFSFileFilter();
		allCreateStatementsWithTheirFile = new ArrayList<MatchingResult>();
	}

	/**
	 * Searches for all local matches to a given statement and ranks those
	 * matches after their similarity to the given statement. The highest ranked
	 * match is returned. If no match was found, null is returned. First the
	 * name of all tables are matched. If no tables are found or more than one
	 * table is found, all statements are compared for their fields.
	 */
	public MatchingResult findMostPotentialLocalMatch(SQLCreate xlinkStmt) {
		// Check for matching table names
		List<MatchingResult> matches = findAllLocalMatches(xlinkStmt);

		// There are no SQL Files in the Working Directory
		if (allCreateStatementsWithTheirFile.isEmpty()) {
			return null;
		}

		if (matches.size() == 1) {
			// One table matched, return it.
			return matches.get(0);
		} else {
			if (matches.isEmpty()) {
				// No table matched, all SQL statements will be checked.
				matches = allCreateStatementsWithTheirFile;
			}

			MatchingResult mostPotentialMatch = matches.get(0);
			int maxMatchValue = matchValueOfMatchingResult(mostPotentialMatch,
					xlinkStmt);
			for (MatchingResult match : matches) {
				int currentMatchValue = matchValueOfMatchingResult(match,
						xlinkStmt);
				if (currentMatchValue > maxMatchValue) {
					maxMatchValue = currentMatchValue;
					mostPotentialMatch = match;
				}
			}

			if (matchValueOfMatchingResult(mostPotentialMatch, xlinkStmt) == 0) {
				return null;
			}

			return mostPotentialMatch;
		}
	}

	/**
	 * Searches the working directory for local matches to a given SQLCreate
	 * object from the OpenEngSB. The search is implemented in with an BFS
	 * approach.
	 */
	private List<MatchingResult> findAllLocalMatches(SQLCreate xlinkStmt) {
		fetchFilesAndDirectoriesToSearch(workingDirectory);

		List<MatchingResult> matches = new ArrayList<MatchingResult>();
		File file;
		while ((file = directoriesAndFilesToSearch.poll()) != null) {
			if (file.isFile()) {
				List<SQLCreateModel> createStmts;
				try {
					createStmts = SQLParseUtils
							.parseCreateStatementsFromFile(file);
					int indexInFile = findIndexOfCreateStmt(createStmts,
							xlinkStmt, true);
					/*if (indexInFile == -1) {
						indexInFile = findIndexOfCreateStmt(createStmts,
								xlinkStmt, false);
					}*/
					if (indexInFile != -1) {
						matches.add(new MatchingResult(file, createStmts,
								indexInFile));
					}
					// Only add file if it conaints at least one create
					// statement
					if (!createStmts.isEmpty()) {
						allCreateStatementsWithTheirFile
								.add(new MatchingResult(file, createStmts,
										indexInFile));
					}
				} catch (IOException e) {
					SqlCreateViewer
							.writeErrorWithoutExit(
									"Error reading file, during search for local matches.",
									e);
				} catch (SQLFileNotWellFormedException e) {
					SqlCreateViewer
							.writeErrorWithoutExit(
									"Encountered an not well formed SQL file, during search for local matches.",
									e);
				}
			} else if (file.isDirectory()) {
				fetchFilesAndDirectoriesToSearch(file);
			}
		}
		return matches;
	}

	/**
	 * Adds all SQLFiles and Subdirectories to the Queue
	 */
	private void fetchFilesAndDirectoriesToSearch(File rootDirectory) {
		directoriesAndFilesToSearch.addAll(Arrays.asList(rootDirectory
				.listFiles(bfsFileFilter)));
	}

	/**
	 * Returns the index of the first SQLCreateModel object which matches the
	 * SQLCreate's object tableName
	 */
	private int findIndexOfCreateStmt(List<SQLCreateModel> createList,
			SQLCreate stmt, boolean strict) {
		int index = 0;
		for (SQLCreateModel create : createList) {
			if (strict) {
				if (create.getTableName().toLowerCase().equals(
						stmt.getTableName().toLowerCase())) {
					return index;
				}
			} else {
				if (create.getTableName().toLowerCase().contains(
						stmt.getTableName().toLowerCase())) {
					return index;
				}
			}
			index++;
		}
		return -1;
	}

	/**
	 * Returns the maximum matching value of the SQL create statements of the
	 * given File. This int value indicates, how many many SQLFiels are alike
	 * between the two given statements.
	 */
	private int matchValueOfMatchingResult(
			MatchingResult currentMatchingResult, SQLCreate xlinkStmt) {
		// If one containing SQL Statement is already matching, use this one
		if (currentMatchingResult.getIndexOfMatch() != -1) {
			return matchValueOfCreate(
					currentMatchingResult.getMatchingCreate(), xlinkStmt);
		}
		// If otherwise, compare all SQL Statements
		int maxMatchValue = matchValueOfCreate(currentMatchingResult
				.getCreateStatements().get(0), xlinkStmt);
		int matchIndex = -1;
		if (maxMatchValue > 0) {
			matchIndex = 0;
		}
		for (int i = 0; i < currentMatchingResult.getCreateStatements().size(); i++) {
			int currentMatchValue = matchValueOfCreate(currentMatchingResult
					.getCreateStatements().get(i), xlinkStmt);
			if (currentMatchValue > maxMatchValue) {
				maxMatchValue = currentMatchValue;
				matchIndex = i;
			}
		}
		currentMatchingResult.setIndexOfMatch(matchIndex);
		return maxMatchValue;
	}

	/**
	 * Returns an int value which indicates, how many many SQLFiels are alike
	 * between the two given statements.
	 */
	private int matchValueOfCreate(SQLCreateModel create, SQLCreate xlinkStmt) {
		int matchValue = 0;
		if(create.getTableName().contains(xlinkStmt.getTableName())){
			matchValue++;
		}
		for (int i = 0; i < xlinkStmt.getFields().length; i++) {
			String fieldRepresentation = xlinkStmt.getFields()[i]
					.getFieldName()
					+ " " + xlinkStmt.getFields()[i].getFieldType();
			if (create.getCreateBody().toLowerCase().contains(
					fieldRepresentation.toLowerCase())) {
				matchValue++;
			}
		}
		return matchValue;
	}

	/**
	 * FileFilter to filter all SQL Files and Subdirectories for the Searchqueue
	 */
	private class BFSFileFilter implements FileFilter {

		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			} else {
				return file.getName().endsWith(".sql");
			}
		}

	}

}

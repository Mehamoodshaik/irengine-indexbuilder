package com.mehamood.ir.project;

import java.io.File;

public class FolderIterator {

	public File[] getFileNames() {
		String folderPath = "src/main/resources/ft911"; // replace with the path to your folder

		// G:\IR Slides\Project Input
		// C:\\Users\\meham\\Downloads\\project1\\ft911
		File folder = new File(folderPath);

		if (folder.isDirectory()) {
			File[] files = folder.listFiles();

			if (files != null) {
				return files;
			}
		}
		return null;
	}

}

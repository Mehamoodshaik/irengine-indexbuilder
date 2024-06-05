package com.mehamood.ir.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyIndexer {
	static Porter stemmer = new Porter();
	static FolderIterator folderIterator = new FolderIterator();
	static StopWordFinder stopWordFinder = new StopWordFinder();
	static HashMap<String, Integer> fileDictionary = new HashMap<>();
	static HashMap<String, Integer> wordDictionary = new HashMap<>();
	static Map<String, Map<Integer, Integer>> invertedIndex = new HashMap<>();
	static Map<Integer, Map<String, Integer>> forwardIndex = new HashMap<>();

	static int wordId = 1;
	static List<String> stopWordList = stopWordFinder.getStopwords();

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		File[] fileNames = folderIterator.getFileNames(); // to get the paths of all files
		for (File file : fileNames) { // iterating through each file
			String fileName = file.toString();
			List<Document> documents = extractDocuments(fileName); // to get all documents in each file

			for (Document document : documents) { // iterating through each document

				// Uncomment below lines if you use files with document ID of format integer for
				// eg. docID= 1

				/*
				 * String fileID = document.getDocno();
				 * buildForwardAndInvertedIndices(document.getContent(), fileID);
				 */

				// Uncomment below lines if you use files with document ID of format
				// filename-integer for eg. docID= ft911-1

				String[] fileID = document.getDocno().split("-");
				buildForwardAndInvertedIndices(document.getContent(), fileID[1]);

			}

		}
		wordDictionary.remove("");

		PrintStream printStream = new PrintStream(new FileOutputStream("parser_output.txt"));
		System.setOut(printStream);
		System.out.println(
				"----------------------------------------------------Forward Index-------------------------------------------------");
		Map<Integer, Map<String, Integer>> sortedForwardIndexTreeMap = new TreeMap<>(forwardIndex);
		for (Entry<Integer, Map<String, Integer>> entry : sortedForwardIndexTreeMap.entrySet()) {
			System.out.print(entry.getKey() + ":");
			Map<String, Integer> wordFrequencyMap = forwardIndex.get(entry.getKey());
			ArrayList<String> words = new ArrayList<>(wordFrequencyMap.keySet());
			Collections.sort(words);
			for (String word : words) {
				int frequency = wordFrequencyMap.get(word);
				System.out.print(word + ":" + frequency + " ");
			}
			System.out.println();
		}

		System.out.println(
				"---------------------------------------------------Inverted Index-------------------------------------------------");

		//Remove the null character which I am not expecting in the output
		invertedIndex.remove("");
		Map<String, Map<Integer, Integer>> sortedInvertedIndexMap = new TreeMap<>(invertedIndex);

		for (Entry<String, Map<Integer, Integer>> entry : sortedInvertedIndexMap.entrySet()) {
			System.out.print(entry.getKey() + ":");
			Map<Integer, Integer> documentMap = invertedIndex.get(entry.getKey());
			ArrayList<Integer> documentIDs = new ArrayList<>(documentMap.keySet());
			Collections.sort(documentIDs);
			for (Integer documentID : documentIDs) {
				int frequency = documentMap.get(documentID);
				System.out.print(documentID + ":" + frequency + " ");
			}
			System.out.println();
		}

		System.out.println("Enter the word to search:");
		String input_word = scanner.next();
		if (!stopWordList.contains(input_word)) {
			String stem = stemmer.word_stem(input_word); // to use stemmer algorithm
			if (invertedIndex.containsKey(stem)) {
				System.out.print(stem + ":");
				Map<Integer, Integer> documentMap = invertedIndex.get(stem);
				ArrayList<Integer> documentIDs = new ArrayList<>(documentMap.keySet());
				Collections.sort(documentIDs);
				for (Integer documentID : documentIDs) {
					int frequency = documentMap.get(documentID);
					System.out.print(documentID + ":" + frequency + " ");
				}
			} else {
				System.out.println("Given Term not found in the documents");
			}
		} else {
			System.out.println("Given Term not found in the documents");
		}

		scanner.close();

	}

	// Extracting Documents from files
	public static List<Document> extractDocuments(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuilder sb = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null) {
			sb.append(line).append(System.lineSeparator());
		}

		reader.close();
		String html = sb.toString();
		List<Document> documents = new ArrayList<Document>();
		Pattern docPattern = Pattern.compile("<DOC>(.*?)</DOC>", Pattern.DOTALL);
		Matcher docMatcher = docPattern.matcher(html);

		while (docMatcher.find()) {
			String docHtml = docMatcher.group(1);
			Pattern docnoPattern = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
			Matcher docnoMatcher = docnoPattern.matcher(docHtml);
			String docno = "";

			if (docnoMatcher.find()) {
				docno = docnoMatcher.group(1);
			}

			Pattern textPattern = Pattern.compile("<TEXT>(.*?)</TEXT>", Pattern.DOTALL);
			Matcher textMatcher = textPattern.matcher(docHtml);
			String textContent = "";

			if (textMatcher.find()) {
				textContent = textMatcher.group(1);
			}

			documents.add(new Document(docno, textContent));
		}

		return documents;
	}

	public static void buildForwardAndInvertedIndices(String textContent, String fileID) {
		Map<String, Integer> stemWordMap = new HashMap<String, Integer>();
		String[] words = textContent.toLowerCase().split("[\\p{Punct}\\s]+");
		String stem = null;
		for (String word : words) {
			if (!containsNumbers(word)) { // to remove words containing numbers
				if (word != "") {
					if (!stopWordList.contains(word)) { // to remove stop words
						stem = stemmer.word_stem(word); // to use stemmer algorithm
						Integer integer = stemWordMap.get(stem);
						if (integer == null) {
							stemWordMap.put(stem, 1);
						} else {
							stemWordMap.put(stem, integer + 1);
						}
						if (!invertedIndex.containsKey(stem)) {
							invertedIndex.put(stem, new HashMap<Integer, Integer>());
						}
						Map<Integer, Integer> documentWordMap = invertedIndex.get(stem);
						if (!documentWordMap.containsKey(Integer.valueOf(fileID))) {
							documentWordMap.put(Integer.valueOf(fileID), 0);
						}
						documentWordMap.put(Integer.valueOf(fileID), documentWordMap.get(Integer.valueOf(fileID)) + 1);

					}
				}
			}

		}

		forwardIndex.put(Integer.valueOf(fileID), stemWordMap);

	}

	public static List<String> tokenizeDocument(String textContent) {
		String[] words = textContent.toLowerCase().split("[\\p{Punct}\\s]+"); // to tokenize the words
		List<String> tokens = new ArrayList<String>();
		List<String> stopWordList = stopWordFinder.getStopwords();
		for (String word : words) {
			if (!containsNumbers(word)) { // to remove words containing numbers
				if (word != "") {
					if (!stopWordList.contains(word)) { // to remove stop words
						String stem = stemmer.word_stem(word); // to use stemmer algorithm
						tokens.add(stem);
						if (!wordDictionary.containsKey(stem)) {
							wordDictionary.put(stem, wordId);
							wordId++;
						}
					}
				}
			}

		}

		return tokens;
	}

	// to remove strings with numbers
	private static boolean containsNumbers(String str) {
		for (char c : str.toCharArray()) {
			if (Character.isDigit(c)) {
				return true;
			}
		}
		return false;
	}

}

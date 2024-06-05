package com.mehamood.ir.project;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StopWordFinder {
    
    public List<String> getStopwords() {
 	   List<String> stopwordlist = null;
 	   try {
 	       String path = "src/main/resources/stopwordlist.txt";
 	       stopwordlist = new ArrayList<>();
 	       File file = new File(path);
 	       Scanner sw = new Scanner(file);
 	       while (sw.hasNextLine()) {
 	           stopwordlist.add(sw.nextLine().replaceAll("\\s",""));
 	       }
 	   } catch (FileNotFoundException exception) {
 	       System.out.println("An error occurred\n" + exception.getMessage());
 	   }
 	   return stopwordlist;
 	}
 	
}

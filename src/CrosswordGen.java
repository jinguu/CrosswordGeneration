/**
 * Created by cece on 5/4/2017.
 */

import java.io.*;

import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

import javax.swing.*;

public class CrosswordGen {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.getContentPane().setLayout(new BorderLayout());

        JPanel container = new JPanel(new FlowLayout());
        final CrosswordPanel panel = new CrosswordPanel();
        container.add(panel);
        f.getContentPane().add(container, BorderLayout.CENTER);

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String width = JOptionPane.showInputDialog("Enter width of crossword:");
                String height = JOptionPane.showInputDialog("Enter height of crossword:");
                generate(panel, Integer.parseInt(width), Integer.parseInt(height));
            }
        });
        f.getContentPane().add(generateButton, BorderLayout.SOUTH);

        f.setSize(500, 500);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static Random random = new Random(0);
    private static void generate(CrosswordPanel panel, int width, int height) {
        int w = width ;
        int h = height ;
        char crossword[][] = new char[w][h];
        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++)
            {
                if (random.nextFloat() > 0.2)
                {
                    crossword[x][y] = 1;
                }
                else {
                    char c = 0;
                    crossword[x][y] = 0;
                }
            }
        }

        System.out.println(Arrays.deepToString(crossword));
        panel.setCrossword(crossword);
    }

}


class CrosswordPanel extends JPanel {
    private JTextField textFields[][];
    private char[][] crossword;
    private int w;
    private int h;
    private ArrayList<ArrayList<Pair>> emptySpaces;
    ArrayList<String> words;
    ArrayList<String> wordsLeft;

    CrosswordPanel () {
        words = parseJSON("src/json/common.json");
    }

    void setCrossword(char array[][]) {
        crossword = array;
        removeAll();
        w = array.length;
        h = array[0].length;
        setLayout(new GridLayout(w, h));
        emptySpaces = new ArrayList<>();


        /*TODO: unify two for loops*/
        for (int x=0; x<w; x++) {
            Pair<Integer, Integer> start = new Pair(x,0);
            int len = 0;
            for (int y=0; y<h; y++) {
                char c = array[x][y];
                if (c == (char) 0 ) { // reached null
                    if(len > 1) { // word is longer than 1
                        Pair<Integer, Integer> end = new Pair(x,y-1);
                        ArrayList<Pair> toAdd = new ArrayList<Pair>(2);
                        toAdd.add(start);
                        toAdd.add(end);
                        emptySpaces.add(toAdd);

                        // reset
                        start = new Pair(x,y+1);
                        len = 0;
                    }
                    else {
                        start = new Pair(x,y+1);
                        len = 0;
                    }
                }
                else if(y == h-1) { // reached end of row
                    if((y - start.getValue() + 1 > 1)) { // word is longer than 1
                        Pair<Integer, Integer> end = new Pair(x,y);
                        ArrayList<Pair> toAdd = new ArrayList<Pair>(2);
                        toAdd.add(start);
                        toAdd.add(end);
                        emptySpaces.add(toAdd);

                        // reset
                        start = new Pair(x,y+1);
                        len = 0;
                    }
                    else {
                        len = 0;
                    }
                }
                else {
                    len++;
                }
            }
        }

        for (int y=0; y<h; y++) {
            Pair<Integer, Integer> start = new Pair(0,y);
            int len = 0;
            for (int x=0; x<w; x++) {
                char c = array[x][y];

                if (c == 0) { // reached null
                    if(len > 1) { // word is longer than 1
                        Pair<Integer, Integer> end = new Pair(x-1,y);
                        ArrayList<Pair> toAdd = new ArrayList<Pair>(2);
                        toAdd.add(start);
                        toAdd.add(end);
                        emptySpaces.add(toAdd);

                        // reset4
                        start = new Pair(x+1,y);
                        len = 0;
                    }
                    else {
                        start = new Pair(x+1,y);
                        len = 0;
                    }
                }
                else if(x == w-1) { // reached end of col
                    if((x - start.getKey() + 1 > 1)) { // word is longer than 1
                        Pair<Integer, Integer> end = new Pair(x,y);
                        ArrayList<Pair> toAdd = new ArrayList<Pair>(2);

                        toAdd.add(start);
                        toAdd.add(end);
                        emptySpaces.add(toAdd);

                        // reset
                        start = new Pair(x+1,y);
                        len = 0;
                    }
                    else {
                        len = 0;
                    }
                }
                else {
                    len++;
                }
            }
        }

        System.out.println(emptySpaces);
        fillCrossword();
        getParent().validate();
        repaint();
    }

    void fillCrossword() {
        wordsLeft = parseJSON("src/json/common.json");
        System.out.println(crossword.toString());
        textFields = new JTextField[w][h];

        for (ArrayList<Pair> arr : emptySpaces) {
            String dir = "";
            System.out.println(arr);
            if (arr.get(0).getValue() == arr.get(1).getValue()) {
                dir = "down";
            } else if (arr.get(0).getKey() == arr.get(1).getKey()) {
                dir = "across";
            }
            String choice = "";
            Collections.shuffle(wordsLeft);
            for (String word : wordsLeft) {
                if (canPlaceWord(arr.get(0), arr.get(1), word) && choice == "") {
                    choice = word;
                    break;
                }
            }
            System.out.println(choice);
            if (dir.equals("across")) {
                int counter = 0;
                for (int i = (int) arr.get(0).getValue(); i < ((int) arr.get(0).getValue() + choice.length()); i++) {
                    crossword[(int) arr.get(0).getKey()][i] = choice.charAt(counter);
                    counter++;
                }
            }
            if (dir.equals("down")) {
                int counter = 0;
                for (int i = (int) arr.get(0).getKey(); i < ((int) arr.get(0).getKey() + choice.length()); i++) {
                    crossword[i][(int) arr.get(0).getValue()] = choice.charAt(counter);
                    counter++;
                }
            }
            wordsLeft.remove(choice);
        }

        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                if(crossword[i][j] != 0 && crossword[i][j] != 1) {
                    textFields[i][j] = new JTextField(Character.toString(crossword[i][j]));
                    textFields[i][j].setFont(textFields[i][j].getFont().deriveFont(20.0f));
                    // System.out.println("set " + crossword[i][j] + " at " + i + ", " + j);
                    add(textFields[i][j]);
                }
                else if(crossword[i][j] == 0) {
                    add(new JLabel());
                }
                else if(crossword[i][j] == 1) {
                    textFields[i][j] = new JTextField(" ");
                    textFields[i][j].setFont(textFields[i][j].getFont().deriveFont(20.0f));
                    // System.out.println("set blank at " + i + ", " + j);
                    add(textFields[i][j]);
                }
            }
        }

    }

    void evalAndBacktrack() {
        int backtracked = 0;
        ArrayList<Pair> pattern = new ArrayList<>(); // space with least options
        int minOptions = words.size();
        int num;
        int branchingFactor = words.size()/(crossword.length*crossword[0].length);


        while(checkSpaces() != 0 || backtracked < 30) {
            //finds the space with the least word options to initialize next
            for (ArrayList<Pair> space : emptySpaces){
                if ( (num = checkWordsAvailable(space.get(0),space.get(1))) < minOptions){
                    minOptions = num;
                    pattern = space;
                }
            }
            String dir = "";
            if (pattern.get(0).getValue() == pattern.get(1).getValue()) {
                dir = "down";
            } else if (pattern.get(0).getKey() == pattern.get(1).getKey()) {
                dir = "across";
            }
            // choose a random word for pattern
            String choice = "";
            for (String word : wordsLeft) {
                if (canPlaceWord(pattern.get(0), pattern.get(1), word) && choice == "") {
                    choice = word;
                    break;
                }
            }
            wordsLeft.remove(choice);
            // still matching words, find next matching

            // out of matching words
            if(minOptions == 0) {
                // backtrack
            }
        }
    }

    ArrayList<String> parseJSON(String json) {
        JSONParser parser = new JSONParser();
        ArrayList<String> words = new ArrayList<String>();

        try {
            Object obj = parser.parse(new BufferedReader(new FileReader( json )));

            JSONObject jsonObject = (JSONObject) obj;
            Object[] dict = ((JSONArray) jsonObject.get("dict")).toArray();
            for(Object word: dict) {
                words.add((String)word);
            }

            /*while (i.hasNext()) {
                System.out.println(i.toString());
                words.add(i.toString());
            }*/

        } catch (FileNotFoundException ex) {
            System.out.println("file not found");
        } catch (IOException ie) {
            System.out.println("input error");
        } catch (ParseException pe) {
            System.out.println("parse error");
            pe.printStackTrace();
        }

        return words;
    }

    boolean canPlaceChar(int row, int col, char c) {
        if(crossword[row][col] == 1 || crossword[row][col] == c) {
            return true;
        }
        return false;
    }

    boolean canPlaceWord(Pair<Integer, Integer> start, Pair<Integer, Integer> end, String word) {
        String dir = "";
        if(start.getKey() == end.getKey()) { // same x = horizontal
            dir = "across";
        }
        else { // same y = vertical
            dir = "down";
        }
        if(start.getKey() < 0 || start.getKey() >= w || start.getValue() < 0 || start.getValue() >= h) {
            System.out.println("out of bounds");
            return false;
        }
        //System.out.println(word);
        if (dir.equals("down")) {
            int length = end.getKey() - start.getKey() + 1;
            if(word.length() == length) { // word fits space
                int counter = 0;
                for(int i = start.getKey(); i<=end.getKey(); i++) {
                    // check if placing this word will complete a word - i.e. all cols left and right are full
                    StringBuilder colsLeft = new StringBuilder("");
                    StringBuilder colsRight = new StringBuilder("");
                    if(!canPlaceChar(i,start.getValue(),word.charAt(counter))) {
                        return false;
                    }
                    System.out.println(word);
                    if (start.getValue() > 0) { // check cols left
                        for (int col = start.getValue()-1; col >= 0; col--) {
                            if (crossword[i][col] != 1) {
                                if(crossword[i][col] != 0)
                                    colsLeft.insert(0, Character.toString(crossword[i][col]));
                            }
                            if (crossword[i][col] == 0) { // you hit a null
                                String str = colsLeft.toString().concat(Character.toString(word.charAt(counter))).concat(colsRight.toString());
                                System.out.println("str: " + str);
                                if (str.length() >= w - col - 1 && str.length() > 1 && !words.contains(str)) {
                                    System.out.println("false");
                                    return false;
                                }
                                else {
                                    break;
                                }
                            }
                        }
                        System.out.println("colsLeft: " + colsLeft);
                    }
                    if (start.getValue() < w-1) { // check cols right
                        for (int col = start.getValue()+1; col < w; col++) {
                            if (crossword[i][col] != 1) {
                                if(crossword[i][col] != 0)
                                    colsRight.append(Character.toString(crossword[i][col]));
                            }
                            if (crossword[i][col] == 0) { // you hit a null
                                String str = colsLeft.toString().concat(Character.toString(word.charAt(counter))).concat(colsRight.toString());
                                System.out.println("str: " + str + str.length());
                                if (str.length() >= w-(w-col) && str.length() > 1 && !words.contains(str)) {
                                    System.out.println("false");
                                    return false;
                                }
                                else {
                                    break;
                                }
                            }
                        }
                        System.out.println("colsRight: " + colsRight);
                    }
                    String str = colsLeft.toString().concat(Character.toString(word.charAt(counter))).concat(colsRight.toString());
                    System.out.println("str: " + str);
                    if (str.length() >= w && !words.contains(str)) {
                        System.out.println(words.contains(str));
                        System.out.println("false");
                        return false;
                    }
                    counter++;
                }
            }
            else {
                return false;
            }
        }
        else if (dir.equals("across")) {
            System.out.println(word);
            int length = end.getValue() - start.getValue() + 1;
            if(word.length() == length) { // word fits space
                int counter = 0;
                for(int i = start.getValue(); i<=end.getValue(); i++) {
                    StringBuilder rowsAbove = new StringBuilder("");
                    StringBuilder rowsBelow = new StringBuilder("");
                    if(!canPlaceChar(start.getKey(),i,word.charAt(counter))) {
                        return false;
                    }
                    // check if placing this word will complete a word - i.e. all rows above and below are full
                    if (start.getKey() > 0) { // check rows above
                        for (int row = start.getKey()-1; row >= 0; row--) {
                            if (crossword[row][i] != 1) {
                                if(crossword[row][i] != 0)
                                    rowsAbove.insert(0, Character.toString(crossword[row][i]));
                            }
                            if (crossword[row][i] == 0) { // you hit a null
                                String str = rowsAbove.toString().concat(Character.toString(word.charAt(counter))).concat(rowsBelow.toString());
                                System.out.println("str: " + str + " " + str.length());
                                if (str.length() >= h-row-1 && str.length() > 1 && !words.contains(str)) {
                                    System.out.println("false");
                                    return false;
                                }
                                else {
                                    break;
                                }
                            }
                        }
                        System.out.println("rowsabove: " + rowsAbove);
                    }
                    if (start.getKey() < h-1) { // check rows below
                        System.out.println("start: " + start.toString() + " end: " + end.toString());
                        for (int row = start.getKey()+1; row < h; row++) {
                            if (crossword[row][i] != 1) {
                                if (crossword[row][i] != 0)
                                    rowsBelow.append(Character.toString(crossword[row][i]));
                            }
                            if (crossword[row][i] == 0) { // you hit a null
                                String str = rowsAbove.toString().concat(Character.toString(word.charAt(counter))).concat(rowsBelow.toString());
                                str.concat(rowsBelow.toString());
                                System.out.println("str: " + str);
                                if (str.trim().length() >= h-(h-row) && str.length() > 1 && !words.contains(str)) {
                                    System.out.println("false");
                                    return false;
                                }
                                else {
                                    break;
                                }
                            }

                        }
                        System.out.println("rowsBelow: " + rowsBelow);
                    }
                    String str = rowsAbove.toString().concat(Character.toString(word.charAt(counter))).concat(rowsBelow.toString());
                    if (str.trim().length() >= h && !words.contains(str)) {
                        System.out.println("false");
                        return false;
                    }
                    counter++;
                }
            }else {
                return false;
            }
        }
        return true;

    }

    char[][] getCrossword() {
        int w = textFields.length;
        int h = textFields[0].length;
        char crossword[][] = new char[w][h];

        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                if (textFields[x][y] != null) {
                    String s = textFields[x][y].getText();
                    if (s.length() > 0) {
                        crossword[x][y] = s.charAt(0);
                    }

                }
            }
        }
        return crossword;
    }

    int checkWordsAvailable(Pair<Integer, Integer> start, Pair<Integer, Integer> end) {
        int count = 0;
        for(String s: wordsLeft) {
            if(canPlaceWord(start, end, s))
                count++;
        }
        return count;
    }

    int checkSpaces() {
        int count = 0;
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++){
                 if (crossword[row][col] == 1){
                     count++;
                 }
            }
        }
        return count;
    }

    void generateCrossword() {
    }

}
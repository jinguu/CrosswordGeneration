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

        f.setSize(700, 700);
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
                    char c = 1;
                    crossword[x][y] = c;
                }
                else {
                    char c = 0;
                    crossword[x][y] = 0;
                }
            }
        }

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
                if (c == 0 ) { // reached null
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
                }
                else if(y == h-1) { // reached end of row
                    if(len > 1) { // word is longer than 1
                        Pair<Integer, Integer> end = new Pair(x,y);
                        ArrayList<Pair> toAdd = new ArrayList<Pair>(2);
                        toAdd.add(start);
                        toAdd.add(end);
                        emptySpaces.add(toAdd);

                        // reset
                        start = new Pair(x,y+1);
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
                if (c == 0 ) { // reached null
                    if(len > 1) { // word is longer than 1
                        Pair<Integer, Integer> end = new Pair(x-1,y);
                        ArrayList<Pair> toAdd = new ArrayList<Pair>(2);
                        toAdd.add(start);
                        toAdd.add(end);
                        emptySpaces.add(toAdd);

                        // reset
                        start = new Pair(x+1,y);
                        len = 0;
                    }
                }
                else if(x == w-1) { // reached end of col
                    if(len > 1) { // word is longer than 1
                        Pair<Integer, Integer> end = new Pair(x,y);
                        ArrayList<Pair> toAdd = new ArrayList<Pair>(2);
                        toAdd.add(start);
                        toAdd.add(end);
                        emptySpaces.add(toAdd);

                        // reset
                        start = new Pair(x+1,y);
                        len = 0;
                    }
                }
                else {
                    len++;
                }
            }
        }


        fillCrossword();

        getParent().validate();
        repaint();
    }

    void fillCrossword() {
        textFields = new JTextField[w][h];
        words = parseJSON("src/json/wordlist.json");

        for(ArrayList<Pair> arr : emptySpaces) {
            String dir = "";
            System.out.println(arr);
            if(arr.get(0).getValue() == arr.get(1).getValue()) {
                dir = "down";
            }
            else if(arr.get(0).getKey() == arr.get(1).getKey()) {
                dir = "across";
            }
            System.out.println(arr);
            String choice = "";
            for(String word : words) {
                if(canPlaceWord(arr.get(0), arr.get(1), word) && choice == "") {
                    choice = word;
                }
            }
            System.out.println(dir);
            if (dir.equals("across")) {
                int counter = 0;
                for(int i= (int) arr.get(0).getValue(); i<((int) arr.get(0).getValue()+choice.length()); i++) {
                    System.out.println("set " + choice.charAt(counter) + " at " + (int) arr.get(0).getValue() + ", " + i);
                    crossword[(int) arr.get(0).getKey()][i] = choice.charAt(counter);
                    counter++;
                }
            }
            if (dir.equals("down")) {
                int counter = 0;
                for(int i= (int) arr.get(0).getKey(); i<((int) arr.get(1).getKey() + choice.length()); i++) {
                    System.out.println("set " + choice.charAt(counter) + " at " + i + ", " + (int) arr.get(0).getValue());
                    crossword[i][(int) arr.get(0).getValue()] = choice.charAt(counter);
                    counter++;
                }
            }
            words.remove(choice);
        }

        for(int i=0; i<h; i++) {
            for(int j=0; j<w; j++) {
                if(crossword[j][i] != 0 && crossword[j][i] != 1) {
                    textFields[j][i] = new JTextField(Character.toString(crossword[j][i]));
                    textFields[j][i].setFont(textFields[j][i].getFont().deriveFont(20.0f));
                    add(textFields[j][i]);
                }
            }
        }

    }

    ArrayList<String> parseJSON(String json) {
        JSONParser parser = new JSONParser();
        ArrayList<String> words = new ArrayList<String>();

        try {
            Object obj = parser.parse(new BufferedReader(new FileReader( json )));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray dict = (JSONArray) jsonObject.get("dict");
            Iterator i = dict.iterator();

            while (i.hasNext()) {
                JSONObject word = (JSONObject) i.next();
                words.add(word.get("word").toString());
            }

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
        if(crossword[row][col] == 0 || crossword[row][col] == c) {
            return true;
        }
        return false;
    }

    boolean canPlaceWord(Pair<Integer, Integer> start, Pair<Integer, Integer> end, String word) {
        String dir = "";
        if(start.getValue() == end.getValue()) { // same y = horizontal
            dir = "across";
        }
        else { // same x = vertical
            dir = "down";
        }
        if(start.getKey() < 0 || start.getKey() >= w || start.getValue() < 0 || start.getValue() >= h) {
            System.out.println("out of bounds");
            return false;
        }

        if (dir.equals("across")) {
            int length = end.getKey() - start.getKey() + 1;
            if(word.length() == length) { // word fits space
                for(int i = start.getKey(); i<end.getKey(); i++) {
                    // check if placing this word will complete a word - i.e. all rows above and below are full
                    String rowsAbove = "";
                    String rowsBelow = "";
                    if (start.getValue() > 0) { // check rows above
                        for (int row = 0; row < start.getValue(); row++) {
                            if (crossword[row][start.getValue()] != 1)
                                rowsAbove.concat(Character.toString(crossword[row][start.getValue()]));
                        }
                    }
                    if (start.getValue() < h) { // check rows below
                        for (int row = start.getValue(); row < h; row++) {
                            if (crossword[row][start.getValue()] != 1)
                                rowsBelow.concat(Character.toString(crossword[row][start.getValue()]));
                        }
                    }
                    String str = rowsAbove + crossword[i][start.getValue()] + rowsBelow;
                    if (str.length() >= h && !words.contains(str)) {
                        return false;
                    }
                }
            }
        }
        else if (dir.equals("down")) {
            int length = end.getValue() - start.getValue() + 1;
            if(word.length() == length) { // word fits space
                for(int i = start.getValue(); i<end.getValue(); i++) {
                    // check if placing this word will complete a word - i.e. all cols left and right are full
                    String colsLeft = "";
                    String colsRight = "";
                    if (start.getKey() > 0) { // check cols left
                        for (int col = 0; col < start.getKey(); col++) {
                            if (crossword[start.getKey()][col] != 1)
                                colsLeft.concat(Character.toString(crossword[start.getKey()][col]));
                        }
                    }
                    if (start.getValue() < h) { // check rows below
                        for (int col = start.getKey(); col < w; col++) {
                            if (crossword[start.getKey()][col] != 1)
                                colsRight.concat(Character.toString(crossword[start.getKey()][col]));
                        }
                    }
                    String str = colsLeft + crossword[i][start.getValue()] + colsRight;
                    if (str.length() >= h && !words.contains(str)) {
                        return false;
                    }
                }
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

}
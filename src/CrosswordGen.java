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
                    char c = (char)('A' + random.nextInt(26));
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

    void setCrossword(char array[][]) {
        crossword = array;
        removeAll();
        w = array.length;
        h = array[0].length;
        setLayout(new GridLayout(w, h));
        emptySpaces = new ArrayList<>();


        /*TODO: unify two for loops*/

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

        fillCrossword();

        getParent().validate();
        repaint();
    }

    void fillCrossword() {
        textFields = new JTextField[w][h];

        JSONParser parser = new JSONParser();
        ArrayList<String> words = new ArrayList<String>();

        try {
            Object obj = parser.parse(new BufferedReader(new FileReader( "src/json/wordlist.json" )));

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

        for(ArrayList<Pair> arr : emptySpaces) {
            if(arr.get(0).getKey().equals(arr.get(1).getKey())) { // same x
                int length = (int) arr.get(1).getValue()+1;
                int row = (int) arr.get(0).getKey();

                for(String word : words) {
                    int counter = 0;
                    if(word.length() == length) { // word fits
                        for(int i= (int) arr.get(0).getValue(); i< length; i++) {
                            textFields[row][i] = new JTextField(word.charAt(counter));
                            textFields[row][i].setFont(textFields[row][i].getFont().deriveFont(20.0f));
                            add(textFields[row][i]);
                            counter++;
                        }
                    }
                }
            }
            else { // same y
                int length = (int) arr.get(1).getKey()+1;
                int col = (int) arr.get(0).getValue();

                for(String word : words) {
                    int counter = 0;
                    if(word.length() == length) { // word fits
                        for(int i= (int) arr.get(0).getKey(); i< (int) arr.get(1).getKey(); i++) {
                            textFields[i][col] = new JTextField(word.charAt(counter));
                            textFields[i][col].setFont(textFields[i][col].getFont().deriveFont(20.0f));
                            add(textFields[i][col]);
                            counter++;
                        }
                    }
                }
            }

        }

/*
        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                char c = crossword[x][y];
                if (c != 0) {
                    textFields[x][y] = new JTextField(String.valueOf(c));
                    textFields[x][y].setFont(textFields[x][y].getFont().deriveFont(20.0f));
                    add(textFields[x][y]);
                }
                else {
                    add(new JLabel());
                }
            }
        }*/
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
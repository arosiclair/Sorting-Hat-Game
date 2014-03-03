package sorting_hat_level_editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 * This program serves as a level editor for The Sorting Hat game. It is
 * capable of making and saving new .sort levels, as well as opening,
 * editing, and saving existing ones. Note that a .sort level does not 
 * arrange tiles, it just specifies where tiles may be arranged. Tile
 * arrangement should be done semi-randomly.
 * 
 * Note that we have designed this level editor such that the entire
 * program is defined inside this one class using inner classes for 
 * all event handlers and the renderer.
 * 
 * @author Richard McKenna
 */
public class SortingHatLevelEditor
{
    // INITIALIZATION CONSTANTS
    
    // THE SORTING HAT CURRENTLY ONLY HAS 4 SORTING ALGORITHMS
    private final String[] ALGORITHM_NAMES = {  "BUBBLE_SORT",
                                                "SELECTION_SORT"};
    
    // THESE CONSTANTS ARE FOR CUSTOMIZATION OF THE GRID
    private final int INIT_GRID_DIM = 3;
    private final int MIN_GRID_DIM = 1;
    private final int MAX_GRID_ROWS = 4;
    private final int MAX_GRID_COLUMNS = 9;
    private final int GRID_STEP = 1;
    private final int TOTAL_TILES = 25;
    private final int MIN_SNAKE_SIZE = 2;

    // TEXTUAL CONSTANTS
    private final String WINDOW_ICON_FILE_NAME = "../TheSortingHat/img/sorting_hat/LevelEditorWindowIcon.png";
    private final String APP_TITLE = "The Sorting Hat Level Editor";
    private final String RESET_BUTTON_TEXT = "Reset";
    private final String OPEN_BUTTON_TEXT = "Open";
    private final String SAVE_AS_BUTTON_TEXT = "Save As";
    private final String COLUMNS_LABEL_TEXT = "Columns: ";
    private final String ROWS_LABEL_TEXT = "Rows: ";
    private final String ALGORITHM_LABEL_TEXT = "Sorting Algorithm: ";
    private final String TILES_REMAINING_LABEL_TEXT = "Tiles Remaining: ";
    private final String SORTING_HAT_DATA_DIR = "../TheSortingHat/data/sorting_hat/";
    private final String SORT_FILE_EXTENSION = ".sort";
    private final String OPEN_FILE_ERROR_FEEDBACK_TEXT = "File not loaded: .sort files only";
    private final String SAVE_AS_ERROR_FEEDBACK_TEXT = "File not saved: must use .sort file extension";
    private final String FILE_LOADING_SUCCESS_TEXT = " loaded successfully";
    private final String FILE_READING_ERROR_TEXT = "Error reading from ";
    private final String FILE_WRITING_ERROR_TEXT = "Error writing to ";
    
    // CONSTANTS FOR FORMATTING THE GRID
    private final Color  GRID_BACKGROUND_COLOR = new Color(238, 238, 238);

    // INSTANCE VARIABLES
    
    // HERE ARE THE UI COMPONENTS
    private JFrame                  window;
    private JPanel                  westPanel;
    private JButton                 openButton;
    private JButton                 saveAsButton;
    private JLabel                  columnsLabel;
    private JSpinner                columnsSpinner;
    private JLabel                  rowsLabel;
    private JSpinner                rowsSpinner;
    private JLabel                  tilesRemainingLabel;
    private JLabel                  algorithmLabel;
    private JComboBox               algorithmComboBox;
    private DefaultComboBoxModel    algorithmComboBoxModel;
    private JButton                 resetButton;

    // WE'LL RENDER THE GRID IN THIS COMPONENT
    private GridRenderer gridRenderer;

    // AND HERE IS THE GRID WE'RE MAKING
    private int gridColumns;
    private int gridRows;
    private LinkedList<Cell> snake;
    private boolean grid[][];
    
    // THIS KEEPS TRACK OF THE NUMBER OF TILES
    // WE STILL HAVE TO PLACE
    private int tilesRemaining;
    
    // THIS WILL LET THE USER SELECT THE FILES TO READ AND WRITE
    private JFileChooser fileChooser;
    
    // THIS WILL HELP US LIMIT THE USER FILE SELECTION CHOICES
    private FileFilter sortFileFilter;
    
    /**
     * This method initializes the level editor application, setting
     * up all data an UI components for use.
     */
    private void init()
    {
        // INIT THE EDITOR APP'S CONTAINER
        initWindow();
        
        // INITIALIZES THE GRID DATA
        initData();

        // LAYOUT THE INITIAL CONTROLS
        initGUIControls();
        
        // HOOK UP THE EVENT HANDLERS
        initHandlers();
        
        // INITIALIZE
        initFileControls();
    }
 
    /**
     * Initializes the window.
     */
    private void initWindow()
    {
        // MAKE THE WINDOW AND SET THE WINDOW TITLE
        window = new JFrame(APP_TITLE);

        // THEN LOAD THE IMAGE
        try
        {
            File imageFile = new File(WINDOW_ICON_FILE_NAME);
            Image windowImage = ImageIO.read(imageFile);
            MediaTracker mt = new MediaTracker(window);
            mt.addImage(windowImage, 0);
            mt.waitForAll();
            window.setIconImage(windowImage);
        }
        catch (Exception e)
        {
            // WE CAN LIVE WITHOUT THE ICON IMAGE IN CASE AN ERROR HAPPENS,
            // SO WE'LL JUST SQUELCH THIS EXCEPTION
        }
        
        // MAKE IT FULL SCREEN
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // JUST CLOSE WHEN SOMEONE HITS X
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Initializes the app data.
     */
    private void initData()
    {
        // START OUT OUR GRID WITH DEFAULT DIMENSIONS
        gridColumns = INIT_GRID_DIM;
        gridRows = INIT_GRID_DIM;

        // THIS WILL STORE THE PATTERN
        snake = new LinkedList();
        
        // NOW MAKE THE GRID EMPTY TO START
        grid = new boolean[gridColumns][gridRows];

        // AND RESET THE CONTENTS
        resetData();
    }

    /**
     * Resets the grid and clears the snake.
     */
    private void resetData()
    {
        // CLEAR THE PATTERN
        snake.clear();
        
        // RESET THE GRID
        for (int i = 0; i < gridColumns; i++)
        {
            for (int j = 0; j < gridRows; j++)
            {
                grid[i][j] = false;
            }
        }
        
        // WE HAVEN'T PLACED ANY TILE LOCATIONS YET
        tilesRemaining = TOTAL_TILES;
    }

    /**
     * Constructs and lays out all UI controls.
     */
    private void initGUIControls()
    {
        // ALL THE GRID DIMENSIONS CONTROLS GO IN THE WEST
        westPanel = new JPanel();
        westPanel.setLayout(new GridBagLayout());

        // WE HAVE 2 SPINNERS FOR UPDATING THE GRID DIMENSIONS, THESE
        // MODELS SPECIFY HOW THEY GET INITIALIZED AND THEIR VALUE BOUNDARIES
        SpinnerModel columnsSpinnerModel = new SpinnerNumberModel(  INIT_GRID_DIM, 
                                                                    MIN_GRID_DIM,
                                                                    MAX_GRID_COLUMNS,
                                                                    GRID_STEP);
        SpinnerModel rowsSpinnerModel = new SpinnerNumberModel( INIT_GRID_DIM,
                                                                MIN_GRID_DIM,
                                                                MAX_GRID_ROWS,
                                                                GRID_STEP);

        // CONSTRUCT ALL THE WEST TOOLBAR COMPONENTS
        openButton = new JButton(OPEN_BUTTON_TEXT);
        saveAsButton = new JButton(SAVE_AS_BUTTON_TEXT);
        saveAsButton.setEnabled(false);
        columnsLabel = new JLabel(COLUMNS_LABEL_TEXT);
        columnsSpinner = new JSpinner(columnsSpinnerModel);
        rowsLabel = new JLabel(ROWS_LABEL_TEXT);
        rowsSpinner = new JSpinner(rowsSpinnerModel);
        tilesRemainingLabel = new JLabel();
        algorithmLabel = new JLabel(ALGORITHM_LABEL_TEXT);
        algorithmComboBoxModel = new DefaultComboBoxModel();
        for (int i = 0; i < ALGORITHM_NAMES.length; i++)
            algorithmComboBoxModel.addElement(ALGORITHM_NAMES[i]);
        algorithmComboBox = new JComboBox(algorithmComboBoxModel);
        resetButton = new JButton(RESET_BUTTON_TEXT);
        
        // MAKE SURE THIS LABEL HAS THE CORRECT TEXT
        updateTilesRemainingLabel();

        // NOW PUT ALL THE CONTROLS IN THE WEST TOOLBAR
        addToWestPanel(openButton,          0, 0, 1, 1);
        addToWestPanel(saveAsButton,        1, 0, 1, 1);
        addToWestPanel(columnsLabel,        0, 1, 1, 1);
        addToWestPanel(columnsSpinner,      1, 1, 1, 1);
        addToWestPanel(rowsLabel,           0, 2, 1, 1);
        addToWestPanel(rowsSpinner,         1, 2, 1, 1);
        addToWestPanel(tilesRemainingLabel, 0, 3, 2, 1);
        addToWestPanel(algorithmLabel,      0, 4, 1, 1);
        addToWestPanel(algorithmComboBox,   1, 4, 1, 1);
        addToWestPanel(resetButton,         0, 5, 2, 1);

        // THIS GUY RENDERS OUR GRID
        gridRenderer = new GridRenderer();
        
        // PUT EVERYTHING IN THE FRAME
        window.add(westPanel, BorderLayout.WEST);
        window.add(gridRenderer, BorderLayout.CENTER);
    }

    /**
     * This method updates the display of the label in the west that
     * displays how many tiles are left to be located. This needs to
     * be called after every time the grid dimensions change or when
     * a new .zom file is loaded.
     */
    private void updateTilesRemainingLabel()
    {
        tilesRemainingLabel.setText(TILES_REMAINING_LABEL_TEXT + tilesRemaining);
    }
    
    /**
     * This helper method assists in using Java's GridBagLayout to arrange
     * components inside the west panel.
     */
    private void addToWestPanel(JComponent comp, int initGridX, int initGridY, int initGridWidth, int initGridHeight)
    {
        // GridBagLayout IS A JAVA LayoutManager THAT CAN BE USED TO
        // ARRANGE COMPONENTS IN A MULTI-DIMENSIONAL GRID WITH COMPONENTS
        // SPANNING MULTIPLE CELLS. FIRST WE INIT THE SETTINGS
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = initGridX;
        gbc.gridy = initGridY;
        gbc.gridwidth = initGridWidth;
        gbc.gridheight = initGridHeight;
        gbc.insets = new Insets(10, 10, 5, 5);
        
        // THEN USING THOSE SETTINGS WE PUT THE COMPONENT IN THE PANEL
        westPanel.add(comp, gbc);
    }

    /**
     * This method initializes all the event handlers needed by 
     * this application.
     */
    private void initHandlers()
    {
        // WE'LL RESPOND TO WHEN THE USER CHANGES THE
        // GRID DIMENSIONS USING THE SPINNERS
        GridSizeHandler gsh = new GridSizeHandler();
        columnsSpinner.addChangeListener(gsh);
        rowsSpinner.addChangeListener(gsh);
        
        // AND THE BUTTON FOR RESETTING THE GRID
        ResetHandler rh = new ResetHandler();
        resetButton.addActionListener(rh);

        // WE'LL UPDATE THE CELL-TILE COUNTS WHEN THE
        // USER CLICKS THE MOUSE ON THE RENDERING PANEL
        GridClickHandler gch = new GridClickHandler();
        gridRenderer.addMouseListener(gch);

        // WE'LL LET THE USER SELECT AND THEN WE'LL OPEN
        // A .ZOM FILE WHEN THE USER CLICKS ON THE OPEN BUTTON
        OpenLevelHandler olh = new OpenLevelHandler();
        openButton.addActionListener(olh);
        
        // AND WE'LL SAVE THE CURRENT LEVEL WHEN THE USER
        // PRESSES THE SAVE AS BUTTON
        SaveAsLevelHandler salh = new SaveAsLevelHandler();
        saveAsButton.addActionListener(salh);
    }

    /**
     * This method initializes the file chooser and the file filter
     * for that control so that the user may select files for saving
     * and loading.
     */
    public void initFileControls()
    {
        // INIT THE FILE CHOOSER CONTROL
        fileChooser = new JFileChooser(SORTING_HAT_DATA_DIR);
        
        // AND THE FILE FILTER, WE'LL DEFINE A SIMPLE
        // ANONYMOUS TYPE FOR THIS
        sortFileFilter = new FileFilter() 
        {
            /**
             * This method limits the types the file chooser
             * can see to .sort files.
             */
            @Override
            public boolean accept(File file) 
            {
                return file.getName().endsWith(SORT_FILE_EXTENSION);
            }

            /**
             * Describes the types of files we'll accept.
             */
            @Override
            public String getDescription() { return SORT_FILE_EXTENSION; }
        };
        
        // AND MAKE SURE THE FILE CHOOSER USES THE FILTER
        fileChooser.setFileFilter(sortFileFilter);
    }
    
    // HERE ARE THE METHODS FOR UPDATING THE GRID VIA MOUSE CLICKS
    
    /**
     * This adds/removes the cell at (x,y) to the snake if it's legal.
     */
    public void tryAddingOrRemovingCell(int x, int y, boolean leftMouseButton)
    {
        // FIGURE OUT THE CORRESPONDING COLUMN & ROW
        int w = gridRenderer.getWidth()/gridColumns;
        int col = x/w;
        int h = gridRenderer.getHeight()/gridRows;
        int row = y/h;
        
        // GET THE VALUE IN THAT CELL
        boolean value = grid[col][row];
        
        // ONLY ADD IF IT WAS A LEFT MOUSE BUTTON CLICK,
        // IF IT'S CURRENTLY NOT IN THE SNAKE, AND WE HAVE MORE TILES TO ADD
        if (leftMouseButton && !value && (tilesRemaining > 0))
        {
            // IF IT'S THE FIRST JUST ADD IT
            if (snake.isEmpty())
            {
                Cell cellToAdd = new Cell(col, row);
                grid[col][row] = true;
                snake.addLast(cellToAdd);
                tilesRemaining--;
            }
            // OTHERWISE TRY ADDING IT ONTO THE TAIL
            boolean tailAdded = addTailIfAdjacent(col, row);
            
            // OR BEFORE THE HEAD
            if (!tailAdded)
            {
                addHeadIfAdjacent(col, row);
            }
        }
        // THE RIGHT MOUSE BUTTON IS FOR REMOVING NODES
        else if (!leftMouseButton && value && (tilesRemaining < TOTAL_TILES))
        {
            // WE CAN ONLY REMOVE THE TAIL
            if (isTail(col, row))
            {
                snake.removeLast();
                tilesRemaining++;
                grid[col][row] = false;
            }
            // OR THE HEAD
            else if (isHead(col, row))
            {
                snake.removeFirst();
                tilesRemaining++;
                grid[col][row] = false;
            }
        }
    }

    /**
     * Tests to see if the (col, row) cell is the tail of the snake and
     * returns true if it is, false if not.
     */
    public boolean isTail(int col, int row)
    {
        if (snake.isEmpty())
            return false;
        return (snake.peekLast().col == col) && (snake.peekLast().row == row);
    }

    /**
     * Tests to see if the (col, row) cell is the head of the snake and
     * returns true if it is, false if not.
     */
    public boolean isHead(int col, int row)
    {
        if (snake.isEmpty())
            return false;
        return (snake.peekFirst().col == col) && (snake.peekFirst().row == row);
    }

    /**
     * Attempts to add the cell at (col, row) to the tail of the snake. Note that
     * if will not add it if it is not adjacent.
     */
    public boolean addTailIfAdjacent(int col, int row)
    {
        // TESTS TO SEE IF THE (col, row) CELL IS ADJACET TO THE TAIL
        if (isAdjacent(col, row, snake.peekLast().col, snake.peekLast().row))
        {
            Cell newTail = new Cell(col, row);
            grid[col][row] = true;
            snake.addLast(newTail);
            tilesRemaining--;            
            return true;
        }
        return false;
    }
    
    /**
     * Attempts to add the cell at (col, row) before the head of the snake. Note that
     * if will not add it if it is not adjacent.
     */
    public boolean addHeadIfAdjacent(int col, int row)
    {
        // TESTS TO SEE IF THE (col, row) CELL IS ADJACET TO THE HEAD
        if (isAdjacent(col, row, snake.peekFirst().col, snake.peekFirst().row))
        {
            Cell newHead = new Cell(col, row);
            grid[col][row] = true;
            snake.addFirst(newHead);
            tilesRemaining--;            
            return true;
        }
        return false;
    }

    /**
     * Returns true if cell 1 (col1, row1) is adjacent to cell 2 (col2, row2).
     * Adjacent means it's a cell that is next to it on its left, right, top,
     * or bottom border. Diagonal cells are not adjacent.
     */
    public boolean isAdjacent(int col1, int row1, int col2, int row2)
    {
        boolean right = ((col1 == (col2 + 1)) && (row1 == row2));
        boolean left = ((col1 == col2 - 1) && (row1 == row2));
        boolean below = ((col1 == col2) && (row1 == (row2 - 1)));
        boolean above = ((col1 == col2) && (row1 == (row2 + 1)));
        return right || left || below || above;
    }

    /**
     * This event handler responds to when the user mouse clicks
     * on the rendering panel. The result is we update the tile
     * assignments on the cell that was clicked.
     */
    class GridClickHandler extends MouseAdapter
    {
         /**
         * This is the method where we respond to mouse clicks. Note
         * that the me argument knows the x,y coordinates of the
         * mouse click on the panel and we can translate that into
         * a click on a cell.
         */
        @Override
        public void mousePressed(MouseEvent me)
        {
            // LET'S TRY ADDING/REMOVING THE CELL THE USER CLICKED ON
            tryAddingOrRemovingCell(me.getX(), me.getY(), me.getButton() == MouseEvent.BUTTON1);
            
            // UPDATE THE TILES REMAINING DISPLAY
            updateTilesRemainingLabel();

            // ACTIVATE/DEACTIVATE THE SAVE BUTTON
            updateSaveButtonState();
            
            // AND REDRAW THE GRID
            gridRenderer.repaint();
        }
    }
    
    // A SORTING GAME ONLY MAKES SENSE IF WE HAVE AT LEAST 2 ITEMS
    // TO SORT, SO WE'LL MAKE THAT THE MINIMUM BEFORE WE CAN SAVE
    public void updateSaveButtonState()
    {
        saveAsButton.setEnabled(snake.size() >= MIN_SNAKE_SIZE);
    }

    // WE'LL USE THIS FOR A SNAKE CELL
    class Cell
    {
        public int col;
        public int row;
        
        public Cell(int initCol, int initRow)
        {
            col = initCol;
            row = initRow;
        }
        
        public boolean equals(Object obj)
        {
            Cell otherCell = (Cell)obj;
            return (col == otherCell.col) && (row == otherCell.row);
        }
        
        public String toString()
        {
            return "(" + col + "," + row + ")";
        }
    }
    
    /**
     * This class serves as the event handler for the two
     * spinners, which allow the user to change the
     * grid dimensions.
     */
    class GridSizeHandler implements ChangeListener
    {
        /**
         * Called when the user changes the value in one of the two spinners,
         * this method rebuilds the grid using the most recent
         * dimension settings, it also tries to copy data from the previous
         * sized grid into the new one. Note that as a grid is made larger
         * we keep all the data, but as a grid is made smaller, we will
         * lose some data.
         */        
        @Override
        public void stateChanged(ChangeEvent e)
        {
            // GET THE NEW GRID DIMENSIONS
            int newGridColumns = Integer.parseInt(columnsSpinner.getValue().toString());
            int newGridRows = Integer.parseInt(rowsSpinner.getValue().toString());
            
            //  MAKE A NEW GRID
            boolean[][] newGrid = new boolean[newGridColumns][newGridRows];
            int totalCellsCopied = 0;
            
            // COPY THE OLD DATA TO THE NEW GRID
            for (int i = 0; i < gridColumns; i++)
            {
                for (int j = 0; j < gridRows; j++)
                {
                    if ((i < newGridColumns) && (j < newGridRows))
                    {
                        newGrid[i][j] = grid[i][j];
                        totalCellsCopied++;
                    }
                }
            }
            
            // NOW UPDATE THE GRID
            gridColumns = newGridColumns;
            gridRows = newGridRows;
            grid = newGrid;
            
            // REMOVE THE TILES OUTSIDE THE GRID
            LinkedList<Cell> tempSnake = new LinkedList();
            boolean endFound = false;
            while (!endFound && !snake.isEmpty())
            {
                Cell c = snake.removeFirst();
               
                if ((c.col >= gridColumns) || (c.row >= gridRows))
                {
                    endFound = true;
                }
                else
                {
                    tempSnake.addLast(c);
                }
            }
            while (!snake.isEmpty())
            {
                Cell c = snake.removeFirst();
                if ((c.col < gridColumns) && (c.row < gridRows))
                    grid[c.col][c.row] = false;
            }

            // LET'S KEEP THE NEW SNAKE
            snake = tempSnake;
            
            // IF WE SHRUNK THE GRID, WE MAY HAVE LOST SOME DATA, SO
            // RETURN THOSE TILES
            tilesRemaining = TOTAL_TILES - snake.size();
            
            // UPDATE THE REMAINING TILES DISPLAY
            updateTilesRemainingLabel();
            
            // AND RE-RENDER THE GRID
            gridRenderer.repaint();        
        }        
    }

    /**
     * Responds to pressing the Reset button. It clears the contents of
     * the grid without changing its dimensions and clears the snake.
     */
    class ResetHandler implements ActionListener
    {
        /**
         * Reset everything and then update the display.
         */
        public void actionPerformed(ActionEvent ae)
        {
            resetData();
            updateTilesRemainingLabel();
            gridRenderer.repaint();
        }
    }
    
    /**
     * This class serves as the event handler for the open
     * level button.
     */
    class OpenLevelHandler implements ActionListener
    {
        /**
         * This method responds to a click on the open level button. It
         * prompts the user for a file to open and then proceeds to
         * load it.
         */
        @Override
        public void actionPerformed(ActionEvent ae)
        {
            // FIRST PROMPT THE USER FOR A FILE NAME
            int buttonSelection = fileChooser.showOpenDialog(openButton);
            
            // MAKE SURE THE USER WANTS TO CONTINUE AND DIDN'T
            // PRESS THE CANCEL OPTION
            if (buttonSelection == JFileChooser.APPROVE_OPTION)
            {   
                // GET THE FILE THE USER SELECTED
                File fileToOpen = fileChooser.getSelectedFile();
                String fileName = fileToOpen.getPath();
                
                // MAKE SURE IT'S A .SORT FILE
                if (!fileName.endsWith(SORT_FILE_EXTENSION))
                {
                    JOptionPane.showMessageDialog(saveAsButton, OPEN_FILE_ERROR_FEEDBACK_TEXT);
                    return;
                }

                // NOW LOAD THE RAW DATA SO WE CAN USE IT
                // OUR LEVEL FILES WILL HAVE THE DIMENSIONS FIRST,
                // FOLLOWED BY THE GRID VALUES
                try
                {
                    // LET'S USE A FAST LOADING TECHNIQUE. WE'LL LOAD ALL OF THE
                    // BYTES AT ONCE INTO A BYTE ARRAY, AND THEN PICK THAT APART.
                    // THIS IS FAST BECAUSE IT ONLY HAS TO DO FILE READING ONCE
                    byte[] bytes = new byte[Long.valueOf(fileToOpen.length()).intValue()];
                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                    FileInputStream fis = new FileInputStream(fileToOpen);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    
                    // HERE IT IS, THE ONLY READY REQUEST WE NEED
                    bis.read(bytes);
                    bis.close();

                    // NOW WE NEED TO LOAD THE DATA FROM THE BYTE ARRAY
                    DataInputStream dis = new DataInputStream(bais);

                    // NOTE THAT WE NEED TO LOAD THE DATA IN THE SAME
                    // ORDER AND FORMAT AS WE SAVED IT

                    // FIRST READ THE ALGORITHM NAME
                    String algorithmName = dis.readUTF();
                    algorithmComboBoxModel.setSelectedItem(algorithmName);
                    
                    // THEN READ THE GRID DIMENSIONS
                    int initGridColumns = dis.readInt();
                    int initGridRows = dis.readInt();
                    boolean[][] newGrid = new boolean[initGridColumns][initGridRows];

                    // AND NOW UPDATE ALL THE CELL VALUES
                    for (int i = 0; i < initGridColumns; i++)
                    {
                        for (int j = 0; j < initGridRows; j++)
                        {
                            newGrid[i][j] = false;
                        }
                    }

                    // READ IN THE SNAKE CELLS
                    int initSnakeLength = dis.readInt();
                    snake.clear();
                    for (int i = 0; i < initSnakeLength; i++)
                    {
                        int col = dis.readInt();
                        int row = dis.readInt();
                        Cell newCell = new Cell(col, row);
                        snake.add(newCell);
                        newGrid[col][row] = true;
                    }

                    // EVERYTHING WENT AS PLANNED SO LET'S MAKE IT PERMANENT
                    columnsSpinner.setValue(initGridColumns);
                    rowsSpinner.setValue(initGridRows);
                    grid = newGrid;
                    gridColumns = initGridColumns;
                    gridRows = initGridRows;                    
                    tilesRemaining = TOTAL_TILES - initSnakeLength;
                    saveAsButton.setEnabled(true);

                    // UPDATE THE DISPLAY
                    updateTilesRemainingLabel();
                    gridRenderer.repaint();
                    
                    // FINALLY TELL THE USER THE LEVEL SUCCESSFULLY LOADED
                    JOptionPane.showMessageDialog(window, fileToOpen.getName() + FILE_LOADING_SUCCESS_TEXT);
                }
                catch(IOException ioe)
                {
                    // AN ERROR HAPPENED, LET THE USER KNOW.
                    JOptionPane.showMessageDialog(saveAsButton, FILE_READING_ERROR_TEXT + fileName, FILE_READING_ERROR_TEXT + fileName, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * This class serves as the event handler for the 
     * Save As button.
     */
    class SaveAsLevelHandler implements ActionListener
    {
        /**
         * This method responds to when the user clicks the save
         * as button. It prompts the user for a file name and then
         * saves the level to that file.
         */
        @Override
        public void actionPerformed(ActionEvent ae)
        {
            // FIRST PROMPT THE USER FOR A FILE NAME
            int buttonSelection = fileChooser.showSaveDialog(saveAsButton);
            
            // MAKE SURE THE USER WANTS TO CONTINUE AND DIDN'T SELECT
            // THE CANCEL BUTTON
            if (buttonSelection == JFileChooser.APPROVE_OPTION)
            {   
                // GET THE FILE THE USER SELECTED
                File fileToSave = fileChooser.getSelectedFile();
                String fileName = fileToSave.getPath();
                
                // MAKE SURE IT'S THE CORRECT FILE TYPE
                if (!fileName.endsWith(SORT_FILE_EXTENSION))
                {
                    JOptionPane.showMessageDialog(saveAsButton, SAVE_AS_ERROR_FEEDBACK_TEXT);
                    return;
                }
                
                // OUR LEVEL FILES WILL HAVE THE DIMENSIONS FIRST,
                // FOLLOWED BY THE GRID VALUES
                try
                {
                    // WE'LL WRITE EVERYTHING IN BINARY. NOTE THAT WE
                    // NEED TO MAKE SURE WE SAVE THE DATA IN THE SAME
                    // FORMAT AND ORDER WITH WHICH WE READ IT LATER
                    FileOutputStream fos = new FileOutputStream(fileName);
                    DataOutputStream dos = new DataOutputStream(fos);
                    
                    // FIRST SAVE THE ALGORITHM NAME FOR THIS LEVEL
                    int algorithmIndex = algorithmComboBox.getSelectedIndex();
                    dos.writeUTF(ALGORITHM_NAMES[algorithmIndex]);      
                    
                    // THEN WRITE THE DIMENSIONS
                    dos.writeInt(gridColumns);
                    dos.writeInt(gridRows);
                    
                    // NOW THE NUMBER OF CELLS IN THE SNAKE
                    dos.writeInt(snake.size());
                    
                    // NOW THE SNAKE HEAD LOCATION
                    Iterator<Cell> it = snake.iterator();
                    Cell c = it.next();
                    dos.writeInt(c.col);
                    dos.writeInt(c.row);
                    
                    // AND NOW ALL THE CELLS IN THE SNAKE
                    while (it.hasNext())
                    {
                        c = it.next();
                        dos.writeInt(c.col);
                        dos.writeInt(c.row);
                    }
                }
                catch(IOException ioe)
                {
                    // AN ERROR HAS HAPPENED, LET THE USER KNOW
                    JOptionPane.showMessageDialog(saveAsButton, FILE_WRITING_ERROR_TEXT + fileName, FILE_WRITING_ERROR_TEXT + fileName, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * This class renders the grid for us. Note that we also listen for
     * mouse clicks on it.
     */
    class GridRenderer extends JPanel
    {
        /**
         * Default constructor, it will initialize the background
         * color for the grid.
         */
        public GridRenderer()
        {
            setBackground(GRID_BACKGROUND_COLOR);
        }

        /**
         * This function is called each time the panel is rendered. It
         * will draw the grid, including all the cells and the numeric
         * values in each cell. 
         */
        @Override
        public void paintComponent(Graphics g)
        {
            // CLEAR THE PANEL
            super.paintComponent(g);
            
            // CALCULATE THE GRID CELL DIMENSIONS
            int w = (this.getWidth())/SortingHatLevelEditor.this.gridColumns;
            int h = (this.getHeight())/SortingHatLevelEditor.this.gridRows;

            // NOW RENDER EACH CELL
            int x = 0, y = 0;
            
            // WE'LL MAKE THE HEAD THE MOST READ AND WORK TOWARDS BLACK
            int blue = 255;
            Iterator<Cell> it = snake.listIterator();
            while (it.hasNext())
            {
                Cell c = it.next();
                x = (c.col * w);
                y = (c.row * h);
                g.setColor(new Color(0, 0, blue));
                g.fillRoundRect(x, y, w, h, 10, 10);
                blue -= 10;
            }
            g.setColor(Color.black);

            x = 0;
            y = 0;

            // DRAW A BORDER AROUND ALL CELLS
            for (int i = 0; i < gridColumns; i++)
            {
                y = 0;
                for (int j = 0; j < gridRows; j++)
                {
                    // DRAW THE CELL
                    g.drawRoundRect(x, y, w, h, 10, 10);
                                        
                    // ON TO THE NEXT ROW
                    y += h;
                }
                // ON TO THE NEXT COLUMN
                x += w;
            }   
        }        
    }
    
    /**
     * This is where execution of the level editor begins.
     */
    public static void main(String[] args)
    {
        // MAKE THE EDITOR
        SortingHatLevelEditor app = new SortingHatLevelEditor();
        
        // INITIALIZE THE APP
        app.init();
        
        // AND OPEN THE WINDOW
        app.window.setVisible(true);
    }
}
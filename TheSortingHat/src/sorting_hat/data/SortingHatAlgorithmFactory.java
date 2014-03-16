package sorting_hat.data;

import java.util.ArrayList;
import java.util.HashMap;
import sorting_hat.ui.SortingHatTile;

/**
 * This factory class builds the sorting algorithm objects to be
 * used for sorting in the game.
 *
 * @author Richard McKenna & _____________________
 */
public class SortingHatAlgorithmFactory
{
    // STORES THE SORTING ALGORITHMS WE MAY WISH TO USE
    static HashMap<SortingHatAlgorithmType, SortingHatAlgorithm> premadeSortingHatAlgorithms = null;

    /**
     * For getting a particular sorting algorithm. Note that the first
     * time it is called it initializes all the sorting algorithms and puts 
     * them in a hash map to be retrieved as needed to setup levels when loaded.
     */
    public static SortingHatAlgorithm buildSortingHatAlgorithm( SortingHatAlgorithmType algorithmType,
                                                                ArrayList<SortingHatTile> initDataToSort)
    {
        // INIT ALL THE ALGORITHMS WE'LL USE IF IT HASN'T DONE SO ALREADY
        if (premadeSortingHatAlgorithms == null)
        {
            premadeSortingHatAlgorithms = new HashMap();
            premadeSortingHatAlgorithms.put(SortingHatAlgorithmType.BUBBLE_SORT,    new BubbleSortAlgorithm(initDataToSort, algorithmType.toString()));
            premadeSortingHatAlgorithms.put(SortingHatAlgorithmType.SELECTION_SORT, new SelectionSortAlgorithm(initDataToSort, algorithmType.toString()));
        }
        // RETURN THE REQUESTED ONE
        return premadeSortingHatAlgorithms.get(algorithmType);
    }
}

/**
 * This class builds all the transactions necessary for performing
 * bubble sort on the data structure. This can then be used to
 * compare to student moves during the game.
 */
class BubbleSortAlgorithm extends SortingHatAlgorithm
{
    /**
     * Constructor only needs to init the inherited stuff.
     */
    public BubbleSortAlgorithm(ArrayList<SortingHatTile> initDataToSort, String initName)
    {
        // INVOKE THE PARENT CONSTRUCTOR
        super(initDataToSort, initName);
    }
    
    /**
     * Build and return all the transactions necessary to sort using bubble sort.
     */
    public ArrayList<SortTransaction> generateSortTransactions()
    {
        // HERE'S THE LIST OF TRANSACTIONS
        ArrayList<SortTransaction> transactions = new ArrayList();
        
        // FIRST LET'S COPY THE DATA TO A TEMPORARY ArrayList
        ArrayList<SortingHatTile> copy = new ArrayList();
        for (int i = 0; i < dataToSort.size(); i++)
            copy.add(dataToSort.get(i));

        // NOW SORT THE TEMPORARY DATA STRUCTURE
        for (int i = copy.size()-1; i > 0; i--)
        {
            for (int j = 0; j < i; j++)
            {
                // TEST j VERSUS j+1
                if (copy.get(j).getID() > copy.get(j+1).getID())
                {
                    // BUILD AND KEEP THE TRANSACTION
                    SortTransaction sT = new SortTransaction(j, j+1);
                    transactions.add(sT);
                    
                    // SWAP
                    SortingHatTile temp = copy.get(j);
                    copy.set(j, copy.get(j+1));
                    copy.set(j+1, temp);
                }
            }
        }
        return transactions;
    }
}

class SelectionSortAlgorithm extends SortingHatAlgorithm{
    
    public SelectionSortAlgorithm(ArrayList<SortingHatTile> initDataToSort, String initName)
    {
        // INVOKE THE PARENT CONSTRUCTOR
        super(initDataToSort, initName);
    }
    
    public ArrayList<SortTransaction> generateSortTransactions(){
        
        ArrayList<SortTransaction> transactions = new ArrayList();
        
        //Copy SortingHatTiles to sort.
        ArrayList<SortingHatTile> copy = new ArrayList();
        for (SortingHatTile tile : dataToSort){
            copy.add(tile);
        }
        
        for (int i = 0; i < copy.size() - 1; i++){
            //Create a reference to the minimum tile that will be found and its index.
            SortingHatTile min = copy.get(i + 1);
            int minIndex = i + 1;
            
            //Find the minimum tile.
            for (int j = i + 1; j < copy.size() - 1; j++){
                if (min.getID() > copy.get(j + 1).getID()){
                    min = copy.get(j + 1);
                    minIndex = j + 1;             
                }
            }
            
            //Swap the minimum with i
            SortingHatTile temp = copy.get(i);
            copy.set(i, min);
            copy.set(minIndex, temp);
            
            //Generate sort transaction
            SortTransaction trans = new SortTransaction(i, minIndex);
            transactions.add(trans);
        }
        
        return transactions;
    }
}
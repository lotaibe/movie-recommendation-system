import org.apache.commons.csv.*;
import java.util.*;
/*
 * @author (Lota Ibe)
 * @version (July 2020)
 */
public interface Rater {
    public void addRating(String item, double rating);
    
    public boolean hasRating(String item);
    
    public HashMap<String,Rating> getaRating();
    
    public String getID();
    
    public double getRating(String item);

    public int numRatings();
    
    public ArrayList<String> getItemsRated();
}

import java.util.*;
/**
 * 
 * @author (Lota Ibe) 
 * @version (July 2020)
 */
public class FourthRatings {
    //As we can see, every instance variable has been eliminated as requested
    public FourthRatings() {
        this("ratings.csv");
    }
    
    public FourthRatings(String ratingsfile){
        RaterDatabase.addRatings("data/" + ratingsfile);
    }
    
    public Rater getRater(String rater_id){
        return RaterDatabase.getRater(rater_id);
    }
    
    public int getRaterSize(){
        return RaterDatabase.size();
    }
    //Modified as requested in this assignment by using RaterDatabase class:
    public double getAverageByID(String id, int minimalRaters){
        ArrayList<Rater> Raters = RaterDatabase.getRaters();
        double count = 0;
        double numRatings = 0;
        double average = 0;
        if(minimalRaters == 0){
            return 0.0;
        }
        for (Rater r : Raters){
            HashMap<String,Rating> Ratings = r.getaRating();
            for(Rating rat : Ratings.values()){
                if(rat.getItem().equals(id)){
                    double value = rat.getValue();
                    numRatings++;
                    count = count + value;
                }
            }
        }
        if(numRatings< minimalRaters){
            return -1;
        }
        else{
            average = count/numRatings;
            return average;
        }
    }
    
    public ArrayList<Rating> getAverageRatings(int minimalRaters){
        ArrayList<Rating> averageRatings = new ArrayList<Rating>();
        ArrayList<String> movies = MovieDatabase.filterBy(new TrueFilter());
        for(String m : movies){
            getAverageByID(m,minimalRaters);
            Rating a = new Rating(m,getAverageByID(m,minimalRaters));
            if(a.getValue() > -1){
                averageRatings.add(a);
            }
        }
        return averageRatings;
    }
    
    public ArrayList<Rating> getAverageRatingsByFilter(int minimalRaters, Filter filterCriteria){
        ArrayList<Rating> averageAndFilter = new ArrayList<Rating>();
        ArrayList<String> movies = MovieDatabase.filterBy(filterCriteria);
        for(String m : movies){
            getAverageByID(m,minimalRaters);
            Rating a = new Rating(m,getAverageByID(m,minimalRaters));
            if(a.getValue() > -1){
                averageAndFilter.add(a);
            }
        }
        return averageAndFilter;
    }
    
   
    private double dotProduct(Rater me, Rater r){
        /*This method returns a double value with the affinity between two Raters. The higher the number,
        the higher the affinity as well*/
        HashMap<String,Rating> myRatings = me.getaRating();
        HashMap<String,Rating> hisRatings = r.getaRating();
        double dotProduct = 0;
        //System.out.println("MyRatings has " +myRatings.size()+ " movies");
        //System.out.println("hisRatings has " + hisRatings.size() + " movies");
        for(int i=0; i<myRatings.size();i++){
            ArrayList<Rating> movieA = new ArrayList<Rating>();
            for(Rating a : myRatings.values()){
                 movieA.add(a);
            }
            for(Rating b : hisRatings.values()){
                if (movieA.toString().contains(b.getItem())){
                    for(i=0;i<movieA.size();i++){
                        if(movieA.get(i).getItem().equals(b.getItem())){
                            double finalValue =0;
                            //System.out.println("Equal movie found: " + b.getItem() + " " + b.getValue() + " and " + movieA.get(i).getItem()+ " " + movieA.get(i).getValue());
                            finalValue = (b.getValue()-5)* (movieA.get(i).getValue()-5);
                            //System.out.println("finalValue = " + finalValue);
                            dotProduct = dotProduct+ finalValue;
                            //System.out.println("dotProduct = " + dotProduct);
                        }
                    }
                }
            }
        }
        return dotProduct;
    }
    
    private ArrayList<Rating> getSimilarities(String id){
        ArrayList<Rating> similarRatings = new ArrayList<Rating>();
        Rater me = RaterDatabase.getRater(id);
        for(Rater r : RaterDatabase.getRaters()){
            if(!r.getID().equals(id)){
                if(dotProduct(me,r)>0){
                    similarRatings.add(new Rating(r.getID(),dotProduct(me,r)));
                }
            }
        }
        Collections.sort(similarRatings,Collections.reverseOrder());
        return similarRatings;
    }
    
    public ArrayList<Rating> getSimilarRatings(String id, int numSimilarRaters, int minimalRaters){
        try{
        ArrayList<Rating> similarRatings = getSimilarities(id);
        ArrayList<Rating> getRatings = new ArrayList<Rating>();
        ArrayList<String> movies = MovieDatabase.filterBy(new TrueFilter());
        double numratings = 0;
        HashMap<String,ArrayList<Double>>favRaters = new HashMap<String,ArrayList<Double>>();
        for(String movieID : movies){
            for(int i=0; i<numSimilarRaters; i++){
                Rating r = similarRatings.get(i);
                //This gets how big is the similarity:
                double coef = r.getValue();
                //This gets each similar rater id:
                String rater_id = r.getItem();
                ArrayList<Rater> Raters = RaterDatabase.getRaters();
                
                for(Rater rat : Raters){
                    HashMap<String,Rating> Ratings = rat.getaRating();
                    if(rater_id.equals(rat.getID())){
                        for(Rating rats : Ratings.values()){
                            //If the movie was voted by my "soulmates":
                            if(rats.getItem().equals(movieID)){
                                //if my hash does not have the movie, add movie and similarity value:
                                ArrayList<Double> coefs = new ArrayList<Double>();
                                if(!favRaters.containsKey(rats.getItem())){
                                    coefs.add(coef*rats.getValue());
                                    favRaters.put(rats.getItem(),coefs);
                                }
                                //If it is already in hashmap, add the similarity rate to the value hashmap:
                                else{
                                    ArrayList<Double> mine = favRaters.get(rats.getItem());
                                    mine.add(coef*rats.getValue());
                                    favRaters.put(rats.getItem(),mine);
                                }
                            }
                        }
                    }
                }
            }
        }
        for ( String s : favRaters.keySet()){
            if( favRaters.get(s).size() >=minimalRaters){
                double total =0;
                for(double num : favRaters.get(s)){
                    total = total+ num;
                }
                //Final calculation for the similarity rate, according to minimalRaters parameter:
                double finalValue = total/favRaters.get(s).size();
                getRatings.add(new Rating(s,finalValue));
            }
        }
        //We sort them to have a rational TOP:
        Collections.sort(getRatings,Collections.reverseOrder());
        return getRatings;
       }
       catch (Exception e){
           System.out.println("One of the variables is out of bounds, insert smaller parameter variables or another user");
           return null;
       }
   }
   
   public ArrayList<Rating> getSimilarRatingsByFilter(String id, int numSimilarRaters, int minimalRaters, Filter filterCriteria){
        try{
        ArrayList<Rating> similarRatings = getSimilarities(id);
        ArrayList<Rating> getRatings = new ArrayList<Rating>();
        //Same method as before, but as easy as running a filterCriteria.
        ArrayList<String> movies = MovieDatabase.filterBy(filterCriteria);
        double numratings = 0;
        HashMap<String,ArrayList<Double>>favRaters = new HashMap<String,ArrayList<Double>>();
        for(String movieID : movies){
            for(int i=0; i<numSimilarRaters; i++){
                Rating r = similarRatings.get(i);
                //r(i) es el top de cada rater_id con su coef
                //System.out.println(r);
                //este coef tiene que estar multiplicado por cada peli en comun. 
                double coef = r.getValue();
                //este es cada rater afin:
                String rater_id = r.getItem();
                ArrayList<Rater> Raters = RaterDatabase.getRaters();
                
                for(Rater rat : Raters){
                    HashMap<String,Rating> Ratings = rat.getaRating();
                    if(rater_id.equals(rat.getID())){
                        for(Rating rats : Ratings.values()){
                            //Si la peli la han votado mis almas gemelas:
                            if(rats.getItem().equals(movieID)){
                                //System.out.println("Esta peli: " + rats.getItem() + " y esta " + movieID + 
                                                   //"la ha visto este id " + rater_id);
                                 //Si mi ranking no tiene la peli, añade peli y coef
                                ArrayList<Double> coefs = new ArrayList<Double>();
                                if(!favRaters.containsKey(rats.getItem())){
                                    /*System.out.println("Añado peli y coef que sin multip es " + coef 
                                            + " porque " + rat.getID() + " ha visto " + movieID + " y le puso una nota de " 
                                            +rats.getValue());*/
                                    coefs.add(coef*rats.getValue());
                                    favRaters.put(rats.getItem(),coefs);
                                }
                                //Si ya la tiene, añade el coef
                                else{
                                    /*System.out.println("****Añado coeff a peli que sin multip es " + coef + " porque " 
                                        + rat.getID() + " tambien ha visto " + movieID+ " y le puso una nota de "
                                        +rats.getValue());*/
                                    ArrayList<Double> mine = favRaters.get(rats.getItem());
                                    mine.add(coef*rats.getValue());
                                    favRaters.put(rats.getItem(),mine);
                                }
                            }
                        }
                    }
                }
            }
        }
        for ( String s : favRaters.keySet()){
            if( favRaters.get(s).size() >=minimalRaters){
                double total =0;
                for(double num : favRaters.get(s)){
                    total = total+ num;
                }
                double finalValue = total/favRaters.get(s).size();
                getRatings.add(new Rating(s,finalValue));
            }
        }
        Collections.sort(getRatings,Collections.reverseOrder());
        return getRatings;
       }
       catch (Exception e){
           System.out.println("One of the variables is out of bounds, insert smaller parameter variables or another user");
           return null;
       }
   }
}

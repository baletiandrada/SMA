package com.example.booksapp;

import com.example.booksapp.dataModels.AppreciateBookModel;
import com.example.booksapp.dataModels.BookRankingModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Double.NaN;

class SharedBooks{
    private String book_id;
    private int book_is_shared;

    public SharedBooks(String book_id, int book_is_shared){
        this.book_id = book_id;
        this.book_is_shared = book_is_shared;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public int getBook_is_shared() {
        return book_is_shared;
    }

    public void setBook_is_shared(int book_is_shared) {
        this.book_is_shared = book_is_shared;
    }
}

public class ComputeUserBasedSimilarity {

    public static double sim_pearson(String user1_id, String user2_id, List<AppreciateBookModel> books_ratings){
        ArrayList<SharedBooks> shared_book_list = new ArrayList<SharedBooks>();
        final double[] sum_of_squares1 = {0}, sum_of_squares2={0}, sum_of_rating1={0}, sum_of_rating2={0}, product_sum={0};

        for(AppreciateBookModel book_rating_first : books_ratings){
            if(user1_id.equals(book_rating_first.getUser_id())){
                for(AppreciateBookModel book_rating_second : books_ratings){
                    if(user2_id.equals(book_rating_second.getUser_id())){
                        if(!book_rating_first.getUser_id().equals(book_rating_second.getUser_id())
                                && book_rating_first.getBook_id().equals(book_rating_second.getBook_id())){
                            shared_book_list.add(new SharedBooks(book_rating_first.getBook_id(), 1));
                            int rating_int1= Integer.parseInt(book_rating_first.getRating());
                            int rating_int2= Integer.parseInt(book_rating_second.getRating());
                            sum_of_rating1[0] += rating_int1;
                            sum_of_rating2[0] += rating_int2;
                            sum_of_squares1[0] += Math.pow(Integer.parseInt(book_rating_first.getRating()), 2);
                            sum_of_squares2[0] += Math.pow(Integer.parseInt(book_rating_second.getRating()), 2);
                            product_sum[0] += rating_int1 * rating_int2;
                        }
                    }
                }
            }
        }
        if(shared_book_list.size()==0)
            return 0;
        long n = shared_book_list.size();
        double num = product_sum[0] - sum_of_rating1[0]*(double)sum_of_rating2[0]/n;
        double den_left = sum_of_squares1[0] - (double)Math.pow(sum_of_rating1[0],2)/n;
        double den_right = sum_of_squares2[0] - (double)Math.pow(sum_of_rating2[0],2)/n;
        double den = Math.sqrt(den_left*den_right);

        if(den==0)
            return 0;
        return (double) num/den;
    }

    public static double sim_euclidean(String user1_id, String user2_id, List<AppreciateBookModel> books_ratings){
        ArrayList<SharedBooks> shared_book_list = new ArrayList<>();
        final double[] sum_of_squares = {0};

        for(AppreciateBookModel book_rating_first : books_ratings){
            if(user1_id.equals(book_rating_first.getUser_id())){
                for(AppreciateBookModel book_rating_second : books_ratings){
                    if(user2_id.equals(book_rating_second.getUser_id())){
                        if(!book_rating_first.getUser_id().equals(book_rating_second.getUser_id())
                                && book_rating_first.getBook_id().equals(book_rating_second.getBook_id())){

                            shared_book_list.add(new SharedBooks(book_rating_first.getBook_id(), 1));
                            int difference = Integer.parseInt(book_rating_first.getRating()) - Integer.parseInt(book_rating_second.getRating());
                            sum_of_squares[0] += Math.pow(difference, 2);
                        }
                    }
                }
            }
        }
        if(shared_book_list.size()==0)
            return 0;
        else
            return (double) 1/(1+Math.sqrt(sum_of_squares[0]));
    }


    public static boolean listContainsKey(String key, List<AppreciateBookModel> list){
        for(AppreciateBookModel ratingData: list){
            if(ratingData.getBook_id().equals(key))
                return true;
        }
        return false;
    }

    public static boolean listContainsBook(String key, List<BookRankingModel> list){
        for(BookRankingModel bookRanking: list){
            if(bookRanking.getBook_id().equals(key))
                return true;
        }
        return false;
    }

    public static int getKeyIndex(String key, List<AppreciateBookModel> list){
        int i;
        for(i=0; i<list.size();i++){
            if(list.get(i).getBook_id().equals(key))
                return i;
        }
        return -1;
    }

    public static ArrayList<BookRankingModel> getRecommandations(String logged_in_user_id, List<String> users_list, List<String> user_books, List<AppreciateBookModel> books_ratings){

        List<AppreciateBookModel> totals = new ArrayList<>();
        List<AppreciateBookModel> simSums = new ArrayList<>();

        ArrayList<BookRankingModel> rankings = new ArrayList<>();

        for(String user_id : users_list){
            double sim_score=-1;
            if(user_id.equals(logged_in_user_id))
                continue;

            sim_score = sim_euclidean(logged_in_user_id, user_id, books_ratings);

            if(sim_score==0)
                continue;

            double finalSim_score = sim_score;

            for(AppreciateBookModel book_rating : books_ratings){

                boolean put_zero_value=true;

                if(user_id.equals(book_rating.getUser_id())){
                    if(!bookExistsInList(book_rating.getBook_id(), user_books)){
                        double value;
                        if(listContainsKey(book_rating.getBook_id(), totals)){
                            int index = getKeyIndex(book_rating.getBook_id(), totals);
                            value = Double.parseDouble(totals.get(index).getRating()) + Double.parseDouble(book_rating.getRating())*finalSim_score;
                        }
                        else
                            value = Integer.parseInt(book_rating.getRating())*finalSim_score;

                        AppreciateBookModel model = new AppreciateBookModel();
                        model.setBook_id(book_rating.getBook_id());
                        model.setRating(String.valueOf(value));
                        totals.add(model);

                        double simValue;
                        if(listContainsKey(book_rating.getBook_id(), simSums)){
                            int index = getKeyIndex(book_rating.getBook_id(), simSums);
                            simValue = Double.parseDouble(simSums.get(index).getRating()) + Double.parseDouble(book_rating.getRating())*finalSim_score;
                        }
                        else
                            simValue = Integer.parseInt(book_rating.getRating())*finalSim_score;

                        model.setRating(String.valueOf(simValue));
                        simSums.add(model);

                        put_zero_value=false;
                    }
                }
                if(put_zero_value){
                    AppreciateBookModel model = new AppreciateBookModel();
                    model.setBook_id(book_rating.getBook_id());
                    model.setRating(String.valueOf(0.0));
                    totals.add(model);
                    simSums.add(model);
                }
            }
        }

        double rankings_sum=0;
        for(AppreciateBookModel ratingModel : totals){
            if(!listContainsBook(ratingModel.getBook_id(), rankings)){
                if(!bookExistsInList(ratingModel.getBook_id(), user_books)) {
                    BookRankingModel bookRanking = new BookRankingModel(ratingModel.getBook_id(), 0);
                    int index_simSum = getKeyIndex(ratingModel.getBook_id(), simSums);
                    double ranking = Double.parseDouble(ratingModel.getRating()) / Double.parseDouble(simSums.get(index_simSum).getRating());
                    if(!Double.isNaN(ranking))
                        bookRanking.setRanking_score(ranking);
                    rankings.add(bookRanking);
                    rankings_sum += Double.parseDouble(ratingModel.getRating());
                }
            }
        }

        if(rankings_sum==0)
            return null;

        Collections.sort(rankings, Collections.reverseOrder());
        return rankings;
    }

    public static boolean bookExistsInList(String bookId, List<String> user_books){
       for(String book_id : user_books){
           if(book_id.equals(bookId))
               return true;
       }
        return false;
   }

}

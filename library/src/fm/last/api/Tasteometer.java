package fm.last.api;

/**
 * @author Casey Link <unnamedrambler@gmail.com
 *
 */
public class Tasteometer {
    String score;
    String[] results;
    
    public Tasteometer(String score, String[] results)
    {
        this.score = score;
        this.results = results;
    }
    
    public String getScore()
    {
        return score;
    }
    
    public String[] getResults()
    {
        return results;
    }
}
